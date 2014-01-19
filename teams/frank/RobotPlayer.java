package frank;

import java.util.*;

import battlecode.common.*;

import static battlecode.common.RobotType.*;

public class RobotPlayer {
    private static RobotController rc;
    private static RobotType type;

    private static void die() {
        throw new RuntimeException("I'm melting, melting. Ohhhhh, what a world, what a world...");
    }

    public static void run (RobotController rc) {
        RobotPlayer.rc = rc;
        RobotPlayer.type = rc.getType();
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

    private static void playAsHq() {
        while (true) {
            // TODO
            rc.yield();
        }
    }

    private static void playAsNoiseTower() {
        while (true) {
            // TODO
            rc.yield();
        }
    }

    private static void playAsPastr() {
        while (true) {
            // TODO
            rc.yield();
        }
    }

    private static void playAsSoldier() {
        while (true) {
            // TODO
            rc.yield();
        }
    }
}
