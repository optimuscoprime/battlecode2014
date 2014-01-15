package sound00;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.Clock;

/**
 * HQ - continuous spawn strategy, while attacking nearby enemy robots
 */
public class SpawnStrategy implements Strategy {
	RobotController rc;
	RobotInfo info;
	MapLocation hqLocation;
	Direction directionToEnemy;
	MapLocation primarySpawnLocation;
    Team enemyTeam;
	
	public SpawnStrategy(RobotController rc) throws GameActionException {
		this.rc = rc;
		this.info = rc.senseRobotInfo(rc.getRobot());
		this.hqLocation = rc.senseHQLocation();
		this.directionToEnemy = this.hqLocation.directionTo(rc.senseEnemyHQLocation());
		this.primarySpawnLocation = hqLocation.add(directionToEnemy);
        this.enemyTeam = this.info.team.opponent();
	}
	
	public void play() throws GameActionException {
		if (rc.isActive()) {
            
            // in principle the HQ is really good at killing enemies
            // BUT
            // killing nearby enemies almost never happens
            // because the HQ is so busy spawning more robots
            // and it cant shoot during spawning
			Tactics.killNearbyEnemies(rc, info);
            
			spawnRobot();
		}
	}
    
	public void spawnRobot() throws GameActionException {
		if (rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
			if (rc.senseObjectAtLocation(primarySpawnLocation) == null) {
				rc.spawn(directionToEnemy);
			} else {
				for (Direction d : Navigation.DIRECTIONS) {
					MapLocation loc = hqLocation.add(d);
					if (rc.senseObjectAtLocation(loc) == null) {
						rc.spawn(d);
						break;
					}
				}				
			}
		}
	}
}
