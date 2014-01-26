package gk_roman;

import gk_roman.Navigation.Move;

import java.util.Deque;
import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class RomanStrategy implements Strategy {

	final RobotController rc;
	final Team TEAM;
	final Team ENEMY_TEAM;
	final int SENSOR_RADIUS;
	final MapLocation HQ;
	final MapLocation ENEMY_HQ;
	MapLocation target;
	LocalSurvey local;
	Random rand;
	
	int patience;
	int bravery;
	
	//time to wait for enemy to get near
	static final int FORTITUDE = 3;
	//times to get near stronger forces before chaning target
	static final int PERSISTENCE = 4;
	
	public RomanStrategy(RobotController rc) {
		super();
		rand = new Random(rc.getRobot().getID());
		TEAM = rc.getTeam();
		ENEMY_TEAM = TEAM.opponent();
		SENSOR_RADIUS = rc.getType().sensorRadiusSquared;
		HQ = rc.senseHQLocation();
		
		this.rc = rc;
		ENEMY_HQ = rc.senseEnemyHQLocation();
		target = ENEMY_HQ;
		local = new LocalSurvey(rc);
		
		patience = 0;
		bravery = PERSISTENCE;
	}
	
	public void findTarget() throws GameActionException {
		if (bravery == 0 || local.current.equals(target)) {
			MapLocation newTarget; 
			MapLocation[] loc = rc.sensePastrLocations(ENEMY_TEAM);
			if (loc.length > 0) {
				newTarget = loc[0];
			} else {
				newTarget = ENEMY_HQ;
			}
			
			if (newTarget.equals(target)) {
				//need to navigate to target
//				rc.setIndicatorString(2, "ASSASSIN");
//				Deque<Move> path = Navigation.pathAStarAvoid(rc, target, ENEMY_HQ, RobotType.HQ.attackRadiusMaxSquared);
//				while(Navigation.attackMoveOnPath(rc, path, SENSOR_RADIUS, ENEMY_TEAM)) {
//					break;
//				}
			}
			target = newTarget;
			bravery = PERSISTENCE;
		} 
	}

	@Override
	public void play() throws GameActionException {
		local.SenseLocation();
		local.SenseNearbyRobots();
		local.CalculatePowerBalance();
		findTarget();
		
		rc.setIndicatorString(0, "power:" + local.powerBalance);
		rc.setIndicatorString(1, "bravery: " + bravery + "\td: " + local.current.directionTo(target) + "\ttarget: " + target);
		
		if (rc.isActive()) {
			rc.setIndicatorString(2, "");
			boolean unhealthy = rc.getHealth() < RobotType.SOLDIER.maxHealth * 0.5;
			if (unhealthy) {
				if (rand.nextBoolean()) {
					Navigation.sneakToward(rc, local.current.directionTo(HQ));					
				} else {
					Navigation.sneakToward(rc, local.current.directionTo(target).opposite());
				}
				rc.setIndicatorString(2, "run");
			} else {
				if (local.enemies.length == 0) {
					//move towards target
					Navigation.sneakToward(rc, local.current.directionTo(target));
					rc.setIndicatorString(2, "move");
				} else if (local.powerBalance < 0) {
					if (rand.nextBoolean()) {
						Navigation.sneakToward(rc, local.current.directionTo(HQ));					
					} else {
						Navigation.sneakToward(rc, local.current.directionTo(target).opposite());
					}
					bravery--;
					rc.setIndicatorString(2, "avoid");
				} else {
					//attack
					if (!local.attack()) {
						if (patience <= 0) {
							Navigation.sneakToward(rc, local.current.directionTo(target));	
							patience = FORTITUDE;
							rc.setIndicatorString(2, "lock step");
						} else {
							patience--;
							rc.setIndicatorString(2, "wait");
						}
					} else {
						patience = FORTITUDE;
						rc.setIndicatorString(2, "attack + wait");
					}
				}
			}
		}
	}

}
