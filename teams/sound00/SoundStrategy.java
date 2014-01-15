package sound00;

import sound00.Comms.Message;
import sound00.Navigation.Move;

import java.util.Deque;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

/**
 * SoundStrategy
 *
 * 	I intend to make this adaptation from SoundStrategy build a sound-tower
 * 	Then herd cows towards the HQ.
 * 	I'll probably make a pasture near the HQ.  
 *	Then make a HQ which shoots any enemies that come near, since the HQ seems like powerful defence.
 *	Then I'll make soldiers retreat to herd if they've got < half life, and suicide if they can't run away?

 */
import battlecode.common.RobotType;
public class SoundStrategy implements Strategy {
	RobotController rc;
	Random rand;
	Team enemy;
	RobotInfo info;
	MapLocation enemyHqLoc;
	MapLocation centerLoc;
	Direction wanderingDirection;

	public SoundStrategy(RobotController rc, Random rand) throws GameActionException {
		this.rc = rc;
		this.enemy = rc.getTeam().opponent();
		this.info = rc.senseRobotInfo(rc.getRobot());
		this.rand = rand;
		this.enemyHqLoc=rc.senseEnemyHQLocation();
		this.centerLoc=new MapLocation(rc.getMapHeight()/2,rc.getMapWidth()/2);
		wanderingDirection = null;
	}
	
	public void play() throws GameActionException {
		// set the below to middle of the map ?
		//MapLocation dest = Abilities.ClosestPastr(rc, rc.getLocation(), enemy);
		MapLocation dest=centerLoc;
		Deque<Move> path = Navigation.pathAStar(rc, dest);
		while(Navigation.attackMoveOnPath(rc, path, info.type.attackRadiusMaxSquared, enemy)) {
				  Tactics.killNearbyEnemies(rc, info);
		}
		Tactics.killNearbyEnemies(rc, info);
		if (rc.isActive()) {
				// deploy sound
			rc.construct(RobotType.NOISETOWER);
		} else {
			rc.yield();				
		}
	}
}




//Direction wonder = null;
//while (true) {
//	MapLocation dest = Abilities.ClosestPastr(rc, rc.getLocation(), enemy);
//	if (dest == null) {	
//		Message m = Comms.ReadMessage(rc);
//		if (m != null && m.type == Comms.Type.CONVERGE) {
//			dest = m.loc;
//		}
//	}
//	if (dest != null) {
//		Deque<Move> path = Navigation.pathAStar(rc, dest);
//		while(Navigation.attackMoveOnPath(rc, path, info.type.attackRadiusMaxSquared, enemy)) {
//			Tactics.killNearbyEnemies(rc, info);
//		}
//	}
//	Tactics.killNearbyEnemies(rc, info);
//	if (rc.isActive()) {
//		wonder = Navigation.wonder(rc, rand, wonder);
//	} else {
//		rc.yield();				
//	}
//}
