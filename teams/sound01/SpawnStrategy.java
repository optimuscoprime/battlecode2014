package sound01;

import sound01.Comms.Message;

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
	//MapLocation primarySpawnLocation;
    Team enemyTeam;
	
	public SpawnStrategy(RobotController rc) throws GameActionException {
		this.rc = rc;
		this.info = rc.senseRobotInfo(rc.getRobot());
		this.hqLocation = rc.senseHQLocation();
		this.directionToEnemy = this.hqLocation.directionTo(rc.senseEnemyHQLocation());
		//this.primarySpawnLocation = hqLocation.add(directionToEnemy);
        this.enemyTeam = this.info.team.opponent();
	}
	
	public void play() throws GameActionException {
		if (rc.isActive()) {
			int soundChannel=1;
			int pastrChannel=2;
			Message ms = Comms.ReadMessage(rc,soundChannel);
			Message mp = Comms.ReadMessage(rc,pastrChannel);
			if (ms!=null){
				if(rc.senseObjectAtLocation(ms.loc)==null){
					rc.broadcast(soundChannel,0);
				}
			}       
			if (mp!=null){
				if(rc.senseObjectAtLocation(mp.loc)==null){
					rc.broadcast(pastrChannel,0);
				}
			}     	    
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
			if (rc.canMove(directionToEnemy)) {
				rc.spawn(directionToEnemy);
			} else {
				for (Direction d : Navigation.DIRECTIONS) {
					//MapLocation loc = hqLocation.add(d);
					if (rc.canMove(d)) {
						rc.spawn(d);
						break;
					}
				}				
			}
		}
	}
}
