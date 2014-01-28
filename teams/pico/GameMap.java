package pico;

import java.util.*;

import battlecode.common.*;
import static pico.Util.*;
import static battlecode.common.Direction.*;
import static battlecode.common.TerrainTile.*;

public class GameMap {
	private RobotController rc;
	
	public TerrainTile[][] map;
	public int width;
	public int height;
	
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
		
		log("Started sensing terrain tiles...");
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {	
				map[x][y] = rc.senseTerrainTile(new MapLocation(x,y));
			}
		}
		
		log("Finished sensing terrain tiles.");
		
		printMap(map);
	}
	
	private void printMap(TerrainTile[][] map) {
		log("Started printing map...");
		
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
		
		log("Finished printing map.");

	}
	
	public boolean isTraversable(int x, int y) {
		return (x >= 0 && 
				x < width &&
				y >= 0 &&
				y < height &&
				map[x][y] != VOID);
	}
}
