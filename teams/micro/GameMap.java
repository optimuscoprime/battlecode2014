package micro;

import java.util.*;

import battlecode.common.*;
import static micro.Util.*;
import static battlecode.common.Direction.*;
import static battlecode.common.TerrainTile.*;

public class GameMap {
	private RobotController rc;
	
	public TerrainTile[][] map1x1;
	public TerrainTile[][] map2x2;
	public TerrainTile[][] map4x4;
	
    public static Direction[] allDirections = new Direction[] {
        EAST,
        NORTH_EAST,
        NORTH,
        NORTH_WEST,
        WEST,
        SOUTH_WEST,
        SOUTH,
        SOUTH_EAST
    };	
	
	public GameMap(Robot robot, RobotType robotType, RobotController rc) {
		this.rc = rc;
		
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
		die();
	}
	
	private TerrainTile[][] coarsenMap(TerrainTile[][] originalMap) {
		int originalWidth = originalMap[0].length;
		int originalHeight = originalMap.length;
		
		int newWidth = originalWidth / 2;
		int newHeight = originalHeight / 2;
		
		TerrainTile[][] newMap = new TerrainTile[newWidth][newHeight];
				
		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				int oldX = x*2;
				int oldY = y*2;
				if (originalMap[oldX][oldY] == VOID || 
					originalMap[oldX+1][oldY] == VOID ||
					originalMap[oldX+1][oldY+1] == VOID ||
					originalMap[oldX][oldY+1] == VOID) {
					newMap[x][y] = VOID;
				} else if (originalMap[oldX][oldY] == ROAD || 
					originalMap[oldX+1][oldY] == ROAD ||
					originalMap[oldX+1][oldY+1] == ROAD ||
					originalMap[oldX][oldY+1] == ROAD) {
					newMap[x][y] = ROAD;
				} else {
					newMap[x][y] = NORMAL;
				}
			}
		}
		
		return newMap;
	}
	
	private void printMap(TerrainTile[][] map) {
		int width = map[0].length;
		int height = map.length;
		
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
					mapCharacter = ' ';
				}
				System.out.printf("%c", mapCharacter);
			}
			System.out.printf("\n");
		}
		System.out.printf("==================================================================\n");
		System.out.printf("\n");

	}
	
	public boolean isTraversable(TerrainTile[][] map, int x, int y) {
		int width = map[0].length;
		int height = map.length;
		return (x >= 0 && x < width && y >= 0 && y < height && map[x][y] != VOID);
	}
	
	// idea: there is some symmetry in the map
	// don't need to explore all paths
	
	public Deque<MapLocation> generatePath(MapLocation from, MapLocation to) {
		
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
			path2x2 = refinePath(path4x4, from2x2, to2x2);
			if (path2x2 != null) {
				path1x1 = refinePath(path2x2, from1x1, to1x1);
			}
		}
			
		// couldn't go from 4x4 to 2x2 to 1x1
		// then try 2x2 to 1x1 instead
		if (path1x1 == null) {
			path2x2 = generatePath(map2x2, from2x2, to2x2);
			if (path2x2 != null) {
				path1x1 = refinePath(path2x2, from1x1, to1x1);			
			} 
		}
		
		// couldn't go from 2x2 to 1x1
		// then try 1x1 directly
		if (path1x1 == null) {
			path1x1 = generatePath(map1x1, from1x1, to1x1);
		}
		
		if (path1x1 == null) {
			die("No path found");
		}
		
		return path1x1;
	}

	private Deque<MapLocation> generatePath(TerrainTile[][] map, MapLocation from, MapLocation to) {
		//Deque<MapLocation> path = new ArrayDeque<MapLocation>();
		
		return null;
	}	

	private Deque<MapLocation> refinePath(Deque<MapLocation> path, MapLocation from, MapLocation to) {
		
		return null;
	}

	private MapLocation coarsenPoint(MapLocation from1x1) {
		
		return null;
	}


}
