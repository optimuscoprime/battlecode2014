package awesemo;

import static awesemo.Util.RADIO_CHANNEL_WAYPOINT;
import static awesemo.Util.countSoldiers;
import static awesemo.Util.locationToInt;

import java.util.*;

import battlecode.common.*;
import static awesemo.Util.*;

public class PastrPlayer extends BasicPlayer implements Player {
	public PastrPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
			    		
		//if (rc.isActive()) {
			
			//nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, 40, myTeam);
			
			//numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobots, rc);			
			
		maybeAskForBackup();
		//}
		
		rc.yield();
	}
	
	private void maybeAskForBackup() throws GameActionException {

    	MapLocation waypointLocation = null;
   
		Robot[] nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
		Map<Robot, RobotInfo> nearbyFriendlyRobotInfo = senseAllRobotInfo(nearbyFriendlyRobots);
		nearbyFriendlyRobots = nearbyFriendlyRobotInfo.keySet().toArray(new Robot[0]);
		
		int numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobotInfo);	    	
    	
		if (numNearbyFriendlySoldiers < 3 || myHealth < myRobotType.maxHealth) {
    		
    		waypointLocation = myLocation;
	
    	}    		
    	
    	if (waypointLocation != null) {
    		rc.broadcast(RADIO_CHANNEL_WAYPOINT, locationToInt(waypointLocation));
    	} else {
    		rc.broadcast(RADIO_CHANNEL_WAYPOINT, 0);
 
    	}
	}		
}
