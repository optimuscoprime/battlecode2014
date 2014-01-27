package micro;

import java.util.*;

import battlecode.common.*;
import static micro.Util.*;
import static battlecode.common.Direction.*;
import static battlecode.common.TerrainTile.*;

public class GameMap {
	private RobotController rc;
	private Robot robot;
	private RobotType robotType;
	
	public TerrainTile[][] map1x1;
	public TerrainTile[][] map2x2;
	public TerrainTile[][] map4x4;
	
	// prefer diagonals
    public static Direction[] allDirections = new Direction[] {
    	NORTH_EAST,
    	NORTH_WEST,
    	SOUTH_WEST,
    	SOUTH_EAST,
        EAST,
        NORTH,
        WEST,
        SOUTH,
    };	
	
	public GameMap(Robot robot, RobotType robotType, RobotController rc) {
		this.rc = rc;
		this.robot = robot;
		this.robotType = robotType;
		
		int width = rc.getMapWidth();
		int height = rc.getMapHeight();

		// sense terrain tiles, build a map
		
		this.map1x1 = new TerrainTile[width][height];
		
		log(robot, robotType, "Sensing terrain tiles...");
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {	
				map1x1[x][y] = rc.senseTerrainTile(new MapLocation(x,y));
			}
		}
		
		log(robot, robotType, "Finished sensing terrain tiles");
		
		this.map2x2 = coarsenMap(map1x1);
		this.map4x4 = coarsenMap(map2x2);
		
		printMap(map1x1);
		printMap(map2x2);
		printMap(map4x4);
		//die();
		
