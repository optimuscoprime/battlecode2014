package frank;

import java.util.*;

import battlecode.common.*;

import static battlecode.common.RobotType.*;

public class RobotPlayer {
    private static RobotController rc;

    private static void die() {
        throw new RuntimeException("die");
    }

    public static void run (RobotController rc) {
        RobotPlayer.rc = rc;
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
