package micro;

import java.util.*;

import battlecode.common.*;

public class Util {
	
	public static Random random;
	
	public static void init(int seed) {
	    random = new Random(seed);
	}
	
	// Implementing Fisher–Yates shuffle (http://stackoverflow.com/questions/1519736/random-shuffling-of-an-array)
	public static void shuffle (Object[] ar) {
		for (int i = ar.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			// Simple swap
			Object a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
	
	public static void log(Robot robot, RobotType robotType, String message) {
		System.out.printf("%-15s ID %5d  ROUND %5d  BYTECODES USED %5d: %s\n", robotType, robot.getID(), Clock.getRoundNum(), Clock.getBytecodeNum(), message);
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
        //throw new RuntimeException(message, e);
    }	
}
