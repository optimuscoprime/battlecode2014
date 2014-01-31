package awesemo;

import java.util.*;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static awesemo.Util.*;
import static battlecode.common.RobotType.*;

public class HeadquartersPlayer extends BasicPlayer implements Player  {
		    
	private MapLocation pastrConstructionLocation;
	private MapLocation noiseTowerConstructionLocation;
	private int waypointRound;
	private Robot[] allFriendlyRobots;
	private Map<Robot, RobotInfo> allFriendlyRobotInfo;
	private int numAllFriendlySoldiers;
	
	public HeadquartersPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		
		super.playOneTurn();
		
		allFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
		allFriendlyRobotInfo = senseAllRobotInfo(allFriendlyRobots);
		allFriendlyRobotInfo.keySet().toArray(new Robot[0]);
		
		numAllFriendlySoldiers = countSoldiers(allFriendlyRobotInfo);		
		
		//while (true) {
		//	gameMap.nextDirectionTo(myHqLocation, enemyHqLocation);
		//}
		
	
		// evaluate strategy
		
		// things we can do:
		//
		// 1. order other units around
		// 2. attack nearby enemies
		// 3. spawn a new robot
		
		// 1. order other units around
		// 1a. tell someone to become a noisetower
		// 1b. tell someone to become a pastr
		// 1c. tell someone to go to a position on the map
		
		// 2. attack nearby enemies
		// sense nearby enemies (to HQ)
		// attack them
		
		// 3. spawn a new robot
		// if not attacking
		
		//allFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
		//numAllFriendlySoldiers = countSoldiers(allFriendlyRobots, rc);
		
		//nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, myRobotType.sensorRadiusSquared*2, myTeam);
		//numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobots, rc);		
		
		boolean didAttack = false;
		
		if (rc.isActive()) {
			didAttack = attackNearbyEnemies();
		}
		
		boolean didSpawn = false;
		
		if (!didAttack && rc.isActive()) {
			didSpawn = tryToSpawn();
		}
		

		
		//if (friendlyRobots.length > 16) {
		
		if (numAllFriendlySoldiers > 4) {
			
			maybeAskForNoiseTower();
			
			maybeAskForPastr();
		
		}
		
		//if (Clock.getRoundNum() > 500) {
		//maybeCreateExploringWaypoint();	
		//}
	
		// try creating maps
		
		createCachedMaps();
		 
