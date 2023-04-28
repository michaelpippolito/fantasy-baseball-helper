import lombok.Getter;
import lombok.Setter;

public class PositionPlayerStats implements Stats {
    @Getter @Setter
    private int atBats;

    @Getter @Setter
    private int runs;

    @Getter @Setter
    private int hits;

    @Getter @Setter
    private int runsBattedIn;

    @Getter @Setter
    private int walks;

    @Getter @Setter
    private int strikouts;

    @Getter @Setter
    private int plateAppearances;

    @Getter @Setter
    private double battingAverage;

    @Getter @Setter
    private double onBasePercentage;

    @Getter @Setter
    private double slugging;

    @Getter @Setter
    private double OPS;

    @Getter @Setter
    private int pitchesSeen;

    @Getter @Setter
    private int strikesSeen;

    @Getter @Setter
    private double winProbabilityAdded;

    @Getter @Setter
    private double averageLeverageIndex;

    @Getter @Setter
    private double winProbabilityAddedPlus;

    @Getter @Setter
    private double basesOutRunsAdded;

    @Getter @Setter
    private double putOuts;

    @Getter @Setter
    private double assists;

    @Getter @Setter
    private int singles;

    @Getter @Setter
    private int doubles;

    @Getter @Setter
    private int triples;

    @Getter @Setter
    private int homeRuns;

    @Getter @Setter
    private int hitByPitch;

    @Getter @Setter
    private int groundedIntoDoublePlay;

    @Getter @Setter
    private int intentionalWalks;

    @Getter @Setter
    private int stolenBases;

    @Getter @Setter
    private int caughtStealing;

    @Getter @Setter
    private int sacrificeHits;

    @Getter @Setter
    private int sacrificeFlys;

}
