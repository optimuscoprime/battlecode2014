package gk_attack;

import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class RobotPlayer {
	public static MapLocation HQ_LOC;
	public static MapLocation ENEMY_HQ_LOC;
	public static Direction HQ_TO_ENEMY;
	public static MapLocation HQ_SPAWN;
	
	public static Team ENEMY;
	
	public static int ROBOT_ATTACK_RADIUS;
	
	public static void setup(RobotController rc) {
		ENEMY = rc.getTeam().opponent();
		ENEMY_HQ_LOC = rc.senseEnemyHQLocation();
		HQ_LOC = rc.senseHQLocation();
		HQ_TO_ENEMY = HQ_LOC.directionTo(ENEMY_HQ_LOC);
		HQ_SPAWN = HQ_LOC.add(HQ_TO_ENEMY);
		ROBOT_ATTACK_RADIUS = (int)Math.sqrt(RobotType.SOLDIER.attackRadiusMaxSquared);
	}
	
	public static void run(RobotController rc) {
		setup(rc);
		Random rand = new Random(rc.getRobot().getID());
		while (true) {
			try {
				if (rc.getType() == RobotType.HQ) {
					if (rc.isActive() && rc.senseObjectAtLocation(HQ_SPAWN) == null) {
						rc.spawn(HQ_TO_ENEMY);
					}
				}
				if (rc.getType() == RobotType.SOLDIER) {
					if (rc.isActive()) {
						MapLocation[] pastures = rc.sensePastrLocations(ENEMY);
						MapLocation dest = null;
						if (pastures.length > 0) {
							dest = pastures[0];
						} else {
							dest = randomMapLocation(rc, rand);
						}
						moveTo(rc, dest);
						attackNearby(rc, dest);
					}
				}
			} catch(GameActionException e) {
				throw new RuntimeException(e);
			}
			rc.yield();
		}
	}
	
	public static MapLocation randomMapLocation(RobotController rc, Random rand) {
		int y = rand.nextInt(rc.getMapHeight());
		int x = rand.nextInt(rc.getMapWidth());
		return new MapLocation(x,y);
	}
	
	public static void attackNearby(RobotController rc, MapLocation loc) throws GameActionException {
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, loc, ROBOT_ATTACK_RADIUS, ENEMY);
		for (Robot r : enemies) {
			RobotInfo info = rc.senseRobotInfo(r);
			MapLocation rl = info.location;
			if (rc.canAttackSquare(rl)) {
				rc.attackSquare(rl);
				break;
			}
		}
	}
	
	public static void moveTo(RobotController rc, MapLocation dest) throws GameActionException {
		MapLocation loc = rc.getLocation();
		while (!loc.equals(dest)) {
			Direction d = loc.directionTo(dest);
			if (rc.isActive()) {
				if (rc.canMove(d)) {
					rc.move(d);
				} else {
					break;
				}
			}
			rc.yield();
		}
	}
}
