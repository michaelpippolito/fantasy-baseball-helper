import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class Player {
    @Getter @Setter
    private String name;
    @Getter @Setter
    private Team team;
    @Getter @Setter
    private PositionGroup positionGroup;
    @Getter @Setter
    private List<Stats> gameStats;
    @Getter @Setter
    private double WAR;
    @Getter @Setter
    private String url;

    public void updateStats(Player player) {
        this.gameStats.addAll(player.getGameStats());
    }

    public double getFIP() throws Exception {
        if (this.positionGroup.isPitcher()) {
            double strikeouts = 0.0;
            double walks = 0.0;
            double homeRuns = 0.0;
            double inningsPitched = 0.0;

            for (Stats gameStat : this.gameStats) {
                PitcherStats pitcherGameStats = (PitcherStats) gameStat;

                strikeouts += pitcherGameStats.getStrikeouts();
                walks += pitcherGameStats.getWalks();
                homeRuns += pitcherGameStats.getHomeRuns();
                inningsPitched += pitcherGameStats.getInningsPitched();
            }

            double homeRunsPer9 = (homeRuns * 13.0) / inningsPitched;
            double walksPer9 = (walks * 3.0) / inningsPitched;
            double strikeoutsPer9 = (strikeouts * 3.2) / inningsPitched;

            return (homeRunsPer9 + walksPer9 - strikeoutsPer9) + 3.2;
        } else {
            throw new Exception("Can not calculate FIP for a position player");
        }
    }

    public double calculateERA() throws Exception {
        if (this.positionGroup.isPitcher()) {
            double earnedRuns = 0.0;
            double inningsPitched = 0.0;

            for (Stats gameStat : this.gameStats) {
                PitcherStats pitcherGameStats = (PitcherStats) gameStat;
                earnedRuns += pitcherGameStats.getEarnedRuns();
                inningsPitched += pitcherGameStats.getInningsPitched();
            }
            return (earnedRuns * 9.0) / inningsPitched;
        } else {
            throw new Exception("Can not calculate ERA for a " + this.positionGroup.getName() + " player");
        }
    }
}
