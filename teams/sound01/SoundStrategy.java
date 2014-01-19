package sound01;

import sound01.Comms.Message;
import sound01.Navigation.Move;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

/**
 * Aggressive attack strategy
 */
public class SoundStrategy implements Strategy {
	RobotController rc;
	Random rand;
	Team ENEMY;
	RobotInfo INFO;
	Direction wanderingDirection;
	MapLocation firstTarget;
	MapLocation currentTarget;
	Direction DIRECTION_TO_HQ;
	List<MapLocation> targetList;//=new ArrayList<MapLocation>();
	int pos;

	public SoundStrategy(RobotController rc, Random rand) throws GameActionException {
		targetList=new ArrayList<MapLocation>();
		this.rc = rc;
		int increment=4;
		//int increment=NOISE_SCARE_RANGE_SMALL; // this doesnt seem to exist in GameConstants?
		// The above strategy is pretty poor.  How about two-dimensional forloop
		// building an array of target MapLocations we can hit.
		for(int x=0;x<rc.getMapWidth();x=x+increment){
			for(int y=0; y<rc.getMapHeight();y=y+increment){
				if (rc.isActive()) {
					currentTarget=new MapLocation(x,y);
					if(rc.canAttackSquare(currentTarget)){
						targetList.add(currentTarget);
						//System.out.println("Adding x:" + x + " y:" + y);
					}
				}else{
					rc.yield();
				}	
			}
		}
		pos=0;  
	}
	
	public void play() throws GameActionException {
		
		if (rc.isActive()) {
			rc.attackSquare(targetList.get(pos));
			if(pos<targetList.size()-1){
				pos++;
			}else{
				pos=0;
			}
		}else{
			rc.yield();
		}
		/*
		if(!rc.canAttackSquare(currentTarget)){
			currentTarget=firstTarget;
		}
		rc.attackSquare(currentTarget);
		currentTarget=currentTarget.add(DIRECTION_TO_HQ);
		currentTarget=currentTarget.add(DIRECTION_TO_HQ);
		*/
	}
}


