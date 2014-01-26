package gk_roman;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import gk_roman.Strategy;

public class FarmStrategy implements Strategy {

	public final RobotController rc;
	MapLocation farm;
	Random rand;
	int d = GameConstants.NOISE_SCARE_RANGE_SMALL;
	int dh = d/2;
	
	public FarmStrategy(RobotController rc) {
		super();
		this.rc = rc;
		
		rand = new Random(rc.getRobot().getID());
		
		double growthField[][] = rc.senseCowGrowth();
		double maxGrowth = 0;
		for (int i = 0; i < rc.getMapWidth(); i++) {
			for (int j = 0; j < rc.getMapHeight(); j++) {
				double growth = growthField[i][j];
				if (growth > maxGrowth && rand.nextBoolean()) {
					farm = new MapLocation(i,j);
					maxGrowth = growth;
				}
			}
		}
	}



	@Override
	public void play() throws GameActionException {
		MapLocation current = rc.getLocation();
		if (farm != null) {
			if (current.equals(farm)) {
				double here = rc.senseCowsAtLocation(current);
				for (int i = -dh; i < dh && current.equals(farm); i++) {
					for (int j = -dh; j < dh; j++) {
						MapLocation nearby = current.add(i,j);
						if (rc.canSenseSquare(nearby)) {
							double there = rc.senseCowsAtLocation(nearby);
							if (there > here) {
								farm = nearby;
								break;
							}
						}
					}
				}
			} else {
				if (rc.isActive()) {
					Navigation.sneakToward(rc, current.directionTo(farm));	
				}				
			}
		}
	}

}
