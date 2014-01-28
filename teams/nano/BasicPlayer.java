package nano;

import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;
import battlecode.common.*;

import java.util.*;

import static nano.Util.*;

public abstract class BasicPlayer implements Player {

	protected RobotController rc;

	protected GameMap gameMap;

	protected RobotType myRobotType;
	protected int myRobotId;
	protected Team myTeam;
	
	protected Team opponentTeam;

	protected Robot myRobot;
	protected MapLocation myLocation;
	protected Robot[] friendlyRobots;
	
    public final Direction[] allDirections = new Direction[] {
    	// prefer diagonal directions
        NORTH_EAST,
        NORTH_WEST,
        SOUTH_WEST,
        SOUTH_EAST,
        EAST,
        NORTH,
        WEST,
        SOUTH
    };
	
	public BasicPlayer(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
		this.myRobot = robot;
		this.myRobotId = robotId;
		this.myTeam = team;
		this.opponentTeam = team.opponent();
		this.myRobotType = robotType;

		this.rc = rc;
		
		// every player builds their own map
		gameMap = new GameMap(robotId, team, robotType, rc);
	}
	
	public abstract void playOneTurn() throws GameActionException;
	
    protected boolean attackNearbyEnemies() throws GameActionException {
    	log("Started attackNearbyEnemies()...");
    	
        boolean didAttack = false;
        
        MapLocation myLocation = rc.getLocation();
        
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
            Robot.class,
            myLocation,
            myRobotType.attackRadiusMaxSquared,
            opponentTeam
        );

        if (nearbyEnemies.length > 0) {

            final Map<Robot, RobotInfo> allRobotInfo = new HashMap<Robot, RobotInfo>();
            
        	for (int i=0; i < nearbyEnemies.length; i++) {
        		// this sense is guaranteed to succeed because we already used senseNearbyGameObjects
        		RobotInfo robotInfo = rc.senseRobotInfo(nearbyEnemies[i]);
        		allRobotInfo.put(nearbyEnemies[i], robotInfo);        		
        	}
        	
        	sort(nearbyEnemies, new Comparator<Robot>() {
        		// idea:
        		// prefer to attack pastrs
        		// otherwise soldiers
        		// otherwise noisetowers
        		// never hqs
        		
				@Override
				public int compare(Robot o1, Robot o2) {
					RobotInfo info1 = allRobotInfo.get(o1);
					RobotInfo info2 = allRobotInfo.get(o2);
					
					if (info1.type == PASTR && info2.type != PASTR) {
						return -1;
					} else if (info1.type != PASTR && info2.type == PASTR) {
						return 1;
					} else if (info1.type == SOLDIER && info2.type != SOLDIER) {
						return -1;
					} else if (info1.type != SOLDIER && info2.type == SOLDIER) {
						return 1;
					} else if (info1.type == NOISETOWER && info2.type != NOISETOWER) {
						return -1;
					} else if (info1.type != NOISETOWER && info2.type == NOISETOWER) {
						return 1;
					} else if (info1.type == HQ && info2.type != HQ) {
						return 1;
					} else if (info1.type != HQ && info2.type == HQ) {
						return -1;
					} else {
						// if same type, just sort on health
						return new Double(info1.health).compareTo(info2.health);
					}
				}
        	});
        	
        	// try to attack one of them as long as it isn't the HQ
        	for (Robot nearbyEnemyRobot: nearbyEnemies) {
        		RobotInfo info = allRobotInfo.get(nearbyEnemyRobot);
        		if (info.type != HQ && rc.canAttackSquare(info.location)) {
        			rc.attackSquare(info.location);
        			didAttack = true;
        			break;
        		}
        	}
        }

        log("Finished attackNearbyEnemies().");
        
        return didAttack;
    }	

    private static void sortLocationsByDistanceDescending(List<MapLocation> locations, final MapLocation from) {
        Collections.sort(locations, new Comparator<MapLocation>() {
            public int compare(final MapLocation a, final MapLocation b) {
                return new Integer(from.distanceSquaredTo(b)).compareTo(from.distanceSquaredTo(a));
            }
        });
    }
}
