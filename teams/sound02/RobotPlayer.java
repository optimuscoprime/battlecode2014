package sound02;

import sound02.Comms.Message;


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
				//make the below looks for coms as to whether a sound tower exists
				//then whether a pasture exists.
				// fall back to this logic when a soldier is injured (to rebuild).
				int soundChannel=1;
				int pastrChannel=2;
				Message ms = Comms.ReadMessage(rc,soundChannel);
				Message mp = Comms.ReadMessage(rc,pastrChannel);
				
				if (ms==null){
						  BuildSoundStrategy soundStrategy = new BuildSoundStrategy(rc, rand);
						  playSingleStrategy(rc, soundStrategy);
				}else if((rc.sensePastrLocations(rc.getTeam()).length < 1)&&( mp==null) ){
						  BuildPastrStrategy pastrBuildStrategy = new BuildPastrStrategy(rc, rand);
						  playSingleStrategy(rc, pastrBuildStrategy);
				}else{
						  AttackStrategy attackStrategy = new AttackStrategy(rc, rand);
						  playSingleStrategy(rc, attackStrategy);
				}
			} else if (type == RobotType.NOISETOWER){
					  SoundStrategy soundStrategy = new SoundStrategy(rc, rand);
					  playSingleStrategy(rc, soundStrategy);
			}else if (type == RobotType.PASTR){
					  //SoundStrategy soundStrategy = new SoundStrategy(rc, rand);
					  //playSingleStrategy(rc, soundStrategy);
					  while(true){
						  if(rc.getHealth()<rc.getType().maxHealth){
								//call for help.
						  }
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
