package awesemo;

import java.util.*;

import battlecode.common.*;
import static awesemo.Util.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import static awesemo.Util.*;

public class SoldierPlayer extends BasicPlayer implements Player {
	
	private MapLocation pastrConstructionMapLocation;
	private MapLocation noiseTowerConstructionMapLocation;

	private MapLocation waypointLocation;
	private int waypointRound;
	private int lastWaypointRoundNum = 0;
	
	public SoldierPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
		
		boolean didAttack = false;
		
		if (rc.isActive()) {
			didAttack = attackNearbyEnemies();
		} else {
			rc.setIndicatorString(1, "not active");
		}
		
		if (!didAttack && rc.isActive()) {
			
			// prefer to construct noise towers
			
			boolean constructingNoiseTower = false;
			// check the broadcasts
			int intNoiseTowerLocation = rc.readBroadcast(RADIO_CHANNEL_REQUEST_NOISETOWER);
			if (intNoiseTowerLocation != 0) {
				noiseTowerConstructionMapLocation = intToLocation(intNoiseTowerLocation);
				if (myLocation.equals(noiseTowerConstructionMapLocation)) {
					if (numNearbyFriendlySoldiers > 2) {
						rc.construct(NOISETOWER);
					}
				} else {
					// check if another robot is there
					//Robot robot = rc.senseObjectAtLocation(noiseTowerConstructionMapLocation);
					
					rc.setIndicatorString(1,  "build noisetower");
					gotoLocation(noiseTowerConstructionMapLocation);
				}
				constructingNoiseTower = true;
			}
			
			if (!constructingNoiseTower) {
				boolean constructingPastr = false;
				
				// check the broadcasts
				int intPastrTowerLocation = rc.readBroadcast(RADIO_CHANNEL_REQUEST_PASTR);
				if (intPastrTowerLocation != 0) {
					pastrConstructionMapLocation = intToLocation(intPastrTowerLocation);
					if (myLocation.equals(pastrConstructionMapLocation)) {
						if (numNearbyFriendlySoldiers > 2) {
							rc.construct(PASTR);
						}
					} else {
						rc.setIndicatorString(1,  "build pastr");
						gotoLocation(pastrConstructionMapLocation);
					}
					constructingPastr = true;
				}					
			
				if (!constructingPastr) {
					
					// maybe we should construct one anyway
					// if (rc.senseCowsAtLocation(myLocation) > 1000 && cowGrowth[myLocation.x][myLocation.y] 
						
					// don't listen for waypoints all the time
					//int thisRoundNum = Clock.getRoundNum();
					
					int intWaypointLocation = rc.readBroadcast(RADIO_CHANNEL_WAYPOINT);
					if (intWaypointLocation != 0) {
						//if (thisRoundNum > lastWaypointRoundNum) {
						waypointLocation = intToLocation(intWaypointLocation);
						
						
						
						// check if there is a closer friendly soldier
						
						int closerRobots = 0;
						int myDistanceToWaypoint = myLocation.distanceSquaredTo(waypointLocation);
						
						for (RobotInfo info : allFriendlyRobotInfo.values()) {
							if (info.location.distanceSquaredTo(waypointLocation) < myDistanceToWaypoint) {
								closerRobots++;
							}
						}
						
						if (closerRobots > 5) {
							
							rc.setIndicatorString(1,  "ignoring waypoint: " + waypointLocation);
							waypointLocation = null; // don't go there
							
							
						} else {
							rc.setIndicatorString(1,  "received waypoint: " + waypointLocation);
						}
						
						
						//lastWaypointRoundNum = thisRoundNum;
						//} else {
							// keep current waypoint before overriding
						//}
					} else {
						// allow waypoint resets at any time
						waypointLocation = null;
					}
					
					
					if (myLocation.equals(waypointLocation)) {
						waypointLocation = null;
					}
					
					if (waypointLocation == null) {
						MapLocation[] enemyPastrLocations = rc.sensePastrLocations(opponentTeam);
						
						if (enemyPastrLocations.length > 0) {
			    		
				    		// pick the pastr location that is closest to us
				    		
				    		sort(enemyPastrLocations, new Comparator<MapLocation>() {
								@Override
								public int compare(MapLocation o1, MapLocation o2) {
									return new Integer(myLocation.distanceSquaredTo(o1)).compareTo(myLocation.distanceSquaredTo(o2));
								}
				    		});
				    		
				    		waypointLocation = enemyPastrLocations[0];

				    		rc.setIndicatorString(1,  "created pastr waypoint: " + waypointLocation);
						}
					}
					

					
					if (waypointLocation != null) { // && numNearbyFriendlySoldiers > 2) {
						//log("going to waypoint");
						//rc.setIndicatorString(1,  "goto waypoint");
						gotoLocation(waypointLocation);
					} else {
						
						//MapLocation rallyPoint = ;
						
						//MapLocation rallyPoint = getSoldierCenterLocation();
						//if (rallyPoint != null) {
						//	gotoLocation(myHqLocation);
						//}
						
						
						gotoLocation(enemyHqLocation);
						
						//if (Util.random.nextDouble() < 0.1) {
						//	moveRandomly();
						//}
						
						// make a random move for now
						//if (random.nextDouble() < 0.25) {
						//	rc.setIndicatorString(0,  "moving randomly");
						//	moveRandomly();
						//} else {
						//	rc.setIndicatorString(0, "going to hq");
						//	gotoLocation(myHqLocation);
						//}
					}
					
				}
			}
		}	
		
		rc.yield();
	}


}
