package awesemo;

import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import battlecode.common.*;

import java.util.*;

import awesemo.Util;
import static awesemo.Util.*;

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

	protected Robot[] allFriendlyRobots;

	protected int numAllFriendlySoldiers;

	protected Robot[] nearbyFriendlyRobots;

	protected int numNearbyFriendlySoldiers;

	protected RobotInfo myRobotInfo = null;
	
	protected double myHealth;

	private MapLocation rallyPoint;

	protected Map<Robot, RobotInfo> allFriendlyRobotInfo;

	protected Map<Robot, RobotInfo> nearbyFriendlyRobotInfo;

	protected ArrayDeque<MapLocation> trail;
    
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
	
		this.rallyPoint = null;
		
		this.trail = new ArrayDeque<MapLocation>();
		
		//new MapLocation((int) (2/3.0 * myHqLocation.x + 1/3.0 * enemyHqLocation.x), (int) (2/3.0 * myHqLocation.y + 1/3.0 * enemyHqLocation.y));
		
		//while (!gameMap.isTraversable(rallyPoint.x, rallyPoint.y)) {
		//	rallyPoint = rallyPoint.add(rallyPoint.directionTo(myHqLocation));
		//}
	}
	
	public void playOneTurn() throws GameActionException {
		// keep this up to date
		myRobotInfo = rc.senseRobotInfo(myRobot);
		myHealth = myRobotInfo.health;
		myLocation = myRobotInfo.location;
		
		allFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
		allFriendlyRobotInfo = senseAllRobotInfo(allFriendlyRobots);
		allFriendlyRobots = allFriendlyRobotInfo.keySet().toArray(new Robot[0]);
		
		numAllFriendlySoldiers = countSoldiers(allFriendlyRobotInfo);
		
		// pastr has a terrible sensor radius, so always use 35 here
		nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
		nearbyFriendlyRobotInfo = senseAllRobotInfo(nearbyFriendlyRobots);
		nearbyFriendlyRobots = nearbyFriendlyRobotInfo.keySet().toArray(new Robot[0]);
		
		numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobotInfo);	
		
		rc.setIndicatorString(0, ""); // gotoLocation
		
		rc.setIndicatorString(1, ""); // 
		
		rc.setIndicatorString(2, "");
		
		if (trail.size() > 10) {
			trail.remove();
		}
	}
	
    private Map<Robot, RobotInfo> senseAllRobotInfo(Robot[] robots) throws GameActionException {
    	
    	Map<Robot, RobotInfo> allInfo = new HashMap<Robot, RobotInfo>();
    	
    	for (Robot robot: robots) {
    		if (rc.canSenseObject(robot)) {
    			try {
    				RobotInfo info = rc.senseRobotInfo(robot);
    				allInfo.put(robot,  info);
    			} catch (GameActionException e) {
    				// ?
    			}
    		}
    	}

    	return allInfo;
	}

	protected boolean attackNearbyEnemies() throws GameActionException {
    	
    	//log("Started attackNearbyEnemies()...");
    	
        boolean tookAction = false;
        
        MapLocation myLocation = rc.getLocation();
        
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
            Robot.class,
            myLocation,
            HUGE_RADIUS,
            opponentTeam
        );
        final Map<Robot, RobotInfo> nearbyEnemyInfo = senseAllRobotInfo(nearbyEnemies);
        nearbyEnemies = nearbyEnemyInfo.keySet().toArray(new Robot[0]);
        
        int numNearbyEnemySoldiers = countSoldiers(nearbyEnemyInfo);
        
    	if (myRobotType == SOLDIER) {
    		if (myHealth < myRobotType.maxHealth / 3 || numNearbyFriendlySoldiers < numNearbyEnemySoldiers || numNearbyFriendlySoldiers < 2) {
    			
    			// pick a good rally point
    			
    			rallyPoint = getSoldierCenterLocation(); 	
    				
    			if (rallyPoint == null) {
    				rallyPoint = myHqLocation;
    			}
    			
    			// maybe we should retreat
    			if (myLocation.distanceSquaredTo(rallyPoint) >= 35) {
    				// go there
    				rc.setIndicatorString(1, "go to rally point");
    				gotoLocation(rallyPoint);
    				tookAction = true;
    			}
    		}        		
    	}        

        if (!tookAction && nearbyEnemies.length > 0) {
        	
        	sort(nearbyEnemies, new Comparator<Robot>() {
        		// idea:
        		// prefer to attack pastrs
        		// otherwise soldiers
        		// otherwise noisetowers
        		// never hqs
        		
				@Override
				public int compare(Robot o1, Robot o2) {
					RobotInfo info1 = nearbyEnemyInfo.get(o1);
					RobotInfo info2 = nearbyEnemyInfo.get(o2);
					
//					if (info1.type == PASTR && info2.type != PASTR) {
//						return -1;
//					} else if (info1.type != PASTR && info2.type == PASTR) {
//						return 1;
//					} else if (info1.type == SOLDIER && info2.type != SOLDIER) {
//						return -1;
//					} else if (info1.type != SOLDIER && info2.type == SOLDIER) {
//						return 1;
//					} else if (info1.type == NOISETOWER && info2.type != NOISETOWER) {
//						return -1;
//					} else if (info1.type != NOISETOWER && info2.type == NOISETOWER) {
//						return 1;
//					} else if (info1.type == HQ && info2.type != HQ) {
//						return 1;
//					} else if (info1.type != HQ && info2.type == HQ) {
//						return -1;
//					} else {
//						// if same type, just sort on health
					return new Double(info1.health).compareTo(info2.health);
					//}
				}
        	});
        	        	
        	// try to attack one of them as long as it isn't the HQ
        	for (Robot nearbyEnemyRobot: nearbyEnemies) {
        		RobotInfo info = nearbyEnemyInfo.get(nearbyEnemyRobot);
        		if (info.type != HQ && info.type != NOISETOWER) {
        			if (rc.canAttackSquare(info.location)) {
	        			rc.attackSquare(info.location);
	        			tookAction = true;
	        			break;
	        		}
        		}
        	}
        	
//        	if (!tookAction && myRobotType == SOLDIER) {
//        		
//        		// there are soldiers nearby, but none of them were attackable
//        		
//
//        		
//	        	// try to move towards one of them as long as it isn't the hq
//	        	for (Robot nearbyEnemyRobot: nearbyEnemies) {
//	        		RobotInfo info = allRobotInfo.get(nearbyEnemyRobot);
//	        		if (info.type != HQ) {
//	        			gotoLocation(info.location);
//	        			tookAction = true;
//	        			break;
//	        		}
//	        	}      	
//        	}
        	
        }

        //log("Finished attackNearbyEnemies().");
        
        return tookAction;
    }	
    
    protected MapLocation getSoldierCenterLocation() {
    	MapLocation centerLocation = null;
    	
    	if (numAllFriendlySoldiers > 1) {
    		double totalX = 0;
    		double totalY = 0;
    		
    		int n = 0;
    		
    		// find the average pos
    		for (RobotInfo info: allFriendlyRobotInfo.values()) {
    			if (info.type == SOLDIER) {
    				totalX += info.location.x;
    				totalY += info.location.y;
    				n++;
    			}
    		}
    		
    		double averageX = totalX / n;
    		double averageY = totalY / n;    	
    		
    		centerLocation = new MapLocation((int) averageX, (int) averageY);
    		
    		if (!gameMap.isTraversable(centerLocation)) {
    			// would be better to search around the center loc
    			centerLocation = myHqLocation;
    		}
    		
//    		while (!gameMap.isTraversable(centerLocation)) {
//    			// try exploring around
//    		
//    		}
    	}
		
		return centerLocation;
	}

	protected void moveRandomly() {
    	rc.setIndicatorString(2, "moveRandomly");
    	
		// let's try moving randomly
		shuffle(randomDirections);
		for (Direction randomDirection: randomDirections) {
			boolean canMove = rc.canMove(randomDirection);
	
			// handle this elsewhere?
			
			MapLocation newLocation = myLocation.add(randomDirection);
		
			if (trail.contains(newLocation)) {
				canMove = false;
			}
			
			if (canMove) {
	
				int newDistanceToEnemyHq = enemyHqLocation.distanceSquaredTo(newLocation);
				if (newDistanceToEnemyHq <= RobotType.HQ.attackRadiusMaxSquared) {
					//log("can't move");
					canMove = false;
				}
			}
			
			if (canMove) {
				
				try	{
					//log("sneaking randomly");
					rc.sneak(randomDirection);
					trail.add(newLocation);
					break;
				} catch (GameActionException e) {
					// we already checked canMove
					//die(e);
				}
			}
		} 			
    }
    
	protected void gotoLocation(MapLocation toLocation) throws GameActionException {
		
		//if (numNearbyFriendlySoldiers < 3) {
		//	toLocation = rallyPoint;
		//}
		
		rc.setIndicatorString(0, "gotoLocation: " + toLocation);
		
		//log("started nextDirectionTo...");
		
		// idea: return to spawn before doing a messy calculation
		// idea: do the flood fill in parallel (use many robots?) - for big maps
		
		Direction direction = gameMap.nextDirectionTo(myLocation,toLocation);		
	    //log("finished nextDirectionTo.");
		
		// just try going there, if we are still waiting for the perfect map
		if (direction == null) {
			direction = approximateDirectionTo(myLocation, toLocation);
		}
	    
	    if (direction != null) {
	    	MapLocation newLocation = myLocation.add(direction);
	    	
	    	boolean canMove = rc.canMove(direction);
	        
	    	// don't go near enemy hq
			if (canMove) {	
				int newDistanceToEnemyHq = enemyHqLocation.distanceSquaredTo(toLocation);
				if (newDistanceToEnemyHq <= RobotType.HQ.attackRadiusMaxSquared) {
					//log("can't move");
					canMove = false;
				}
			}
	    	
			if (canMove) {
				
				MapLocation focusLocation = getFocusLocation();
								
				int newDistanceToFocus = focusLocation.distanceSquaredTo(newLocation);
				int currentDistanceToFocus = focusLocation.distanceSquaredTo(myLocation);
				
				//int distanceToMyHq = myHqLocation.distanceSquaredTo(myLocation);
				
				//double newLocationCows = rc.senseCowsAtLocation(newLocation);
				
				try	{
					if (newDistanceToFocus < currentDistanceToFocus) { // && newLocationCows > 100) {
						// herd cattle
						rc.move(direction);
					} else {
						// don't disturb cattle
						rc.sneak(direction);
					}
					trail.add(newLocation);
				} catch (GameActionException e) {
					// don't die here
					// we already checked canMove
					// die(e);
				}
				
			} else {
				
				// TODO: should sneak/herd here maybe
				
				// maybe move randomly
				
				if (Util.random.nextDouble() < 0.1) {
					moveRandomly();
				}
			}
	    }
	}   
	
	protected MapLocation getFocusLocation() {
		//log("getFocusLocation start");
		
		MapLocation[] friendlyPastrLocations = rc.sensePastrLocations(myTeam);
		
		MapLocation focusLocation = myHqLocation;
		
		if (friendlyPastrLocations.length > 0) {
		
    		// pick the pastr location that is closest to us
    		
    		sort(friendlyPastrLocations, new Comparator<MapLocation>() {
				@Override
				public int compare(MapLocation o1, MapLocation o2) {
					return new Integer(myLocation.distanceSquaredTo(o1)).compareTo(myLocation.distanceSquaredTo(o2));
				}
    		});
    		
    		focusLocation = friendlyPastrLocations[0];
		}
		
		//log("getFocusLocation end");
		
		return focusLocation;
	}
	
	public Direction approximateDirectionTo(MapLocation from, MapLocation to) {
		Direction exactDirection = from.directionTo(to);
		Direction approximateDirection = exactDirection;
		
		
		if (!trail.contains(from.add(approximateDirection)) & rc.canMove(approximateDirection)) {
			// good
		} else {
			approximateDirection = exactDirection.rotateLeft();
			if (trail.contains(from.add(approximateDirection)) || !rc.canMove(approximateDirection)) {
				approximateDirection = exactDirection.rotateRight();
				if (trail.contains(from.add(approximateDirection)) || !rc.canMove(approximateDirection)) {
					approximateDirection = exactDirection.rotateLeft().rotateLeft();
					if (trail.contains(from.add(approximateDirection)) || !rc.canMove(approximateDirection)) {
						approximateDirection = exactDirection.rotateRight().rotateRight();
					}
				}
			}
		}
		
		return approximateDirection;
	}	
}
