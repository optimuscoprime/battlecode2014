package phillip;

import java.util.*;

import battlecode.common.*;
import static phillip.Util.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import static phillip.Util.*;

public class SoldierPlayer extends BasicPlayer implements Player {
	
	private static final int WAYPOINT_MIN_CLOSER_SOLDIERS = 5;
	private static final int MAX_TRAIL_SIZE = 20;
	private double[][] cowGrowth;
	private MapLocation toLocation = null;
	private Deque<MapLocation> trail;
	private GameMap gameMap;

	
	public SoldierPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		
		this.trail = new ArrayDeque<MapLocation>();

		cowGrowth = rc.senseCowGrowth();
		
		// every player builds their own map
		gameMap = new GameMap(robotId, team, robotType, rc, enemyHqLocation, width, height);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
		
		if (!rc.isActive()) {
			
			rc.setIndicatorString(2, "not active");
			
			if (toLocation != null) {
				// keep caching
				gameMap.nextDirectionTo(myLocation, toLocation);
			}
			
			rc.yield();
			return;			
				
		} else {
			
			rc.setIndicatorString(2, "active");
			
			// should recalculate if active
			this.toLocation = null;
			
//////////// ATTACKING START ////////////
			
			// check for nearby friends
			
			Robot[] nearbyFriendsWhoCanSeeMe = rc.senseNearbyGameObjects(Robot.class,myLocation,myRobotType.sensorRadiusSquared,myTeam);
			Map<Robot, RobotInfo> nearbyFriendsWhoCanSeeMeMap = senseAllRobotInfo(nearbyFriendsWhoCanSeeMe);			
			RobotInfo[] nearbyFriendsWhoCanSeeMeInfos = nearbyFriendsWhoCanSeeMeMap.values().toArray(new RobotInfo[0]);
			
			// check for nearby enemies
			
			Robot[] nearbyEnemiesWhoCanSeeMe = rc.senseNearbyGameObjects(Robot.class,myLocation,HUGE_RADIUS,opponentTeam);
			Map<Robot, RobotInfo> nearbyEnemiesWhoCanSeeMeMap = senseAllRobotInfo(nearbyEnemiesWhoCanSeeMe);
			
			RobotInfo[] nearbyEnemiesWhoCanSeeMeInfos = nearbyEnemiesWhoCanSeeMeMap.values().toArray(new RobotInfo[0]);
			
			// we want to sort by their health			
			
			sort(nearbyEnemiesWhoCanSeeMeInfos, new Comparator<RobotInfo>() {
				@Override
				public int compare(RobotInfo o1, RobotInfo o2) {
					return Double.compare(o1.health,  o2.health);
				}
			});
			
			int potentialEnemySoldierAttackers = 0;
			
			double lowestHealthEnemySoldierHealth = Integer.MAX_VALUE;
			RobotInfo lowestHealthEnemySoldierInfo = null;
			
			for (RobotInfo nearbyEnemyInfo : nearbyEnemiesWhoCanSeeMeInfos) {
				if (nearbyEnemyInfo.type == SOLDIER) {
					
					int distanceToSoldier = nearbyEnemyInfo.location.distanceSquaredTo(myLocation);
				   
					// could consider nearbyEnemyInfo.actionDelay < 1 here
				    if (distanceToSoldier <= nearbyEnemyInfo.type.attackRadiusMaxSquared) {
				 	   potentialEnemySoldierAttackers++;
				    }
				   
				    double enemySoldierHealth = nearbyEnemyInfo.health;
				    
				    if (enemySoldierHealth < lowestHealthEnemySoldierHealth) {
				 	   lowestHealthEnemySoldierHealth = enemySoldierHealth;
			   		   lowestHealthEnemySoldierInfo = nearbyEnemyInfo;
		   		    }
				}
			}
			
			for (RobotInfo nearbyEnemyInfo : nearbyEnemiesWhoCanSeeMeInfos) {
				if (nearbyEnemyInfo.type == SOLDIER) {
					
					if (rc.canAttackSquare(nearbyEnemyInfo.location)) {
						
						boolean attacked = false;
						
						if (potentialEnemySoldierAttackers == 1) {
							
							// only one attacker
							
							if ( nearbyEnemyInfo.robot.getID() > myRobotId &&
								 (nearbyEnemyInfo.health <= myHealth || nearbyEnemyInfo.health <= myRobotType.attackPower) ) {      
								
								// we shoot first and they will eventually die first
								
								attacked = true;
								
							} else if (nearbyEnemyInfo.robot.getID() < myRobotId &&
									 nearbyEnemyInfo.health < myHealth - nearbyEnemyInfo.type.attackPower ) {   
								
								// they shoot first but they will eventually die first
								
								attacked = true;
							}
							
						} else {
							
							// multiple attackers
							
							int potentialFriendlySoldierAttackers = 0;
							
							for (RobotInfo nearbyFriendInfo: nearbyFriendsWhoCanSeeMeInfos) {
								if (nearbyFriendInfo.type == SOLDIER) {
									if (nearbyFriendInfo.location.distanceSquaredTo(nearbyEnemyInfo.location) <= nearbyFriendInfo.type.attackRadiusMaxSquared) {
										potentialFriendlySoldierAttackers++;
									}
								}
							}
							
							if (potentialFriendlySoldierAttackers > potentialEnemySoldierAttackers) {
								
								attacked = true;
								
							} else {
								
								// TODO
								// unclear - maybe run away?
								
							}	
						}
						
						if (attacked) {
							rc.setIndicatorString(0, "attacking location: " + nearbyEnemyInfo.location);
							rc.attackSquare(nearbyEnemyInfo.location);
							rc.yield();
							return;
						}
					}
				}
			}
							
			if (potentialEnemySoldierAttackers > 0) {
								
				// just attack for now?
				
				for (RobotInfo nearbyEnemyInfo : nearbyEnemiesWhoCanSeeMeInfos) {
					if (nearbyEnemyInfo.type == SOLDIER) {
						if (rc.canAttackSquare(nearbyEnemyInfo.location)) {
							rc.setIndicatorString(1, "(UNWISE) attacking nearby soldier: " + nearbyEnemyInfo.location);
							rc.attackSquare(nearbyEnemyInfo.location);
							rc.yield();
							return;
						}
					}
				}				
				
				//if () {
					// TODO
					// if we can retreat, maybe we should do that
					// toLocation = ?
				
				//} else {
					// TODO
				
					// otherwise, maybe we should attack anyway?
				//}
				
			
			} else {
				
				if (nearbyEnemiesWhoCanSeeMeInfos.length > 0) {
					
					// maybe there is a PASTR or a NOISETOWER that we can attack
					
					// prefer to attack pastrs
					for (RobotInfo nearbyEnemyInfo : nearbyEnemiesWhoCanSeeMeInfos) {
						if (nearbyEnemyInfo.type == PASTR) {
							if (rc.canAttackSquare(nearbyEnemyInfo.location)) {
								rc.setIndicatorString(1, "attacking nearby pastr: " + nearbyEnemyInfo.location);
								rc.attackSquare(nearbyEnemyInfo.location);
								rc.yield();
								return;
							}
						}
					}
					
					// otherwise try attacking noisetowers
					for (RobotInfo nearbyEnemyInfo : nearbyEnemiesWhoCanSeeMeInfos) {
						if (nearbyEnemyInfo.type == NOISETOWER) {
							if (rc.canAttackSquare(nearbyEnemyInfo.location)) {
								rc.setIndicatorString(1, "attacking nearby noisetower: " + nearbyEnemyInfo.location);
								rc.attackSquare(nearbyEnemyInfo.location);
								rc.yield();
								return;
							}
						}
					}		
										
					// maybe there is a soldier who we can walk towards
					
					sort(nearbyEnemiesWhoCanSeeMeInfos, new Comparator<RobotInfo>() {
						@Override
						public int compare(RobotInfo o1, RobotInfo o2) {
							return Integer.compare(o1.location.distanceSquaredTo(myLocation), o2.location.distanceSquaredTo(myLocation));
						}
					});					
					
					toLocation = nearbyEnemiesWhoCanSeeMeInfos[0].location;
					rc.setIndicatorString(1, "moving towards nearby enemy: " + toLocation);
					gotoLocation(toLocation); 
					rc.yield();
					return;

					
				} else {
					
					
					// TODO
					
					
					// no enemy attackers, and no enemies nearby
					//
					// maybe we should regroup?
					// maybe we should heal?
					
					// toLocation =?
					
				}
			}			
			
/////////// ATTACKING END ////////////		
									
/////////// PASTR/NOISETOWER BACKUP START ////////////			
			
			// check for all friends
			Robot[] allFriendlyRobots = rc.senseNearbyGameObjects(Robot.class,myLocation,HUGE_RADIUS,myTeam);
			Map<Robot, RobotInfo> allFriendlyRobotsMap = senseAllRobotInfo(allFriendlyRobots);			
			RobotInfo[] allFriendlyRobotsInfos = allFriendlyRobotsMap.values().toArray(new RobotInfo[0]);	
			
			// a PASTR or NOISETOWER might have asked for backup
			
			// prefer to help pastrs
			
			int intWaypointLocation = rc.readBroadcast(RADIO_CHANNEL_PASTR_BACKUP);
			if (intWaypointLocation != 0) {
				MapLocation waypointLocation = intToLocation(intWaypointLocation);
				
				// the PASTR might already be dead - check first
				
				boolean pastrAtWaypoint = false;

				for (RobotInfo friendlyRobotInfo : allFriendlyRobotsInfos) {
					if (friendlyRobotInfo.type == PASTR &&
						friendlyRobotInfo.location.equals(waypointLocation)) {
						pastrAtWaypoint = true;
						
						// warn the others
						rc.broadcast(RADIO_CHANNEL_PASTR_BACKUP, 0);
						
						break;
					}
				}				
				
				if (pastrAtWaypoint) {
					// check if any other soldiers are closer
				
					int closerSoldiers = 0;
					int myDistanceToWaypoint = myLocation.distanceSquaredTo(waypointLocation);
				
					for (RobotInfo friendlyRobotInfo : allFriendlyRobotsInfos) {
						
						if (friendlyRobotInfo.type == SOLDIER && 
							friendlyRobotInfo.location.distanceSquaredTo(waypointLocation) < myDistanceToWaypoint) {
							
							closerSoldiers++;
							
							if (closerSoldiers >= WAYPOINT_MIN_CLOSER_SOLDIERS) {
								break;
							}
						}
					}
					
					if (closerSoldiers >= WAYPOINT_MIN_CLOSER_SOLDIERS) {
						
						rc.setIndicatorString(1,  "ignoring pastr backup waypoint: " + waypointLocation);
						
					} else {
						rc.setIndicatorString(1,  "going to pastr backup waypoint: " + waypointLocation);
						toLocation = waypointLocation;
						gotoLocation(toLocation);
						rc.yield();
						return;
					}
				}
			}
			
			// otherwise help noisetowers
			
			intWaypointLocation = rc.readBroadcast(RADIO_CHANNEL_NOISETOWER_BACKUP);
			if (intWaypointLocation != 0) {
				MapLocation waypointLocation = intToLocation(intWaypointLocation);
				
				// the NOISETOWER might already be dead - check first
				
				boolean noisetowerAtWaypoint = false;
				
				for (RobotInfo friendlyRobotInfo : allFriendlyRobotsInfos) {
					if (friendlyRobotInfo.type == NOISETOWER &&
						friendlyRobotInfo.location.equals(waypointLocation)) {
						noisetowerAtWaypoint = true;
						
						// warn the others
						rc.broadcast(RADIO_CHANNEL_NOISETOWER_BACKUP, 0);
						
						break;
					}
				}
					
				if (noisetowerAtWaypoint) {				
				
					// check if any other soldiers are closer
				
					int closerSoldiers = 0;
					int myDistanceToWaypoint = myLocation.distanceSquaredTo(waypointLocation);
				
					for (RobotInfo friendlyRobotInfo : allFriendlyRobotsInfos) {
						
						if (friendlyRobotInfo.type == SOLDIER && 
							friendlyRobotInfo.location.distanceSquaredTo(waypointLocation) < myDistanceToWaypoint) {
							
							closerSoldiers++;
							
							if (closerSoldiers >= WAYPOINT_MIN_CLOSER_SOLDIERS) {
								break;
							}
						}
					}
					
					if (closerSoldiers >= WAYPOINT_MIN_CLOSER_SOLDIERS) {
						
						rc.setIndicatorString(1,  "ignoring noisetower backup waypoint: " + waypointLocation);
						
					} else {
						rc.setIndicatorString(1,  "going to noisetower backup waypoint: " + waypointLocation);
						toLocation = waypointLocation;
						gotoLocation(toLocation);
						rc.yield();
						return;
					}
				}
			}						
			
/////////// PASTR/NOISETOWER BACKUP END ////////////

/////////// ASKED TO CONSTRUCT PASTR/NOISETOWER START ////////////
			
			int numNearbyFriendlySoldiers = 0;
			for (RobotInfo nearbyFriendInfo: nearbyFriendsWhoCanSeeMeInfos) {
				if (nearbyFriendInfo.type == SOLDIER) {
					numNearbyFriendlySoldiers++;
				}
			}
			
			// HQ might have asked for a pastr
		
			int intConstructionLocation = rc.readBroadcast(RADIO_CHANNEL_REQUEST_PASTR);
			if (intConstructionLocation != 0) {
				MapLocation constructionLocation = intToLocation(intConstructionLocation);
				if (myLocation.equals(constructionLocation)) {
					if (numNearbyFriendlySoldiers > 2) {
						rc.setIndicatorString(1, "constructing pastr here");
						rc.construct(PASTR);
						rc.yield();
						return;
					} else {
						// wait?
					}
				} else {
					rc.setIndicatorString(1,  "moving to pastr construction location");
					toLocation = constructionLocation;
					gotoLocation(toLocation);
					rc.yield();
					return;
				}
			}					
		
			// HQ might have asked for a broadcast tower
		
			intConstructionLocation = rc.readBroadcast(RADIO_CHANNEL_REQUEST_NOISETOWER);
			if (intConstructionLocation != 0) {
				MapLocation constructionLocation = intToLocation(intConstructionLocation);
				if (myLocation.equals(constructionLocation)) {
					if (numNearbyFriendlySoldiers > 2) {
						rc.setIndicatorString(1, "constructing noisetower here");
						rc.construct(NOISETOWER);
						rc.yield();
						return;
					} else {
						// wait?
					}
				} else {
					rc.setIndicatorString(1,  "moving to noisetower construction location");
					toLocation = constructionLocation;
					gotoLocation(toLocation);
					rc.yield();
					return;
				}
			}	
			
/////////// ASKED TO CONSTRUCT PASTR/NOISETOWER END ////////////
			
/////////// DECIDED TO CONSTRUCT PASTR/NOISETOWER START ////////////			
			
			// maybe we should construct a pastr anyway
			// if it is a nice spot
										
			if (rc.senseCowsAtLocation(myLocation) > 1000 &&
				cowGrowth[myLocation.x][myLocation.y] > 1 &&  // TODO what is a good growth rate?
				numNearbyFriendlySoldiers > 1) {
				
				int numNearbyPastrs = 0;
				
				for (RobotInfo robotInfo: nearbyFriendsWhoCanSeeMeInfos) {
					if (robotInfo.type == PASTR || robotInfo.isConstructing) {
						numNearbyPastrs++;
						break;
					}
				}
				
				if (numNearbyPastrs == 0) {
					int distanceToMyHq = myLocation.distanceSquaredTo(myHqLocation);
					int distanceToEnemyHq = myLocation.distanceSquaredTo(enemyHqLocation);
					
					if (distanceToMyHq < distanceToEnemyHq && 
						distanceToMyHq > RobotType.NOISETOWER.attackRadiusMaxSquared) {
						
						rc.setIndicatorString(1, "found a good spot for a pastr, building here");
						rc.construct(PASTR);
						rc.yield();
						return;					
					}
				}
			}
			
/////////// DECIDED TO CONSTRUCT PASTR/NOISETOWER END ////////////			
			
/////////// DESTROY ENEMY PASTR START ////////////		
	
		
			MapLocation[] enemyPastrLocations = rc.sensePastrLocations(opponentTeam);
			
			if (enemyPastrLocations.length > 0) {
			
	    		// pick the pastr location that is closest to us
				
				MapLocation closestEnemyPastrLocation = null;
				int closestEnemyPastrDistance = Integer.MAX_VALUE;
				
				for (MapLocation enemyPastrLocation: enemyPastrLocations) {
					int thisDistance = myLocation.distanceSquaredTo(enemyPastrLocation);
					if (thisDistance < closestEnemyPastrDistance) {
						closestEnemyPastrDistance = thisDistance;
						closestEnemyPastrLocation = enemyPastrLocation;
					}
				}
				
				rc.setIndicatorString(1,  "going to enemy pastr location: " + closestEnemyPastrLocation);
				toLocation = closestEnemyPastrLocation;
				gotoLocation(toLocation);
				rc.yield();
				return;    		
			}
		
/////////// DESTROY ENEMY PASTR END ////////////		

/////////// WANDERING START ////////////
		
			if (Util.random.nextDouble() < 0.1) {
				rc.setIndicatorString(1, "wandering randomly");
				moveRandomly();
				rc.yield();
				return;
			}
		
/////////// WANDERING END ////////////	
		
			// shouldn't make it here often
			
			rc.setIndicatorString(1, "nothing to do");
			rc.yield();
			return;
		}
	
	}

