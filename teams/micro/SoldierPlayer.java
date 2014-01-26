package micro;

import java.util.*;
import battlecode.common.*;

public class SoldierPlayer extends BasicPlayer implements Player {

	private MapLocation waypoint = null;
	
	public SoldierPlayer(Robot robot, RobotType robotType, RobotController rc) {
		super(robot, robotType, rc);
	}

	@Override
	public void playOneTurn() {
		
		if (waypoint == null) {
			// pick one randomly
			//rc.senseTerrainTile(loc);
		}
	
		if (waypoint != null) {
			// go there
		}
	}
}
