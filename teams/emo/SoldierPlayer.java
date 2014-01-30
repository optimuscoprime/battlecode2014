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
	
	private boolean kamikaze = false;

	public SoldierPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		super(robot, robotId, team, robotType, rc);
		myHqLocation = rc.senseHQLocation();
		enemyHqLocation = rc.senseEnemyHQLocation();
	}

	@Override
	public void playOneTurn() throws GameActionException {
		super.playOneTurn();
		
		Robot[] allFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, HUGE_RADIUS, myTeam);
		
		Robot[] nearbyFriendlyRobots = rc.senseNearbyGameObjects(Robot.class, myRobotType.sensorRadiusSquared*2, myTeam);

		int numFriendlySoldiers = countSoldiers(allFriendlyRobots, rc);
		int numNearbyFriendlySoldiers = countSoldiers(nearbyFriendlyRobots, rc);
		
		boolean didAttack = false;
		
		if (rc.isActive()) {
			didAttack = attackNearbyEnemies();
		}
		
		if (!didAttack && rc.isActive()) {
			if (numFriendlySoldiers > 5) {
				// small chance of going kamikaze
				if (Util.random.nextDouble() < 0.1) {
					kamikaze = true;
				}
			}
		}
		
		if (!didAttack && kamikaze && rc.isActive()) {
			gotoLocation(enemyHqLocation);
		}
		
		if (!didAttack && !kamikaze && rc.isActive()) {
			
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
					
					rc.setIndicatorString(0,  "going to noisetower location");
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
						rc.setIndicatorString(0,  "going to pastr location");
						gotoLocation(pastrConstructionMapLocation);
					}
					constructingPastr = true;
				}					
			
				if (!constructingPastr) {
					
					
						
					// don't listen for waypoints all the time
					int thisRoundNum = Clock.getRoundNum();
					
					int intWaypointLocation = rc.readBroadcast(RADIO_CHANNEL_WAYPOINT);
					if (intWaypointLocation != 0) {
						if (thisRoundNum > lastWaypointRoundNum + 10) {
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
						
						if (enemyPastrLocations.length > 0) {
			    		
				    		// pick the pastr location that is closest to us
				    		
				    		sort(enemyPastrLocations, new Comparator<MapLocation>() {
								@Override
								public int compare(MapLocation o1, MapLocation o2) {
									return new Integer(myLocation.distanceSquaredTo(o1)).compareTo(myLocation.distanceSquaredTo(o2));
								}
				    		});
				    		
				    		waypointLocation = enemyPastrLocations[0];
						}
					}
					

					
					if (waypointLocation != null && numNearbyFriendlySoldiers > 2) {
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
		
		rc.yield();
	}


}
