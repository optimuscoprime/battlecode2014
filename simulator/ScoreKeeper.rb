

class ScoreKeeper

  # scores[map][team] = FixNum
  def initialize
    @scores = {}
  end

  def add_result(map, team, score)
    @scores[map] ||= {}
    @scores[map][team] ||= 0
    @scores[map][team] += score
  end

  def print_overall_scores
    puts "========================================"
    team_scores = {}
    @scores.each do |map, teams|
      teams.each do |team, score|
        team_scores[team] ||= 0
        team_scores[team] += score
      end
    end
    team_scores.sort_by { |team, score| score}.each do |team, score|
      puts "Team '#{team}' scored '#{score}' overall."
    end
    puts "========================================"
  end

  def write_map_report(filename)
    current_ios_ish_time = Time.now.strftime("%Y_%m_%d_%H_%M_%S")
    content = "<html><head><title>Generated at #{current_ios_ish_time}</title></head><body>"
    
    @scores.each do |map, teams|
      content << "<h1>Map: #{map}</h1>\n"
      content << "<table border=\"0\">"
      teams.sort_by { |team, score| score }.reverse.each do |team, score|
        content << "<tr><td>#{team}</td><td>#{score}</td></tr>"
      end
      content << "</table>"
    end
    
    content << "</body></html>"
    File.open(filename, "wb") { |f| f.write content }
  end
  
end
