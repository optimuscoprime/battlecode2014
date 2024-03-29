package brute01;

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
import battlecode.common.GameConstants;
import brute01.Comms.Message;

public class Navigation {
	private static void cleverMove(RobotController rc, Direction d)throws GameActionException{
		MapLocation current=rc.getLocation();
		MapLocation next=current.add(d);
		int pastrChannel=2;
		Message m = Comms.ReadMessage(rc,pastrChannel);
		if(m==null){
			rc.move(d);
		}else{
			MapLocation pastrLoc=m.loc;
			if( (new Integer(pastrLoc.distanceSquaredTo(current)).compareTo( pastrLoc.distanceSquaredTo(next))) > 0){
				rc.move(d);
			}else{
				rc.sneak(d); // we should probably only bother if there's actually cows.
			}
		}
	}
	/**
	 * walks one square in a given direction, or picks a random direction
	 */
	public static Direction wonder(RobotController rc, Random rand, Direction d) throws GameActionException {
		MapLocation enemyHQ=rc.senseEnemyHQLocation();
		Direction danger=rc.getLocation().directionTo(enemyHQ);
		if((d==danger) && (rc.getLocation().distanceSquaredTo(enemyHQ)<= rc.getType().attackRadiusMaxSquared )){
			d=d.opposite();
		}
		if (d != null && rc.canMove(d)) {
			rc.move(d);
			return d;
		} else {
			int i = rand.nextInt(DIRECTIONS.length);
			for (int j = (DIRECTIONS.length - i); --j >= 0;) {
				d = DIRECTIONS[j];
				if (rc.canMove(d)) {
					//rc.move(d);
					cleverMove(rc,d);
					return d;
				}
			}
			for (int j=i; --j >= 0;) {
				d = DIRECTIONS[j];
				if (rc.canMove(d)) {
					//rc.move(d);
					cleverMove(rc,d);
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
						//rc.move(top.direction);
						cleverMove(rc,top.direction);
					} else if(rc.canMove(top.direction.rotateLeft())){
						cleverMove(rc,top.direction.rotateLeft());
					}else {
						return false;
					}
				}
			}
			rc.yield();
		}
		return false;
	}
	public static boolean moveOnPath(RobotController rc, Deque<Move> path) throws GameActionException {
		while (!path.isEmpty()) {
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
						float delta = (d.isDiagonal()) ? (float) GameConstants.SOLDIER_DIAGONAL_MOVEMENT_ACTION_DELAY_FACTOR : 1;
						//ROAD_ACTION_DELAY_FACTOR
						if ( tile ==TerrainTile.ROAD ){
							delta= (float) GameConstants.ROAD_ACTION_DELAY_FACTOR * delta;
						} 
							
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
