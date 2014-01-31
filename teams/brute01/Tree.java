package brute01;

import java.util.*;

import battlecode.common.*;

class BotInfo {
   public MapLocation location;
   public int id;
   public Team team;
   
   public Direction bestDirection;
   public int bestScore;
   
   public BotInfo(MapLocation location, int id, Team team) {
      this.location = location;
      this.id = id;
      this.team = team;

      this.bestDirection = null; 
      this.bestScore = 0;

   }
}

public class Tree {

   public static void minMax(Team ourTeam) {

      List<BotInfo> botsList = new ArrayList<BotInfo>();

      // loop over radio channels
      while (true) {
         // TODO
         MapLocation location = null;
         int id = 0;
         Team team = null;
         botsList.add(new BotInfo(location, id, team));
      }

      BotInfo[] bots = botsList.toArray(new BotInfo[0]);

      Arrays.sort(bots, new Comparator<BotInfo>() {
         public int compare(BotInfo o1, BotInfo o2) {
            return new Integer(o1.id).compareTo(o2.id);
         }
      });

      recurse(bots, 0, ourTeam);
      // when this is finished, check out bots[i].bestDirection for each i
      
      // to convert direction to int
      // direction.toOrdinal()  - gives a number

      // to convert int to direction
      // Direction.values()[intDirection];      
   }
   
   private static int recurse(BotInfo[] bots, int botIndex, Team ourTeam) {

      if (botIndex < bots.length) {
     
         BotInfo bot = bots[botIndex];

         int bestTotalScore = 0;
    
         for (Direction direction: Direction.values()) {
            MapLocation oldBotLocation = bot.location;
            bot.location = bot.location.add(direction);
         
            int totalScore = recurse(bots, botIndex+1, ourTeam);
      
      
            // min/max ?
         
            if (bot.team == ourTeam) {
               if (totalScore > bestTotalScore) {
                  bestTotalScore = totalScore;
                  if (bots[botIndex].bestDirection == null || bots[botIndex].bestScore < bestTotalScore) {
                     bots[botIndex].bestScore = bestTotalScore;
                     bots[botIndex].bestDirection = direction;
                  }
               }
            } else {
               // enemy
               if (totalScore < bestTotalScore) {
                  bestTotalScore = totalScore;
                  if (bots[botIndex].bestDirection == null || bots[botIndex].bestScore > bestTotalScore) {
                     bots[botIndex].bestScore = bestTotalScore;
                     bots[botIndex].bestDirection = direction;
                  }
               }     
            }
         
            bot.location = oldBotLocation;
         }
      
         return bestTotalScore;
      
      } else {
   
         // finished, no more bots left
         // run scoring function here and return
         // go through all bots (not bestBots)
      
         int totalScore = 0;
      
         for (int i=0; i < bots.length; i++) {
            BotInfo shooter = bots[i];
            for (int j=0; j < bots.length; j++) {
               if (i == j) {
                  continue;
               }
            
               // TODO take into account locations/distances/etc
            
               BotInfo victim = bots[j];
              
               if (shooter.team == ourTeam) {
                  // our team is the shooter
                  if (victim.team != shooter.team) {//TODO and within range
                     totalScore++; 
                     // continue; // I think.  We only need to verify theres at least one target.
                  }
               } else {
                  // enemy team is the shooter
                  if (victim.team != shooter.team) {
                     totalScore--;
                     // continue; // I think.  We only need to verify theres at least one target.
                  }
               }
            }
         }
         return totalScore;
      }
   }
 
}


