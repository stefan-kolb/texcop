package textools.cop;

import textools.FileTask;
import textools.commands.latex.Latex;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexCop implements FileTask {
    private Config config;

    private String name;
    private List<Pattern> matches;
    private String message;

    public RegexCop(String name, List<String> regEx, String message) {
        this.name = name;
        this.matches = regEx.stream().map(Pattern::compile).collect(Collectors.toList());
        this.message = message;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return message;
    }

    @Override
    public List<Offense> execute(Path filePath) {
//        CopConfig cc = config.forCop(getName());
//        if (cc != null && cc.isEnabled()) {
//            return new ArrayList<>(0);
//        }

        final List<Offense> offenses = new ArrayList<>();
        Latex.with(filePath, (line, lineNumber, file) -> {
            for (Pattern pattern : matches) {
                offenses.addAll(applyPattern(line, lineNumber, pattern, message));
            }
        });
        return offenses;
    }

    private List<Offense> applyPattern(String line, int lineNumber, Pattern pattern, String message) {
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
