package sound04;

import sound04.Comms.Message;
import sound04.Navigation.Move;

import java.util.Deque;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Clock;
import battlecode.common.Team;

/**
 * Aggressive attack strategy
 *
 * Ian has plans for what happends when retreating -
 * by shooting out to the side I think single soldiers can perhaps herd a *lot*
 */
public class AttackStrategy implements Strategy {
	RobotController rc;
	Random rand;
	Team ENEMY;
	RobotInfo INFO;
	Direction wanderingDirection;

	public AttackStrategy(RobotController rc, Random rand) throws GameActionException {
		this.rc = rc;
		this.ENEMY = rc.getTeam().opponent();
		this.INFO = rc.senseRobotInfo(rc.getRobot());
		this.rand = rand;
		wanderingDirection = null;
	}

	public MapLocation mostCows(RobotController rc) throws GameActionException{
		//canSenseSquare(ML);
		//senseCowsAtLocation(ML);
		//ML.add(x,y);
		//array
		MapLocation center=rc.getLocation();
		MapLocation currentLoc=null;
		MapLocation maxLoc=null;
		double curCows=0;
		double maxCows=500;
		int[] x={-2,-1,0,1,2};
		int[] y={-2,-1,0,1,2};
		for (int xi = 0; xi < x.length; xi++){
			for(int yi=0; yi< y.length; yi++){
				currentLoc=center.add(x[xi],y[yi]);
				curCows=rc.senseCowsAtLocation(currentLoc);
				if(curCows > maxCows){
					maxCows=curCows;
					maxLoc=currentLoc;
				}
			}
		}
		return maxLoc;
	}
	
	public void play() throws GameActionException {
		MapLocation dest=null;
		if((Clock.getRoundNum()%3)==0){	//save operations for only some turns.
			dest = Abilities.ClosestPastr(rc, rc.getLocation(), ENEMY);
		}
		// insert code here to look for things calling for help; set that as dest.
		int helpChannel=3;
		int helpWithin=500;
		Message help=Comms.ReadMessage(rc,helpChannel);
		if(help!=null){
			if((help.val + helpWithin) > Clock.getRoundNum()){
				dest=help.loc;
			}
		}

		if(rc.getHealth()<(rc.getType().maxHealth/4)){ //we should actually just herd.
			if(rc.isActive()){
				dest=rc.senseHQLocation();	//run away!
				Navigation.moveGreedy(rc,dest,2); //make this away from e
				Deque<Move> path = Navigation.pathAStar(rc, dest);
				while(Navigation.moveOnPath(rc,path)){
					Tactics.fightOrFlight(rc, INFO);
				}
			}
		}else if (dest != null) {
			Deque<Move> path = Navigation.pathAStar(rc, dest);
			while(Navigation.attackMoveOnPath(rc, path, INFO.type.attackRadiusMaxSquared, ENEMY)) {
				Tactics.fightOrFlight(rc, INFO);
			}
		}
		Tactics.fightOrFlight(rc, INFO);//check for enemies?
		//herd
		if(rc.isActive()){
			MapLocation herd=mostCows(rc);
			MapLocation hq=rc.senseHQLocation();
			MapLocation loc=rc.getLocation();
			if(herd!=null){
				//rc.attackSquare(herd); // heh that was fun.
				Direction opp=herd.directionTo(hq).opposite();
				MapLocation pushLoc=herd.add(opp).add(opp);
				if(loc==pushLoc){
					Direction home=loc.directionTo(hq);
					if(rc.canMove(home)){
						rc.move(home);
					}
				}else{
					if(rc.canMove(loc.directionTo(pushLoc))){
						rc.sneak(loc.directionTo(pushLoc));
					}
				}
			}
		}
		if (rc.isActive()) {
			wanderingDirection = Navigation.wonder(rc, rand, wanderingDirection);
		} 
		//yield runs straight after play in playSingleStrategy
	}
}
