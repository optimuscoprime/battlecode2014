package emo;

import static battlecode.common.RobotType.SOLDIER;

import java.util.*;

import battlecode.common.*;

public class Util {
	
	public static int HUGE_RADIUS = Integer.MAX_VALUE;
	
	public static int RADIO_CHANNEL_REQUEST_NOISETOWER = 10;

	//public static int RADIO_CHANNEL_BUILDING_NOISETOWER = 11;

	public static int RADIO_CHANNEL_REQUEST_PASTR = 20;

	//public static int RADIO_CHANNEL_BUILDING_PASTR = 21;
	
	public static int RADIO_CHANNEL_WAYPOINT = 30;

	
	public static Random random;
	private static RobotType robotType;
	private static int robotId;
	
	public static void init(int robotId, RobotType robotType) {
	    random = new Random(robotId);
	    Util.robotId = robotId;
	    Util.robotType = robotType;
	}
	
	// Implementing Fisher–Yates shuffle (http://stackoverflow.com/questions/1519736/random-shuffling-of-an-array)
	public static <E> void shuffle (E[] items) {
		int n = items.length;
		
		for (int i = n-1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			// Simple swap
			E tmp = items[index];
			items[index] = items[i];
			items[i] = tmp;
		}
	}

	// insertion sort using a comparator
	// fast for small array
	public static <E> void sort(E[] items, Comparator<E> c) {
		int n = items.length;
		
		for (int i=1; i < n; i++) {
			for (int j=i; j > 0 && c.compare(items[j-1], items[j]) > 0; j--) {
				// swap
				E tmp = items[j];
				items[j] = items[j-1];
				items[j-1] = tmp;
			}
		}
	}
	
	private static <E> boolean isSorted(E[] items, Comparator<E> c) {
		boolean sorted = true;
		int n = items.length;
		for (int i = 1; i < n; i++) {
			if (c.compare(items[i-1], items[i]) > 0) {
				sorted = false;
				break;
			}
		}
		return sorted;
	}
	
	public static void log(String message) {
		System.out.printf("%-15s ID %5d  ROUND %5d  BYTECODES USED %5d: %s\n", robotType, robotId, Clock.getRoundNum(), Clock.getBytecodeNum(), message);
	}
	
    protected static void die () {
        die("I'm melting, melting. Ohhhhh, what a world, what a world...");
    }
    
    protected static void die(String message) {
    	die(message, null);
    }
    
    protected static void die(Exception e) {
    	die("I'm melting, melting. Ohhhhh, what a world, what a world...", e);
    }

    protected static void die (String message, Exception e) {
    	if (e != null) {
    		e.printStackTrace();
    	} 
    	//throw new RuntimeException(message, e);
    }	
    
    public static int locationToInt(MapLocation mapLocation) {
    	int intLocation = (1000 * mapLocation.x) + mapLocation.y + 1;
    	return intLocation;
    }
    
    public static MapLocation intToLocation(int intLocation) {
    	intLocation = intLocation - 1;
    	int x = intLocation / 1000;
    	int y = intLocation % 1000;
    	return new MapLocation(x,y);
    }
    
	public static void main(String[] args) {
		runTests();
	}	
	
	private static void runTests() {
		{
			Integer[] items = {5,4,3,2,1};
			Comparator<Integer> c = new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
				
			};
			sort(items, c);
			if (!isSorted(items, c)) {
				die(Arrays.toString(items));
			}
		}
		{
			Integer[] items = {1,2,3,4,5};
			Comparator<Integer> c = new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
				
			};
			sort(items, c);
			if (!isSorted(items, c)) {
				die(Arrays.toString(items));
			}
		}	
		{
			Integer[] items = {5,1,2,3,4,5};
			Comparator<Integer> c = new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o1.compareTo(o2);
				}
				
			};
			sort(items, c);
			if (!isSorted(items, c)) {
				die(Arrays.toString(items));
			}
		}			
		System.out.printf("All tests passed!");
	}   
	
	public static int countSoldiers (Robot[] robots, RobotController rc) throws GameActionException {
		int numSoldiers = 0;
		for (Robot robot: robots) {
			if (rc.canSenseObject(robot)) {
				RobotInfo info = rc.senseRobotInfo(robot);
				if (info.type == SOLDIER) {
					numSoldiers++;
				}
			}
		}
		return numSoldiers;
	}
}
