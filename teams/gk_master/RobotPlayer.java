package gk_master;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class RobotPlayer {
	public static void run(RobotController rc) {
		try {
			Random rand = new Random(rc.getRobot().getID());
			RobotType type = rc.getType();
			
			if (type == RobotType.HQ) {
				SpawnStrategy spawnStrategy = new SpawnStrategy(rc);
				playSingleStrategy(rc, spawnStrategy);
				
			} else if (type == RobotType.SOLDIER) {
				AttackStrategy attackStrategy = new AttackStrategy(rc, rand);
				playSingleStrategy(rc, attackStrategy);
			}
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
