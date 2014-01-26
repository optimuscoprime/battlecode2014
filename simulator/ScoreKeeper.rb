

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

end
