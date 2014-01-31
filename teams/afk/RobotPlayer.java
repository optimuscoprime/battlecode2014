package afk;

import java.util.*;
import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc) {
		while (true) {
			try {
				if (rc.isActive()) {
					if (rc.getType() == RobotType.HQ) {
						for (Direction direction: Direction.values()) {
							if (rc.canMove(direction)) {
								rc.spawn(direction);
							}
						}
					} else if (rc.getType() == RobotType.SOLDIER) {
						rc.construct(RobotType.PASTR);
					}
				}
			} catch (GameActionException e) {
				//
			}
			rc.yield();
		}
	}
}
