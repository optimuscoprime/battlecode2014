package sound03;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Direction;
import battlecode.common.Clock;
import sound03.Comms.Message;

public class Tactics {
	
	//attacks nearby enemies until they are all dead
	public static void killNearbyEnemies(RobotController rc, RobotInfo info) throws GameActionException {
		int r = info.type.sensorRadiusSquared;
		Team et = info.team.opponent();
		MapLocation loc = rc.getLocation();
		
		Robot enemies[] = rc.senseNearbyGameObjects(Robot.class, loc, r, et);
		boolean allClear = enemies.length == 0;
		while (!allClear) {
			allClear = true;
			for (int i = 0; i < enemies.length; i++) {
				Robot e = enemies[i];
				while(true) {
					if (rc.isActive()) {
						if (rc.canSenseObject(e)) {
							RobotInfo ei = rc.senseRobotInfo(e);
							MapLocation eloc = ei.location;
							if (ei.type != RobotType.HQ) {
								if (rc.canAttackSquare(eloc)) {
									rc.attackSquare(eloc);	
								} else {
									break;
								}
							} else {
								break;
							}
						} else {
							break;
						}
					} else {
						rc.yield();
					}
				}
			}
			
			if (!allClear) {
				enemies = rc.senseNearbyGameObjects(Robot.class, loc, r, et);
				allClear = enemies.length == 0;
			}
		}
	}
	public static void getHelp(RobotController rc, MapLocation targetLoc) throws GameActionException {
		int help_channel=3;
		int helpWithin=5;
		Message help=Comms.ReadMessage(rc,help_channel);
		if(help!=null){
			//if( ((help.val+helpWithin) < (Clock.getRoundNum()%16) )||(Clock.getRoundNum()%16 ==0)){
			if( targetLoc !=rc.senseEnemyHQLocation()){
				//System.out.println("help:" + rc.getLocation() + " help.val:" + help.val + " clock:" + (Clock.getRoundNum())%16);
				//broadcast for help.   Lets make other bots run at the enemy.
				Comms.BroadcastMessage(rc,help_channel,
						Comms.Message.create(Comms.Type.HELP, targetLoc, Clock.getRoundNum()%16, rc.getRobot().getID())
						);
			}
		}else{	//no msg existed yet
			Comms.BroadcastMessage(rc,help_channel,
					Comms.Message.create(Comms.Type.HELP, rc.getLocation(), Clock.getRoundNum()%16, rc.getRobot().getID())
					);

		}
	}
	public static void fightOrFlight(RobotController rc, RobotInfo info) throws GameActionException {
		int r = info.type.sensorRadiusSquared;
		Team et = info.team.opponent();
		MapLocation loc = rc.getLocation();

		/*
ideas for here:
I want to make it 'hold' a target to skip computation and just fire for n loops before re-assessing.
When re-assessing see if theres more friendlies or enemies nearby.  Run to friendlies if outnumbered?
maybe even when we're getting shot we just want that soldier to yell help and run away.

*/

		Robot enemies[] = rc.senseNearbyGameObjects(Robot.class, loc, r, et);
		Robot friendlies[] = rc.senseNearbyGameObjects(Robot.class, loc, r, rc.getTeam());
		while(enemies.length >0){
		
			boolean allClear = enemies.length == 0;
			double lowestEH=100000;
			MapLocation targetLoc=null;
			if(rc.isActive()){
				if((enemies.length<friendlies.length)&&(rc.getHealth()>(rc.getType().maxHealth/3)) ){
						//we have strength.
					for (int i = 0; i < enemies.length; i++) {
						Robot e = enemies[i];
						RobotInfo ei = rc.senseRobotInfo(e);
						if(ei.health<lowestEH){	//pickoff weakest .. also skips hq.
							targetLoc = ei.location;
							lowestEH=ei.health;
							//if(ei.type == RobotType.HQ) { //run or we could make movement not go there.
						}
					}
						//so here the tloc & lowestEH should be set.
					if(targetLoc!=null){
						if (rc.canAttackSquare(targetLoc)) {
							getHelp(rc, targetLoc);
							rc.attackSquare(targetLoc);
						}
					}
				}else{
					//viable strategies are suicide or running.
					for (int i = 0; i < enemies.length; i++) {
						targetLoc=rc.senseRobotInfo(enemies[i]).location;
						if(targetLoc==rc.senseEnemyHQLocation()){
							return;
						}
						Direction awayFromE=rc.getLocation().directionTo(targetLoc).opposite();
						if(rc.isActive()){
							getHelp(rc, targetLoc);
							if(rc.canMove(awayFromE)){
								rc.move(awayFromE);
							//}else if(rc.canMove(awayFromE.rotateLeft())){
							//	rc.move(awayFromE.rotateLeft());
							//}else if(rc.canMove(awayFromE.rotateRight())){
							//	rc.move(awayFromE.rotateRight());
							}else{	//can't run?
								if (rc.canAttackSquare(targetLoc)) {
									rc.attackSquare(targetLoc);
								}
							}
						}
					}
				}
			}
			rc.yield();
			enemies= rc.senseNearbyGameObjects(Robot.class, loc, r, et);
			friendlies = rc.senseNearbyGameObjects(Robot.class, loc, r, rc.getTeam());
		}
	}
	public static void killNearbyEnemies2(RobotController rc, RobotInfo info) throws GameActionException {
		int sensorRadius = info.type.sensorRadiusSquared;
		Team enemyTeam = info.team.opponent();
		MapLocation loc = rc.getLocation();
		Robot enemies[] = rc.senseNearbyGameObjects(Robot.class, loc, sensorRadius, enemyTeam);

		boolean backup = false;
		for (int i = 0; i < enemies.length; i++) {
			Robot e = enemies[i];
			while(true) {
				if (rc.isActive()) {
					if (rc.canSenseObject(e)) {
						RobotInfo ei = rc.senseRobotInfo(e);
						MapLocation eloc = ei.location;
						
						if (ei.type != RobotType.HQ) {
							if (!backup) {
								Comms.BroadcastMessage(rc,0, Comms.Message.create(Comms.Type.CONVERGE, eloc, 0, rc.getRobot().getID()));
								backup = true;
							}
							if (rc.canAttackSquare(eloc)) {
								rc.attackSquare(eloc);	
							} else {
								break;
							}
						}else{
							return;
						}
					} else {
						break;
					}
				} else {
					rc.yield();
				}
			}
		}
	}
}
