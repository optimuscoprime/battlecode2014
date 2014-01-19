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
            Arrays.sort(nearbyEnemies, new Comparator<Robot>() {
                public int compare(Robot a, Robot b) {
                    RobotInfo aInfo = null;
                    RobotInfo bInfo = null;
                    try {
                        aInfo = rc.senseRobotInfo(a);
                    } catch (GameActionException e) {
                        die(e);
                    }
                    try {
                        bInfo = rc.senseRobotInfo(b);
                    } catch (GameActionException e) {
                        die(e);
                    }
                    return new Integer(location.distanceSquaredTo(bInfo.location)).compareTo(location.distanceSquaredTo(aInfo.location));
                }
            });
            RobotInfo furthestNearbyEnemyInfo = null;
            try {
                furthestNearbyEnemyInfo = rc.senseRobotInfo(nearbyEnemies[0]);
            } catch (GameActionException e) {
                die(e);
            }
            MapLocation furthestNearbyEnemyLocation = furthestNearbyEnemyInfo.location;
            try {
                rc.attackSquare(furthestNearbyEnemyLocation);
            } catch (GameActionException e) {
                die(e);
            }

            didAttack = true;
        }

        return didAttack;
    }

    private static void shuffle (Direction[] items) {
        for (int i = items.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            Direction temp = items[index];
            items[index] = items[i];
            items[i] = temp;
        }
    }

    private static void tryToSpawn () {
        // check surrounding squares
        // spawn in one of them

        // shuffle directions first
        shuffle(allDirections);

        for (Direction direction: allDirections) {
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

            if (rc.isActive()) {
                // try to attack nearby enemies
                if (attackNearbyEnemies()) {
                    // attacking them
                } else {
                    // otherwise lets just try to move randomly
                    shuffle(allDirections);
                    for (Direction direction: allDirections) {
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

            // TODO non-active stuff
            // TODO active stuff
            rc.yield();
        }
    }
}
