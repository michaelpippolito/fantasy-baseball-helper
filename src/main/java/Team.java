import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Team {
    ATLANTA_BRAVES("ATL", "Atlanta Braves"),
    HOUSTON_ASTROS("HOU", "Houston Astros"),
    LOS_ANGELES_ANGELS("LAA", "Los Angeles Angels"),
    LOS_ANGELES_DODGERS("LAD", "Los Angeles Dodgers"),
    NEW_YORK_METS("NYM", "New York Mets"),
    NEW_YORK_YANKEES("NYY", "New York Yankees"),
    PHILADELPHIA_PHILLIES("PHI", "Philadelphia Phillies"),
    TAMPA_BAY_RAYS("TBR", "Tampa Bay Rays"),
    TORONTO_BLUE_JAYS("TOR", "Toronto Blue Jays"),
    SAN_DIEGO_PADRES("SDP", "San Diego Padres"),
    SEATTLE_MARINERS("SEA", "Seattle Mariners");

    @Getter
    private String abbreviation;

    @Getter
    private String name;
}
