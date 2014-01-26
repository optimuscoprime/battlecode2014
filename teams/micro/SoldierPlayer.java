package micro;

import java.util.*;
import battlecode.common.*;
import static micro.Util.*;

public class SoldierPlayer extends BasicPlayer implements Player {

	private MapLocation waypoint = null;
	private Deque<MapLocation> path = null;
	private MapLocation next = null;
	
	public SoldierPlayer(Robot robot, RobotType robotType, RobotController rc) {
		super(robot, robotType, rc);
	}

	@Override
	public void playOneTurn() {
		
		if (rc.isActive()) {
			attackNearbyEnemies();
		}
		
		MapLocation from = rc.getLocation();
		
		if (waypoint == null) {
			waypoint = new MapLocation(rc.getMapWidth()-from.x, rc.getMapHeight()-from.y);
		}
	
		if (waypoint != null && path == null) {
			// go there
			path = gameMap.generatePath(from, waypoint);
		}
		
		if (next != null || (path != null && !path.isEmpty())) {
			
			if (next == null) {
				next = path.remove();
			}
			
//			log(robot, robotType, "got real path");
//			// print path
//			while (!path.isEmpty()) {
//				MapLocation current = path.remove();
//				System.out.printf("[%d,%d]\n", current.x, current.y);
//			}
//			System.out.printf("Path done!\n");
//			rc.breakpoint();
			
			if (rc.isActive()) {
				Direction direction = from.directionTo(next);
				//System.out.printf("%s from\n", from);
				//System.out.printf("%s next\n", next);
				//System.out.printf("%s\n", direction);
				if (rc.canMove(direction)) {
					try	{
						rc.move(direction);
						next = null;
					} catch (GameActionException e) {
						die(e);
					}
				} else {
					// let's try moving randomly
					shuffle(allDirections);
					for (Direction randomDirection: allDirections) {
						if (rc.canMove(randomDirection)) {
							try	{
								log(robot, robotType, "moving randomly");
								rc.move(randomDirection);
								break;
							} catch (GameActionException e) {
								die(e);
							}
						}
					}
					//System.out.printf("Can't move\n");
				}
			} else {
				//System.out.printf("Not active\n");
			}
		}
	}
	
	
}
