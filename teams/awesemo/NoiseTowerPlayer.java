package awesemo;

import java.util.*;

import battlecode.common.*;
import static awesemo.Util.*;

public class NoiseTowerPlayer extends BasicPlayer implements Player {
	
	private List<MapLocation> pulseLocations;
	private MapLocation focusLocation = null;
	private int i = 0;
	private int n = 0;
	private double[][] allCowGrowth;
	
	public NoiseTowerPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		
		// static
		myLocation = rc.getLocation();
		
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
	}
	
	private void sortTargets() {
		Collections.sort(pulseLocations, new Comparator<MapLocation>() {
			@Override
			public int compare(MapLocation o1, MapLocation o2) {
				return new Integer(focusLocation.distanceSquaredTo(o2)).compareTo(focusLocation.distanceSquaredTo(o1));
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
			
			while (!rc.isActive()) {
				rc.yield();
			} 			
			
			maybeAskForBackup();
			
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
					surroundingCows += rc.senseCowsAtLocation(surroundingLocation);
					canSense = true;
				}
			}
							
			if (!canSense) {
				rc.setIndicatorString(0, "can't sense, attacking blind");
				rc.attackSquare(pulseLocation);
				rc.yield();
			} else if (surroundingCows > 10) {
				rc.setIndicatorString(0, "has some cows, attacking");
				rc.attackSquare(pulseLocation);
				rc.yield();
			} else {
				rc.setIndicatorString(0, "no cows, not attacking");
			}			
			
		}
		i++;
	}
	
	private void maybeAskForBackup() throws GameActionException {

    	MapLocation waypointLocation = null;
   
		Robot[] nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
		Map<Robot, RobotInfo> nearbyFriendlyRobotInfo = senseAllRobotInfo(nearbyFriendlyRobots);
		nearbyFriendlyRobots = nearbyFriendlyRobotInfo.keySet().toArray(new Robot[0]);
		
		int numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobotInfo);	    	
    	
		if (numNearbyFriendlySoldiers < 3 || myHealth < myRobotType.maxHealth) {
    		
    		waypointLocation = myLocation;
	
    	}    		
    	
    	if (waypointLocation != null) {
    		rc.broadcast(RADIO_CHANNEL_NOISETOWER_BACKUP, locationToInt(waypointLocation));
    	} else {
    		rc.broadcast(RADIO_CHANNEL_NOISETOWER_BACKUP, 0);
 
    	}
	}
	
	protected MapLocation getFocusLocation() {
		//log("getFocusLocation start");
		
		MapLocation[] friendlyPastrLocations = rc.sensePastrLocations(myTeam);
		
		MapLocation focusLocation = myLocation;
		
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
}
