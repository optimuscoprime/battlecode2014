package nano;

import java.util.*;

import battlecode.common.*;
import static nano.Util.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;

public class SoldierPlayer extends BasicPlayer implements Player {
	
	private MapLocation pastrConstructionMapLocation;

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
				pastrConstructionMapLocation = intToLocation(intPastrTowerLocation);
				if (myLocation.equals(pastrConstructionMapLocation)) {
					rc.construct(PASTR);
				} else {
					log("Going to pastr location");
					gotoLocation(pastrConstructionMapLocation);
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
					
					friendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
												
					// make a random move for now
					if (random.nextDouble() < 0.1) {
						moveRandomly();
					} else if (friendlyRobots.length > 8) {
						//if (pastrConstructionMapLocation != null) {
						//	gotoLocation(pastrConstructionMapLocation);
						//} else {
							//if (random.nextDouble() < 0.9) {
							//	MapLocation hqLocation = rc.senseHQLocation();
							//	gotoLocation(hqLocation);
							//} else{
								MapLocation hqLocation = rc.senseEnemyHQLocation();
								gotoLocation(hqLocation);
							//}
						//}
					}
					
				}
			}
		}	
	}

	private void gotoLocation(MapLocation location) {
		Direction direction = myLocation.directionTo(location);
		if (rc.canMove(direction)) {
			try	{
				rc.move(direction);
			} catch (GameActionException e) {
				die(e);
			}
		} else {
			moveRandomly();
		}
	}
}
