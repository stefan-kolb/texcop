package textools.cop;

public class Offense {
    public final Location location;
    public final String message;

    public Offense(Location location, String message) {
        this.location = location;
        this.message = message;
    }
}
