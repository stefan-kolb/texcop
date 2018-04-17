package texcop.cop;

public class Offense {
    public final Location location;
    public final String message;
    public final String copName;

    public Offense(Location location, String message) {
        this.location = location;
        this.message = message;
        this.copName = "";
    }

    public Offense(Location location, String message, String copName) {
        this.location = location;
        this.message = message;
        this.copName = copName;
    }
}
