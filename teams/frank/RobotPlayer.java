package frank;

import java.util.*;

import battlecode.common.*;
static import battlecode.common.RobotType.*;

public class RobotPlayer {
    private static final RobotController rc;

    private static void die() {
        // asserts are disabled by default
        printf("Die\n");
        System.exit(-1);
    }

    public static void run (RobotController rc) {
        this.rc = rc;
        while (true) {
            playMove();
            rc.yield();
        }
    }

    private static void playMove() {
        switch (rc.getType()) {
            case HQ:
                playHqMove();
                break;
            case NOISETOWER:
                playNoiseTowerMove();
                break;
            case PASTR:
                playPastrMove();
                break;
            case SOLDIER:
                playSoldierMove();
                break;
        }
    }

    private static void playHqMove() {
        die();
    }

    private static void playNoiseTowerMove() {
        die();
    }

    private static void playPastrMove() {
        die();
    }

    private static void playSoldierMove() {
        die();
    }
}
