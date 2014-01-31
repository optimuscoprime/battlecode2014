import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Overlord {
	public static void main(String args[]) {
		int processors = Runtime.getRuntime().availableProcessors();
		
		GameRunner.puts("=== " + processors + " PROCESSORS DETECTED ===");
		
		ArrayList<String> maps = new ArrayList<String>();
		ArrayList<String> teams = new ArrayList<String>();

		// run ./generate_maps_imports.sh to get this list

		// ian's map
		maps.add("deimos.xml");

		maps.add("almsman.xml");
		maps.add("backdoor.xml");
		maps.add("bakedpotato.xml");
		maps.add("blocky.xml");
		maps.add("cadmic.xml");
		maps.add("castles.xml");
		maps.add("corners.xml");
		maps.add("desolation.xml");
		maps.add("divide.xml");
		maps.add("donut.xml");
		maps.add("fenced.xml");
		maps.add("filling.xml");
		maps.add("flagsoftheworld.xml");
		maps.add("flytrap.xml");
		maps.add("friendly.xml");
		maps.add("fuzzy.xml");
		maps.add("gilgamesh.xml");
		maps.add("highschool.xml");
		maps.add("highway.xml");
		maps.add("house.xml");
		maps.add("hydratropic.xml");
		maps.add("hyperfine.xml");
		maps.add("intermeningeal.xml");
		maps.add("itsatrap.xml");
		maps.add("librarious.xml");
		maps.add("magnetism.xml");
		maps.add("meander.xml");
		maps.add("moba.xml");
		maps.add("moo.xml");
		maps.add("neighbors.xml");
		maps.add("oasis.xml");
		maps.add("onramp.xml");
		maps.add("overcast.xml");
		maps.add("pipes.xml");
		maps.add("race.xml");
		maps.add("reticle.xml");
		maps.add("rushlane.xml");
		maps.add("s1.xml");
		maps.add("siege.xml");
		maps.add("simple.xml");
		maps.add("smiles.xml");
		maps.add("spots.xml");
		maps.add("spyglass.xml");
		maps.add("steamedbuns.xml");
		maps.add("stitch.xml");
		maps.add("sweetspot.xml");
		maps.add("temple.xml");
		maps.add("terra.xml");
		maps.add("traffic.xml");
		maps.add("troll.xml");
		maps.add("unself.xml");
		maps.add("valve.xml");
		maps.add("ventilation.xml");

                //teams.add("frank");
		//teams.add("micro");
		//teams.add("nano");
		//teams.add("pico");		

		//teams.add("gk_attack");
		//teams.add("gk_master");
		//teams.add("sc01");
		//teams.add("sound00");
		//teams.add("sound01");
		//teams.add("sound02");
		//teams.add("sound03");

		teams.add("emo");		
		//teams.add("fredo");		
		teams.add("awesemo");		
		teams.add("gk_roman");
		teams.add("sc02");
		teams.add("sound04");
		//teams.add("brute01");
		teams.add("hubertTheFraternal");

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

	public static final String APP_DIR = System.getProperty("user.dir") + "/..";
        
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

		String command;

		command = "mkdir -p " + dir;
		puts(command);
		runCommand(command);
								
		command = "cp -r " + APP_DIR + "/* " + dir + "/";
		puts(command);
		runCommand(command);
		
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
