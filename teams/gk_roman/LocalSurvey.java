package gk_roman;

import gk_roman.Comms.Type;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class LocalSurvey {

	final RobotController rc;
	final Team TEAM;
	final Team ENEMY_TEAM;
	final MapLocation ENEMY_HQ_LOC;
	final int SENSOR_RADIUS;
	final int SHOOTING_RADIUS;
	MapLocation current;
	Robot[] enemies;
	Robot[] friends;
	RobotInfo[] enemyInfo;
	RobotInfo[] friendInfo;
	
	boolean banzai;
	int powerBalance;
	
	public LocalSurvey(RobotController rc) {
		this.rc = rc;
		TEAM = rc.getTeam();
		ENEMY_TEAM = rc.getTeam().opponent();
		SENSOR_RADIUS = rc.getType().sensorRadiusSquared;
		SHOOTING_RADIUS = rc.getType().attackRadiusMaxSquared;
		this.ENEMY_HQ_LOC = rc.senseEnemyHQLocation();
		
		SenseLocation();
		SenseNearbyRobots();
		CalculatePowerBalance();
	}
	
	public void SenseNearbyRobots() {
		enemies = rc.senseNearbyGameObjects(Robot.class, current, SENSOR_RADIUS, ENEMY_TEAM);
		friends = rc.senseNearbyGameObjects(Robot.class, current, SENSOR_RADIUS, TEAM);
		
		enemyInfo = null;
		friendInfo = null;
	}
	
	public void SenseNearbyRobotInfo() throws GameActionException {
		enemyInfo = new RobotInfo[enemies.length];
		friendInfo = new RobotInfo[friends.length];
		
		for (int i = 0; i < enemies.length; i++) {
			Robot enemy = enemies[i];
			if (rc.canSenseObject(enemy)) {
				enemyInfo[i] = rc.senseRobotInfo(enemy);
			}
		}
		for (int i = 0; i < friends.length; i++) {
			Robot friend = friends[i];
			if (rc.canSenseObject(friend)) {
				friendInfo[i] = rc.senseRobotInfo(friend);				
			}
		}
	}
	
	public void CalculatePowerBalance() {
		banzai = friends.length > 5;
		if (NearEnemyHQ() && !banzai) {
			powerBalance = -1;
		} else {
			if (enemyInfo == null) {
				powerBalance = friends.length - enemies.length;				
			} else {
				int friendlyPower = 0;
				int enemyPower = 0;
				for (RobotInfo info : friendInfo) {
					if (info.health > RobotType.SOLDIER.maxHealth * 0.5) {
						friendlyPower += info.health;
					}
				}
				for (RobotInfo info : enemyInfo) {
					enemyPower += info.health;
				}
				powerBalance = friendlyPower - enemyPower;
			}
		}
	}
	
	public void SenseLocation() {
		current = rc.getLocation();
	}
	
	public boolean NearEnemyHQ() {
		int d = current.distanceSquaredTo(ENEMY_HQ_LOC);
		return d <= SENSOR_RADIUS;
	}
	
	public boolean attack() throws GameActionException {
		for (Robot enemy : enemies) {
			if (rc.canSenseObject(enemy)) {
				RobotInfo info = rc.senseRobotInfo(enemy);
				if (info.type != RobotType.HQ && info.type != RobotType.NOISETOWER) {
					if (rc.canAttackSquare(info.location)) {
						rc.attackSquare(info.location);
						return true;
					}
				}
			}
		}
		return false;
	}
}
