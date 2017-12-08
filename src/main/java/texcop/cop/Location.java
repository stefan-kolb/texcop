package texcop.cop;

public class Location {
    public final String sourceLine;
    public final int line;
    public final int column;
    public final int length;

    public Location(String sourceLine, int lineNumber, int columnNumber, int length) {
        this.sourceLine = sourceLine;
        this.line = lineNumber;
        this.column = columnNumber;
        this.length = length;
    }
}
