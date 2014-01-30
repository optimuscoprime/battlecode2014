package emo;

import java.util.*;

import battlecode.common.*;
import static emo.Util.*;
import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import static emo.Util.*;

public class SoldierPlayer extends BasicPlayer implements Player {
	
	private MapLocation pastrConstructionMapLocation;
	private MapLocation noiseTowerConstructionMapLocation;

	private MapLocation waypointLocation;
	private int waypointRound;
	private int lastWaypointRoundNum = 0;

	public SoldierPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		myHqLocation = rc.senseHQLocation();
		enemyHqLocation = rc.senseEnemyHQLocation();
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
		
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
					rc.setIndicatorString(0,  "going to pastr location");
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
						
						rc.setIndicatorString(0,  "going to noisetower location");
						gotoLocation(noiseTowerConstructionMapLocation);
					}
					constructingNoiseTower = true;
				}
				
				if (!constructingNoiseTower) {
					
					Robot[] nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, myRobotType.sensorRadiusSquared, myTeam);
					
						
					// don't listen for waypoints all the time
					int thisRoundNum = Clock.getRoundNum();
					
					int intWaypointLocation = rc.readBroadcast(RADIO_CHANNEL_WAYPOINT);
					if (intWaypointLocation != 0) {
						if (thisRoundNum > lastWaypointRoundNum + 50) {
							waypointLocation = intToLocation(intWaypointLocation);
							lastWaypointRoundNum = thisRoundNum;
						} else {
							// keep current waypoint before overriding
						}
					} else {
						// allow waypoint resets at any time
						waypointLocation = null;
					}
					
					
					if (myLocation.equals(waypointLocation)) {
						waypointLocation = null;
					}
					
					if (waypointLocation == null) {
						MapLocation[] enemyPastrLocations = rc.sensePastrLocations(opponentTeam);
			    		
			    		// pick the pastr location that is closest to us
			    		
			    		sort(enemyPastrLocations, new Comparator<MapLocation>() {
							@Override
							public int compare(MapLocation o1, MapLocation o2) {
								return new Integer(myLocation.distanceSquaredTo(o1)).compareTo(myLocation.distanceSquaredTo(o2));
							}
			    		});
			    		
			    		waypointLocation = enemyPastrLocations[0];
					}
					
					int numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobots, rc);
					
					if (waypointLocation != null && numNearbyFriendlySoldiers > 1) {
						//log("going to waypoint");
						rc.setIndicatorString(0,  "going to waypoint: " + waypointLocation);
						gotoLocation(waypointLocation);
					} else {
						// make a random move for now
						if (random.nextDouble() < 0.25) {
							rc.setIndicatorString(0,  "moving randomly");
							moveRandomly();
						} else {
							rc.setIndicatorString(0, "going to hq");
							gotoLocation(myHqLocation);
						}
					}
					
				}
			}
		}	
	}


}
