package texcop.commands;

import org.junit.Test;
import texcop.commands.latex.Link;

import static org.junit.Assert.assertEquals;

public class LinkTest {

    @Test
    public void validateUrl() throws Exception {
        Link link = new Link("http://esbperformance.org/", null, null);
        assertEquals(200, link.getStatusCode());
    }

}
