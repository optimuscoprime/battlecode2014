package micro;

import java.util.*;

import battlecode.common.*;
import static micro.Util.*;

public class SoldierPlayer extends BasicPlayer implements Player {

	private MapLocation waypoint = null;
	private List<MapLocation> path = null;
	private MapLocation next = null;
	private int i = 0;
	private boolean reverse = false;
	
	public SoldierPlayer(Robot robot, RobotType robotType, RobotController rc) {
		super(robot, robotType, rc);
	}

	@Override
	public void playOneTurn() throws GameActionException {
		
		int numSoldiers = rc.senseNearbyGameObjects(Robot.class, 999999, rc.getTeam()).length - 1;
		
		// first soldiers builds a pastr
		if (rc.isActive() && numSoldiers == 1) {
			rc.construct(RobotType.PASTR);
			rc.yield();
		}
			
		if (rc.isActive()) {
			attackNearbyEnemies();
		}
		
		MapLocation from = rc.getLocation();
		
		if (waypoint == null) {
			
			boolean found = false;
			
			int randomX;
			int randomY;
			
			do {
				randomX = Util.random.nextInt(rc.getMapWidth());
				randomY = Util.random.nextInt(rc.getMapHeight());
				if (gameMap.map1x1[randomX][randomY] != TerrainTile.VOID) {
					found = true;
				}
			} while (!found);
			
			waypoint = new MapLocation(randomX, randomY);
		}
	
		if (waypoint != null && path == null) {
			// go there
			path = new ArrayList<MapLocation>(gameMap.generatePath(from, waypoint));
		}
		
		
		if (path != null) {
				
			if (next == null) {
				next = path.get(i);
				if (reverse) {
					i--;
				} else {
					i++;
				}
				if (i >= path.size()) {
					reverse = true;	
					i--;
				}
				if (i < 0) {
					reverse = false;
					i++;
				}				
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
