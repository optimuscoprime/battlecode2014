package sc02;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {

	// save things that will never change
	//  (unless looking up any of these things coses zero bytecodes)
	public static int mapWidth;
	public static int mapHeight;
	public static MapLocation my_hq = null;
	public static MapLocation enemy_hq = null;
	public static MapLocation mapCenter = null;
	public static MapLocation leftCorner = null;
	public static MapLocation rightCorner = null;
	public static MapLocation myCorner = null;
	public static MapLocation enemyCorner = null;

	public static void run(RobotController rc) {

		int birthday = Clock.getRoundNum();
		getBearings(rc);
		int rank = rc.senseRobotCount();

		while (true) {
			try {
				if (rc.getType() == RobotType.HQ) {
					HQPlayer(rc, birthday);
				} else if (rc.getType() == RobotType.SOLDIER) {
					RandomMovePlayer(rc, birthday);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			rc.yield();
		}

	}

	public static void getBearings(RobotController rc) {
		if (my_hq == null) {
			mapWidth = rc.getMapWidth();
			mapHeight = rc.getMapHeight();

			mapCenter = new MapLocation(mapWidth/2, mapHeight/2);

			my_hq = rc.senseHQLocation();
			enemy_hq = rc.senseEnemyHQLocation();
			if (my_hq.x < enemy_hq.x) {
				if (my_hq.y < enemy_hq.y) {
					myCorner = new MapLocation(0, 0);
					enemyCorner = new MapLocation(mapWidth-1, mapHeight-1);
					leftCorner = new MapLocation(mapWidth-1, 0);
					rightCorner = new MapLocation(0, mapHeight-1);
				} else {
					myCorner = new MapLocation(0, mapHeight-1);
					enemyCorner = new MapLocation(mapWidth-1, 0);
					leftCorner = new MapLocation(0, 0);
					rightCorner = new MapLocation(mapWidth-1, mapHeight-1);
				}
			} else {
				if (my_hq.y < enemy_hq.y) {
					myCorner = new MapLocation(mapWidth-1, 0);
					enemyCorner = new MapLocation(0, mapHeight-1);
					leftCorner = new MapLocation(mapWidth-1, mapHeight-1);
					rightCorner = new MapLocation(0, 0);
				} else {
					myCorner = new MapLocation(mapWidth-1, mapHeight-1);
					enemyCorner = new MapLocation(0, 0);
					leftCorner = new MapLocation(0, mapHeight-1);
					rightCorner = new MapLocation(mapWidth-1, 0);
				}
			}
		}
	}

	public static void HQPlayer(RobotController rc, int birthday) throws Exception {
		if (rc.isActive() && ! rc.isConstructing() && rc.senseRobotCount() < GameConstants.MAX_ROBOTS) {
			Direction dir = rc.getLocation().directionTo(enemy_hq);

			for (int i = 0; i < 8; ++i) {
				if (rc.canMove(dir)) {
					try {
						rc.spawn(dir);
					} catch (Exception eX) {}
				} else {
					dir = dir.rotateLeft();
				}
			}
		}
	}

	public static void CloseToHomeBuilderPlayer(RobotController rc, int birthday, int rank) throws Exception {
		if (rc.isActive() && !rc.isConstructing()) {
			int round_num = Clock.getRoundNum();
			int age = round_num - birthday;
			if (age < 5) {
				int dir = rank % 4;
				if (dir == 0) {
					moveTowardsLocation(rc, myCorner, true);
				} else if (dir == 1) {
					moveTowardsLocation(rc, leftCorner, true);
				} else if (dir == 2) {
					moveTowardsLocation(rc, enemyCorner, true);
				} else {
					moveTowardsLocation(rc, rightCorner, true);
				}
			} else {
				rc.construct(RobotType.PASTR);
			}
		}
	}

	public static void BuilderPlayer(RobotController rc, int birthday, int rank) throws Exception {
		if (rc.isActive() && !rc.isConstructing()) {
			MapLocation my_loc = rc.getLocation();
			int round_num = Clock.getRoundNum();

			boolean loud = false;
			
			// really want the guys to go out for a while
			// then at a point not too late, storm home together
			// then take 4 more sneak steps and build nicely spaced PASTRs
			MapLocation dest = null;
			int age = round_num - birthday;;
			if (age < 30) {
				dest = leftCorner;
			} else if (age < 60) {
				dest = enemyCorner;
			} else if (age < 90) {
				dest = rightCorner;
			} else if (age < 120) {
				dest = enemyCorner;
			} else if (age < 140) {
				dest = my_hq;
				loud = true;
			} else if (age < 150) {
				if (rank % 4 == 0) {
					dest = myCorner;
				} else if (rank % 4 == 1) {
					dest = leftCorner;
				} else if (rank % 4 == 2) {
					dest = rightCorner;
				} else if (rank % 4 == 3) {
					dest = enemyCorner;
				}
			} else {
				rc.construct(RobotType.PASTR);
				return;
			}
			
			moveTowardsLocation(rc, dest, !loud);
			
		}
	}	

	public static void ChargeAtEnemyPlayer(RobotController rc) throws Exception {
		
	}

	public static MapLocation randomMovePlayerDest = null;	
	public static void RandomMovePlayer(RobotController rc, int birthday) throws Exception {
		if (randomMovePlayerDest == null) { randomMovePlayerDest = leftCorner; }
		MapLocation my_loc = rc.getLocation();
		if (rc.isActive() && !rc.isConstructing()) {
			boolean could_move = false;
			if (my_loc.isAdjacentTo(randomMovePlayerDest)) {
				could_move = moveTowardsLocationDirect(rc, randomMovePlayerDest, false);
			} else {
				could_move = moveTowardsLocationSimple(rc, randomMovePlayerDest, false);
			}
			if (could_move) {
				return;
			} else {
				GameObject [] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 3, rc.getTeam());
				if (nearbyRobots.length == 0) {
					rc.construct(RobotType.PASTR);
				} else if (randomMovePlayerDest.equals(myCorner)) {
					randomMovePlayerDest = leftCorner;
				} else if (randomMovePlayerDest.equals(leftCorner)) {
					randomMovePlayerDest = enemyCorner;
				} else if (randomMovePlayerDest.equals(enemyCorner)) {
					randomMovePlayerDest = rightCorner;
				} else {
					randomMovePlayerDest = myCorner;
				}
			}
		}
	}

	public static boolean moveTowardsLocationDirect(RobotController rc, MapLocation loc, boolean sneak) throws Exception {
		MapLocation my_loc = rc.getLocation();
		Direction dir = my_loc.directionTo(loc);
		
		if (loc.equals(my_loc)) return false;
			
		if (rc.canMove(dir)) {
			if (sneak) rc.sneak(dir); else rc.move(dir);
			return true;
		}

		return false;
	}

	public static boolean moveTowardsLocationSimple(RobotController rc, MapLocation loc, boolean sneak) throws Exception {
		MapLocation my_loc = rc.getLocation();
		Direction dir = my_loc.directionTo(loc);
		
		if (loc.equals(my_loc)) return false;
		
		if (rc.canMove(dir)) {
			if (sneak) rc.sneak(dir); else rc.move(dir);
			return true;
		}
		
		Direction dir_left = dir.rotateLeft();
		Direction dir_right = dir.rotateRight();

		boolean can_move_left = rc.canMove(dir_left);
		boolean can_move_right = rc.canMove(dir_right);
		
		if (can_move_left && can_move_right) {
			MapLocation location_in_dir_left = my_loc.add(dir_left);
			MapLocation location_in_dir_right = my_loc.add(dir_right);
			
			int dir_dist_to_loc_via_left = location_in_dir_left.distanceSquaredTo(loc);
			int dir_dist_to_loc_via_right = location_in_dir_right.distanceSquaredTo(loc);
			
			if (dir_dist_to_loc_via_left < dir_dist_to_loc_via_right) {
				if (sneak) rc.sneak(dir_left); else rc.move(dir_left);
				return true;
			} else {
				if (sneak) rc.sneak(dir_right); else rc.move(dir_right);
				return true;
			}
		} else if (can_move_right) {
			if (sneak) rc.sneak(dir_right); else rc.move(dir_right);
			return true;
		} else if (can_move_left) {
			if (sneak) rc.sneak(dir_left); else rc.move(dir_left);
			return true;
		}
		return false;
	}

	public static boolean moveTowardsLocation(RobotController rc, MapLocation loc, boolean sneak) throws Exception {
		MapLocation my_loc = rc.getLocation();
		Direction dir = my_loc.directionTo(loc);

		if (loc.equals(my_loc)) return false;
		
		if (rc.canMove(dir)) {
			if (sneak) rc.sneak(dir); else rc.move(dir);
			return true;
		}

		Direction dir_left = dir.rotateLeft();
		Direction dir_right = dir.rotateRight();

		boolean can_move_left = rc.canMove(dir_left);
		boolean can_move_right = rc.canMove(dir_right);
		
		if (can_move_left && can_move_right) {
			MapLocation location_in_dir_left = my_loc.add(dir_left);
			MapLocation location_in_dir_right = my_loc.add(dir_right);
			
			int dir_dist_to_loc_via_left = location_in_dir_left.distanceSquaredTo(loc);
			int dir_dist_to_loc_via_right = location_in_dir_right.distanceSquaredTo(loc);
			
			if (dir_dist_to_loc_via_left < dir_dist_to_loc_via_right) {
				if (sneak) rc.sneak(dir_left); else rc.move(dir_left);
				return true;
			} else {
				if (sneak) rc.sneak(dir_right); else rc.move(dir_right);
				return true;
			}
		} else if (can_move_right) {
			if (sneak) rc.sneak(dir_right); else rc.move(dir_right);
			return true;
		} else if (can_move_left) {
			if (sneak) rc.sneak(dir_left); else rc.move(dir_left);
			return true;
		} else {
			Direction dir_left_left = dir_left.rotateLeft();
			Direction dir_right_right = dir_right.rotateRight();
			
			boolean can_move_left_left = rc.canMove(dir_left_left);
			boolean can_move_right_right = rc.canMove(dir_right_right);
			
			if (can_move_left_left && can_move_right_right) {
				MapLocation location_in_dir_left_left = my_loc.add(dir_left_left);
				MapLocation location_in_dir_right_right = my_loc.add(dir_right_right);
				
				int dir_dist_to_loc_via_left_left = location_in_dir_left_left.distanceSquaredTo(loc);
				int dir_dist_to_loc_via_right_right = location_in_dir_right_right.distanceSquaredTo(loc);
				
				if (dir_dist_to_loc_via_left_left < dir_dist_to_loc_via_right_right) {
					if (sneak) rc.sneak(dir_left_left); else rc.move(dir_left_left);
					return true;
				} else {
					if (sneak) rc.sneak(dir_right_right); else rc.move(dir_right_right);
					return true;
				}
			} else if (can_move_right_right) {
				if (sneak) rc.sneak(dir_right_right); else rc.move(dir_right_right);
				return true;
			} else if (can_move_left_left) {
				if (sneak) rc.sneak(dir_left_left); else rc.move(dir_left_left);
				return true;
			} else {
				
				// TODO : maybe move just somewhere, even if is is backwards
				
			}
		}
		return false;
	}

}


