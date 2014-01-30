package brute01;

import brute01.Comms.Message;


import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Clock;
import battlecode.common.MapLocation;

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
				//int soundChannel=1;
				int pastrChannel=2;
				//Message ms = Comms.ReadMessage(rc,soundChannel);
				Message mp = Comms.ReadMessage(rc,pastrChannel);
				
				//if (ms==null){
				//		  BuildSoundStrategy soundStrategy = new BuildSoundStrategy(rc, rand);
				//		  playSingleStrategy(rc, soundStrategy);
				if((rc.sensePastrLocations(rc.getTeam()).length < 1)&&( mp==null) ){
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
				MapLocation targetLoc=null;
				while(true){
					double lowestEH=100000;
					Robot enemies[] = rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), 9, rc.getTeam().opponent());
					
					if(enemies.length>0){
						for (int i = 0; i < enemies.length; i++) {
							Robot e = enemies[i];
							RobotInfo ei = rc.senseRobotInfo(e);
							if(ei.health<lowestEH){ //pickoff weakest .. also skips hq.
								targetLoc = ei.location;
								lowestEH=ei.health;
								//if(ei.type == RobotType.HQ) { //run or we could make movement not go there.
							}
						}	//call for help.
						Comms.BroadcastMessage(rc,3,
								Comms.Message.create(Comms.Type.HELP, targetLoc,0, rc.getRobot().getID())
								);

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
