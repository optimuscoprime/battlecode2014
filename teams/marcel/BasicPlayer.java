package marcel;

import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import battlecode.common.*;

import java.util.*;

import marcel.Util;
import static marcel.Util.*;

public abstract class BasicPlayer implements Player {

	protected RobotController rc;

	protected RobotType myRobotType;
	protected int myRobotId;
	protected Team myTeam;
	
	protected Team opponentTeam;

	protected Robot myRobot;
	protected MapLocation myLocation;
	
	protected MapLocation enemyHqLocation;
	protected MapLocation myHqLocation;
	
	protected int width;
	protected int height;
	
    public final Direction[] allDirections = new Direction[] {
    	// prefer diagonal directions
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_WEST,
        SOUTH_EAST,
        EAST,
        NORTH,
        WEST,
        SOUTH
    };
    
    public Direction[] randomDirections = allDirections.clone();

	protected RobotInfo myRobotInfo = null;
	
	protected double myHealth;

	public BasicPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		this.myRobot = robot;
		this.myRobotId = robotId;
		this.myTeam = team;
		this.opponentTeam = team.opponent();
		this.myRobotType = robotType;

		this.rc = rc;
		
		this.enemyHqLocation = rc.senseEnemyHQLocation();
		this.myHqLocation = rc.senseHQLocation();
		
		this.width = rc.getMapWidth();
		this.height = rc.getMapHeight();
	}
	
	public void playOneTurn() throws GameActionException {
		// keep this up to date each turn
		
		myRobotInfo = rc.senseRobotInfo(myRobot);
		myHealth = myRobotInfo.health;
		myLocation = myRobotInfo.location;
		
		//if (rc.isActive()) {
		//	rc.setIndicatorString(0, ""); // gotoLocation
		//	rc.setIndicatorString(1, ""); // strategy
		//	rc.setIndicatorString(2, ""); // active/not active	
		//}
	}
}