		log(robot, robotType, "generated maps");
	}
	
	private TerrainTile[][] coarsenMap(TerrainTile[][] originalMap) {
		int originalWidth = originalMap.length;
		int originalHeight = originalMap[0].length;
		
		int newWidth = originalWidth / 2;
		int newHeight = originalHeight / 2;
		
		TerrainTile[][] newMap = new TerrainTile[newWidth][newHeight];
				
		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				
				int oldX = x*2;
				if (oldX >= originalWidth) {
					oldX = originalWidth - 1;
				}
				
				int oldY = y*2;
				if (oldY >= originalHeight) {
					oldY = originalHeight - 1;
				}
				
				if (originalMap[oldX][oldY] == VOID || 
					(oldX + 1 < originalWidth && originalMap[oldX+1][oldY] == VOID) ||
					(oldX + 1 < originalWidth && oldY + 1 < originalHeight && originalMap[oldX+1][oldY+1] == VOID) ||
					(oldY + 1 < originalHeight && originalMap[oldX][oldY+1] == VOID) ) {
					newMap[x][y] = VOID;
				} else if (originalMap[oldX][oldY] == ROAD || 
					(oldX + 1 < originalWidth && originalMap[oldX+1][oldY] == ROAD) ||
					(oldX + 1 < originalWidth && oldY + 1 < originalHeight && originalMap[oldX+1][oldY+1] == ROAD) ||
					(oldY + 1 < originalHeight && originalMap[oldX][oldY+1] == ROAD) ) {
					newMap[x][y] = ROAD;
				} else {
					newMap[x][y] = NORMAL;
				}
			}
		}
		
		return newMap;
	}
	
	private void printMap(TerrainTile[][] map) {
		int width = map.length;
		int height = map[0].length;
		
		System.out.printf("==================================================================\n");
		System.out.printf("Printing %d by %d map", width, height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				char mapCharacter;
				if (map[x][y] == VOID) {
					mapCharacter = 'V';
				} else if (map[x][y] == ROAD) {
					mapCharacter = 'R';
				} else {
					mapCharacter = '.';
				}
				System.out.printf("%c", mapCharacter);
			}
			System.out.printf("\n");
		}
		System.out.printf("==================================================================\n");
		System.out.printf("\n");

	}
	
	public boolean isTraversable(TerrainTile[][] map, int x, int y) {
		int width = map.length;
		int height = map[0].length;
		return (x >= 0 && x < width && y >= 0 && y < height && map[x][y] != VOID);
	}
	
	// idea: there is some symmetry in the map
	// don't need to explore all paths
	
	public Deque<MapLocation> generatePath(MapLocation from, MapLocation to) {
		
		log(robot, robotType, "start generating path");
		
		MapLocation from1x1 = from;
		MapLocation from2x2 = coarsenPoint(from1x1);
		MapLocation from4x4 = coarsenPoint(from2x2);
					
		MapLocation to1x1 = to;
		MapLocation to2x2 = coarsenPoint(to1x1);
		MapLocation to4x4 = coarsenPoint(to2x2);
		
		Deque<MapLocation> path1x1 = null;				
		Deque<MapLocation> path2x2 = null;
		Deque<MapLocation> path4x4 = null;
		
		path4x4 = generatePath(map4x4, from4x4, to4x4); 
		if (path4x4 != null) {
			log(robot, robotType, "4x4 ok");

			path2x2 = refinePath(path4x4, map2x2, from2x2, to2x2);
			
			log(robot, robotType, "4x4 to 2x2 refined");

			if (path2x2 != null) {
				path1x1 = refinePath(path2x2, map1x1, from1x1, to1x1);
				
				log(robot, robotType, "2x2 to 1x1 refined");

			}
		}
		
		// couldn't go from 4x4 to 2x2 to 1x1
		// then try 2x2 to 1x1 instead
		if (path1x1 == null) {
			log(robot, robotType, "4x4 failed");

			
			path2x2 = generatePath(map2x2, from2x2, to2x2);
			if (path2x2 != null) {
				path1x1 = refinePath(path2x2, map1x1, from1x1, to1x1);			
			} 
		}
		
		// couldn't go from 2x2 to 1x1
		// then try 1x1 directly
		if (path1x1 == null) {
			log(robot, robotType, "2x2 failed");

			path1x1 = generatePath(map1x1, from1x1, to1x1);
			
			if (path1x1 != null) {
				log(robot, robotType, "1x1 ok");
			} else {
				log(robot, robotType, "1x1 failed");
			}
		}
		
		if (path1x1 == null) {
			//die("No path found");
		}
		
		log(robot, robotType, "finish generating path");
		
		return path1x1;
	}

	private Deque<MapLocation> generatePath(TerrainTile[][] map, MapLocation from, MapLocation to) {
		
		Deque<MapLocation> path = new ArrayDeque<MapLocation>();
		
		Set<MapLocation> explored = new HashSet<MapLocation>();
		
		Deque<MapLocation> queue = new ArrayDeque<MapLocation>();
		queue.add(from);
		
		Map<MapLocation, MapLocation> parent = new HashMap<MapLocation, MapLocation>();
		
		boolean found = false;
		
		while (!found && !queue.isEmpty()) {
			MapLocation current = queue.remove();
			if (current.equals(to)) {
				// done
				found = true;
			} else {
				// add neighbours as long as they haven't been explored yet
				for (Direction direction: allDirections) {
					MapLocation child = current.add(direction);
					if (!explored.contains(child) && isTraversable(map, child.x, child.y)) {
						parent.put(child, current);
						explored.add(child);
						queue.add(child);
					}
				}
			}
		}
		
		// make path
		if (found) {
			// make path (follow parents)
			MapLocation current = to;
			while (!current.equals(from)) {
				path.addFirst(current);
				current = parent.get(current);
			}
		} else {
			path = null;
		}
		
		return path;
	}	

	private Deque<MapLocation> refinePath(Deque<MapLocation> path, TerrainTile[][] refinedMap, MapLocation refinedFrom, MapLocation refinedTo) {
		
		Deque<MapLocation> refinedPath = new ArrayDeque<MapLocation>();
		
		MapLocation previous = refinedFrom;
		
		while(!path.isEmpty()) {
			MapLocation current = path.remove();
			
			MapLocation refinedCurrent = new MapLocation(current.x*2, current.y*2);
			
			// special case, go to exactly the place we want
			if (path.isEmpty()) {
				refinedCurrent = refinedTo;
			}
			
			Deque<MapLocation> refinedSubPath = generatePath(refinedMap, previous, refinedCurrent);
			while (!refinedSubPath.isEmpty()) {
				MapLocation refinedSubLocation = refinedSubPath.remove();
				refinedPath.add(refinedSubLocation);
				previous = refinedSubLocation;
			}
		}
			
		return refinedPath;
	}

	private MapLocation coarsenPoint(MapLocation from) {
		return new MapLocation(from.x/2, from.y/2);
	}


}
