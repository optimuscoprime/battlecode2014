package marcel;

import java.util.*;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static marcel.Util.*;
import static battlecode.common.RobotType.*;

public class HeadquartersPlayer extends BasicPlayer implements Player  {
		    
	private MapLocation pastrConstructionLocation;
	private MapLocation noiseTowerConstructionLocation;
	private int waypointRound;
	private Robot[] allFriendlyRobots;
	private Map<Robot, RobotInfo> allFriendlyRobotInfo;
	private int numAllFriendlySoldiers;
	private GameMap gameMap;

	
	public HeadquartersPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		
		// hack - spawn a robot before creating map
		
		boolean spawned = false;
		
		if (rc.isActive()) {
			spawned = tryToSpawn();
		}
		
		// every player builds their own map
		gameMap = new GameMap(robotId, team, robotType, rc, enemyHqLocation, width, height);
		
		if (spawned) {
			rc.yield();
		}
	}

	@Override
	public void playOneTurn() throws GameActionException {
		
		super.playOneTurn();
		
		allFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
		allFriendlyRobotInfo = senseAllRobotInfo(allFriendlyRobots);
		allFriendlyRobotInfo.keySet().toArray(new Robot[0]);
		
		//numAllFriendlySoldiers = countSoldiers(allFriendlyRobotInfo);		
		
		if (rc.isActive()) {
			
			// try attacking nearby enemies
	    	
	        boolean didAttack = false;
	        
	        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class,myLocation,HUGE_RADIUS,opponentTeam);
	        
	        final Map<Robot, RobotInfo> nearbyEnemyMap = senseAllRobotInfo(nearbyEnemies);
	        
	        RobotInfo[] nearbyEnemyInfos = nearbyEnemyMap.values().toArray(new RobotInfo[0]);
	        
	        
	        //int numNearbyEnemySoldiers = countSoldiers(nearbyEnemyInfo);       

	        if (nearbyEnemyInfos.length > 0) {
	        	
	        	sort(nearbyEnemyInfos, new Comparator<RobotInfo>() {        		
					@Override
					public int compare(RobotInfo o1, RobotInfo o2) {
						return Double.compare(o1.health,o2.health);
						
					}
	        	});
	        	        	
	        	// try to attack one of them
	        	for (RobotInfo nearbyInfo: nearbyEnemyInfos) {
        			if (rc.canAttackSquare(nearbyInfo.location)) {
        				try {
        					rc.attackSquare(nearbyInfo.location);
        					didAttack = true;
        					break;
        				} catch (GameActionException e) {
        					die(e);
        				}
	        		}
	        	}
	        	
	        	if (!didAttack) {
	        		// try to attack with splash damage
	        		
	            	for (RobotInfo nearbyInfo: nearbyEnemyInfos) {
	            		MapLocation splashDamageLocation = nearbyInfo.location.add(nearbyInfo.location.directionTo(myLocation));
	        			if (rc.canAttackSquare(splashDamageLocation)) {
	        				try {
	        					rc.attackSquare(splashDamageLocation);
	        					didAttack = true;
	        					break;
	        				} catch (GameActionException e) {
	        					die(e);
	        				}
		        		}
	            	}       		
	        	}
	        	
	        }
		
	        if (!didAttack) {
	        	tryToSpawn();
	        }
		}
		
		//if (Clock.getRoundNum() > 500) {
		
			maybeAskForNoiseTower();
		
			maybeAskForPastr();
		//}
				 
		rc.yield();
		return;
	}
	
	private void maybeAskForPastr() throws GameActionException {
		// if we don't have a noise tower near our spawn, ask for one
		// requirement: "behind" our spawn
		// requirement: open space
						
        MapLocation[] pastrLocations = rc.sensePastrLocations(myTeam);
		int numPastrs = pastrLocations.length;
		
		boolean havePastr = false;
		boolean constructing = false;
		
		if (numPastrs > 0) {
			havePastr = true;
		}
		
		if (!havePastr && pastrConstructionLocation != null) {
			Robot robot = (Robot) rc.senseObjectAtLocation(pastrConstructionLocation);
			if (robot != null) {
				RobotInfo info = rc.senseRobotInfo(robot);
				if (info.isConstructing) {
					constructing = true;
				//} else {
				//	havePastr = true;
				}
			}
		}
		
		if (havePastr) {
			rc.setIndicatorString(0,  "cancel pastr (have pastr)");
			rc.broadcast(RADIO_CHANNEL_REQUEST_PASTR, 0);
		} else if (!constructing) {		
			pastrConstructionLocation = findGoodPastrLocation();
			if (pastrConstructionLocation != null) {
				rc.setIndicatorString(0,  "ask for pastr");
				rc.broadcast(RADIO_CHANNEL_REQUEST_PASTR, locationToInt(pastrConstructionLocation));
			} else {
				rc.setIndicatorString(0,  "cancel pastr (no good location)");
				rc.broadcast(RADIO_CHANNEL_REQUEST_PASTR, 0);
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
					die(e);
				}
			}
		}
		
		boolean haveNoiseTower = false;
		boolean constructing = false;
		
		if (numNoiseTowers > 0) {
			haveNoiseTower = true;
		}

		if (!haveNoiseTower && noiseTowerConstructionLocation != null) {
			Robot robot = (Robot) rc.senseObjectAtLocation(noiseTowerConstructionLocation);
			if (robot != null) {
				RobotInfo info = rc.senseRobotInfo(robot);
				if (info.isConstructing) {
					constructing = true;
				//} else {
				//	haveNoiseTower = true;
				}
			}
		}		
		
		if (haveNoiseTower) {
			rc.broadcast(RADIO_CHANNEL_REQUEST_NOISETOWER, 0);
			rc.setIndicatorString(1,  "cancel noisetower (have noisetower)");
		} else if (!constructing) {		
			noiseTowerConstructionLocation = findGoodNoiseTowerLocation(); 
			if (noiseTowerConstructionLocation != null) {
				rc.setIndicatorString(1,  "ask for noisetower");
				rc.broadcast(RADIO_CHANNEL_REQUEST_NOISETOWER, locationToInt(noiseTowerConstructionLocation));				
			} else {
				rc.setIndicatorString(1,  "cancel noisetower (no good location)");
				rc.broadcast(RADIO_CHANNEL_REQUEST_NOISETOWER, 0);
			}
		}
	}

	private MapLocation findGoodPastrLocation() throws GameActionException {
		// want a location near the HQ
		// that doesn't have many voids around it
		
		// idea; consider all locations near us
		// do a fancy sort
		
		MapLocation[] possibleConstructionLocations = MapLocation.getAllMapLocationsWithinRadiusSq(myLocation, myRobotType.sensorRadiusSquared);
	    Arrays.sort(possibleConstructionLocations, new Comparator<MapLocation>() {
			@Override
			public int compare(MapLocation o1, MapLocation o2) {
				// distance to our HQ + distance to enemy HQ
				int result = new Integer(o2.distanceSquaredTo(enemyHqLocation)).compareTo(o1.distanceSquaredTo(enemyHqLocation));
				if (result == 0) {
					result = new Integer(o1.distanceSquaredTo(myHqLocation)).compareTo(o2.distanceSquaredTo(myHqLocation));
				}
				return result;
			}
	    });
	    
	    // sorted by locations that are far away from enemy hq
	    
		MapLocation bestLocation = null;
		int bestFreeTiles = 0;
	    
	    for (MapLocation possibleConstructionLocation: possibleConstructionLocations) {
	    	
	    	if (!gameMap.isTraversable(possibleConstructionLocation)) {
	    		continue;
	    	}
	    	
			int freeTiles = 0;
			
			for (Direction direction: allDirections) {
				MapLocation possibleLocation = possibleConstructionLocation.add(direction);
				if (gameMap.isTraversable(possibleLocation.x, possibleLocation.y)) {					
					freeTiles++;
				}
			}
			
			if (freeTiles > 4) {
				boolean canBuild = true;
				
				// check if there is a construction there
				Robot robot = (Robot) rc.senseObjectAtLocation(possibleConstructionLocation);
				
				if (robot != null) {
					RobotInfo info = rc.senseRobotInfo(robot);
					if (info.type != SOLDIER) {
						canBuild = false;
					}
				}
						
				if (canBuild && freeTiles > bestFreeTiles) {
					bestLocation = possibleConstructionLocation;
					bestFreeTiles = freeTiles;
				}
			}

			if (bestFreeTiles > 4) {
				break;
			}			
		}
		
		return bestLocation;
	}
	
	private MapLocation findGoodNoiseTowerLocation() throws GameActionException {
		// want a location near the HQ
		// that doesn't have many voids around it
		
		// idea; consider all locations near us
		// do a fancy sort
		
		MapLocation[] pastrLocations = rc.sensePastrLocations(myTeam);
		int numPastrs = pastrLocations.length;
		
		if (numPastrs == 0) {
			return null;
		}
		
		MapLocation closestPastrLocation = pastrLocations[0];
		int closestPastrDistance = myLocation.distanceSquaredTo(pastrLocations[0]);
				
		for (MapLocation pastrLocation : pastrLocations) {
			int thisDistance = myLocation.distanceSquaredTo(pastrLocation);
			if (thisDistance < closestPastrDistance) {
				closestPastrDistance = thisDistance;
				closestPastrLocation = pastrLocation;
			}
		}
		
		final MapLocation finalClosestPastrLocation = closestPastrLocation;
		
		MapLocation[] possibleConstructionLocations = MapLocation.getAllMapLocationsWithinRadiusSq(myLocation, myRobotType.sensorRadiusSquared);
	    Arrays.sort(possibleConstructionLocations, new Comparator<MapLocation>() {
			@Override
			public int compare(MapLocation o1, MapLocation o2) {
				// distance to our HQ + distance to enemy HQ
				int result = new Integer(o1.distanceSquaredTo(finalClosestPastrLocation)).compareTo(o2.distanceSquaredTo(finalClosestPastrLocation));
				if (result == 0) {
					result = new Integer(o1.distanceSquaredTo(myHqLocation)).compareTo(o2.distanceSquaredTo(myHqLocation));
				}
				return result;
			}
	    });
	    
	    // sorted by locations that are far away from enemy hq
	    
		MapLocation bestLocation = null;
		int bestFreeTiles = 0;
	    
	    for (MapLocation possibleConstructionLocation: possibleConstructionLocations) {
	    	
	    	if (!gameMap.isTraversable(possibleConstructionLocation)) {
	    		continue;
	    	}	    	
	    	
			int freeTiles = 0;
			
			for (Direction direction: allDirections) {
				MapLocation possibleLocation = possibleConstructionLocation.add(direction);
				if (gameMap.isTraversable(possibleLocation.x, possibleLocation.y)) {					
					freeTiles++;
				}
			}
			
			if (freeTiles > 4) {
				boolean canBuild = true;
				
				// check if there is a construction there
				Robot robot = (Robot) rc.senseObjectAtLocation(possibleConstructionLocation);
				
				if (robot != null) {
					RobotInfo info = rc.senseRobotInfo(robot);
					if (info.type != SOLDIER) {
						canBuild = false;
					}
				}
						
				if (canBuild && freeTiles > bestFreeTiles) {
					bestLocation = possibleConstructionLocation;
					bestFreeTiles = freeTiles;
				}
			}

			if (bestFreeTiles > 4) {
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
	                    rc.spawn(direction);
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
	
	
}
