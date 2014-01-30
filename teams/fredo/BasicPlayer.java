package fredo;

import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import battlecode.common.*;

import java.util.*;

import fredo.Util;
import static fredo.Util.*;

public abstract class BasicPlayer implements Player {

	protected RobotController rc;

	protected GameMap gameMap;

	protected RobotType myRobotType;
	protected int myRobotId;
	protected Team myTeam;
	
	protected Team opponentTeam;

	protected Robot myRobot;
	protected MapLocation myLocation;
	
	protected MapLocation enemyHqLocation;
	protected MapLocation myHqLocation;
	
	//protected Robot[] friendlyRobots;
	
    public final Direction[] allDirections = new Direction[] {
    	// prefer diagonal directions
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_WEST,
        SOUTH_EAST,
        EAST,
        NORTH,
        WEST,
        SOUTH
    };
    
    public Direction[] randomDirections = allDirections.clone();

    
	public BasicPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		this.myRobot = robot;
		this.myRobotId = robotId;
		this.myTeam = team;
		this.opponentTeam = team.opponent();
		this.myRobotType = robotType;

		this.rc = rc;
		
		// every player builds their own map
		gameMap = new GameMap(robotId, team, robotType, rc);
		
		this.enemyHqLocation = rc.senseEnemyHQLocation();
		this.myHqLocation = rc.senseHQLocation();
		this.myLocation = rc.getLocation();
	}
	
	public void playOneTurn() throws GameActionException {
		// keep this up to date
		myLocation = rc.getLocation();
	}
	
    protected boolean attackNearbyEnemies() throws GameActionException {
    	
    	//log("Started attackNearbyEnemies()...");
    	
        boolean didAttack = false;
        
        MapLocation myLocation = rc.getLocation();
        
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
            Robot.class,
            myLocation,
            HUGE_RADIUS,
            opponentTeam
        );

        if (nearbyEnemies.length > 0) {

            final Map<Robot, RobotInfo> allRobotInfo = new HashMap<Robot, RobotInfo>();
            
        	for (int i=0; i < nearbyEnemies.length; i++) {
        		// this sense is guaranteed to succeed because we already used senseNearbyGameObjects
        		RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[i]);
        		allRobotInfo.put(nearbyEnemies[i], robotInfo);        		
        	}
        	
        	sort(nearbyEnemies, new Comparator<Robot>() {
        		// idea:
        		// prefer to attack pastrs
        		// otherwise soldiers
        		// otherwise noisetowers
        		// never hqs
        		
				@Override
				public int compare(Robot o1, Robot o2) {
					RobotInfo info1 = allRobotInfo.get(o1);
					RobotInfo info2 = allRobotInfo.get(o2);
					
					if (info1.type == PASTR && info2.type != PASTR) {
						return -1;
					} else if (info1.type != PASTR && info2.type == PASTR) {
						return 1;
					} else if (info1.type == SOLDIER && info2.type != SOLDIER) {
						return -1;
					} else if (info1.type != SOLDIER && info2.type == SOLDIER) {
						return 1;
					} else if (info1.type == NOISETOWER && info2.type != NOISETOWER) {
						return -1;
					} else if (info1.type != NOISETOWER && info2.type == NOISETOWER) {
						return 1;
					} else if (info1.type == HQ && info2.type != HQ) {
						return 1;
					} else if (info1.type != HQ && info2.type == HQ) {
						return -1;
					} else {
						// if same type, just sort on health
						return new Double(info1.health).compareTo(info2.health);
					}
				}
        	});
        	
        	// try to attack one of them as long as it isn't the HQ
        	for (Robot nearbyEnemyRobot: nearbyEnemies) {
        		RobotInfo info = allRobotInfo.get(nearbyEnemyRobot);
        		if (info.type != HQ) {
        			if (rc.canAttackSquare(info.location)) {
	        			rc.attackSquare(info.location);
	        			didAttack = true;
	        			break;
	        		}
        		}
        	}
        	
        	if (!didAttack) {
	        	// try to attack one of them as long as it isn't the HQ
	        	for (Robot nearbyEnemyRobot: nearbyEnemies) {
	        		RobotInfo info = allRobotInfo.get(nearbyEnemyRobot);
	        		if (info.type != HQ) {
	        			if (myRobotType == SOLDIER) {
		        			gotoLocation(info.location);
		        			didAttack = true;
		        			break;
		        		}
	        		}
	        	}      	
        	}
        }

        //log("Finished attackNearbyEnemies().");
        
        return didAttack;
    }	
    
    protected void moveRandomly() {
		// let's try moving randomly
		shuffle(randomDirections);
		for (Direction randomDirection: randomDirections) {
			boolean canMove = rc.canMove(randomDirection);
			
			if (canMove) {
				MapLocation toLocation = myLocation.add(randomDirection);
	
				int newDistanceToEnemyHq = enemyHqLocation.distanceSquaredTo(toLocation);
				if (newDistanceToEnemyHq <= RobotType.HQ.attackRadiusMaxSquared) {
					log("can't move");
					canMove = false;
				}
			}
			
			if (canMove) {
				try	{
					//log("sneaking randomly");
					rc.sneak(randomDirection);
					break;
				} catch (GameActionException e) {
					die(e);
				}
			}
		} 			
    }
    
	protected void gotoLocation(MapLocation toLocation) {
		
		//log("started nextDirectionTo...");
		
		// idea: return to spawn before doing a messy calculation
		// idea: do the flood fill in parallel (use many robots?) - for big maps
		
		Direction direction = gameMap.nextDirectionTo(myLocation,toLocation);		
	    //log("finished nextDirectionTo.");
		
		// just try going there, if we are still waiting for the perfect map
		if (direction == null) {
			direction = myLocation.directionTo(toLocation);
			//if (!rc.canMove(direction)) {
			//	direction = null;
			//}
		}
	    
	    if (direction != null) {

	    	boolean canMove = rc.canMove(direction);
	    	
	    	// don't go near enemy hq
	    	if (canMove) {
				int newDistanceToEnemyHq = enemyHqLocation.distanceSquaredTo(toLocation);
				if (newDistanceToEnemyHq <= RobotType.HQ.attackRadiusMaxSquared) {
					canMove = false;
				}
	    	}
	    	
			if (canMove) {
				
				MapLocation focusLocation = myHqLocation;
								
				int newDistanceToFocus = focusLocation.distanceSquaredTo(toLocation);
				int currentDistanceToFocus = focusLocation.distanceSquaredTo(myLocation);
				
				try	{
					if (newDistanceToFocus < currentDistanceToFocus) {
						// herd cattle
						rc.move(direction);
					} else {
						// don't disturb cattle
						rc.sneak(direction);
					}
				} catch (GameActionException e) {
					die(e);
				}
			} else {
				moveRandomly();
			}
	    }
	}   
	
	protected MapLocation getFocusLocation() {
		MapLocation[] friendlyPastrLocations = rc.sensePastrLocations(myTeam);
		
		MapLocation focusLocation;
		
		if (friendlyPastrLocations.length > 0) {
		
    		// pick the pastr location that is closest to us
    		
    		sort(friendlyPastrLocations, new Comparator<MapLocation>() {
				@Override
				public int compare(MapLocation o1, MapLocation o2) {
					return new Integer(myLocation.distanceSquaredTo(o1)).compareTo(myLocation.distanceSquaredTo(o2));
				}
    		});
    		
    		focusLocation = friendlyPastrLocations[0];
		} else {
			focusLocation = myLocation;
		}
		
		return focusLocation;
	}
}
