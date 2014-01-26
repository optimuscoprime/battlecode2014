package micro;

import static battlecode.common.Direction.*;
import battlecode.common.*;

import java.util.*;
import static micro.Util.*;

public abstract class BasicPlayer implements Player {

	protected RobotController rc;

	protected GameMap gameMap;

	protected Robot robot;

	protected RobotType robotType;

    public static Direction[] allDirections = new Direction[] {
        EAST,
        NORTH_EAST,
        NORTH,
        NORTH_WEST,
        WEST,
        SOUTH_WEST,
        SOUTH,
        SOUTH_EAST
    };
	
	public BasicPlayer(Robot robot, RobotType robotType, RobotController rc) {
		this.robot = robot;
		this.rc = rc;
		this.robotType = robotType;
		
		// every player builds their own map
		gameMap = new GameMap(robot, robotType, rc);
	}
	
	public abstract void playOneTurn();
	
    protected boolean attackNearbyEnemies() {
        boolean didAttack = false;

        MapLocation location = rc.getLocation();
        RobotType type = rc.getType();
        Team opponent = rc.getTeam().opponent();
        
        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(
            Robot.class,
            location,
            type.attackRadiusMaxSquared,
            opponent
        );

        if (nearbyEnemies.length > 0) {
            // idea: attack the nearby enemy that is the furthest away from our position
            // (because they might retreat)

            MapLocation[] nearbyEnemyLocations = new MapLocation[nearbyEnemies.length];
            for (int i=0; i < nearbyEnemies.length; i++) {
                // copy across the location
                RobotInfo info = null;
                try {
                    info = rc.senseRobotInfo(nearbyEnemies[i]);
                } catch (GameActionException e) {
                    die(e);
                }
                nearbyEnemyLocations[i] = info.location;
            }

            sortLocationsByDistanceDescending(nearbyEnemyLocations, location);

            try {
                rc.attackSquare(nearbyEnemyLocations[0]);
            } catch (GameActionException e) {
                die(e);
            }

            didAttack = true;
        }

        return didAttack;
    }	

    private static void sortLocationsByDistanceDescending(MapLocation[] locations, final MapLocation from) {
        Arrays.sort(locations, new Comparator<MapLocation>() {
            public int compare(final MapLocation a, final MapLocation b) {
                return new Integer(from.distanceSquaredTo(b)).compareTo(from.distanceSquaredTo(a));
            }
        });
    }
}
