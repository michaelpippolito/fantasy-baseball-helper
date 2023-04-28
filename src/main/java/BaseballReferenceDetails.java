import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BaseballReferenceDetails {
    DOUBLE("2B"),
    TRIPLE("3B"),
    HOME_RUN("HR"),
    SACRIFICE_HIT("SH"),
    SACRIFICE_FLY("SF"),
    INTENTIONAL_WALKS("IW"),
    HIT_BY_PITCH("HBP"),
    GROUNDED_INTO_DOUBLE_PLAY("GDP"),
    STOLEN_BASE("SB"),
    CAUGHT_STEALING("CS");

    @Getter
    private final String webValue;

    public static BaseballReferenceDetails fromString(String text) {
        for (BaseballReferenceDetails details : BaseballReferenceDetails.values()) {
            if (details.getWebValue().equals(text)) {
                return details;
            }
        }
        return null;
    }
}
