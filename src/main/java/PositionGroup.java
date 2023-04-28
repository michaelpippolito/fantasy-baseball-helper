import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PositionGroup {
    BULLPEN("Bullpen"),
    INFIELD("Infield"),
    OUTFIELD_DH("Outfield/DH"),
    ROTATION("Rotation");

    @Getter
    private final String name;

    public boolean isPitcher() {
        return this.equals(BULLPEN) || this.equals(ROTATION);
    }
}
