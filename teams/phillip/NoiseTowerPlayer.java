package phillip;

import java.util.*;

import battlecode.common.*;
import static phillip.Util.*;

public class NoiseTowerPlayer extends BasicPlayer implements Player {
	
	private List<MapLocation> pulseLocations;
	private MapLocation focusLocation = null;
	private int i = 0;
	private int n = 0;
	private double[][] allCowGrowth;
	private GameMap gameMap;
	private int lastBackupRound;

	
	public NoiseTowerPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		
		
		// static location
		myLocation = rc.getLocation();
		
		// every player builds their own map
		gameMap = new GameMap(robotId, team, robotType, rc, enemyHqLocation, width, height);		
		
		pulseLocations = new ArrayList<MapLocation>();
		
		for (int x = 0; x < gameMap.width; x += 1) {
			for (int y = 0; y < gameMap.height; y += 1) {
				MapLocation pulseLocation = new MapLocation(x,y);
				int attackDistance = myLocation.distanceSquaredTo(pulseLocation);
				if (gameMap.isTraversable(pulseLocation.x, pulseLocation.y) && 
						attackDistance >= GameConstants.NOISE_SCARE_RANGE_LARGE/3 && 
						attackDistance <= myRobotType.attackRadiusMaxSquared) {
					pulseLocations.add(pulseLocation);
				}
			}
		}
		
		//focusLocation = myLocation;
		
		i = 0;
		
		allCowGrowth = rc.senseCowGrowth();
		
		lastBackupRound = 0;
	}
	
	private void sortTargets() {
		Collections.sort(pulseLocations, new Comparator<MapLocation>() {
			@Override
			public int compare(MapLocation o1, MapLocation o2) {
				return Integer.compare(focusLocation.distanceSquaredTo(o2),focusLocation.distanceSquaredTo(o1));
			}
		});				
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
		
		// check if we have a pastr location
		
		MapLocation newFocusLocation = getFocusLocation();
		
		if (!newFocusLocation.equals(focusLocation)) {
			focusLocation = newFocusLocation;
			sortTargets();
		}
		
		if (i >= pulseLocations.size()) {
			i = 0;
			n++;
		}
		
		if (i%3 == n%3 && i < pulseLocations.size()) {
			
			MapLocation pulseLocation = pulseLocations.get(i);
			
			//if (allCowGrowth[pulseLocation.x][pulseLocation.y] < 0.5) {
			//	// small chance we just skip it
			//	if (Util.random.nextDouble() < 0.5) {
			//		playOneTurn();
			//	}
			//}
			
			double surroundingCows = 0;
			
			boolean canSense = false;
							
			for (Direction direction: allDirections) {
				MapLocation surroundingLocation = pulseLocation.add(direction);
				if (rc.canSenseSquare(surroundingLocation)) {
					try {
						surroundingCows += rc.senseCowsAtLocation(surroundingLocation);
						canSense = true;
					} catch (GameActionException e) {
						die(e);
					}
				}
			}
							
			if (!canSense) {
				
				rc.setIndicatorString(0, "can't sense, attacking blind");
				
				while (!rc.isActive()) {
					rc.setIndicatorString(2,  "not active");
					rc.yield();
				} 		
				rc.setIndicatorString(2,  "active");
				rc.attackSquare(pulseLocation);
				rc.yield();
				
			} else if (surroundingCows > 10) {
				
				rc.setIndicatorString(0, "has some cows, attacking");
				
				while (!rc.isActive()) {
					rc.setIndicatorString(2,  "not active");
					rc.yield();
				} 		
				rc.setIndicatorString(2,  "active");
				rc.attackSquare(pulseLocation);
				rc.yield();
				
			} else {
				rc.setIndicatorString(0, "no cows, not attacking");
			}	
			
			maybeAskForBackup();
			
		}
		i++;
	}
	
	private void maybeAskForBackup() throws GameActionException {
		
		int currentRound = Clock.getRoundNum();
		
		if (currentRound == lastBackupRound) {
			return;
		} else {
			lastBackupRound = currentRound;
		}
		
		log("maybeAskForBackup start");

    	MapLocation waypointLocation = null;
   
		Robot[] nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
		Map<Robot, RobotInfo> nearbyFriendlyRobotMap = senseAllRobotInfo(nearbyFriendlyRobots);
		
		int numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobotMap.values());	    	
    	
		if (numNearbyFriendlySoldiers < 4 || myHealth < myRobotType.maxHealth) {
    		
    		waypointLocation = myLocation;
	
    	}    		
    	
    	if (waypointLocation != null) {
    		rc.broadcast(RADIO_CHANNEL_NOISETOWER_BACKUP, locationToInt(waypointLocation));
    	} else {
    		rc.broadcast(RADIO_CHANNEL_NOISETOWER_BACKUP, 0);
 
    	}
    	
    	log("maybeAskForBackup end");
	}
	
	protected MapLocation getFocusLocation() {
		
		log("getFocusLocation start");
				
		MapLocation[] friendlyPastrLocations = rc.sensePastrLocations(myTeam);
		
		MapLocation focusLocation = myLocation;
		
		if (friendlyPastrLocations.length > 0) {
		
    		// pick the pastr location that is closest to us
    		
    		sort(friendlyPastrLocations, new Comparator<MapLocation>() {
				@Override
				public int compare(MapLocation o1, MapLocation o2) {
					return Integer.compare(myLocation.distanceSquaredTo(o1),myLocation.distanceSquaredTo(o2));
				}
    		});
    		
    		focusLocation = friendlyPastrLocations[0];
		}
				
		log("getFocusLocation end");
		
		return focusLocation;
	}	
}
