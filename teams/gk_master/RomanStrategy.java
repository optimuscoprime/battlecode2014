package gk_master;

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
	MapLocation target;
	LocalSurvey local;
	int patience;
	static final int FORTITUDE = 5;
	
	public RomanStrategy(RobotController rc) {
		super();
		TEAM = rc.getTeam();
		ENEMY_TEAM = TEAM.opponent();
		SENSOR_RADIUS = rc.getType().sensorRadiusSquared;
		HQ = rc.senseHQLocation();
		
		this.rc = rc;
		target = rc.senseEnemyHQLocation();
		local = new LocalSurvey(rc);
	}
	
	public void findTarget() {
		MapLocation[] loc = rc.sensePastrLocations(ENEMY_TEAM);
		if (loc.length > 0) {
			target = loc[0];
		}
	}

	@Override
	public void play() throws GameActionException {
		findTarget();
		local.SenseLocation();
		local.SenseNearbyRobots();
		local.CalculatePowerBalance();
		
		rc.setIndicatorString(0, "" + local.powerBalance);
		rc.setIndicatorString(1, "" + rc.getHealth());
		
		if (rc.isActive()) {
			boolean unhealthy = rc.getHealth() < RobotType.SOLDIER.maxHealth * 0.5;
			if (unhealthy) {
				Navigation.stepToward(rc, local.current.directionTo(HQ));
			} else {
				if (local.enemies.length == 0) {
					//move towards target
					Navigation.stepToward(rc, local.current.directionTo(target));
					patience = FORTITUDE;
				} else if (local.powerBalance < 0) {
					Navigation.stepToward(rc, local.current.directionTo(HQ));
					patience = FORTITUDE;
				} else {
					//attack
					if (!local.attack()) {
						if (patience <= 0) {
							Navigation.stepToward(rc, local.current.directionTo(target));	
							patience = FORTITUDE;
						} else {
							patience--;
						}
					} else {
						patience = FORTITUDE;
					}
				}
			}
		}
	}

}
