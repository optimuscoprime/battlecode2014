package emo;

import java.util.*;

import battlecode.common.*;

public class PastrPlayer extends BasicPlayer implements Player {
	public PastrPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
		
		// TODO Auto-generated method stub
		rc.yield();
	}
}
