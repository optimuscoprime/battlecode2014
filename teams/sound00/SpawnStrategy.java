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
			Tactics.killNearbyEnemies(rc, INFO);
            attackNearbyRobots();
			spawnRobot();
		}
	}
    
    public void attackNearbyRobots() throws GameActionException {
        RobotInfo enemy = Abilities.NearbyEnemy(rc, hqLocation, info.type.attackRadiusSquared, enemyTeam);
        if (enemy != null) {
            enemyLocation = enemy.location;
			if (rc.canAttackSquare(enemyLocation)) {
				rc.attackSquare(enemyLocation);	
            }
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
