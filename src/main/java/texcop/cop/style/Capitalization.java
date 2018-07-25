package texcop.cop.style;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import texcop.FileTask;
import texcop.commands.latex.Latex;
import texcop.cop.Config;
import texcop.cop.Location;
import texcop.cop.Offense;

/**
 * Chicago style capitalization of titles
 */
public class Capitalization implements FileTask {
    private static final String MESSAGE = "Use capitalization for titles";
    private static final Pattern TITLES = Pattern.compile("\\\\(?:part|chapter|section|subsection|subsubsection|paragraph|subparagraph|caption)\\{(.*?)\\}");
    private static final List<String> EXCLUDES = Arrays.asList("a", "an", "the", "to", "for", "and", "with", "nor", "but", "or", "per", "yet", "so", "of", "on", "in", "at", "since", "for", "ago", "before", "to", "past", "by", "in", "at", "into", "onto", "from", "as", "under");

    private Config config;

    @Override
    public String getName() {
        return "Style/Capitalization";
    }

    @Override
    public String getDescription() {
        return MESSAGE;
    }

    @Override
    public List<Offense> execute(Path filePath) {
        // TODO use as default impl in interface
        config = Config.load();

        final List<Offense> offenses = new ArrayList<>();
        Latex.with(filePath, (line, lineNumber, file) -> {
            offenses.addAll(applyPattern(file, lineNumber, line));
        });
        return offenses;
    }

    List<Offense> applyPattern(Path texFile, int lineNumber, String line) {
        List<Offense> offenses = new ArrayList<>();

        Matcher matcher = TITLES.matcher(line);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                List<String> words = Arrays.asList(matcher.group(i).split("\\b"));
                for (String word : words) {
                    if (word.matches("\\W+") || word.matches("[0-9]+") || EXCLUDES.contains(word.toLowerCase())) {
                        continue;
                    }
                    if (!Character.isUpperCase(word.charAt(0))) {
                        offenses.add(new Offense(new Location(line, lineNumber, matcher.start(i) + matcher.group(i).indexOf(word), word.length()), MESSAGE));
                    }
                }
            }
        }
        return offenses;
    }
}
