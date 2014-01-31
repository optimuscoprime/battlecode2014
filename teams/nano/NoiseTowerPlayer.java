package nano;

import java.util.*;

import battlecode.common.*;

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
						attackDistance >= GameConstants.NOISE_SCARE_RANGE_LARGE/2 && 
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
		int n = 0;
		while (true) {
			n++;
			for (int i=0; i < pulseLocations.size(); i++) {
				
				if (i % 7 == n % 7) {
					continue;
				}				
				
				MapLocation pulseLocation = pulseLocations.get(i);
				
				
				if (!rc.isActive()) {
					rc.yield();
				} 
				
				rc.attackSquare(pulseLocation); 
				rc.yield();
			}
		}
	}
}
