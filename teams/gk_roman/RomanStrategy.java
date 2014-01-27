package gk_roman;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
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
	
	//time to hold position while enemy is in sight before advancing
	static final int DISIPLINE = 3;
	//time to hold ground against powerful foe before regrouping
	static final int FORTITUDE = 2;
	//fraction of health where the robot breaks rank and runs home
	static final double MORAL = 0.5;
	
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
		
		patience = DISIPLINE;
		bravery = FORTITUDE;
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
			target = newTarget;
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
			boolean unhealthy = rc.getHealth() < RobotType.SOLDIER.maxHealth * MORAL;
			if (unhealthy) {
				Navigation.stepToward(rc, local.current.directionTo(HQ));
				rc.setIndicatorString(2, "run");
			} else {
				if (local.enemies.length == 0) {
					//move towards target
					Navigation.bugWalk(rc, target);
//					Navigation.stepToward(rc, local.current.directionTo(target));
					rc.setIndicatorString(2, "move");
				} else if (local.powerBalance < 0) {
					if (bravery <= 0 || !local.attack()) {
						//back away from target
//						Navigation.bugWalk(rc, HQ);
						Navigation.stepToward(rc, local.current.directionTo(HQ));
						rc.setIndicatorString(2, "avoid");
					} else {
						//attacked enemy
						rc.setIndicatorString(2, "hold the line");
						bravery--;
					}
				} else {
					bravery = FORTITUDE;
					if (local.attack()) {
						//attack enemy
						patience = DISIPLINE;
						rc.setIndicatorString(2, "attack + wait");
					} else {
						if (patience <= 0 || local.banzai) {
							//move in
							Navigation.stepToward(rc, local.current.directionTo(target));	
							patience = DISIPLINE;
							rc.setIndicatorString(2, "lock step");
						} else {
							//hold
							patience--;
							rc.setIndicatorString(2, "hold to fire");
						}
					}
				}
			}
		}
	}

}
