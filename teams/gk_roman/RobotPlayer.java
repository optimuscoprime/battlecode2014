package gk_roman;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {
	public static void run(RobotController rc) {
		try {
//			Random rand = new Random(rc.getRobot().getID());
			RobotType type = rc.getType();

			Strategy strategy = null;
			if (type == RobotType.HQ) {
				strategy = new SpawnStrategy(rc);
			} else if (type == RobotType.SOLDIER) {
				strategy = new RomanStrategy(rc);
			} else {
			}

			playSingleStrategy(rc, strategy);
		} catch(GameActionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void playSingleStrategy(RobotController rc, Strategy strategy) throws GameActionException {
		while (true) {
			strategy.play();
			rc.yield();
		}	
	}
}
