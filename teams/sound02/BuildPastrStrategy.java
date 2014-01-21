package sound02;

import sound02.Comms.Message;
import sound02.Navigation.Move;

import java.util.Deque;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

/**
 * BuildPastrStrategy
 *
 * 	I intend to make this adaptation from BuildSoundStrategy to build a pastr near hq.
 * 	I'll probably make a pasture near the HQ.  
 *	  Then I'll make soldiers retreat to herd if they've got < half life / suicide if they can't run?
 *

 */
import battlecode.common.RobotType;
public class BuildPastrStrategy implements Strategy {
	RobotController rc;
	Random rand;
	Team enemy;
	RobotInfo info;
	MapLocation enemyHqLoc;
	MapLocation myHqLoc;
	//MapLocation centerLoc;
	Direction wanderingDirection;

	public BuildPastrStrategy(RobotController rc, Random rand) throws GameActionException {
		this.rc = rc;
		this.enemy = rc.getTeam().opponent();
		this.info = rc.senseRobotInfo(rc.getRobot());
		this.rand = rand;
		this.enemyHqLoc=rc.senseEnemyHQLocation();
		this.myHqLoc=rc.senseHQLocation();
		//this.centerLoc=new MapLocation(rc.getMapHeight()/2,rc.getMapWidth()/2);
		wanderingDirection = null;
	}
	
	public void play() throws GameActionException {
		// set the below to middle of the map ?
		//MapLocation dest = Abilities.ClosestPastr(rc, rc.getLocation(), enemy);
		Direction safeDir=myHqLoc.directionTo(enemyHqLoc).opposite() ;
		MapLocation dest=myHqLoc.add(safeDir).add(safeDir) ;
		// for pasture we might use the above but make it another square further.
		Deque<Move> path = Navigation.pathAStar(rc, dest);
		while(Navigation.attackMoveOnPath(rc, path, info.type.attackRadiusMaxSquared, enemy)) {
				  Tactics.killNearbyEnemies(rc, info);
		}
		Tactics.killNearbyEnemies(rc, info);
		if (rc.isActive()) {
			// deploy PASTR
			int pastr_channel=2;
			Comms.BroadcastMessage(rc,pastr_channel,Comms.Message.create(Comms.Type.PASTR, rc.getLocation(), 0, rc.getRobot().getID()));
			rc.construct(RobotType.PASTR);
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
