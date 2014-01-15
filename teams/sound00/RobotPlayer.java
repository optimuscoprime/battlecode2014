package sound00;

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
				rc.setIndicatorString(0," " + rc.getRobot().getID());
				if (rc.getRobot().getID() < 200 ){
						  SoundStrategy soundStrategy = new SoundStrategy(rc, rand);
						  playSingleStrategy(rc, soundStrategy);
				}else{
						  AttackStrategy attackStrategy = new AttackStrategy(rc, rand);
						  playSingleStrategy(rc, attackStrategy);
				}
			} else if (type == RobotType.NOISETOWER){
			   while(true){
						  rc.yield();
				}
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
