package pico;

import java.util.*;

import battlecode.common.*;
import static pico.Util.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;

public class SoldierPlayer extends BasicPlayer implements Player {
	
	private MapLocation pastrConstructionMapLocation;
	private MapLocation noiseTowerConstructionMapLocation;

	private MapLocation hqLocation;
	private MapLocation waypointLocation;
	private int waypointRound;

	public SoldierPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		hqLocation = rc.senseHQLocation();
	}

	@Override
	public void playOneTurn() throws GameActionException {
		boolean didAttack = false;
		
		myLocation = rc.getLocation();
		
		if (rc.isActive()) {
			didAttack = attackNearbyEnemies();
		}
		
		if (!didAttack && rc.isActive()) {
			
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
					noiseTowerConstructionMapLocation = intToLocation(intNoiseTowerLocation);
					if (myLocation.equals(noiseTowerConstructionMapLocation)) {
						rc.construct(NOISETOWER);
					} else {
						// check if another robot is there
						//Robot robot = rc.senseObjectAtLocation(noiseTowerConstructionMapLocation);
						
						log("Going to noisetower location");
						gotoLocation(noiseTowerConstructionMapLocation);
					}
					constructingNoiseTower = true;
				}
				
				if (!constructingNoiseTower) {
					
					friendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
												
					if (waypointLocation == null && Clock.getRoundNum() - waypointRound > 25) {
						// try to read a waypoint
						int intWaypointLocation = rc.readBroadcast(RADIO_CHANNEL_WAYPOINT);
						if (intWaypointLocation != 0) {
							waypointLocation = intToLocation(intWaypointLocation);
							waypointRound = Clock.getRoundNum();
						}
					}
					
					if (waypointLocation != null && myLocation.equals(waypointLocation)) {
						waypointLocation = null;
					}
					
					if (waypointLocation != null && friendlyRobots.length > 4) {
						gotoLocation(waypointLocation);
					} else {
						// make a random move for now
						if (random.nextDouble() < 0.25) {
							moveRandomly();
						} else {
							gotoLocation(hqLocation);
						}
					}
					
				}
			}
		}	
	}

	private void gotoLocation(MapLocation toLocation) {
		Direction direction = myLocation.directionTo(toLocation);
		if (rc.canMove(direction)) {
			int newDistanceToHq = hqLocation.distanceSquaredTo(toLocation);
			int currentDistanceToHq = hqLocation.distanceSquaredTo(myLocation);
			try	{
				if (newDistanceToHq < currentDistanceToHq) {
					// moving closer to HQ
					rc.move(direction);
				} else {
					// don't disturb cattle
					rc.sneak(direction);
				}
			} catch (GameActionException e) {
				die(e);
			}
		} else {
			moveRandomly();
		}
	}
}
