package micro;

import java.util.*;

import battlecode.common.*;
import static micro.Util.*;

public class HeadquartersPlayer extends BasicPlayer implements Player  {
		
	public HeadquartersPlayer(Robot robot, RobotType robotType, RobotController rc) {
		super(robot, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		boolean didAttack = false;
		
		if (rc.isActive()) {
			didAttack = attackNearbyEnemies();
		}
		
		if (rc.isActive() && !didAttack) {
			tryToSpawn();
		}
	}
	
    private void tryToSpawn () {
        // check surrounding squares
        // spawn in one of them
    	
    	shuffle(allDirections); 

        // shuffle directions first
        for (Direction direction: allDirections) {
            if (rc.canMove(direction)) {
                try {
                	log(robot, robotType, "Trying to spawn...");
                    rc.spawn(direction);
                } catch (GameActionException e) {
                    die(e);
                }
                break;
            }
        }
    }	
}
