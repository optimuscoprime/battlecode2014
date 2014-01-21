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
 * Aggressive attack strategy
 *
 * Ian has plans for what happends when retreating -
 * by shooting out to the side I think single soldiers can perhaps herd a *lot*
 */
public class AttackStrategy implements Strategy {
	RobotController rc;
	Random rand;
	Team ENEMY;
	RobotInfo INFO;
	Direction wanderingDirection;

	public AttackStrategy(RobotController rc, Random rand) throws GameActionException {
		this.rc = rc;
		this.ENEMY = rc.getTeam().opponent();
		this.INFO = rc.senseRobotInfo(rc.getRobot());
		this.rand = rand;
		wanderingDirection = null;
	}
	
	public void play() throws GameActionException {
		MapLocation dest = Abilities.ClosestPastr(rc, rc.getLocation(), ENEMY);

		if(rc.getHealth()<rc.getType().maxHealth){
			if(rc.isActive()){
				dest=rc.senseHQLocation();	//run away!
				//System.out.println("Ouch!\n");
				Navigation.moveGreedy(rc,dest,2);
				//Deque<Move> path = Navigation.pathAStar(rc, dest);
				//while(Navigation.moveOnPath(rc,path)){
				//	System.out.println("For mother Russia!\n");
				//	rc.selfDestruct();
				//	System.out.println("Running?\n");
				//}
			}
		}
		if (dest != null) {
			Deque<Move> path = Navigation.pathAStar(rc, dest);
			while(Navigation.attackMoveOnPath(rc, path, INFO.type.attackRadiusMaxSquared, ENEMY)) {
				Tactics.killNearbyEnemies(rc, INFO);
			}
		}
		Tactics.killNearbyEnemies(rc, INFO);
		if (rc.isActive()) {
			wanderingDirection = Navigation.wonder(rc, rand, wanderingDirection);
		} else {
			rc.yield();				
		}
	}
}




//Direction wonder = null;
//while (true) {
//	MapLocation dest = Abilities.ClosestPastr(rc, rc.getLocation(), ENEMY);
//	if (dest == null) {	
//		Message m = Comms.ReadMessage(rc);
//		if (m != null && m.type == Comms.Type.CONVERGE) {
//			dest = m.loc;
//		}
//	}
//	if (dest != null) {
//		Deque<Move> path = Navigation.pathAStar(rc, dest);
//		while(Navigation.attackMoveOnPath(rc, path, INFO.type.attackRadiusMaxSquared, ENEMY)) {
//			Tactics.killNearbyEnemies(rc, INFO);
//		}
//	}
//	Tactics.killNearbyEnemies(rc, INFO);
//	if (rc.isActive()) {
//		wonder = Navigation.wonder(rc, rand, wonder);
//	} else {
//		rc.yield();				
//	}
//}
