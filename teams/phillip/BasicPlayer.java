package phillip;

import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import battlecode.common.*;

import java.util.*;

import phillip.Util;
import static phillip.Util.*;

public abstract class BasicPlayer implements Player {

	protected RobotController rc;

	protected GameMap gameMap;

	protected RobotType myRobotType;
	protected int myRobotId;
	protected Team myTeam;
	
	protected Team opponentTeam;

	protected Robot myRobot;
	protected MapLocation myLocation;
	
	protected MapLocation enemyHqLocation;
	protected MapLocation myHqLocation;
	
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
		
		// every player builds their own map
		gameMap = new GameMap(robotId, team, robotType, rc);
		
		this.enemyHqLocation = rc.senseEnemyHQLocation();
		this.myHqLocation = rc.senseHQLocation();
	}
	
	public void playOneTurn() throws GameActionException {
		// keep this up to date each turn
		
		myRobotInfo = rc.senseRobotInfo(myRobot);
		myHealth = myRobotInfo.health;
		myLocation = myRobotInfo.location;
		
		if (rc.isActive()) {
			rc.setIndicatorString(0, ""); // gotoLocation
			rc.setIndicatorString(1, ""); // 
			rc.setIndicatorString(2, "");		
		}
	}
	
    protected Map<Robot, RobotInfo> senseAllRobotInfo(Robot[] robots) throws GameActionException {
    	
    	Map<Robot, RobotInfo> allInfo = new HashMap<Robot, RobotInfo>();
    	
    	for (Robot robot: robots) {
    		if (rc.canSenseObject(robot)) {
    			try {
    				allInfo.put(robot,  rc.senseRobotInfo(robot));
    			} catch (GameActionException e) {
    				die(e);
    			}
    		}
    	}

    	return allInfo;
	}
    

}
