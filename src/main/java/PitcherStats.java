import lombok.Getter;
import lombok.Setter;

public class PitcherStats implements Stats {
    @Getter @Setter
    private double inningsPitched;

    @Getter @Setter
    private int hits;

    @Getter @Setter
    private int runs;

    @Getter @Setter
    private int earnedRuns;

    @Getter @Setter
    private int walks;

    @Getter @Setter
    private int strikeouts;

    @Getter @Setter
    private int homeRuns;

    @Getter @Setter
    private double earnedRunAverage;

    @Getter @Setter
    private int battersFaced;

    @Getter @Setter
    private int pitches;

    @Getter @Setter
    private int strikeOuts;

    @Getter @Setter
    private boolean isReliefAppearance; // marker in case a starter makes a relief appearance, otherwise can take WAR directly from baseball-reference
}
