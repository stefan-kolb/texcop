package texcop.cop;

import texcop.FileTask;
import texcop.commands.latex.Latex;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexCop implements FileTask {
    private Config config;

    private String name;
    private List<Pattern> matches;
    private String message;

    private boolean disabled;

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
            lookForInlineConfig(line);

            // TODO skip comments again properly
            if (line.startsWith("%")) {
                return;
            }

            if (!disabled) {
                for (Pattern pattern : matches) {
                    offenses.addAll(applyPattern(line, lineNumber, pattern, message));
                }
            }
        });
        return offenses;
    }

    private void lookForInlineConfig(String line) {
        // % texcop:disable Style/AmericanEnglish, Style/KeyboardWarrior
        final String inlineCop = "% texcop:";
        final String inlineEnable = inlineCop + "enable";
        final String inlineDisable = inlineCop + "disable";

        if (line.startsWith(inlineCop)) {
            boolean enable = line.startsWith(inlineEnable);
            boolean disable = line.startsWith(inlineDisable);
            String command = enable ? inlineEnable : inlineDisable;

            String[] cops = line.replaceFirst(command, "").split(",");
            boolean matchCop = Arrays.stream(cops).map(String::trim).filter(c -> this.getName().equals(c)).count() > 0;
            // TODO auto enable after one line?
            if (matchCop) {
                if (enable) {
                    disabled = false;
                } else if (disable) {
                    disabled = true;
                }
            }
        }
    }

    private boolean isComment(String line) {
        return line.startsWith("%");
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
