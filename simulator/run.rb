#!/usr/bin/ruby

$:.unshift File.dirname(__FILE__)

require 'fileutils'
require 'rubygems'
require 'yaml'
require 'ScoreKeeper.rb'

config = YAML.load_file("sim.yml")

APP_DIR = config['app_dir']
DEBUG   = config['debug']

# ----------------------------------------
# C O N F I G   S E C T I O N

# -- specify a custom list of teams to all play each other
# -- Leave this as empty (which is '[]') to make all teams in 'teams/' directory play each other.

# -- overall scoring of match results
SCORE_WIN = config['score_win']
SCORE_TIE = config['score_tie']
SCORE_LOSS = config['score_loss']

# -- if you want to log game recordings to a timestamped file (true), or pipe to /dev/null (false)
SAVE_GAME_RECORD = false

# -- set to true to make bots be able to play themselves
BOTS_CAN_PLAY_SELF = config['bots_can_play_self']

# END CONFIG SECTION
# ----------------------------------------



# get a list of teams together
teams = []
unless config['use_all_teams']
  teams = config['custom_teams']
else
  puts "Using all teams" if DEBUG
  teams_dir = File.join(APP_DIR, "teams")
  Dir.foreach(teams_dir) do |f|
    f = File.expand_path(File.join(teams_dir, f))    
    if File.directory?(f) and not f.include? "team085"
      if File.exist?(File.join(f, "RobotPlayer.java"))
        teams << File.basename(f)
      end
    end
  end
end
if DEBUG
  puts "Found these teams:"
  teams.each do |t|
    puts "  #{t}"
  end
else
  puts "Found #{teams.count} teams"
end


# get a list of the maps in the map dir
maps = []
unless config['use_all_maps']
  maps = config['custom_maps']
else
  puts "Using all maps" if DEBUG
  maps_dir = File.join(APP_DIR, "maps")

  Dir.foreach(maps_dir) do |f|
    f = File.expand_path(File.join(maps_dir, f))
  
    if f =~ /xml$/
      maps << File.basename(f)
    end
  end
end
if DEBUG
  puts "Found these maps:"
  maps.each do |m|
    puts "  #{m}"
  end
else
  puts "Found #{maps.count} maps."
end


scores = ScoreKeeper.new


counter = 0
if BOTS_CAN_PLAY_SELF
  total_games_to_run = teams.count * teams.count * maps.count
else
  total_games_to_run = (teams.count * teams.count - teams.count) * maps.count
end

puts "About to play #{total_games_to_run} games."
puts "Let's get ready to rumble..."

bc_conf_filename = File.expand_path(File.join(APP_DIR, "bc.conf"))

teams.each do |teamA|
  teams.each do |teamB|
    if teamA == teamB
      next unless BOTS_CAN_PLAY_SELF
    end
    
    maps.each do |map|
    
      puts "Backing up existing bc.conf" if DEBUG    
      FileUtils.copy(bc_conf_filename, bc_conf_filename + ".backup")
      # ----------
      
      #puts "Editing bc.conf to set up match"
      bc_conf_contents = File.read(bc_conf_filename)    
      bc_conf_contents.gsub!(/^.*bc.game.maps=.*$/, "bc.game.maps=#{map}")
      bc_conf_contents.gsub!(/^.*bc.game.team-a=.*$/, "bc.game.team-a=#{teamA}")
      bc_conf_contents.gsub!(/^.*bc.game.team-b=.*$/, "bc.game.team-b=#{teamB}")
      
      if SAVE_GAME_RECORD
        current_iso_ish_time = Time.now.strftime("%Y_%m_%d_%H_%M_%S")      
        match_record_save_file = File.expand_path(File.join(File.dirname(__FILE__), "#{current_iso_ish_time}-#{map.gsub(".xml", "")}-#{teamA}-#{teamB}.rms"))
      else
        match_record_save_file = "/dev/null"
      end
      bc_conf_contents.gsub!(/^.*bc.server.save-file=.*$/, "bc.server.save-file=#{match_record_save_file}")
      
      File.open(bc_conf_filename, 'wb') { |f| f.write bc_conf_contents }

      prev_dir = File.expand_path(Dir.pwd)
      Dir.chdir(APP_DIR)

      counter += 1
      #STDOUT.sync = true # dont buffer printing.
      print "Running match [#{counter}/#{total_games_to_run}] (#{teamA}) vs (#{teamB}) @ (#{map.gsub(".xml", "")})\t\t"
      # run the match
      content = `ant file`
      
      if content =~ /\(A\) wins/
        puts "#{teamA} wins"
        scores.add_result(map, teamA, SCORE_WIN)
        scores.add_result(map, teamB, SCORE_LOSS)
      elsif content =~ /\(B\) wins/
        puts "#{teamB} wins"
        scores.add_result(map, teamA, SCORE_LOSS)
        scores.add_result(map, teamB, SCORE_WIN)
      else
        puts "Unknown match result (please add code to catch this)"
        puts "content=#{content}"
      end
      
      Dir.chdir(prev_dir)
      
      # ----------
      #puts "Copying bc.conf backup back to original location"
      FileUtils.copy(bc_conf_filename + ".backup", bc_conf_filename)            
    end
  end
end



scores.print_overall_scores

scores.write_map_report("/home/rupert/output.html")
