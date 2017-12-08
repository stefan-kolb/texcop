package texcop.commands;

import texcop.FileTask;
import texcop.commands.latex.Latex;
import texcop.commands.latex.Link;
import texcop.cop.Offense;
import texcop.tasks.FileSystemTasks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Find acronyms defined in the acronym package but that are not yet included.
 */
public class ValidateLinks implements FileTask {

    @Override
    public String getName() {
        return "validate-links";
    }

    @Override
    public String getDescription() {
        return "detects malformed and unreachable urls";
    }

    private List<Offense> checkLinks(Link link) {
        List<Offense> offenses = new ArrayList<>();
        /*
        final Map<String, List<Link>> urlToLink = links.stream().collect(Collectors.groupingBy(l -> l.url));
        urlToLink.forEach((url, ls) -> {
            if(ls.size() > 1) {
                System.out.format("URL %s is duplicated in %s%n", url, ls.stream().map(l -> l.file + "#" + l.lineNumber).collect(Collectors.joining(";")));
            }
        });
        */
        try {
            link.validateUrl();
        } catch (MalformedURLException e) {
            offenses.add(new Offense(link.location, String.format("URL is malformed %s%n", e.getMessage())));
        }

        try {
            final int statusCode = link.getStatusCode();
            if (statusCode < 0) {
                offenses.add(new Offense(link.location, "URL returned no response"));
            } else if (statusCode != 200) {
                offenses.add(new Offense(link.location, String.format("URL returned HTTP status code %d%n", statusCode)));
            }
        } catch (IOException e) {
            offenses.add(new Offense(link.location, String.format("URL %s %s%n", link.url, e.getMessage())));
        }
        return offenses;
    }

    @Override
    public List<Offense> execute(Path filePath) {
        List<Link> links = new ArrayList<>();
        Latex.with(filePath, (line, lineNumber, file) -> links.addAll(Link.find(line, lineNumber, file)));
        return links.stream()
                .parallel()
                .map(this::checkLinks)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
