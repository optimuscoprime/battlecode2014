package sound02;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Abilities {
	
	/**
	 * Finds the closest pasture to a given location
	 */
	public static MapLocation ClosestPastr(RobotController rc, MapLocation loc, Team team) {
		MapLocation[] pastures = rc.sensePastrLocations(team);
		MapLocation closest = null;
		int mind = Integer.MAX_VALUE;
		for (MapLocation pastr : pastures) {
			int d = pastr.distanceSquaredTo(loc);
			if (d < mind) {
				closest = pastr;
				mind = d;
			}
		}
		return closest;
	}
	
	/**
	 * Finds a nearby non-HQ robot
	 */
	public static RobotInfo NearbyEnemy(RobotController rc, MapLocation loc, int radius, Team enemyTeam) throws GameActionException {
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, loc, radius, enemyTeam);
		for (Robot r : enemies) {
			if (rc.canSenseObject(r)) {
				RobotInfo info = rc.senseRobotInfo(r);
				MapLocation rl = info.location;
				if (info.type != RobotType.HQ && rc.canAttackSquare(rl)) {
					return info;
				}				
			}
		}
		return null;
	}
	
	public static MapLocation randomMapLocation(RobotController rc, Random rand) {
		int y = rand.nextInt(rc.getMapHeight());
		int x = rand.nextInt(rc.getMapWidth());
		return new MapLocation(x,y);
	}
}
