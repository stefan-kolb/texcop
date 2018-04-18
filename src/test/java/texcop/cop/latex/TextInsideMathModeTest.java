package texcop.cop.latex;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import texcop.cop.Offense;

import static org.junit.Assert.*;

public class TextInsideMathModeTest {
    private TextInsideMathMode cop;

    @Before
    public void init() {
        cop = new TextInsideMathMode();
    }

    @Test
    public void emptyLine() {
        List<Offense> offenses = cop.applyPattern(null, 0, "");
        assertEquals(0, offenses.size());
    }

    @Test
    public void noMathEnvironment() {
        List<Offense> offenses = cop.applyPattern(null, 0, "this is only text");
        assertEquals(0, offenses.size());
    }

    @Test
    public void noText() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$f(x) = a^2 + b^2 + c$");
        assertEquals(0, offenses.size());
    }

    @Test
    public void dollarMathEnvironment() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$f(x) = undefined$");
        assertEquals(1, offenses.size());
    }

    @Test
    public void correctLocation() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$f(x) = undefined$");
        assertEquals(8, offenses.get(0).location.column);
        assertEquals(9, offenses.get(0).location.length);
    }

    @Test
    public void correctLocationWithTabs() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$\tf(x) = undefined$");
        assertEquals(9, offenses.get(0).location.column);
        assertEquals(9, offenses.get(0).location.length);
    }

    @Test
    public void correctLocationWithSpaces() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$    f(x) = undefined$");
        assertEquals(12, offenses.get(0).location.column);
        assertEquals(9, offenses.get(0).location.length);
    }

    @Test
    public void ignoreMacros() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$f(x) = \\undefined{text}$");
        assertEquals(0, offenses.size());
    }

    @Test
    public void ignoreCommands() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$f(x) = \\undefined$");
        assertEquals(0, offenses.size());
    }

    @Test
    public void notBetweenEnvironments() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$f(x)$ text $f(x)$");
        assertEquals(0, offenses.size());
    }

    @Test
    public void insideParentheses() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$N_{text}$");
        assertEquals(1, offenses.size());
    }

    @Test
    public void correctInsideParentheses() {
        List<Offense> offenses = cop.applyPattern(null, 0, "$N_{\\mathit{text}}$");
        assertEquals(0, offenses.size());
    }
}