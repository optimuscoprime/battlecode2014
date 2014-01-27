
$:.unshift File.dirname(__FILE__)

require 'ScoreKeeper.rb'

SCORE_WIN = 1
SCORE_LOSS = 0;

scores = ScoreKeeper.new

while line = STDIN.gets
  if re = line.match(/(\S+) wins on (\S+) against (\S+)/)
    scores.add_result(re[2], re[1], SCORE_WIN)
    scores.add_result(re[2], re[3], SCORE_LOSS)
  end
end

scores.print_overall_scores

output_file = File.expand_path("/tmp/battlecode2014/output.html")
scores.write_map_report(output_file)
puts "Full results report printed to #{output_file}"
