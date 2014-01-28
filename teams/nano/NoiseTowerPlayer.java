package nano;

import java.util.*;

import battlecode.common.*;

public class NoiseTowerPlayer extends BasicPlayer implements Player {
	public NoiseTowerPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		// TODO
		// check map squares for cows/cow growth rate
		// pulse from there
	}
}
