package emo;

import static emo.Util.RADIO_CHANNEL_WAYPOINT;
import static emo.Util.countSoldiers;
import static emo.Util.locationToInt;

import java.util.*;

import battlecode.common.*;
import static emo.Util.*;

public class PastrPlayer extends BasicPlayer implements Player {
	private Robot[] nearbyFriendlyRobots;
	private int numNearbyFriendlySoldiers;

	public PastrPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
			    		
		if (rc.isActive()) {
			
			nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, 40, myTeam);
			
			numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobots, rc);			
			
			maybeCreateExploringWaypoint();
		}
		
		rc.yield();
	}
	
	private void maybeCreateExploringWaypoint() throws GameActionException {

    	MapLocation waypointLocation = null;
    	

    	if (numNearbyFriendlySoldiers < 2) {
    		
    		waypointLocation = myLocation;
	
    	}    		
    	
    	if (waypointLocation != null) {
    		rc.broadcast(RADIO_CHANNEL_WAYPOINT, locationToInt(waypointLocation));
    	} else {
    		rc.broadcast(RADIO_CHANNEL_WAYPOINT, 0);
 
    	}
	}		
}
