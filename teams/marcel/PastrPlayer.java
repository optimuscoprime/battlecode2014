package marcel;

import java.util.*;

import battlecode.common.*;
import static marcel.Util.*;

public class PastrPlayer extends BasicPlayer implements Player {
	public PastrPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();    		
			
		maybeAskForBackup();
		
		rc.yield();
		return;
	}
	
	private void maybeAskForBackup() throws GameActionException {

    	MapLocation waypointLocation = null;
   
		Robot[] nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, 35, myTeam);
		Map<Robot, RobotInfo> nearbyFriendlyRobotMap = senseAllRobotInfo(nearbyFriendlyRobots);
		
		int numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobotMap.values());	    	
    	
		if (numNearbyFriendlySoldiers < 3 || myHealth < myRobotType.maxHealth) {
    		
    		waypointLocation = myLocation;
	
    	}    		
    	
    	if (waypointLocation != null) {
    		rc.broadcast(RADIO_CHANNEL_PASTR_BACKUP, locationToInt(waypointLocation));
    	} else {
    		rc.broadcast(RADIO_CHANNEL_PASTR_BACKUP, 0);
 
    	}
	}		
}
