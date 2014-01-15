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
	MapLocation firstTarget;
	MapLocation currentTarget;
	Direction DIRECTION_TO_HQ;

	public SoundStrategy(RobotController rc, Random rand) throws GameActionException {
		this.rc = rc;
		this.ENEMY = rc.getTeam().opponent();
		this.INFO = rc.senseRobotInfo(rc.getRobot());
		this.rand = rand;
		wanderingDirection = null;
		//for now lets actually just start at the max range closest to the enemyhq
		// and 'pulse' towards our HQ.
		Direction DIRECTION_TO_ENEMY = INFO.location.directionTo(rc.senseEnemyHQLocation());
		DIRECTION_TO_HQ = DIRECTION_TO_ENEMY.opposite();
		// lets make a function that generates the line between A-B where A is closest enemy hq.
		// B is closest friendly HQ.
		firstTarget=rc.getLocation();
		MapLocation furtherTarget=firstTarget.add(DIRECTION_TO_ENEMY);
		while(rc.canAttackSquare(furtherTarget)){
			firstTarget=furtherTarget;
			furtherTarget=furtherTarget.add(DIRECTION_TO_ENEMY);
		}
		currentTarget=firstTarget;
	}
	
	public void play() throws GameActionException {
		if(!rc.canAttackSquare(currentTarget)){
			currentTarget=firstTarget;
		}
			  rc.attackSquare(currentTarget);
			  currentTarget=currentTarget.add(DIRECTION_TO_HQ);
			  currentTarget=currentTarget.add(DIRECTION_TO_HQ);
	}
}


