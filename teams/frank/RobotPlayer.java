package frank;

import java.util.*;

import battlecode.common.*;

import static battlecode.common.RobotType.*;
import static battlecode.common.Direction.*;

public class RobotPlayer {
    private static RobotController rc;
    private static Robot robot;
    private static RobotInfo info;
    private static RobotType type;
    private static Team team;
    private static Team opponent;

    private static MapLocation location;
    private static double actionDelay;
    private static double health;

    private static Random random;

    private static boolean exploring = false;

    private static Direction[] allDirections = new Direction[] {
        EAST,
        NORTH_EAST,
        NORTH,
        NORTH_WEST,
        WEST,
        SOUTH_WEST,
        SOUTH,
        SOUTH_EAST
    };

    private static void die () {
        die(null);
    }

    private static void die (Exception e) {
        throw new RuntimeException("I'm melting, melting. Ohhhhh, what a world, what a world...", e);
    }

    public static void init (RobotController rc) {
        RobotPlayer.rc = rc;
        RobotPlayer.robot = rc.getRobot();
        try {
            RobotPlayer.info = rc.senseRobotInfo(robot);
        } catch (GameActionException e) {
            die(e);
        }
        RobotPlayer.type = info.type;
        RobotPlayer.team = info.team;
        RobotPlayer.opponent = team.opponent();
        RobotPlayer.random = new Random(robot.getID());
    }

    private static void reinit () {
        try {
            RobotPlayer.info = rc.senseRobotInfo(robot);
        } catch (GameActionException e) {
            die(e);
        }
        RobotPlayer.location = info.location;
        RobotPlayer.actionDelay = info.actionDelay;
        RobotPlayer.health = info.health;
    }

    public static void run (RobotController rc) {
        init(rc);

        switch (type) {
            case HQ:
                playAsHq();
                break;
            case NOISETOWER:
                playAsNoiseTower();
                break;
            case PASTR:
                playAsPastr();
                break;
            case SOLDIER:
                playAsSoldier();
                break;
        }
    }

    private static void playAsHq () {
        boolean shouldSpawn = true;

        while (true) {
            reinit();

            // TODO non-active stuff

            // active stuff
            if (rc.isActive()) {
                if (!shouldSpawn && random.nextDouble() > 0.9) {
                    shouldSpawn = true;
                }
                if (attackNearbyEnemies()) {
                    // if we attacked, then with high probability, should set shouldSpawn to false
                    // so that we can attack next time too
                    shouldSpawn = false;
                } else if (shouldSpawn) {
                    tryToSpawn();
                } else {
                    // wait - enemies might come back, need to shoot them
                }
            }

            rc.yield();
        }
    }

    private static boolean attackNearbyEnemies() {
        boolean didAttack = false;

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

    private static void tryToSpawn () {
        // check surrounding squares
        // spawn in one of them

        List<Direction> shuffledDirections = Arrays.asList(allDirections);
        Collections.shuffle(shuffledDirections);

        // shuffle directions first
        for (Direction direction: shuffledDirections) {
            if (rc.canMove(direction)) {
                try {
                    rc.spawn(direction);
                } catch (GameActionException e) {
                    die(e);
                }
                break;
            }
        }
    }

    private static void playAsNoiseTower () {
        while (true) {
            reinit();

            // TODO non-active stuff
            // TODO active stuff
            rc.yield();
        }
    }

    private static void playAsPastr () {
        while (true) {
            reinit();

            // re-init

            // TODO non-active stuff
            // TODO active stuff
            rc.yield();
        }
    }

    private static void playAsSoldier () {
        while (true) {
            reinit();

            // TODO non-active stuff

            // active stuff
            if (rc.isActive()) {
                // try to attack nearby enemies
                if (attackNearbyEnemies()) {
                    // attacking them
                } else if (maybeBecomePastr()) {
                    // becoming a pastr
                } else {
                    // otherwise
                    // sometimes, lets move towards a pastr
                    // other times, lets just move randomly

                    MapLocation[] friendlyPastrLocations = rc.sensePastrLocations(team);

                    sortLocationsByDistanceDescending(friendlyPastrLocations, location);


                    if (random.nextDouble() < 0.05) {
                        // small chance to start or stop exploring
                        exploring = !exploring;
                    }

                    if (friendlyPastrLocations.length > 0 && !exploring && random.nextDouble() < 0.8) {
                        for (MapLocation friendlyPastrLocation : friendlyPastrLocations) {
                            Direction nextDirection = location.directionTo(friendlyPastrLocation);
                            if (rc.canMove(nextDirection)) {
                                try {
                                    rc.move(nextDirection);
                                } catch (GameActionException e) {
                                    die(e);
                                }
                                break;
                            }
                        }
                    } else {
                        List<Direction> shuffledDirections = Arrays.asList(allDirections);
                        Collections.shuffle(shuffledDirections);
                        for (Direction direction: shuffledDirections) {
                            if (rc.canMove(direction)) {
                                try {
                                    rc.move(direction);
                                } catch (GameActionException e) {
                                    die(e);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            rc.yield();
        }
    }

    private static boolean maybeBecomePastr() {
        boolean becamePastr = false;
        double cowsAtLocation = 0;
        try {
            cowsAtLocation = rc.senseCowsAtLocation(location);
        } catch (GameActionException e) {
            die(e);
        }
        if (cowsAtLocation > 1000) {
            try {
                rc.construct(PASTR);
            } catch (GameActionException e) {
                die(e);
            }
            becamePastr = true;
        }
        return becamePastr;
    }

    private static void sortLocationsByDistanceDescending(MapLocation[] locations, final MapLocation from) {
        Arrays.sort(locations, new Comparator<MapLocation>() {
            public int compare(final MapLocation a, final MapLocation b) {
                return new Integer(from.distanceSquaredTo(b)).compareTo(from.distanceSquaredTo(a));
            }
        });
    }
}
