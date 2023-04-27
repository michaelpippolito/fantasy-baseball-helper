import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BaseballReferenceHelper {

    private static final String BASEBALL_REFERENCE_BASE_URL = "https://www.baseball-reference.com";

    private static final String TEAM_ABBREVIATION_REPLACE_URL = "{abbr}";
    private static final String TEAM_DATA_ENDPOINT = "/teams/" + TEAM_ABBREVIATION_REPLACE_URL + "/2023.shtml";
    private static final String TEAM_DATA_PAGE_TIMELINE_CLASS_NAME = "timeline";

    private static final String TEAM_NAME_REPLACE_ELEMENT = "{team}";
    private static final String PITCHING_TABLE_ID = TEAM_NAME_REPLACE_ELEMENT + "pitching";

    private static final double WAR_REPLACEMENT_LEVEL = 0.294;

    private static String getUrl(String endpoint) {
        return getUrl(endpoint, null, null);
    }

    private static String getUrl(String endpoint, String replace, String replaceWith) {
        if (replace != null && !replace.isEmpty() && replaceWith != null) {
            return (BASEBALL_REFERENCE_BASE_URL + endpoint).replace(replace, replaceWith);
        } else {
            return BASEBALL_REFERENCE_BASE_URL + endpoint;
        }
    }

    public static Document getHtmlDocument(String url) throws IOException, InterruptedException {
        Thread.sleep(2000); // to avoid HTTP 429 error
        return Jsoup.connect(url).get();
    }

    public static List<String> getAllGameUrls(Team team) throws IOException, InterruptedException {
        List<String> gameUrls = new ArrayList<>();

        Document htmlDocument = getHtmlDocument(getUrl(TEAM_DATA_ENDPOINT, TEAM_ABBREVIATION_REPLACE_URL, team.getAbbreviation()));
        Elements timelineResults = htmlDocument.getElementsByClass(TEAM_DATA_PAGE_TIMELINE_CLASS_NAME);
        for (Element timelineResult : timelineResults) {
            for (Node timelineNode : timelineResult.childNodes().stream().filter(node -> node.hasAttr("class")).collect(Collectors.toList())) {
                Element timelineNodeElement = (Element) timelineNode;
                if (timelineNodeElement.attributes().get("class").equals("result")) {
                    for (Node gameNode : timelineNodeElement.childNodes().stream().filter(node -> node.hasAttr("class")).collect(Collectors.toList())) {
                        Element gameElement = (Element) gameNode;
                        if (gameElement.hasAttr("tip")) {
                            if (!gameElement.attributes().get("tip").equals("Off Day")) {
                                String gameDate = gameElement.attributes().get("tip");

                                if (Character.isDigit(gameDate.charAt(0))) {
                                    for (Node gameDataNode : gameElement.childNodes().stream().filter(node -> node.hasAttr("href")).collect(Collectors.toList())) {
                                        String gameEndpoint = gameDataNode.attributes().get("href");
                                        gameUrls.add(getUrl(gameEndpoint));
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
        return gameUrls;
    }

    public static Player getStartingPitcher(Team team, Document gameDocument) {
        Player startingPitcher = new Player();

        Element pitchingTableElement = getPitchingTableElement(team, gameDocument);
        Element startingPitcherElement = pitchingTableElement.select("th[csk=\"0\"]").first().parent();

        startingPitcher.setName(getPitcherName(startingPitcherElement));
        startingPitcher.setUrl(getPitcherUrl(startingPitcherElement));
        startingPitcher.setTeam(team);
        startingPitcher.setPositionGroup(PositionGroup.ROTATION);
        startingPitcher.setGameStats(new ArrayList<>(Collections.singletonList(getPitcherStats(startingPitcherElement))));

        return startingPitcher;
    }

    public static Map<String, Player> getReliefPitchers(Team team, Document gameDocument) {
        Map<String, Player> reliefPitchers = new HashMap<>();

        Element pitchingTableElement = getPitchingTableElement(team, gameDocument);
        List<Element> reliefPitcherElements = pitchingTableElement.select("th[csk~=[1-9]+]").stream().map(Element::parent).collect(Collectors.toList());

        for (Element reliefPitcherElement : reliefPitcherElements) {
            Player reliefPitcher = new Player();
            reliefPitcher.setName(getPitcherName(reliefPitcherElement));
            reliefPitcher.setUrl(getPitcherUrl(reliefPitcherElement));
            reliefPitcher.setTeam(team);
            reliefPitcher.setPositionGroup(PositionGroup.BULLPEN);
            reliefPitcher.setGameStats(new ArrayList<>(Collections.singletonList(getPitcherStats(reliefPitcherElement))));
            reliefPitchers.put(reliefPitcher.getName(), reliefPitcher);
        }

        return reliefPitchers;
    }

    private static Element getPitchingTableElement(Team team, Document gameDocument) {
        Node pitchingTableComment = gameDocument.select("div[id~=all_\\d+]").stream().filter(element ->
                element.childNodes().stream().anyMatch(node ->
                        node instanceof Element && ((Element) node).select(
                                "span[data-label=\"Pitching Lines and Info\"]"
                        ).size() > 0
                )
        ).findFirst().orElse(null).childNodes().stream().filter(node -> node instanceof Comment).findFirst().orElse(null);

        Document pitchingTableDocument = Jsoup.parse(
                pitchingTableComment.toString()
                        .replace("<!--", "")
                        .replace("-->", "")
        );
        return pitchingTableDocument.getElementById(PITCHING_TABLE_ID.replace(TEAM_NAME_REPLACE_ELEMENT, team.getName().replaceAll(" ", "")));
    }

    private static String getPitcherName(Element pitcherElement) {
        return pitcherElement.select("th").first().firstElementChild().firstChild().toString();
    }

    private static String getPitcherUrl(Element pitcherElement) {
        return pitcherElement.select("th").first().firstElementChild().attributes().get("href");
    }

    private static PitcherStats getPitcherStats(Element pitcherElement) {
        PitcherStats pitcherStats = new PitcherStats();
        pitcherStats.setInningsPitched(Double.parseDouble(pitcherElement.select("td[data-stat=\"IP\"]").first().firstChild().toString()));
        pitcherStats.setHits(Integer.parseInt(pitcherElement.select("td[data-stat=\"H\"]").first().firstChild().toString()));
        pitcherStats.setRuns(Integer.parseInt(pitcherElement.select("td[data-stat=\"R\"]").first().firstChild().toString()));
        pitcherStats.setEarnedRuns(Integer.parseInt(pitcherElement.select("td[data-stat=\"ER\"]").first().firstChild().toString()));
        pitcherStats.setWalks(Integer.parseInt(pitcherElement.select("td[data-stat=\"BB\"]").first().firstChild().toString()));
        pitcherStats.setStrikeouts(Integer.parseInt(pitcherElement.select("td[data-stat=\"SO\"]").first().firstChild().toString()));
        pitcherStats.setHomeRuns(Integer.parseInt(pitcherElement.select("td[data-stat=\"HR\"]").first().firstChild().toString()));
        pitcherStats.setEarnedRunAverage(Double.parseDouble(pitcherElement.select("td[data-stat=\"earned_run_avg\"]").first().firstChild().toString()));
        pitcherStats.setBattersFaced(Integer.parseInt(pitcherElement.select("td[data-stat=\"batters_faced\"]").first().firstChild().toString()));
        pitcherStats.setPitches(Integer.parseInt(pitcherElement.select("td[data-stat=\"pitches\"]").first().firstChild().toString()));
        if (pitcherElement.select("th").first().attributes().get("csk").equals("0")) {
            pitcherStats.setReliefAppearance(false);
        } else {
            pitcherStats.setReliefAppearance(true);
        }
        return pitcherStats;
    }

    public static double getWARSimple(Player player) throws IOException, InterruptedException {
        return Double.parseDouble(getHtmlDocument(getUrl(player.getUrl())).select("span[data-tip^=\"<strong>Wins Above Replacement\"]").first().parent().select("p").first().firstChild().toString());
    }

    public static double calculateBWARPitcher(Player player) throws Exception {
        if (player.getPositionGroup().isPitcher()) {
            double strikeouts = 0.0;
            double walks = 0.0;
            double homeRuns = 0.0;
            double inningsPitched = 0.0;
            double earnedRuns = 0.0;
            double hits = 0.0;

            for (Stats gameStat : player.getGameStats()) {
                PitcherStats pitcherGameStats = (PitcherStats) gameStat;
                strikeouts += pitcherGameStats.getStrikeouts();
                walks += pitcherGameStats.getWalks();
                homeRuns += pitcherGameStats.getHomeRuns();
                inningsPitched += pitcherGameStats.getInningsPitched();
                earnedRuns += pitcherGameStats.getEarnedRuns();
                hits += pitcherGameStats.getHits();
            }

            double homeRunsPer9 = (homeRuns * 13.0) / inningsPitched;
            double homeRunsPer9Innings = (homeRuns * 9.0) / inningsPitched;
            double walksPer9 = (walks * 3.0) / inningsPitched;
            double walksPer9Innings = (walks * 9.0) / inningsPitched;
            double strikeoutsPer9 = (strikeouts * 3.2) / inningsPitched;
            double hitsPer9Innings = (hits * 9.0) / inningsPitched;
            double ERA = (earnedRuns * 9.0) / inningsPitched;
            double fip = (homeRunsPer9 + walksPer9 - strikeoutsPer9) + 3.2;

            double runsAllowed = (13 * homeRunsPer9Innings + 3 * (walksPer9Innings + hitsPer9Innings)) * inningsPitched / 9;
            double pitchingRuns = (earnedRuns - fip) * 9 + runsAllowed;
            double leagueReplacementRunsPerIP = 0.294;
            double playerReplacementRunsPerIP = pitchingRuns / inningsPitched;
            double winsAboveReplacement = (playerReplacementRunsPerIP - leagueReplacementRunsPerIP) / 10;
            return winsAboveReplacement;
        } else {
            throw new Exception("Can not calculate pitcher bWAR for a " + player.getPositionGroup().getName() + " player");
        }
    }

}
