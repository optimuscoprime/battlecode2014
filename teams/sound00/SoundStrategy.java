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
 * Aggressive attack strategy
 */
public class SoundStrategy implements Strategy {
	RobotController rc;
	Random rand;
	Team ENEMY;
	RobotInfo INFO;
	Direction wanderingDirection;
	MapLocation target;

	public SoundStrategy(RobotController rc, Random rand) throws GameActionException {
		this.rc = rc;
		this.ENEMY = rc.getTeam().opponent();
		this.INFO = rc.senseRobotInfo(rc.getRobot());
		this.rand = rand;
		wanderingDirection = null;
		//for now lets actually just start at the max range closest to the enemyhq
		// and 'pulse' towards our HQ.
		Direction DIRECTION_TO_ENEMY = INFO.location.directionTo(rc.senseEnemyHQLocation());
		// lets make a function that generates the line between A-B where A is closest enemy hq.
		// B is closest friendly HQ.
		target=rc.getLocation();
		MapLocation furtherTarget=target.add(DIRECTION_TO_ENEMY);
		while(rc.canAttackSquare(furtherTarget)){
			target=furtherTarget;
			furtherTarget=furtherTarget.add(DIRECTION_TO_ENEMY);
		}
	}
	
	public void play() throws GameActionException {
			  rc.attackSquare(target);
	}
}


