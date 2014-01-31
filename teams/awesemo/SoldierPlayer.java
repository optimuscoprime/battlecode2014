package awesemo;

import java.util.*;

import battlecode.common.*;
import static awesemo.Util.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import static awesemo.Util.*;

public class SoldierPlayer extends BasicPlayer implements Player {
	
	private MapLocation pastrConstructionMapLocation;
	private MapLocation noiseTowerConstructionMapLocation;

	private MapLocation waypointLocation;
	private int waypointRound;
	private int lastWaypointRoundNum = 0;
	private Robot[] nearbyFriendlyRobots;
	private Map<Robot, RobotInfo> nearbyFriendlyRobotInfo;
	private int numNearbyFriendlySoldiers;
	private Robot[] allFriendlyRobots;
	private Map<Robot, RobotInfo> allFriendlyRobotInfo;
	private int numAllFriendlySoldiers;
	
	protected Deque<MapLocation> trail;

	
	public SoldierPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		
		this.trail = new ArrayDeque<MapLocation>();

	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
		
		if (!rc.isActive()) {
			
			rc.setIndicatorString(1, "not active");
	
		} else {
			
			if (trail.size() > 10) {
				trail.remove();
			}
			
			allFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
			allFriendlyRobotInfo = senseAllRobotInfo(allFriendlyRobots);
			allFriendlyRobots = allFriendlyRobotInfo.keySet().toArray(new Robot[0]);
			
			numAllFriendlySoldiers = countSoldiers(allFriendlyRobotInfo);		
			
			// pastr has a terrible sensor radius, so always use 35 here
			nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
			nearbyFriendlyRobotInfo = senseAllRobotInfo(nearbyFriendlyRobots);
			nearbyFriendlyRobots = nearbyFriendlyRobotInfo.keySet().toArray(new Robot[0]);
			
			numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobotInfo);	
			
			boolean tookAction = attackNearbyEnemies();
			
			// try making a broadcast tower
			if (!tookAction) {
				// check the broadcasts
				int intNoiseTowerLocation = rc.readBroadcast(RADIO_CHANNEL_REQUEST_NOISETOWER);
				if (intNoiseTowerLocation != 0) {
					noiseTowerConstructionMapLocation = intToLocation(intNoiseTowerLocation);
					if (myLocation.equals(noiseTowerConstructionMapLocation)) {
						if (numNearbyFriendlySoldiers > 2) {
							rc.construct(NOISETOWER);
						}
					} else {
						// check if another robot is there
						//Robot robot = rc.senseObjectAtLocation(noiseTowerConstructionMapLocation);
						
						rc.setIndicatorString(1,  "build noisetower");
						gotoLocation(noiseTowerConstructionMapLocation);
					}
					tookAction = true;
				}				
			}
			
			// try making a pastr
			if (!tookAction) {
				// check the broadcasts
				int intPastrTowerLocation = rc.readBroadcast(RADIO_CHANNEL_REQUEST_PASTR);
				if (intPastrTowerLocation != 0) {
					pastrConstructionMapLocation = intToLocation(intPastrTowerLocation);
					if (myLocation.equals(pastrConstructionMapLocation)) {
						if (numNearbyFriendlySoldiers > 2) {
							rc.construct(PASTR);
						}
					} else {
						rc.setIndicatorString(1,  "build pastr");
						gotoLocation(pastrConstructionMapLocation);
					}
					tookAction = true;
				}					
			}
			
