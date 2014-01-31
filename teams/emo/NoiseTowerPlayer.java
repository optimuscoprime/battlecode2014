package emo;

import java.util.*;

import battlecode.common.*;
import static emo.Util.*;

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
			} else if (surroundingCows > 10) {
				rc.setIndicatorString(0,  "has some cows, attacking");
				rc.attackSquare(pulseLocation);
			} else {
				log("no cows, not attacking");
			}			
			
		}
		i++;
	}
}
