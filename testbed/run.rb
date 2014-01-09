
require 'fileutils'

APP_DIR = "/Applications/Battlecode2014"
PASSIVE = false
DEBUG = false
BOTS_CAN_PLAY_SELF = false

WIN = 3
LOSS = 1










def log(s)
  puts "DEBUG: #{s}" if DEBUG
end

def discover_maps
  maps_dir = File.join(APP_DIR, "maps")
  maps = []
  Dir.foreach(maps_dir) do |f|
    f = File.expand_path(File.join(maps_dir, f))
    
    if f =~ /xml$/
      map = File.basename(f)
      log("Found map '#{map}'")
      maps << File.basename(map)
    end
  end
  return maps
end


def discover_teams
  teams_dir = File.join(APP_DIR, "teams")
  teams = []
  Dir.foreach(teams_dir) do |f|
    f = File.expand_path(File.join(teams_dir, f))
    if File.directory?(f)
      if File.exists?(File.join(f, "RobotPlayer.java"))
        team = File.basename(f)
        log("Found team '#{team}'")
        teams << File.basename(team)
      end
    end
  end
  return teams
end

maps = discover_maps
teams = discover_teams

team_scores = {}
teams.each { |t| team_scores[t] = 0 }

bc_conf_filename = File.expand_path(File.join(APP_DIR, "bc.conf"))

teams.each do |teamA|
  teams.each do |teamB|
    next if teamA == teamB && !BOTS_CAN_PLAY_SELF
    
    maps.each do |map|
      log("Backing up exising bc.conf file")
      FileUtils.copy(bc_conf_filename, bc_conf_filename + ".backup") unless PASSIVE
      
      bc_conf_contents = File.read(bc_conf_filename)
      bc_conf_contents.gsub!(/^.*bc.game.maps=.*$/, "bc.game.maps=#{map}")
      bc_conf_contents.gsub!(/^.*bc.game.team-a=.*$/, "bc.game.team-a=#{teamA}")
      bc_conf_contents.gsub!(/^.*bc.game.team-b=.*$/, "bc.game.team-b=#{teamB}")
      log("Saving current game values to bc.conf")
      File.open(bc_conf_filename, 'wb') { |f| f.write bc_conf_contents } unless PASSIVE
      
      Dir.chdir(APP_DIR) do
        print "Running match '#{teamA}' vs '#{teamB}' @ map '#{map.gsub(".xml", "")}' \t\t"
        if PASSIVE
          puts ""
        else
          content = `ant file`
          if content =~ /\(A\) wins/
            puts "#{teamA} wins"
            team_scores[teamA] += WIN
            team_scores[teamB] += LOSS
          elsif content =~ /\(B\) wins/
            puts "#{teamB} wins"
            team_scores[teamA] += LOSS
            team_scores[teamB] += WIN
          else
            puts "Unknown game result"
          end
        end
      end
      
      log("Restoring previous bc.conf")
      FileUtils.copy(bc_conf_filename + ".backup", bc_conf_filename) unless PASSIVE
    end
  end
end


puts "====================="
puts "==== L A D D E R ===="
team_scores.sort_by { |name, score| score }.reverse.each do |team|
  puts "#{team[0]}\t#{team[1]}"
end
