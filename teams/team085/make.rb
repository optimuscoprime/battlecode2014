#!/usr/bin/env ruby
# -*- Ruby -*-

APP_DIR = "/Applications/Battlecode2014"

my_dir = File.expand_path(File.dirname(__FILE__))

unless ARGV[0]
  puts "Usage: ruby make.rb <bot name>"
  puts "  e.g. $ ruby make.rb sc01"
  exit 1
end

bot = ARGV[0]

puts "Removing all Java source files in the team085 directory before copying new ones in"
`rm -r *.java`
`rm -r *.class`
if File.exists?("RobotPlayer.java")
  puts "Looks like I couldn't clear the files up properly"
  exit 1
end

puts "Copying all Java files over"
main_src_file = "../#{bot}/RobotPlayer.java"
unless File.exists?(main_src_file)
  puts "Could not find main file '#{src_file}'. Are you sure '../#{bot}' contains Java source files."
  exit 1
end
`cp -f ../#{bot}/*.java .`
unless File.exists?("RobotPlayer.java")
  puts "Looks like file copy didn't work (can't at least find a RobotPlayer.java after the file copy)"
  exit 1
end

puts "Changing package to 'team085' in all Java source files"
Dir["*.java"].each do |java_source_file|
  content = File.read(java_source_file)
  content.gsub!(/^\s*package\s+#{bot}\s*;/, "package team085;")
  File.open(java_source_file, "wb") { |f| f.write content }

  content = File.read(java_source_file)
  unless content.include?("package team085")
    puts "Failed to update package in file '#{java_source_file}'"
    exit 1
  end
end

puts "Changing to App directory"
Dir.chdir(APP_DIR) do

  # b/c Steve is subborn and unwilling to face facts and chckout his repo inside the app's repo
  # else: everyone else should already have all the correct files set up in #{APP_DIR}/teams/team085
  if `hostname`.strip =~ /cossell/
    puts "Making sure a team085 directory exists"
    unless File.exists?("teams/team085")
      puts "Doesn't exist, creating"
      
      if File.symlink?("teams/team085")
        `rm -f teams/team085`
      end
      
      Dir.chdir("teams") do
        puts "$ ln -s #{my_dir}"
        `ln -s #{my_dir}`
      end
    end
  end
  
  puts "Removing old submission.jar"
  if File.exists?("submission.jar")
    `rm -r submission.jar`
  end

  puts "Building a submission.jar"
  puts "$ ant -Dteam=team085 jar"
  content = `ant -Dteam=team085 jar 2>&1`
  
  if content =~ /BUILD FAILED/
    puts "=== BUILD FAILED WITH OUTPUT ==="
    puts "----------------------------------------"
    puts content
    puts "----------------------------------------"
    exit 1
  end
  
  unless File.exists?("submission.jar")
    puts "Can't find a submission.jar, might have failed to build"
  end

  puts ""
  puts "$ jar tf submission.jar"
  system("jar tf submission.jar")

end

puts ""
puts "  DONE"
puts ""
puts "Please upload submission.jar to Upload Player page"
puts ""
