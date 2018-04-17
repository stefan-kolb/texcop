package texcop.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.Test;
import texcop.cop.Config;
import texcop.cop.CopConfig;
import texcop.cop.Offense;
import texcop.cop.RegexCop;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ValidateLatexTest {

    private List<RegexCop> cops = new ArrayList<>();

    private void loadRules() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("texcop/cop/style/rules.yml");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Config config = mapper.readValue(is, Config.class);
            for (Map.Entry<String, CopConfig> entry : config.entrySet()) {
                cops.add(new RegexCop(entry.getKey(), entry.getValue().matches, entry.getValue().message));
            }
        } catch (Exception e) {
            System.err.println("Error reading rules.yml: " + e.getMessage());
        }
    }

    @Test
    public void testRules() throws Exception {
        loadRules();
        Path errorFile = Paths.get("src/test/resources/errors.tex");

        Set<String> matchedRules = new HashSet<>();

        List<String> violatedRules = new ArrayList<>();
        for (RegexCop entry : cops) {

            List<Offense> offenses = entry.execute(errorFile);
            if (!offenses.isEmpty()) {
                violatedRules.add(entry.getName());
                matchedRules.add(entry.getName());
            }
        }

        assertTrue(violatedRules.size() > 0);

        Set<String> untestedRules = cops.stream().map(e -> e.getName()).collect(Collectors.toSet());
        untestedRules.removeAll(matchedRules);

        assertEquals(new HashSet<String>(), untestedRules);
    }
}
