 package sc01;

 import battlecode.common.*;
 import java.util.*;

 public class RobotPlayer {

	 // save things that will never change
	 //  (unless looking up any of these things coses zero bytecodes)
	 public static int mapWidth;
	 public static int mapHeight;
	 public static MapLocation my_hq = null;
	 public static MapLocation enemy_hq = null;
	 public static MapLocation leftCorner = null;
	 public static MapLocation rightCorner = null;
	 public static MapLocation myCorner = null;
	 public static MapLocation enemyCorner = null;

	 public static void run(RobotController rc) {

		 int birthday = Clock.getRoundNum();
		 getBearings(rc);

		 while (true) {
			 try {
				 if (rc.getType() == RobotType.HQ) {
					 HQPlayer(rc, birthday);
				 } else if (rc.getType() == RobotType.SOLDIER) {
					 BuilderPlayer(rc, birthday);
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

	 public static void BuilderPlayer(RobotController rc, int birthday) throws Exception {
		 if (rc.isActive() && !rc.isConstructing()) {
			 MapLocation my_loc = rc.getLocation();
			 int round_num = Clock.getRoundNum();

			 if (my_loc.distanceSquaredTo(enemy_hq) < 3) {
				 rc.construct(RobotType.PASTR);
				 return;
			 } else if ((my_loc.x % 4) + (my_loc.y % 4) == 0) {
				 rc.construct(RobotType.PASTR);
				 return;
			 } else {
				 // TODO : make relative to GameConstants
				 boolean goHome = false;
				 if ((round_num / 400) % 2 == 0) {
					 goHome = false;
				 }
				 boolean loud = false;

				 // really want the guys to go out for a while
				 // then at a point not too late, storm home together
				 // then take 4 more sneak steps and build nicely spaced PASTRs
				 MapLocation dest;
				 int fad = (round_num - birthday) % 150;
				 if (fad < 30) {
					 dest = leftCorner;
				 } else if (fad < 60) {
					 dest = enemyCorner;
				 } else if (fad < 90) {
					 dest = rightCorner;
				 } else if (fad < 120) {
					 dest = enemyCorner;
				 } else {
					 dest = my_hq;
					 loud = true;
				 }
				
				moveTowardsLocation(rc, dest, !loud);
				return;
			}
		}
	}	

	public static void RunHomePlayer(RobotController rc) throws Exception {
		if (rc.isActive()) {
			
		}
	}

	public static void moveTowardsLocation(RobotController rc, MapLocation loc, boolean sneak) throws Exception {
		MapLocation my_loc = rc.getLocation();
		Direction dir = my_loc.directionTo(loc);
		
		if (rc.canMove(dir)) {
			if (sneak) rc.sneak(dir); else rc.move(dir);
			return;
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
				return;
			} else {
				if (sneak) rc.sneak(dir_right); else rc.move(dir_right);
				return;
			}
		} else if (can_move_right) {
			if (sneak) rc.sneak(dir_right); else rc.move(dir_right);
			return;
		} else if (can_move_left) {
			if (sneak) rc.sneak(dir_left); else rc.move(dir_left);
			return;
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
					return;
				} else {
					if (sneak) rc.sneak(dir_right_right); else rc.move(dir_right_right);
					return;
				}
			} else if (can_move_right_right) {
				if (sneak) rc.sneak(dir_right_right); else rc.move(dir_right_right);
				return;
			} else if (can_move_left_left) {
				if (sneak) rc.sneak(dir_left_left); else rc.move(dir_left_left);
				return;
			} else {
				
				// TODO : maybe move just somewhere, even if is is backwards
				
			}
		}

	}

}


