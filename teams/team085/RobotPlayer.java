package team085;

import team085.Navigator.Move;

import java.util.Deque;
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
							dest = pastures[rand.nextInt(pastures.length)];
							Deque<Navigator.Move> path = Navigator.pathAStar(rc, dest);
							while (!path.isEmpty()) {
								if (rc.isActive()) {
									attackNearby(rc, rc.getLocation());
								}
								if (rc.isActive()) {
									Move top = path.removeLast();
									if (top.direction != null) {
										if (rc.canMove(top.direction)) {
											rc.move(top.direction);
										} else {
											break;
										}
									}
								} else {
									rc.yield();								
								}
							}
						} else {
							dest = randomMapLocation(rc, rand);
							Navigator.moveGreedy(rc, dest, 10);
						}
						attackNearby(rc, rc.getLocation());
					}
				}
			} catch(GameActionException e) {
				//throw new RuntimeException(e);
			}
			rc.yield();
		}
	}
	
	public static MapLocation randomMapLocation(RobotController rc, Random rand) {
		int y = rand.nextInt(rc.getMapHeight());
		int x = rand.nextInt(rc.getMapWidth());
		return new MapLocation(x,y);
	}
	
	//attacks a nearby enemy (ignores HQ)
	public static void attackNearby(RobotController rc, MapLocation loc) throws GameActionException {
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, loc, ROBOT_ATTACK_RADIUS, ENEMY);
		for (Robot r : enemies) {
			RobotInfo info = rc.senseRobotInfo(r);
			MapLocation rl = info.location;
			if (info.type != RobotType.HQ && rc.canAttackSquare(rl)) {
				rc.attackSquare(rl);
				break;
			}
		}
	}
}
