public class Runner {
    public static void main(String[] args) throws Exception {
        StatsByPositionGroupHelper statsByPositionGroupHelper = new StatsByPositionGroupHelper();

        Team mikeRotation = Team.HOUSTON_ASTROS;
        Team mikeInfield = Team.PHILADELPHIA_PHILLIES;
        Team mikeOutfield = Team.NEW_YORK_YANKEES;
        Team mikeBullpen = Team.SEATTLE_MARINERS;

        Team jonRotation = Team.NEW_YORK_METS;

        Team nickRotation = Team.ATLANTA_BRAVES;

        Team danRotation = Team.SAN_DIEGO_PADRES;

        System.out.println("----Calculating WAR for Mike's starting rotation----");
        statsByPositionGroupHelper.getStats(mikeRotation, PositionGroup.ROTATION);
        System.out.println();

        System.out.println("----Calculating WAR for Jon's starting rotation----");
        statsByPositionGroupHelper.getStats(jonRotation, PositionGroup.ROTATION);
        System.out.println();

        System.out.println("----Calculating WAR for Nick's starting rotation----");
        statsByPositionGroupHelper.getStats(nickRotation, PositionGroup.ROTATION);
        System.out.println();

        System.out.println("----Calculating WAR for Dan's starting rotation----");
        statsByPositionGroupHelper.getStats(danRotation, PositionGroup.ROTATION);
        System.out.println();
    }
}
