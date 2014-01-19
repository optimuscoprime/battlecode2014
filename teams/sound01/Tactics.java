package sound01;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

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
			if(rc.getHealth()<rc.getType().maxHealth){
				allClear=false;// a lie but we want wounded to run away.
			}
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