//    protected MapLocation getSoldierCenterLocation() {
//    	MapLocation centerLocation = null;
//    	
//    	if (numAllFriendlySoldiers > 1) {
//    		double totalX = 0;
//    		double totalY = 0;
//    		
//    		int n = 0;
//    		
//    		// find the average pos
//    		for (RobotInfo info: allFriendlyRobotInfo.values()) {
//    			if (info.type == SOLDIER) {
//    				totalX += info.location.x;
//    				totalY += info.location.y;
//    				n++;
//    			}
//    		}
//    		
//    		double averageX = totalX / n;
//    		double averageY = totalY / n;    	
//    		
//    		centerLocation = new MapLocation((int) averageX, (int) averageY);
//    		
//    		if (!gameMap.isTraversable(centerLocation)) {
//    			// would be better to search around the center loc
//    			centerLocation = myHqLocation;
//    		}
//    		
////    		while (!gameMap.isTraversable(centerLocation)) {
////    			// try exploring around
////    		
////    		}
//    	}
//		
//		return centerLocation;
//	}
	
	protected void moveRandomly() {
    	//rc.setIndicatorString(2, "moveRandomly");
    	
		// let's try moving randomly
		shuffle(randomDirections);
		
		for (Direction randomDirection: randomDirections) {
			
			boolean canMove = rc.canMove(randomDirection);
			
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
				MapLocation focusLocation = getFocusLocation();
				
				int newDistanceToFocus = focusLocation.distanceSquaredTo(newLocation);
				int currentDistanceToFocus = focusLocation.distanceSquaredTo(myLocation);
				
				try	{
					if (newDistanceToFocus < currentDistanceToFocus) {
						rc.move(randomDirection);
					} else {
						// moving away from focus
						rc.sneak(randomDirection);
					}
					trail.add(newLocation);
					if (trail.size() > MAX_TRAIL_SIZE) {
						trail.remove();
					}
					break;
				} catch (GameActionException e) {
					die(e);
				}
			}
		} 			
    }
    
	protected void gotoLocation(MapLocation toLocation) throws GameActionException {
		
		rc.setIndicatorString(0, "gotoLocation: " + toLocation);
		
		Direction direction = gameMap.nextDirectionTo(myLocation,toLocation);		

		// nextDirectionTo failed - still caching?
		if (direction == null) {
			rc.setIndicatorString(0, "gotoLocation: " + toLocation + " (using approximate direction)");
			direction = approximateDirectionTo(myLocation, toLocation);
		} else {
			rc.setIndicatorString(0, "gotoLocation: " + toLocation + " (cached direction was not null, = " + direction + ")");
		}
	    		
	    if (direction != null) {
	    	MapLocation newLocation = myLocation.add(direction);
	    	
	    	boolean canMove = rc.canMove(direction);
	        	    	
			if (canMove) {
				
				MapLocation focusLocation = getFocusLocation();
								
				int newDistanceToFocus = focusLocation.distanceSquaredTo(newLocation);
				int currentDistanceToFocus = focusLocation.distanceSquaredTo(myLocation);
				
				try	{
					if (newDistanceToFocus < currentDistanceToFocus) { // && newLocationCows > 100) {
						// herd cattle
						rc.move(direction);
					} else {
						// don't disturb cattle
						rc.sneak(direction);
					}
					trail.add(newLocation);
					if (trail.size() > MAX_TRAIL_SIZE) {
						trail.remove();
					}					
				} catch (GameActionException e) {
					// don't die here
					// we already checked canMove
					// die(e);
					die(e);
				}
				
			} else {
				
				// maybe just move randomly then
				
				if (Util.random.nextDouble() < 0.1) {
					rc.setIndicatorString(0, "gotoLocation: " + toLocation + " failed, moving randomly instead");
					moveRandomly();
				}
			}
	    }
	} 
	
	public Direction approximateDirectionTo(MapLocation from, MapLocation to) {
		Direction exactDirection = from.directionTo(to);
		
		Direction approximateDirection = exactDirection;
		
		
		if (!trail.contains(from.add(approximateDirection)) & rc.canMove(approximateDirection)) {
			// ok, good

		} else {
			
			approximateDirection = exactDirection.rotateLeft();
			
			if (trail.contains(from.add(approximateDirection)) || !rc.canMove(approximateDirection)) {
				
				// bad, pick again
				
				approximateDirection = exactDirection.rotateRight();
				
				if (trail.contains(from.add(approximateDirection)) || !rc.canMove(approximateDirection)) {
					
					// bad, pick again
					
					approximateDirection = exactDirection.rotateLeft().rotateLeft();
					
					if (trail.contains(from.add(approximateDirection)) || !rc.canMove(approximateDirection)) {
						
						// bad, pick again
						
						approximateDirection = exactDirection.rotateRight().rotateRight();
					}
				}
			}
			
		}
		
		return approximateDirection;
	}		

	protected MapLocation getFocusLocation() {
		//log("getFocusLocation start");
		
		MapLocation[] friendlyPastrLocations = rc.sensePastrLocations(myTeam);
		
		MapLocation focusLocation = myHqLocation;
		
		if (friendlyPastrLocations.length > 0) {
		
    		// pick the pastr location that is closest to us
			
			MapLocation closestFriendlyPastrLocation = null;
			int closestFriendlyPastrDistance = Integer.MAX_VALUE;
			
			for (MapLocation enemyPastrLocation: friendlyPastrLocations) {
				int thisDistance = myLocation.distanceSquaredTo(enemyPastrLocation);
				if (thisDistance < closestFriendlyPastrDistance) {
					closestFriendlyPastrDistance = thisDistance;
					closestFriendlyPastrLocation = enemyPastrLocation;
				}
			}			
      		
    		focusLocation = closestFriendlyPastrLocation;
		}
		
		//log("getFocusLocation end");
		
		return focusLocation;
	}	
}
