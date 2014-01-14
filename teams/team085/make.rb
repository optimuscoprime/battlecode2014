# -*- Ruby -*-

APP_DIR = "/Applications/Battlecode2014"

my_dir = File.expand_path(File.dirname(__FILE__))

unless ARGV[0]
  puts "Usage: ruby make.rb <bot name>"
  puts "  e.g. $ ruby make.rb sc01"
  exit 1
end

bot = ARGV[0]

puts "Copying RobotPlayer over"
src_file = "../#{bot}/RobotPlayer.java"
unless File.exists?(src_file)
  puts "Could not find file '#{src_file}'"
end
`cp -f #{src_file} .`
unless File.exists?("RobotPlayer.java")
  puts "Looks like file copy didn't work"
  exit 1
end

puts "Changing package name in RobotPlayer"
content = File.read("RobotPlayer.java")
content.gsub!(/^\s*package\s+#{bot}\s*;/, "package team085;")
File.open("RobotPlayer.java", "wb") { |f| f.write content }

content = File.read("RobotPlayer.java")
unless content.include?("package team085")
  puts "Didn't update package in file"
  exit 1
end

puts "Chaning to App directory"
Dir.chdir(APP_DIR) do

  puts "Making sure a team085 directory exists"
  unless File.exists?("teams/team085")
    puts "Doesn't exist, creating"
    
    Dir.chdir("teams") do
      puts "$ ln -s #{my_dir}"
      `ln -s #{my_dir}`
    end
  end
  
  puts "Removing old submission.jar"
  if File.exists?("submission.jar")
    `rm -r submission.jar`
  end

  puts "Building a submission.jar"
  `ant -Dteam=team085 jar`
  
  unless File.exists?("submission.jar")
    puts "Can't find a submission.jar, might have failed to build"
  end
      
end


puts "Upload submission.jar to Upload Player page"
