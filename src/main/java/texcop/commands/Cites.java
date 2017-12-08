package texcop.commands;

import texcop.FileTask;
import texcop.commands.latex.Latex;
import texcop.cop.Offense;
import texcop.tasks.FileSystemTasks;

import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cites implements FileTask {

    @Override
    public String getName() {
        return "cites";
    }

    @Override
    public String getDescription() {
        return "Print used cites";
    }

    public void execute() {
        Map<String, Integer> citations = getCitations();

        // print result
        List<String> cites = new ArrayList<>();
        for (Map.Entry<String, Integer> citation : citations.entrySet()) {
            cites.add(citation.getKey() + " [" + citation.getValue() + "]");
        }
        cites.sort(Collator.getInstance());
        for (String cite : cites) {
            System.out.println(cite);
        }
        System.out.println("========");
        int total = 0;
        int count = 0;
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (Integer integer : citations.values()) {
            total += integer;
            max = Math.max(max, integer);
            min = Math.min(min, integer);
            count++;
        }
        double avg = Math.round((double) total / count);

        System.out.println("Sum [" + total + "], Min [" + min + "], Max [" + max + "], Avg [" + avg + "]");
    }

    @Override
    public List<Offense> execute(Path file) {
        return null;
    }

    private Map<String, Integer> getCitations() {
        Map<String, Integer> citations = new HashMap<>();

        List<Path> texFiles = new FileSystemTasks().getFilesByExtension(".tex");

        Latex.with(texFiles, (line, lineNumber, file) -> {
            // only validate if line is not commented out
            if (!line.startsWith("%")) {
                String regex = "\\\\cite\\{([^\\}]*)\\}";
                Matcher matcher = Pattern.compile(regex).matcher(line);
                while (matcher.find()) {
                    String match = matcher.group(1);
                    String[] matches = match.split(",");
                    for (String m : matches) {
                        if (citations.containsKey(m)) {
                            citations.put(m, citations.get(m) + 1);
                        } else {
                            citations.put(m, 1);
                        }
                    }
                }
            }
        });

        return citations;
    }

}
