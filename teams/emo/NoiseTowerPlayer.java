package emo;

import java.util.*;

import battlecode.common.*;
import static emo.Util.*;

public class NoiseTowerPlayer extends BasicPlayer implements Player {
	
	private List<MapLocation> pulseLocations;
	
	public NoiseTowerPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		
		// static
		myLocation = rc.getLocation();
		
		pulseLocations = new ArrayList<MapLocation>();
		
		for (int x = 0; x < gameMap.width; x += 1) {
			for (int y =0; y < gameMap.height; y += 1) {
				MapLocation pulseLocation = new MapLocation(x,y);
				int attackDistance = myLocation.distanceSquaredTo(pulseLocation);
				if (gameMap.isTraversable(pulseLocation.x, pulseLocation.y) && 
						//attackDistance >= GameConstants.NOISE_SCARE_RANGE_LARGE/2 && 
						attackDistance <= myRobotType.attackRadiusMaxSquared) {
					pulseLocations.add(pulseLocation);
				}
			}
		}
	
		Collections.sort(pulseLocations, new Comparator<MapLocation>() {
			@Override
			public int compare(MapLocation o1, MapLocation o2) {
				return new Integer(myLocation.distanceSquaredTo(o2)).compareTo(myLocation.distanceSquaredTo(o1));
			}
		});		
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
		
		//int n = 0;
		while (true) {
			//n++;
			for (int i=0; i < pulseLocations.size(); i++) {
				
				//if (i % 3 != n % 3) {
				//	continue;
				//}				
				
				MapLocation pulseLocation = pulseLocations.get(i);
				
				while (!rc.isActive()) {
					rc.yield();
				} 
				
				double surroundingCows = 0;
				
				boolean canSense = false;
				
				log("begin sensing");
				
				for (Direction direction: allDirections) {
					MapLocation surroundingLocation = pulseLocation.add(direction);
					if (rc.canSenseSquare(surroundingLocation)) {
						surroundingCows += rc.senseCowsAtLocation(surroundingLocation);
						canSense = true;
					}
				}
				
				log("end sensing");
				
				if (!canSense) {
					log("can't sense, attacking blind");
					rc.attackSquare(pulseLocation);
				} else if (surroundingCows > 10) {
					log("has some cows, attacking");
					rc.attackSquare(pulseLocation);
				} else {
					log("no cows, not attacking");
				}
			}
		}
	}
}
