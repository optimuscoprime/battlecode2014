package micro;

import static battlecode.common.Direction.*;
import battlecode.common.*;

import java.util.*;

public abstract class BasicPlayer implements Player {

	protected RobotController rc;

	protected GameMap gameMap;

	protected Robot robot;

	protected RobotType robotType;

    public static Direction[] allDirections = new Direction[] {
        EAST,
        NORTH_EAST,
        NORTH,
        NORTH_WEST,
        WEST,
        SOUTH_WEST,
        SOUTH,
        SOUTH_EAST
    };
	
	public BasicPlayer(Robot robot, RobotType robotType, RobotController rc) {
		this.robot = robot;
		this.rc = rc;
		this.robotType = robotType;
		
		// every player builds their own map
		gameMap = new GameMap(robot, robotType, rc);
	}
	
	public abstract void playOneTurn();

}
