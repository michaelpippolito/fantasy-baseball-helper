import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsByPositionGroupHelper {

    public void getStats(Team team, PositionGroup positionGroup) throws Exception {
        System.out.println("Looking up " + positionGroup.getName() + " stats for the " + team.getName() + "...");

        switch (positionGroup) {
            case BULLPEN:
                getBullpenStats();
                break;
            case INFIELD:
                getInfieldStats();
                break;
            case OUTFIELD_DH:
                getOutfieldStats();
                break;
            case ROTATION:
                getRotationStats(team);
                break;
        }
    }

    private void getBullpenStats() {

    }

    private void getInfieldStats() {

    }

    private void getOutfieldStats() {

    }

    private void getRotationStats(Team team) throws Exception {
        List<String> gameUrls = BaseballReferenceHelper.getAllGameUrls(team);
        Map<String, Player> startingPitchers = getStartingPitcherStats(team, gameUrls);

        double totalWAR = 0.0;
        for (Player startingPitcher : startingPitchers.values()) {
            System.out.println(startingPitcher.getName() + " has pitched " + startingPitcher.getGameStats().size() + " games with a " + startingPitcher.getWAR() + " WAR");
            totalWAR += startingPitcher.getWAR();
        }
        System.out.printf("The " + team.getName() + " " + PositionGroup.ROTATION.getName() + " have produced %.2f total WAR\n", totalWAR);
    }

    private Map<String, Player> getStartingPitcherStats(Team team, List<String> gameUrls) throws Exception {
        Map<String, Player> reliefPitchers = new HashMap<>();
        Map<String, Player> startingPitchers = new HashMap();

        // first getting stats for every starter and reliever for every game
        for (String gameUrl : gameUrls) {
            Document gameDocument = BaseballReferenceHelper.getHtmlDocument(gameUrl);
            String gameInfo = BaseballReferenceHelper.getGameInfo(gameDocument);

            System.out.println("Getting stats for " + gameInfo);

            Player startingPitcher = BaseballReferenceHelper.getStartingPitcher(team, gameDocument);
            if (startingPitchers.containsKey(startingPitcher.getName())) {
                startingPitchers.get(startingPitcher.getName()).updateStats(startingPitcher);
            } else {
                startingPitchers.put(startingPitcher.getName(), startingPitcher);
            }

            Map<String, Player> reliefPitchersForGame = BaseballReferenceHelper.getReliefPitchers(team, gameDocument);
            for (Player reliefPitcher : reliefPitchersForGame.values()) {
                if (reliefPitchers.containsKey(reliefPitcher.getName())) {
                    reliefPitchers.get(reliefPitcher.getName()).updateStats(reliefPitcher);
                } else {
                    reliefPitchers.put(reliefPitcher.getName(), reliefPitcher);
                }
            }

            Map<String, Player> infieldersForGame = BaseballReferenceHelper.getInfielders(team, gameDocument);
        }

        /*
            Calculating WAR for a starting pitcher.
            Check to make sure this pitcher has not made any relief appearances
            If no relief appearances, simply get the WAR from their baseball-reference page
         */
        for (String pitcher : startingPitchers.keySet()) {
            if (reliefPitchers.containsKey(pitcher)) {
                System.out.println(pitcher + " has appeared as both a starter and reliever - must calculate WAR manually");
                startingPitchers.get(pitcher).setWAR(BaseballReferenceHelper.calculateBWARPitcher(startingPitchers.get(pitcher)));
            } else {
                startingPitchers.get(pitcher).setWAR(BaseballReferenceHelper.getWARSimple(startingPitchers.get(pitcher)));
            }
        }

        return startingPitchers;
    }
}