			if (!tookAction) {
				// maybe we should construct one anyway
				// if (rc.senseCowsAtLocation(myLocation) > 1000 && cowGrowth[myLocation.x][myLocation.y] 
					
				// don't listen for waypoints all the time
				//int thisRoundNum = Clock.getRoundNum();
				
				int intWaypointLocation = rc.readBroadcast(RADIO_CHANNEL_PASTR_BACKUP);
				
				if (intWaypointLocation != 0) {
					//if (thisRoundNum > lastWaypointRoundNum) {
					waypointLocation = intToLocation(intWaypointLocation);
					
					
					
					// check if there is a closer friendly soldier
					
					int closerRobots = 0;
					int myDistanceToWaypoint = myLocation.distanceSquaredTo(waypointLocation);
					
					for (RobotInfo info : allFriendlyRobotInfo.values()) {
						if (info.type == SOLDIER && info.location.distanceSquaredTo(waypointLocation) < myDistanceToWaypoint) {
							closerRobots++;
						}
					}
					
					if (closerRobots > 5) {
						
						rc.setIndicatorString(1,  "ignoring pastr waypoint: " + waypointLocation);
						waypointLocation = null; // don't go there
						
						
					} else {
						rc.setIndicatorString(1,  "received pastr waypoint: " + waypointLocation);
					}
					
					
					//lastWaypointRoundNum = thisRoundNum;
					//} else {
						// keep current waypoint before overriding
					//}
				} else {
					// allow waypoint resets at any time
					waypointLocation = null;
				}
				
				if (waypointLocation == null) {
					
					intWaypointLocation = rc.readBroadcast(RADIO_CHANNEL_NOISETOWER_BACKUP);
					
					if (intWaypointLocation != 0) {
						//if (thisRoundNum > lastWaypointRoundNum) {
						waypointLocation = intToLocation(intWaypointLocation);
						
						
						
						// check if there is a closer friendly soldier
						
						int closerRobots = 0;
						int myDistanceToWaypoint = myLocation.distanceSquaredTo(waypointLocation);
						
						for (RobotInfo info : allFriendlyRobotInfo.values()) {
							if (info.type == SOLDIER && info.location.distanceSquaredTo(waypointLocation) < myDistanceToWaypoint) {
								closerRobots++;
							}
						}
						
						if (closerRobots > 5) {
							
							rc.setIndicatorString(1,  "ignoring noisetower waypoint: " + waypointLocation);
							waypointLocation = null; // don't go there
							
							
						} else {
							rc.setIndicatorString(1,  "received noisetower waypoint: " + waypointLocation);
						}
						
						
						//lastWaypointRoundNum = thisRoundNum;
						//} else {
							// keep current waypoint before overriding
						//}
					} else {
						// allow waypoint resets at any time
						waypointLocation = null;
					}
										
					
				}
				
				
				if (myLocation.equals(waypointLocation)) {
					waypointLocation = null;
				}
				
				if (waypointLocation == null) {
					MapLocation[] enemyPastrLocations = rc.sensePastrLocations(opponentTeam);
					
					if (enemyPastrLocations.length > 0) {
		    		
			    		// pick the pastr location that is closest to us
			    		
			    		sort(enemyPastrLocations, new Comparator<MapLocation>() {
							@Override
							public int compare(MapLocation o1, MapLocation o2) {
								return new Integer(myLocation.distanceSquaredTo(o1)).compareTo(myLocation.distanceSquaredTo(o2));
							}
			    		});
			    		
			    		waypointLocation = enemyPastrLocations[0];

			    		rc.setIndicatorString(1,  "created pastr waypoint: " + waypointLocation);
					}
				}
				

				
				if (waypointLocation != null) { // && numNearbyFriendlySoldiers > 2) {
					//log("going to waypoint");
					//rc.setIndicatorString(1,  "goto waypoint");
					gotoLocation(waypointLocation);
				} else {
					if (Util.random.nextDouble() < 0.1) {
						moveRandomly();
					}
				}				
			}
		}
		
		rc.yield();
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
    		if (myHealth < myRobotType.maxHealth / 3 || numNearbyFriendlySoldiers < numNearbyEnemySoldiers) {
    			
    			// pick a good rally point
    			
    			MapLocation rallyPoint = getSoldierCenterLocation(); 	
    				
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
					//}
					
					return new Double(info1.health).compareTo(info2.health);
					
				}
        	});
        	        	
        	// try to attack one of them as long as it isn't the HQ
        	for (RobotInfo nearbyInfo: nearbyEnemyInfo.values()) {
        		if (nearbyInfo.type != HQ && nearbyInfo.type != NOISETOWER) {
        			if (rc.canAttackSquare(nearbyInfo.location)) {
	        			rc.attackSquare(nearbyInfo.location);
	        			tookAction = true;
	        			break;
	        		}
        		}
        	}
        	
        	if (!tookAction && myRobotType == SOLDIER) {
            	for (RobotInfo nearbyInfo: nearbyEnemyInfo.values()) {
            		if (nearbyInfo.type != HQ && nearbyInfo.type != NOISETOWER) {
            			if (rc.canAttackSquare(nearbyInfo.location)) {
            				gotoLocation(nearbyInfo.location);
    	        			tookAction = true;
    	        			break;
    	        		}
            		}
            	}
        	}
        	
        }

        //log("Finished attackNearbyEnemies().");
        
        return tookAction;
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
			rc.setIndicatorString(0, "gotoLocation: " + toLocation + " (using approximate direction)");
			direction = approximateDirectionTo(myLocation, toLocation);
		} else {
			rc.setIndicatorString(0, "gotoLocation: " + toLocation + " (cached direction was not null, = " + direction + ")");
		}
	    		
	    if (direction != null) {
	    	MapLocation newLocation = myLocation.add(direction);
	    	
	    	boolean canMove = rc.canMove(direction);
	        
	    	// don't go near enemy hq
			//if (canMove) {	
			//	int newDistanceToEnemyHq = enemyHqLocation.distanceSquaredTo(newLocation);
			//	if (newDistanceToEnemyHq <= RobotType.HQ.attackRadiusMaxSquared) {
			//		//log("can't move");
			//		rc.setIndicatorString(2, "too close to enemy HQ");
			//		canMove = false;
			//	}
			//}
	    	
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
