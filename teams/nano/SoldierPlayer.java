package nano;

import java.util.*;

import battlecode.common.*;
import static nano.Util.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;

public class SoldierPlayer extends BasicPlayer implements Player {
	
	public SoldierPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		boolean didAttack = false;
		
		if (rc.isActive()) {
			didAttack = attackNearbyEnemies();
		}
		
		if (!didAttack && rc.isActive()) {
			
			myLocation = rc.getLocation();
			
			boolean constructingPastr = false;
			
			// check the broadcasts
			int intPastrTowerLocation = rc.readBroadcast(RADIO_CHANNEL_REQUEST_PASTR);
			if (intPastrTowerLocation != 0) {
				MapLocation pastrMapLocation = intToLocation(intPastrTowerLocation);
				if (myLocation.equals(pastrMapLocation)) {
					rc.construct(PASTR);
				} else {
					log("Going to pastr location");
					gotoLocation(pastrMapLocation);
				}
				constructingPastr = true;
			}					
			
			if (!constructingPastr) {
				boolean constructingNoiseTower = false;
				// check the broadcasts
				int intNoiseTowerLocation = rc.readBroadcast(RADIO_CHANNEL_REQUEST_NOISETOWER);
				if (intNoiseTowerLocation != 0) {
					MapLocation noiseTowerMapLocation = intToLocation(intNoiseTowerLocation);
					if (myLocation.equals(noiseTowerMapLocation)) {
						rc.construct(NOISETOWER);
					} else {
						log("Going to noisetower location");
						gotoLocation(noiseTowerMapLocation);
					}
					constructingNoiseTower = true;
				}
				
				if (!constructingNoiseTower) {
					// do something
				}
			}
			
	
		}	
	}

	private void gotoLocation(MapLocation noiseTowerMapLocation) {
		// TODO Auto-generated method stub
	}
}
