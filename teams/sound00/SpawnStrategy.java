package sound00;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

/**
 * HQ - continuous spawn strategy, while attacking nearby enemy robots
 */
public class SpawnStrategy implements Strategy {
	RobotController rc;
	RobotInfo INFO;
	MapLocation HQ_LOCATION;
	Direction DIRECTION_TO_ENEMY;
	MapLocation PRIMARY_SPAWN_LOCATION;
	
	public SpawnStrategy(RobotController rc) throws GameActionException {
		this.rc = rc;
		INFO = rc.senseRobotInfo(rc.getRobot());
		HQ_LOCATION = rc.senseHQLocation();
		DIRECTION_TO_ENEMY = this.HQ_LOCATION.directionTo(rc.senseEnemyHQLocation());
		PRIMARY_SPAWN_LOCATION = HQ_LOCATION.add(DIRECTION_TO_ENEMY);
	}
	
	public void play() throws GameActionException {
		if (rc.isActive()) {
			Tactics.killNearbyEnemies(rc, INFO);
			spawnRobot();
		}
	}
	
	public void spawnRobot() throws GameActionException {
		if (rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
			if (rc.senseObjectAtLocation(PRIMARY_SPAWN_LOCATION) == null) {
				rc.spawn(DIRECTION_TO_ENEMY);
			} else {
				for (Direction d : Navigation.DIRECTIONS) {
					MapLocation loc = HQ_LOCATION.add(d);
					if (rc.senseObjectAtLocation(loc) == null) {
						rc.spawn(d);
						break;
					}
				}				
			}
		}
	}
}
