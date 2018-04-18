package texcop.cop.latex;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import texcop.FileTask;
import texcop.commands.latex.Latex;
import texcop.cop.Config;
import texcop.cop.Location;
import texcop.cop.Offense;

/**
 * TODO: detect standard environments such as math
 * https://en.wikibooks.org/wiki/LaTeX/Mathematics#Mathematics_environments
 */
public class TextInsideMathMode implements FileTask {
    private static final String MESSAGE = "Use \\mathit, \\text or \\textnormal for multiletter words in math mode";
    private static final Pattern MATH_MODE = Pattern.compile("(?:\\$|\\\\\\(|\\\\\\[)(.*?)(?:\\$|\\\\\\)|\\\\\\])");
    private static final Pattern MULTILETTER = Pattern.compile("\\\\[a-zA-z]+\\{.*?}|\\\\[a-zA-z]+|([a-zA-Z]{2,})");

    private Config config;

    @Override
    public String getName() {
        return "Latex/TextInsideMathMode";
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

        Matcher matcher = MATH_MODE.matcher(line);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                Matcher words = MULTILETTER.matcher(matcher.group(i));
                while (words.find()) {
                    for (int j = 1; j <= words.groupCount(); j++) {
                        if (words.group(j) != null) {
                            int column = matcher.start(i) + words.start(j);
                            int length = words.end(j) - words.start(j);
                            Location location = new Location(line, lineNumber, column, length);
                            offenses.add(new Offense(location, MESSAGE));
                        }
                    }
                }
            }
        }
        return offenses;
    }
}
