package nano;

import java.util.*;

import battlecode.common.*;

public class NoiseTowerPlayer extends BasicPlayer implements Player {
	public NoiseTowerPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		myLocation = rc.getLocation();
		
		while (true) {
			
			// sort all pulse start points, by their distance from us
			
			
			List<MapLocation> pulseLocations = new ArrayList<MapLocation>();
			
			for (int x = 0; x < gameMap.width; x+= 1) {
				for (int y =0; y < gameMap.width; y+= 1) {
					MapLocation pulseLocation = new MapLocation(x,y);
					if (gameMap.isTraversable(pulseLocation.x, pulseLocation.y)) {
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
			
			for (MapLocation pulseLocation: pulseLocations) {		
				while (!pulseLocation.equals(myLocation)) {
					// (x,y) is our starting position
					// pulse from there, to our home base
					int minDistance = Integer.MAX_VALUE;
					for (Direction direction: allDirections) {
						MapLocation possiblePulseLocation = pulseLocation.add(direction);
						int thisDistance = myLocation.distanceSquaredTo(possiblePulseLocation);
						if (thisDistance < minDistance) {
							pulseLocation = possiblePulseLocation;
							minDistance = thisDistance;
						}
					}
					if (gameMap.isTraversable(pulseLocation.x, pulseLocation.y) && minDistance <= myRobotType.attackRadiusMaxSquared) {
						rc.yield();
						if (rc.isActive()) {
							rc.attackSquare(pulseLocation);
						}
					}
				}
			
			}
		}
		
	}
}
