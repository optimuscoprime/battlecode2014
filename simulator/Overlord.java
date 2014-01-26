import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Overlord {
	public static void main(String args[]) {
		int processors = Runtime.getRuntime().availableProcessors();
		
		GameRunner.puts("=== " + processors + " PROCESSORS DETECTED ===");
		
		ArrayList<String> maps = new ArrayList<String>();
		ArrayList<String> teams = new ArrayList<String>();
		
		maps.add("backdoor.xml");
		maps.add("bakedpotato.xml");
		maps.add("castles.xml");
		maps.add("divide.xml");
		maps.add("neighbors.xml");
		maps.add("onramp.xml");
		maps.add("reticle.xml");
		maps.add("rushlane.xml");
		maps.add("simple.xml");
		maps.add("stitch.xml");

		teams.add("micro");
		teams.add("gk_roman");
		teams.add("gk_attack");
		teams.add("gk_master");
		teams.add("hubertTheFraternal");
		teams.add("sc02");
		teams.add("sound01");
		teams.add("sound03");

		int total_games_to_play = (teams.size() * teams.size() - teams.size()) * maps.size();

		ExecutorService es = Executors.newFixedThreadPool(processors);
		
		int counter = 1;
		for (String teamA : teams) {
			for (String teamB : teams) {
				if (teamA.equals(teamB)) continue;
				for (String map : maps) {
					es.execute(new GameRunner(map, teamA, teamB, counter++, total_games_to_play));
				}
			}
		}
		
		GameRunner.puts("shutdown");
		es.shutdown();
		GameRunner.puts("DONE");
	}

}


class GameRunner implements Runnable {

	public static final String APP_DIR = "/Applications/Battlecode2014";
	
	String map;
	String teamA;
	String teamB;
	int game_number;
	int total_games;
	
	public GameRunner(String map_, String teamA_, String teamB_, int game_number_, int total_games_) {
		map = map_;
		teamA = teamA_;
		teamB = teamB_;
		game_number = game_number_;
		total_games = total_games_;
	}
	
	public void run() {
		String uuid = UUID.randomUUID().toString();
		String dir = "/tmp/battlecode2014/" + uuid + "-" + teamA + "-" + teamB + "-" + map;

		puts("mkdir");
		runCommand("mkdir -p " + dir);
								
		puts("cp");
		runCommand("cp -r " + APP_DIR + "/* " + dir + "/");
		
		puts("set config");
		runCommand("cat " + dir + "/bc.conf " +
				   "| sed 's/^.*bc.game.maps=.*$/bc.game.maps=" + map + "/g' " +
				   "| sed 's/^.*bc.game.team-a=.*$/bc.game.team-a=" + teamA + "/g' " +
				   "| sed 's/^.*bc.game.team-b=.*$/bc.game.team-b=" + teamB + "/g' " +
				   "> " + dir + "/bc.conf.tmp " + 
				   "&& mv " + dir + "/bc.conf.tmp " + dir + "/bc.conf");
		
		puts("run match [" + game_number + "/" + total_games + "]");
		String content = runCommand("cd " + dir + " ; ant file");
		
		if (content.contains("(A) wins")) {
			puts(teamA + " wins on " + map + " against " + teamB);
		} else if (content.contains("(B) wins")) {
			puts(teamB + " wins on " + map + " against " + teamA);
		} else {
			puts("Unknown match result");
		}
		
		puts("rm");
		runCommand("rm -rf " + dir);
		
		puts("done");
	}

	public static void puts(String s) {
		System.out.println("[thread " + Thread.currentThread().getId() + "] " + s);
	}

	public static String runCommand(String command) {
		try {
			String params[] = {"bash", "-c", command};
			Process p = Runtime.getRuntime().exec(params);
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String buffer = "";
			StringBuilder sb = new StringBuilder();
			while ((buffer = in.readLine()) != null) {
				sb.append(buffer + "\n");
			}
			in.close();
			in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((buffer = in.readLine()) != null) {
				sb.append(buffer + "\n");
			}
			in.close();
			return sb.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
}
