package emo;

import java.util.*;
import battlecode.common.*;
import static emo.Util.*;

public class RobotPlayer {
	public static void run(RobotController rc) {
		
		Robot robot = rc.getRobot();
		
		int robotId = robot.getID();
		RobotType robotType = rc.getType();
		
		Team team = robot.getTeam();
		
		Util.init(robotId, robotType);
		
		Player player = null;
		
		switch (robotType) {
			case HQ:
				player = new HeadquartersPlayer(robot, robotId, team, robotType, rc);
				break;
			case NOISETOWER:
				player = new NoiseTowerPlayer(robot, robotId, team, robotType, rc);
				break;
			case PASTR:
				player = new PastrPlayer(robot, robotId, team, robotType, rc);
				break;
			case SOLDIER:
				player = new SoldierPlayer(robot, robotId, team, robotType, rc);
				break;
		}
		
		while(true) {
			try {
				player.playOneTurn();
			} catch (GameActionException e) {
				die(e);
				rc.yield();
			}
			// todo: is this really the best place to yield?
			//rc.yield();
		}
		
	}
}
