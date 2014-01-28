package nano;

import java.util.*;
import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc) {
		
		Robot robot = rc.getRobot();
		
		int id = robot.getID();
		Util.init(id);
		
		Player player = null;
		RobotType robotType = rc.getType();
		switch (robotType) {
			case HQ:
				player = new HeadquartersPlayer(robot, robotType, rc);
				break;
			case NOISETOWER:
				player = new NoiseTowerPlayer(robot, robotType, rc);
				break;
			case PASTR:
				player = new PastrPlayer(robot, robotType, rc);
				break;
			case SOLDIER:
				player = new SoldierPlayer(robot, robotType, rc);
				break;
		}
		
		while(true) {
			try {
				player.playOneTurn();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
			rc.yield();
		}
		
	}
}
