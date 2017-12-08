package texcop.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import texcop.FileTask;
import texcop.cop.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates all .tex files within the current directory and its descendants.
 * <p/>
 * Rules adopted by chktex (http://baruch.ev-en.org/proj/chktex/)
 */
public class ValidateLatex implements FileTask {

    private Config config;
    private List<RegexCop> cops = new ArrayList<>();

    public ValidateLatex() {
        loadConfig();
        loadRules();
    }

    @Override
    public String getName() {
        return "validate-latex";
    }

    @Override
    public String getDescription() {
        return "validates .tex files";
    }

    @Override
    public List<Offense> execute(Path filePath) {
        final List<Offense> offenses = new ArrayList<>();

        for (RegexCop cop : cops) {
            CopConfig cc = config.forCop(getName());
            if (cc != null && cc.isEnabled()) {
                offenses.addAll(cop.execute(filePath));
            }
        }

        return offenses;
    }

    private static Map<String, String> getRules() {
        Map<String, String> rules = new HashMap<>();
        rules.put("^\\\\footnote(\\{|\\[)", "line starts with footnote");
        rules.put(" \\\\label\\{", "space in front of label");
        rules.put("\\\\caption\\{.*\\\\ac\\{", "acronym in caption");
        rules.put(" \\\\footnote(\\{|\\[)", "space in front of footnote");
        rules.put("[^~]\\\\ref", "use '~\\ref' to prevent bad line breaks");
        rules.put("(?<!et( |~)al)\\.~?\\\\cite", "use cite before the dot"); // use negative lookbehind in regex
        rules.put("[^~\\{\\}]\\\\cite[^tp]", "use '~\\cite' to prevent bad line breaks");
        rules.put("But ", "use 'A few words, however, ...' instead");
        rules.put("(While|, while) ", "use 'Although' instead");
        rules.put("''\\.", "move . into quotes");
        rules.put("[Bb]ecause of this", "use hence instead of because of this");
        rules.put("(Java|activiti|camunda~BPM|ODE) \\d+", "Instead of Java 8, use Java~8");

        rules.put("\\b(from| in|and|with|see|In|From|With|And|See)( |~)\\\\cite[^t]", "instead of 'in [x]' use 'Harrer et al. [x]'");

        rules.put("(table|figure|section|listing|chapter|theorem|corollary|definition)~\\\\ref",
                "capitalize Table, Figure, Listing, Section, Chapter, Theorem, Corollary, Definition; use abbreviations: Table, Fig., Sect., Chap., Theorem, Corollary, Definition when used with numbers, e.g. Fig.3, Table 1, Theorem 2");
        rules.put("[0-9]%", "% sign after number is normally invalid");

        rules.put("e\\.g\\.[^,]", "use 'e.g.,' instead of 'e.g.'");
        rules.put("i\\.e\\.[^,]", "use 'i.e.,' instead of 'i.e.'");

        rules.put("cf\\.[^\\\\]", "use 'cf.\\ ' instead of 'cf. '");

        rules.put("(All|The|Of|all|the|of)( |~)[0-9][^0-9]", "write the numbers out, e.g., one out of three");

        rules.put("et\\. al\\.", "use 'et al.' instead of 'et. al.'");
        rules.put("et\\ al\\.", "use 'et~al.' instead of 'et al.'");

        rules.put("\\b[Nn]on[- ]", "join non with word, e.g., nonfunctional instead of non-functional or non functional");

        rules.put("\\b[Tt]eh\\b", "use 'the' instead of 'teh'");

        rules.put("[ ],", "no space before a comma");
        rules.put(",,", "no double comma");

        rules.put("(In|in) order to", "instead of 'in order to' use 'to'");

        rules.put("behaviour", "Use the AE when possible: 'behavior'");

        rules.put("(all of the) ", "Instead of 'all of the' use 'all the'");
        rules.put("Tt ", "Use It instead of Tt");
        rules.put(" a bit ", "Too informal (a bit)");
        rules.put("( a|A) lot of ", "Too informal (a lot of)");
        rules.put("( a|A) couple of ", "Too informal (a couple of)");
        rules.put(" till ", "Too informal (till)");
        rules.put("((?: t|T)hing(?: |s |\\.|s\\.))", "Too informal (thing)");
        rules.put(" (always) ", "Too exaggerated (always)");
        rules.put(" (never) ", "Too exaggerated (never)");

        rules.put("\\[(pp|p)\\. [0-9]+\\]", "Use ~ instead ([p.~4]");
        rules.put("\\\\footnote\\{See \\\\url\\{[^\\}]+\\}\\}", "Remove 'see' as it is unnecessary");

        return rules;
    }

    private static Map<Pattern, String> getCompiledRules() {
        Map<Pattern, String> rules = new HashMap<>();
        for (Map.Entry<String, String> entry : getRules().entrySet()) {
            rules.put(Pattern.compile(entry.getKey()), entry.getValue());
        }
        return rules;
    }

    public static final Map<Pattern, String> COMPILED_RULES = getCompiledRules();

    private void loadConfig() {
        final String configFile = ".texcop.yml";

        if (!Files.exists(Paths.get(configFile))) {
            config = new Config();
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            config = mapper.readValue(Paths.get(configFile).toFile(), Config.class);
        } catch (Exception e) {
            System.err.println("Error reading .texcop.yml: " + e.getMessage());
        }
    }

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

    private List<Offense> applyPattern(Path texFile, int lineNumber, String line, Pattern pattern, String message) {
        List<Offense> offenses = new ArrayList<>();
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            int column = 0;
            int length = 0;

            if (matcher.groupCount() >= 1) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    column = matcher.start(i);
                    length = matcher.end(i) - matcher.start(i);
                }
            } else {
                column = matcher.start();
                length = matcher.end() - matcher.start();
            }

            Location location = new Location(line, lineNumber, column, length);
            offenses.add(new Offense(location, message));
        }
        return offenses;
    }
}