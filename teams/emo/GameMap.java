package emo;

import java.util.*;

import battlecode.common.*;
import static emo.Util.*;
import static battlecode.common.Direction.*;
import static battlecode.common.TerrainTile.*;

public class GameMap {
	private RobotController rc;
	
	public TerrainTile[][] map;
	public Double[][] floodedMap = null;
	public MapLocation cachedToLocation = null;
	public int width;
	public int height;
	private Deque<MapLocation> toVisit = null;

	private boolean finishedCaching = false;

	private boolean reachedFromLocation = false;
	
    public static Direction[] allDirections = new Direction[] {
    	// prefer diagonal directions
    	NORTH_EAST,
    	NORTH_WEST,
    	SOUTH_WEST,
    	SOUTH_EAST,
        EAST,
        NORTH,
        WEST,
        SOUTH,
    };	
	
	public GameMap(int robotId, Team team, RobotType robotType, RobotController rc) {
		this.rc = rc;
		
		this.width = rc.getMapWidth();
		this.height = rc.getMapHeight();

		// sense terrain tiles, build a map
		
		this.map = new TerrainTile[width][height];
		
		//log("Started sensing terrain tiles...");
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {	
				map[x][y] = rc.senseTerrainTile(new MapLocation(x,y));
			}
		}
		
		//log("Finished sensing terrain tiles.");
		
		//printMap(map);
	}
	
	private void printMap(TerrainTile[][] map) {
		//log("Started printing map...");
		
		int width = map.length;
		int height = map[0].length;
		
		System.out.printf("==================================================================\n");
		System.out.printf("Printing %d by %d map", width, height);
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				char mapCharacter;
				switch(map[x][y]) {
					case VOID:
						mapCharacter = 'V';
						break;
					case ROAD:
						mapCharacter = 'R';
						break;
					default:
						mapCharacter = ' ';
						break;
				}
				System.out.printf("%c", mapCharacter);
			}
			System.out.printf("\n");
		}
		System.out.printf("==================================================================\n");
		System.out.printf("\n");
		
		//log("Finished printing map.");

	}
	
	public boolean isTraversable(int x, int y) {
		return (x >= 0 && 
				x < width &&
				y >= 0 &&
				y < height &&
				map[x][y] != VOID);
	}

	public Direction nextDirectionTo(MapLocation fromLocation, MapLocation toLocation) {
		
		if (fromLocation.distanceSquaredTo(toLocation) > 2000) {
			// no point calculating a map
			// just try moving
			return fromLocation.directionTo(toLocation);
		}
		
		if (fromLocation == toLocation) {
			return Direction.NONE;
		}
		
		if (!toLocation.equals(cachedToLocation)) {
			// do the flood fill

			floodedMap = new Double[width][height];
			
			// init
			for (int x=0; x < width; x++) {
				for (int y=0; y < height; y++) {
					floodedMap[x][y] = null;
				}
			}
			
			//int expectedFlooded = width*height;
			
			floodedMap[toLocation.x][toLocation.y] = 0.0;
			
			toVisit = new ArrayDeque<MapLocation>();
			toVisit.add(toLocation);
			
			cachedToLocation = toLocation;
			finishedCaching = false;
		}
		
		// check if from location is in the cached map
		// if not, need to keep flooding
		if (floodedMap[fromLocation.x][fromLocation.y] == null) {
			finishedCaching = false;
			reachedFromLocation = false;
		}
		
		if (!finishedCaching) {
			
			log("still caching map");
			
			while (!reachedFromLocation && !toVisit.isEmpty()) {
				MapLocation currentLocation = toVisit.remove();
				if (currentLocation.equals(fromLocation)) {
					reachedFromLocation = true;
					break;
				} else {
					double thisScore = floodedMap[currentLocation.x][currentLocation.y];
					for (Direction direction: allDirections) {
						MapLocation newLocation = currentLocation.add(direction);								
						if (isTraversable(newLocation.x, newLocation.y) && floodedMap[newLocation.x][newLocation.y] == null) {
							floodedMap[newLocation.x][newLocation.y] = thisScore + 1; //getTileScore(newLocation, direction);
							toVisit.add(newLocation);
						}
					}				
					if (Clock.getBytecodesLeft() < 1000) {
						//log("Used too many bytecodes");
						break;
					}					
				}
			}
			
			if (reachedFromLocation || toVisit.isEmpty()) {
				//log("finished caching");
				finishedCaching = true;
				log("finished caching map");
				
				// print it and then die
				
				//printFloodedMap();
				//rc.breakpoint();
			}
		}
		
		Direction nextDirection = null;
		
		if (finishedCaching) {
			double lowestScore = Integer.MAX_VALUE;
			
			for (Direction direction: allDirections) {
				MapLocation testLocation = fromLocation.add(direction);
				if (isTraversable(testLocation.x, testLocation.y)) {
					Double thisScore = floodedMap[testLocation.x][testLocation.y];
					if (thisScore != null) {
						if (direction.isDiagonal()) {
							thisScore += 0.3; // discourage diagonal directions unless they save time
						}
						if (thisScore < lowestScore) {
							lowestScore = thisScore;
							nextDirection = direction;
						}
					}
				}
			}		
		}

		return nextDirection;
	}

	private void printFloodedMap() {
		for (int x=0; x < width; x++) {
			for (int y =0; y < height; y++) {
				if (floodedMap[x][y] == null) {
					System.out.printf("      ");
				} else {
					System.out.printf("%02.1f  ", floodedMap[x][y]);
				}
			}
			System.out.printf("\n");
		}
	}

	// lower is better
	private Double getTileScore(MapLocation location, Direction direction) {
		Double tileScore = null;
		
		TerrainTile tile = map[location.x][location.y];
		if (tile == ROAD) {
			tileScore = 0.5;
		} else if (tile == NORMAL) {
			tileScore = 1.0;
		}
				
		if (tileScore != null) {
			if (direction.isDiagonal()) {
				tileScore = tileScore * 1.4; 
			}
		}
		
		return tileScore;
	}
}
