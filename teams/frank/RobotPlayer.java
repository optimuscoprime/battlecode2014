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

    private static MapLocation location;
    private static Direction direction;
    private static double actionDelay;
    private static double health;

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
    }

    private static void reinit () {
        try {
            RobotPlayer.info = rc.senseRobotInfo(robot);
        } catch (GameActionException e) {
            die(e);
        }
        RobotPlayer.location = info.location;
        RobotPlayer.direction = info.direction;
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
        while (true) {
            reinit();

            // TODO non-active stuff

            // active stuff
            if (rc.isActive()) {
                boolean shouldSpawn = true;
                // TODO if enemies are nearby, maybe (probability) don't spawn, attack instead?
                if (shouldSpawn) {
                    tryToSpawn();
                }
            }
            rc.yield();
        }
    }

    private static void tryToSpawn () {
        // check surrounding squares
        // spawn in one of them
        
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

            // re-init

            // TODO non-active stuff
            // TODO active stuff
            rc.yield();
        }
    }
}
