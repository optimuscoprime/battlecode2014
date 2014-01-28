package nano;

import java.util.*;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static nano.Util.*;
import static battlecode.common.RobotType.*;


public class HeadquartersPlayer extends BasicPlayer implements Player  {
		    
	private MapLocation pastrConstructionLocation;
	private MapLocation noiseTowerConstructionLocation;
	
	
	public HeadquartersPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		
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
		
		
		boolean didAttack = false;
		
		if (rc.isActive()) {
			didAttack = attackNearbyEnemies();
		}
		
		if (!didAttack && rc.isActive()) {
			tryToSpawn();
		}
		
		friendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
		
		//if (friendlyRobots.length > 16) {
		
		if (Clock.getRoundNum() > 1000) {
			
			maybeAskForPastr();
		
			maybeAskForNoiseTower();
		
			maybeCreateExploringWaypoint();
		
		}

	}

	private void maybeAskForPastr() throws GameActionException {
		// if we don't have a noise tower near our spawn, ask for one
		// requirement: "behind" our spawn
		// requirement: open space
						
		int numPastrs = 0;
		
		for (Robot friendlyRobot: friendlyRobots) {
			RobotInfo info = rc.senseRobotInfo(friendlyRobot);
			if (info.type == PASTR) {
				numPastrs++;
			}
		}
		
		boolean havePastr = false;
		if (numPastrs > 1) {
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
		
		for (Robot friendlyRobot: friendlyRobots) {
			RobotInfo info = rc.senseRobotInfo(friendlyRobot);
			if (info.type == NOISETOWER) {
				numNoiseTowers++;
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
		
		// TODO Auto-generated method stub
		
		MapLocation goodLocation = null;
		
		Deque<MapLocation> possibleLocations = new ArrayDeque<MapLocation>();
		Set<MapLocation> visitedLocations = new HashSet<MapLocation>();
		
		myLocation = rc.senseHQLocation();
		visitedLocations.add(myLocation);
		
		shuffle(randomDirections); 
		
		for (Direction direction: randomDirections) {
			MapLocation possibleLocation = myLocation.add(direction);
			if (gameMap.isTraversable(possibleLocation.x, possibleLocation.y)) {
				possibleLocations.add(possibleLocation);
				visitedLocations.add(possibleLocation);
			}
		}
		
		boolean found = false;
		while (!found && !possibleLocations.isEmpty()) {
			MapLocation currentLocation = possibleLocations.remove();
			
			int goodTiles = 0;
			
			for (Direction direction: randomDirections) {
				MapLocation possibleLocation = currentLocation.add(direction);
				if (gameMap.isTraversable(possibleLocation.x, possibleLocation.y)) {					
					goodTiles++;
					if (!visitedLocations.contains(possibleLocation)) {
						possibleLocations.add(possibleLocation);
						visitedLocations.add(possibleLocation);
					}
				}
			}			
			if (goodTiles > 6) {
				boolean canBuild = true;
				
				// check if there is a construction there
				Robot robot = (Robot) rc.senseObjectAtLocation(currentLocation);
				
				if (robot != null) {
					RobotInfo info = rc.senseRobotInfo(robot);
					if (info.type != SOLDIER) {
						canBuild = false;
					}
				}
						
				if (canBuild) {
					goodLocation = currentLocation;
					break;
				}
			}
			
		}
		
		return goodLocation;
	}
	
    private void maybeCreateExploringWaypoint() {
		// every 100 or 200 rounds or so, create a new waypoint for our team to go to
    	
    	// idea:
    	// go to a square with good cow growth
    	// go to a square with enemy pastr
    	// go to a random square
    	
	}	

	private void tryToSpawn () {
        // check surrounding squares
        // spawn in one of them
    	
    	shuffle(randomDirections); 

        for (Direction direction: randomDirections) {
            if (rc.canMove(direction)) {
                try {
                	log("Trying to spawn...");
                    rc.spawn(direction);
                    log("Spwaned");
                    break;
                } catch (GameActionException e) {
                    die(e);
                }
            }
        }
    }	
}
