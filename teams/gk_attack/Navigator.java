package gk_attack;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class Navigator {

	//move to a location, will give up on first obstacle 
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
	
	//calculate a path to a location, if the location is unreachable, will return a nearby destination
	//note: can be very expensive
	public static Deque<Move> pathAStar(RobotController rc, MapLocation dest) throws GameActionException {
		Deque<Move> path = new LinkedList<Move>();
		TerrainTile destTile = rc.senseTerrainTile(dest);
		int cost = 0;
		if (destTile.isTraversableAtHeight(RobotLevel.ON_GROUND)) {
			Map<MapLocation, Move> been = new HashMap<MapLocation, Move>();
			
			PriorityQueue<Move> next = new PriorityQueue<Move>();
			Map<MapLocation, Move> locs = new HashMap<MapLocation, Navigator.Move>();
			
			MapLocation loc = rc.getLocation();
			next.add(Move.Create(loc, 0, manhattanDistance(loc, dest), null, null));
			Move last = null;
			while(!been.containsKey(dest) && !next.isEmpty()) {
				cost++;
				Move top = next.remove();
				last = top;
				been.put(top.loc, top);
				for (Direction d : DIRECTIONS) {
					MapLocation n = top.loc.add(d);
					TerrainTile tile = rc.senseTerrainTile(n);
					if (tile.isTraversableAtHeight(RobotLevel.ON_GROUND)) {
						Move existing = locs.get(n);
						Move move = new Move(n, top.distance + 1, top.distance + 1 + manhattanDistance(n, dest), top, d);
						if (existing == null || move.cost < existing.cost) {
							if (existing != null) {
								next.remove(existing);
							}
							locs.put(n, move);
							next.add(move);
						}
					}
				}
				for (Direction d : DIRECTIONS_DIAGONAL) {
					MapLocation n = top.loc.add(d);
					TerrainTile tile = rc.senseTerrainTile(n);
					if (tile.isTraversableAtHeight(RobotLevel.ON_GROUND)) {
						Move existing = locs.get(n);
						Move move = new Move(n, top.distance + 1.4, top.distance + 1.4 + manhattanDistance(n, dest), top, d);
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
//			System.out.println("Path Cost:" + Integer.toString(cost));
		}
		return path;
	}
	
	public static class Move implements Comparable<Move> {
		public MapLocation loc;
		public double distance;
		public double cost;
		public Move prev;
		public Direction direction;
		
		public Move(MapLocation loc, double distance, double cost, Move prev, Direction d) {
			this.loc = loc;
			this.distance = distance;
			this.cost = cost;
			this.prev = prev;
			this.direction = d;
		}
		
		public static Move Create(MapLocation loc, double distance, double cost, Move prev, Direction d) {
			return new Move(loc, distance, cost, prev, d);
		}

		@Override
		public int compareTo(Move o) {
			double d = this.cost - o.cost;
			return (d > 0) ? 1 : (d < 0) ? -1 : 0;
		}
	}
	
	public static Direction[] DIRECTIONS = new Direction[]{
		Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
	};
	public static Direction[] DIRECTIONS_DIAGONAL = new Direction[]{
		Direction.NORTH_EAST, Direction.NORTH_WEST, Direction.SOUTH_EAST, Direction.SOUTH_WEST
	};

	public static int manhattanDistance(MapLocation a, MapLocation b) {
		return Math.abs(b.x - a.x) + Math.abs(b.y - a.y);
	}
}
