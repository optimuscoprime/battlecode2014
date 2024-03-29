package emo;

import java.util.*;

import battlecode.common.*;
import static emo.Util.*;
import static battlecode.common.Direction.*;
import static battlecode.common.TerrainTile.*;

class CachedFloodedMap {
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
	
	public Double[][] floodedMap;
	public MapLocation fromLocation;
	public MapLocation toLocation;
	public PriorityQueue<MapLocation> toVisit;
	public boolean finishedCaching = false;
	private GameMap gameMap;
	
	public CachedFloodedMap(final GameMap gameMap, final int width, final int height, final MapLocation fromLocation, final MapLocation toLocation) {
		this.gameMap = gameMap;
		this.fromLocation = fromLocation;
		this.toLocation = toLocation;
		this.finishedCaching = false;
		this.floodedMap = new Double[width][height];
		
		// init
		for (int x=0; x < width; x++) {
			for (int y=0; y < height; y++) {
				floodedMap[x][y] = null;
			}
		}
		
		this.toVisit = new PriorityQueue<MapLocation>(100, new Comparator<MapLocation>() {
			public int compare(MapLocation o1, MapLocation o2) {
				// prefer locations closer to the to location
				return new Integer(toLocation.distanceSquaredTo(o1)).compareTo(toLocation.distanceSquaredTo(o2));
			}
		});
		
		floodedMap[toLocation.x][toLocation.y] = 0.0;
		
		toVisit.add(toLocation);
	}
	
	public void continueCaching() {
			
		log("still caching map");
		
		while (!finishedCaching && !toVisit.isEmpty()) {
			MapLocation currentLocation = toVisit.remove();
			
			if (currentLocation.equals(fromLocation)) {
				
				finishedCaching = true;
				break;
				
			} else {
				
				double thisScore = floodedMap[currentLocation.x][currentLocation.y];
				for (Direction direction: allDirections) {
					MapLocation newLocation = currentLocation.add(direction);								
					if (gameMap.isTraversable(newLocation.x, newLocation.y) && floodedMap[newLocation.x][newLocation.y] == null) {
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
		
		if (toVisit.isEmpty()) {
			finishedCaching = true;
		}
		
		if (finishedCaching) {
			log("finished caching map");
		}
	
	}
//	
//	private void printFloodedMap() {
//		for (int x=0; x < width; x++) {
//			for (int y =0; y < height; y++) {
//				if (floodedMap[x][y] == null) {
//					System.out.printf("      ");
//				} else {
//					System.out.printf("%02.1f  ", floodedMap[x][y]);
//				}
//			}
//			System.out.printf("\n");
//		}
//	}	
}


public class GameMap {
	private RobotController rc;
	
	public TerrainTile[][] map;
	public int width;
	public int height;

	// <to,map>
	public Map<MapLocation, CachedFloodedMap> cachedFloodedMaps = new HashMap<MapLocation, CachedFloodedMap>();
	
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

	public Direction nextDirectionTo(final MapLocation fromLocation, final MapLocation toLocation) {
		
		//if (fromLocation.distanceSquaredTo(toLocation) > 500) {
			// no point calculating a map
			// just try moving
		//	return fromLocation.directionTo(toLocation);
		//}
		
		if (fromLocation == toLocation) {
			return Direction.NONE;
		}
		
		// check if we have a cached map
		
		CachedFloodedMap cachedFloodedMap = cachedFloodedMaps.get(toLocation);
		if (cachedFloodedMap == null) {
			cachedFloodedMap = new CachedFloodedMap(this, height, height, fromLocation, toLocation);
			cachedFloodedMaps.put(toLocation,  cachedFloodedMap);
		}

		// check if from location is in the cached map
		// if not, need to keep flooding
		if (cachedFloodedMap.floodedMap[fromLocation.x][fromLocation.y] == null) {
			cachedFloodedMap.finishedCaching = false;
		}
		
		if (!cachedFloodedMap.finishedCaching) {
			cachedFloodedMap.continueCaching();
		}
		
		Direction nextDirection = null;
		
		if (cachedFloodedMap.finishedCaching) {
			double lowestScore = Integer.MAX_VALUE;
			
			for (Direction direction: allDirections) {
				MapLocation testLocation = fromLocation.add(direction);
				if (isTraversable(testLocation.x, testLocation.y)) {
					Double thisScore = cachedFloodedMap.floodedMap[testLocation.x][testLocation.y];
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
