package gk_roman;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.Team;
import battlecode.common.TerrainTile;

public class Navigation {
	
	/**
	 * walks one square in a given direction, or picks a random direction
	 */
	public static Direction wonder(RobotController rc, Random rand, Direction d) throws GameActionException {
		if (d != null && rc.canMove(d)) {
			rc.move(d);
			return d;
		} else {
			int i = rand.nextInt(DIRECTIONS.length);
			for (int j = i; j < DIRECTIONS.length; j++) {
				d = DIRECTIONS[j];
				if (rc.canMove(d)) {
					rc.move(d);
					return d;
				}
			}
			for (int j = 0; j < i; j++) {
				d = DIRECTIONS[j];
				if (rc.canMove(d)) {
					rc.move(d);
					return d;
				}				
			}
		}
		return null;
	}
	
	/**
	 * moves along a path, until the path is blocked
	 * will stop to attack an enemy if encountered
	 */
	public static boolean attackMoveOnPath(RobotController rc, Deque<Move> path, int attackRadius, Team enemyTeam) throws GameActionException {
		while (!path.isEmpty()) {
			if (rc.isActive()) {
				RobotInfo enemy = Abilities.NearbyEnemy(rc, rc.getLocation(), attackRadius, enemyTeam);
				if (enemy != null && rc.canAttackSquare(enemy.location)) {
					rc.attackSquare(enemy.location);
					return true;
				}
			}
			if (rc.isActive()) {
				Move top = path.removeLast();
				if (top.direction != null) {
					if (rc.canMove(top.direction)) {
						rc.move(top.direction);
					} else {
						return false;
					}
				}
			}
			rc.yield();
		}
		return false;
	}
	
	public static final int[] AROUND = new int[]{0,1,-1,2,-2,3,-3};
	public static final int DS = Direction.values().length;
	public static boolean stepToward(RobotController rc, Direction d) throws GameActionException {
		for (int i : AROUND) {
			Direction dc = Direction.values()[(d.ordinal() + i + DS) % DS]; 
			if (rc.canMove(dc)) {
				rc.move(dc);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * move to a location, will give up on first obstacle, or after max steps
	 */
	public static void moveGreedy(RobotController rc, MapLocation dest, int max) throws GameActionException {
		MapLocation loc = rc.getLocation();
		while (max > 0 && !loc.equals(dest)) {
			Direction d = loc.directionTo(dest);
			if (rc.isActive()) {
				if (rc.canMove(d)) {
					rc.move(d);
				} else {
					break;
				}
			}
			max--;
			rc.yield();
		}
	}
	
 /**
  * calculate a path to a location
  *
  * note: can be very expensive
  */
	public static Deque<Move> pathAStar(RobotController rc, MapLocation dest) throws GameActionException {
		Deque<Move> path = new LinkedList<Move>();
		TerrainTile destTile = rc.senseTerrainTile(dest);
		if (destTile.isTraversableAtHeight(RobotLevel.ON_GROUND)) {
			Map<MapLocation, Move> been = new HashMap<MapLocation, Move>();
			
			PriorityQueue<Move> next = new PriorityQueue<Move>();
			Map<MapLocation, Move> locs = new HashMap<MapLocation, Move>();
			
			MapLocation loc = rc.getLocation();
			Move first = Move.Create(loc, 0, manhattanDistance(loc, dest), null, null);
			next.add(first);
			while(!been.containsKey(dest) && !next.isEmpty()) {
				Move top = next.remove();
				been.put(top.loc, top);
				for (Direction d : DIRECTIONS) {
					MapLocation n = top.loc.add(d);
					TerrainTile tile = rc.senseTerrainTile(n);
					if (tile.isTraversableAtHeight(RobotLevel.ON_GROUND)) {
						Move existing = locs.get(n);
						float delta = (d.isDiagonal()) ? 1.4f : 1;
						float distance = top.distance + delta;
						Move move = new Move(n, distance, distance + manhattanDistance(n, dest), top, d);
						if (existing == null || move.cost < existing.cost) {
							if (existing != null) {
								next.remove(existing);
							}
							locs.put(n, move);
							next.add(move);
						}
					}
				}
			}
			Move end = been.get(dest);
			while (end != null && end.direction != null) {
				path.addLast(end);
				end = end.prev;
			}
		}
		return path;
	}
	
	public static class Move implements Comparable<Move> {
		public MapLocation loc;
		public float distance;
		public float cost;
		public Move prev;
		public Direction direction;
		
		public Move(MapLocation loc, float distance, float cost, Move prev, Direction d) {
			this.loc = loc;
			this.distance = distance;
			this.cost = cost;
			this.prev = prev;
			this.direction = d;
		}
		
		public static Move Create(MapLocation loc, float distance, float cost, Move prev, Direction d) {
			return new Move(loc, distance, cost, prev, d);
		}

		@Override
		public int compareTo(Move o) {
			double d = this.cost - o.cost;
			return (d > 0) ? 1 : (d < 0) ? -1 : 0;
		}
	}
	
	public static Direction[] DIRECTIONS = new Direction[]{
		Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
		Direction.NORTH_EAST, Direction.NORTH_WEST, Direction.SOUTH_EAST, Direction.SOUTH_WEST
	};
	
	public static int manhattanDistance(MapLocation a, MapLocation b) {
		return Math.abs(b.x - a.x) + Math.abs(b.y - a.y);
	}
}
