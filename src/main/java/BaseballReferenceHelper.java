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
    private static final String BATTING_TABLE_ID = TEAM_NAME_REPLACE_ELEMENT + "batting";

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

    public static Map<String, Player> getInfielders(Team team, Document gameDocument) {
        Map<String, Player> infielders = new HashMap<>();

        Element battingTableElement = getBattingTableElement(team, gameDocument);
        List<Element> positionPlayers = battingTableElement.select("th[csk~=[0-9][0-8]]").stream().map(Element::parent).collect(Collectors.toList());
        ;

        for (Element positionPlayerElement : positionPlayers) {
            List<BaseballReferencePosition> positionsPlayed = getPositionPlayerPositionsPlayed(positionPlayerElement);
            if (positionsPlayed.stream().anyMatch(BaseballReferencePosition::isInfield)) {
                Player infielder = new Player();
                infielder.setName(getPositionPlayerName(positionPlayerElement));
                infielder.setUrl(getPositionPlayerUrl(positionPlayerElement));
                infielder.setTeam(team);
                infielder.setPositionGroup(PositionGroup.INFIELD);
                infielder.setGameStats(new ArrayList<>(Collections.singletonList(getPositionPlayerStats(positionPlayerElement, positionsPlayed))));
                infielders.put(infielder.getName(), infielder);
            }

        }

        return infielders;
    }

    private static Element getBattingTableElement(Team team, Document gameDocument) {
        String battingTableDivId = BATTING_TABLE_ID.replace(TEAM_NAME_REPLACE_ELEMENT, team.getName().replaceAll(" ", ""));

        Element battingTableElement = gameDocument.getElementById("all_" + battingTableDivId);

        Document battingTableDocument = battingTableElement.childNodes().stream()
                .filter(childNode -> childNode instanceof Comment)
                .findFirst()
                .map(battingTableComment -> Jsoup.parse(removeHtmlComments(battingTableComment.toString())))
                .orElse(null);

        return battingTableDocument.getElementById(battingTableDivId);
    }

    private static String removeHtmlComments(String html) {
        return html.replace("<!--", "").replace("-->", "");
    }

    private static String getPositionPlayerName(Element positionPlayerElement) {
        Element identifierElement = positionPlayerElement.select("th").first();
        if (identifierElement.firstChild().toString().trim().contains("&nbsp;")) {
            return identifierElement.childNode(1).firstChild().toString();
        }
        return identifierElement.childNode(0).firstChild().toString();
    }

    private static List<BaseballReferencePosition> getPositionPlayerPositionsPlayed(Element positionPlayerElement) {
        Element identifierElement = positionPlayerElement.select("th").first();
        if (identifierElement.firstChild().toString().trim().contains("&nbsp;")) {
            return Arrays.stream(positionPlayerElement.select("th").first().childNode(2).toString().trim().split("-")).map(BaseballReferencePosition::fromString).collect(Collectors.toList());
        }
        return Arrays.stream(positionPlayerElement.select("th").first().childNode(1).toString().trim().split("-")).map(BaseballReferencePosition::fromString).collect(Collectors.toList());
    }

    private static String getPositionPlayerUrl(Element positionPlayerElement) {
        Element identifierElement = positionPlayerElement.select("th").first();
        if (identifierElement.firstChild().toString().trim().contains("&nbsp;")) {
            return identifierElement.childNode(1).attributes().get("href");
        }
        return positionPlayerElement.select("th").first().childNode(0).attributes().get("href");
    }

    private static PositionPlayerStats getPositionPlayerStats(Element positionPlayerElement, List<BaseballReferencePosition> positionsPlayed) {
        PositionPlayerStats positionPlayerStats = new PositionPlayerStats();
        positionPlayerStats.setAtBats(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"AB\"]").first().firstChild().toString()));
        positionPlayerStats.setRuns(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"R\"]").first().firstChild().toString()));
        positionPlayerStats.setHits(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"H\"]").first().firstChild().toString()));
        positionPlayerStats.setRunsBattedIn(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"RBI\"]").first().firstChild().toString()));
        positionPlayerStats.setWalks(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"BB\"]").first().firstChild().toString()));
        positionPlayerStats.setStrikouts(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"SO\"]").first().firstChild().toString()));
        positionPlayerStats.setPlateAppearances(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"PA\"]").first().firstChild().toString()));

        if (positionPlayerStats.getAtBats() > 0) {
            positionPlayerStats.setBattingAverage(Double.parseDouble(positionPlayerElement.select("td[data-stat=\"batting_avg\"]").first().firstChild().toString()));
            positionPlayerStats.setOnBasePercentage(Double.parseDouble(positionPlayerElement.select("td[data-stat=\"onbase_perc\"]").first().firstChild().toString()));
            positionPlayerStats.setSlugging(Double.parseDouble(positionPlayerElement.select("td[data-stat=\"slugging_perc\"]").first().firstChild().toString()));
            positionPlayerStats.setOPS(Double.parseDouble(positionPlayerElement.select("td[data-stat=\"onbase_plus_slugging\"]").first().firstChild().toString()));
            positionPlayerStats.setPitchesSeen(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"pitches\"]").first().firstChild().toString()));
            positionPlayerStats.setStrikesSeen(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"strikes_total\"]").first().firstChild().toString()));
            positionPlayerStats.setWinProbabilityAdded(Double.parseDouble(positionPlayerElement.select("td[data-stat=\"wpa_bat\"]").first().firstChild().toString()));
            positionPlayerStats.setAverageLeverageIndex(Double.parseDouble(positionPlayerElement.select("td[data-stat=\"leverage_index_avg\"]").first().firstChild().toString()));
            positionPlayerStats.setWinProbabilityAddedPlus(Double.parseDouble(positionPlayerElement.select("td[data-stat=\"wpa_bat_pos\"]").first().firstChild().toString()));
            positionPlayerStats.setBasesOutRunsAdded(Double.parseDouble(positionPlayerElement.select("td[data-stat=\"re24_bat\"]").first().firstChild().toString()));

            if (positionsPlayed.stream().noneMatch(position -> position.equals(BaseballReferencePosition.DESIGNATED_HITTER)) || positionsPlayed.size() > 1) {
                positionPlayerStats.setPutOuts(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"PO\"]").first().firstChild().toString()));
                positionPlayerStats.setAssists(Integer.parseInt(positionPlayerElement.select("td[data-stat=\"A\"]").first().firstChild().toString()));
            }
        }


        Node positionPlayerDetailsNode = positionPlayerElement.select("td[data-stat=\"details\"]").first().firstChild();
        if (positionPlayerDetailsNode != null) {
            setPositionPlayerDetails(positionPlayerStats, positionPlayerDetailsNode.toString());
        } else {
            setPositionPlayerDetails(positionPlayerStats, null);
        }
        return positionPlayerStats;
    }

    private static void setPositionPlayerDetails(PositionPlayerStats positionPlayerStats, String details) {
        String[] detailEntries;
        if (details == null) {
            detailEntries = new String[0];
        } else {
            detailEntries = details.split(",");
        }

        int doubles = 0;
        int triples = 0;
        int homeRuns = 0;
        int hitByPitch = 0;
        int groundedIntoDoublePlay = 0;
        int intentionalWalks = 0;
        int stolenBases = 0;
        int caughtStealing = 0;
        int sacrificeHits = 0;
        int sacrificeFlys = 0;

        for (String detailEntry : detailEntries) {
            BaseballReferenceDetails detail;
            int occurrences = 1;
            if (detailEntry.contains("·")) {
                String[] multiEventDetail = detailEntry.split("·");
                occurrences = Integer.parseInt(multiEventDetail[0]);
                detail = BaseballReferenceDetails.fromString(multiEventDetail[1]);
            } else {
                detail = BaseballReferenceDetails.fromString(detailEntry);
            }
            switch (detail) {
                case DOUBLE:
                    doubles += occurrences;
                    break;
                case TRIPLE:
                    triples += occurrences;
                    break;
                case HOME_RUN:
                    homeRuns += occurrences;
                    break;
                case INTENTIONAL_WALKS:
                    intentionalWalks += occurrences;
                    break;
                case HIT_BY_PITCH:
                    hitByPitch += occurrences;
                    break;
                case GROUNDED_INTO_DOUBLE_PLAY:
                    groundedIntoDoublePlay += occurrences;
                    break;
                case STOLEN_BASE:
                    stolenBases += occurrences;
                    break;
                case CAUGHT_STEALING:
                    caughtStealing += occurrences;
                    break;
                case SACRIFICE_HIT:
                    sacrificeHits += occurrences;
                    break;
                case SACRIFICE_FLY:
                    sacrificeFlys += occurrences;
                    break;
            }
        }

        positionPlayerStats.setSingles(positionPlayerStats.getHits() - (doubles + triples + homeRuns));
        positionPlayerStats.setDoubles(doubles);
        positionPlayerStats.setTriples(triples);
        positionPlayerStats.setHomeRuns(homeRuns);
        positionPlayerStats.setHitByPitch(hitByPitch);
        positionPlayerStats.setGroundedIntoDoublePlay(groundedIntoDoublePlay);
        positionPlayerStats.setIntentionalWalks(intentionalWalks);
        positionPlayerStats.setStolenBases(stolenBases);
        positionPlayerStats.setCaughtStealing(caughtStealing);
        positionPlayerStats.setSacrificeHits(sacrificeHits);
        positionPlayerStats.setSacrificeFlys(sacrificeFlys);
    }

    public static String getGameInfo(Document gameDocument) {
        return gameDocument.select("h1").first().firstChild().toString().replace(" Box Score", "");
    }
}