		rc.yield();
	}
	
	private void createCachedMaps() {
		// todo maybe cache maps and broadcast them
	}

	private void maybeAskForPastr() throws GameActionException {
		// if we don't have a noise tower near our spawn, ask for one
		// requirement: "behind" our spawn
		// requirement: open space
						
		int numPastrs = 0;
		
		for (Robot friendlyRobot: allFriendlyRobots) {
			if (rc.canSenseObject(friendlyRobot)) {
				try {
					RobotInfo info = rc.senseRobotInfo(friendlyRobot);
					if (info.type == PASTR) {
						numPastrs++;
					}
				} catch (GameActionException e) {
					// ?
				}
			}
		}
		
		boolean havePastr = false;
		if (numPastrs > 0) {
			havePastr = true;
		}
		
		if (!havePastr && pastrConstructionLocation != null) {
			Robot robot = (Robot) rc.senseObjectAtLocation(pastrConstructionLocation);
			if (robot != null) {
				RobotInfo info = rc.senseRobotInfo(robot);
				if (info.isConstructing) {
					havePastr = true;
				}
			}
		}
		
		if (havePastr) {
			rc.broadcast(RADIO_CHANNEL_REQUEST_PASTR, 0);
		} else {		
			pastrConstructionLocation = findGoodConstructionLocation();
			if (pastrConstructionLocation != null) {
				rc.broadcast(RADIO_CHANNEL_REQUEST_PASTR, locationToInt(pastrConstructionLocation));
			}
		}
		
	}    
    
	private void maybeAskForNoiseTower() throws GameActionException {
		// if we don't have a noise tower near our spawn, ask for one
		// requirement: "behind" our spawn
		// requirement: open space
						
		int numNoiseTowers = 0;
		
		for (Robot friendlyRobot: allFriendlyRobots) {
			if (rc.canSenseObject(friendlyRobot)) {
				try {
					RobotInfo info = rc.senseRobotInfo(friendlyRobot);
					if (info.type == NOISETOWER) {
						numNoiseTowers++;
					}
				} catch (GameActionException e) {
					// ?
				}
			}
		}
		
		boolean haveNoiseTower = false;
		if (numNoiseTowers > 0) {
			haveNoiseTower = true;
		}

		if (!haveNoiseTower && noiseTowerConstructionLocation != null) {
			Robot robot = (Robot) rc.senseObjectAtLocation(noiseTowerConstructionLocation);
			if (robot != null) {
				RobotInfo info = rc.senseRobotInfo(robot);
				if (info.isConstructing) {
					haveNoiseTower = true;
				}
			}
		}		
		
		if (haveNoiseTower) {
			rc.broadcast(RADIO_CHANNEL_REQUEST_NOISETOWER, 0);
		} else {		
			noiseTowerConstructionLocation = findGoodConstructionLocation();
			if (noiseTowerConstructionLocation != null) {
				rc.broadcast(RADIO_CHANNEL_REQUEST_NOISETOWER, locationToInt(noiseTowerConstructionLocation));				
			}
		}
	}

	private MapLocation findGoodConstructionLocation() throws GameActionException {
		// want a location near the HQ
		// that doesn't have many voids around it
				
		
		// idea; consider all locations near us
		// do a fancy sort
		
		
		// sort by distance to enemy HQ
		// prefer bigger distances
		Direction[] sortedDirections = allDirections.clone();
		sort(sortedDirections, new Comparator<Direction>() {
			public int compare(Direction o1, Direction o2) {
				return new Integer(myLocation.add(o2).distanceSquaredTo(enemyHqLocation)).compareTo(myLocation.add(o1).distanceSquaredTo(enemyHqLocation));
			}
			
		}); 
		
		Deque<MapLocation> possibleLocations = new ArrayDeque<MapLocation>();
		Set<MapLocation> visitedLocations = new HashSet<MapLocation>();
		
		visitedLocations.add(myLocation);		
		
		for (Direction direction: sortedDirections) {
			MapLocation possibleLocation = myLocation.add(direction);
			if (gameMap.isTraversable(possibleLocation.x, possibleLocation.y)) {
				possibleLocations.add(possibleLocation);
				visitedLocations.add(possibleLocation);
			}
		}
		
		MapLocation bestLocation = null;
		int bestFreeTiles = 0;
		
		boolean found = false;
		int i =0;
		while (!found && !possibleLocations.isEmpty()) {
			MapLocation currentLocation = possibleLocations.remove();
			
			int freeTiles = 0;
			
			for (Direction direction: sortedDirections) {
				MapLocation possibleLocation = currentLocation.add(direction);
				if (gameMap.isTraversable(possibleLocation.x, possibleLocation.y)) {					
					freeTiles++;
					if (!visitedLocations.contains(possibleLocation)) {
						possibleLocations.add(possibleLocation);
						visitedLocations.add(possibleLocation);
					}
				}
			}
			
			if (freeTiles > 4) {
				boolean canBuild = true;
				
				// check if there is a construction there
				Robot robot = (Robot) rc.senseObjectAtLocation(currentLocation);
				
				if (robot != null) {
					RobotInfo info = rc.senseRobotInfo(robot);
					if (info.type != SOLDIER) {
						canBuild = false;
					}
				}
						
				if (canBuild && freeTiles > bestFreeTiles) {
					bestLocation = currentLocation;
					bestFreeTiles = freeTiles;
				}
			}
			
			i++;
	
			if (bestFreeTiles > 6 || i > 16) {
				break;
			}			
			
		}
		
		return bestLocation;
	}

	private boolean tryToSpawn () {
        // check surrounding squares
        // spawn in one of them
		
		boolean spawned = false;
		
		int robotCount = rc.senseRobotCount();
		
		if (robotCount < GameConstants.MAX_ROBOTS) {
	    	shuffle(randomDirections); 
	
	        for (Direction direction: randomDirections) {
	            if (rc.canMove(direction)) {
	                try {
	                	log("Trying to spawn...");
	                    rc.spawn(direction);
	                    log("Spwaned");
	                    spawned = true;
	                    break;
	                } catch (GameActionException e) {
	                    die(e);
	                }
	            }
	        }
		}
		
        return spawned;
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
					//}
					
					return new Double(info1.health).compareTo(info2.health);
					
				}
        	});
        	        	
        	// try to attack one of them as long as it isn't the HQ
        	for (RobotInfo nearbyInfo: nearbyEnemyInfo.values()) {
        		//if (nearbyInfo.type != HQ && nearbyInfo.type != NOISETOWER) {
        			if (rc.canAttackSquare(nearbyInfo.location)) {
	        			rc.attackSquare(nearbyInfo.location);
	        			tookAction = true;
	        			break;
	        		}
        		//}
        	}
        	
        	if (!tookAction) {
        		// try attacting a nearby square
            	// try to attack one of them as long as it isn't the HQ
            	for (RobotInfo nearbyInfo: nearbyEnemyInfo.values()) {
            		//if (nearbyInfo.type != HQ && nearbyInfo.type != NOISETOWER) {
        			if (rc.canAttackSquare(nearbyInfo.location.add(nearbyInfo.location.directionTo(myLocation)))) {
	        			rc.attackSquare(nearbyInfo.location);
	        			tookAction = true;
	        			break;
	        		}
            		//}
            	}       		
        	}
        	
        }

        //log("Finished attackNearbyEnemies().");
        
        return tookAction;
    }		
}
