package textools.commands;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import textools.Command;
import textools.commands.latex.Latex;
import textools.commands.latex.Link;
import textools.cop.Location;
import textools.cop.Offense;
import textools.tasks.FileSystemTasks;

/**
 * Find acronyms defined in the acronym package but that are not yet included.
 */
public class ValidateLinks implements Command {

    @Override
    public String getName() {
        return "validate-links";
    }

    @Override
    public String getDescription() {
        return "detects malformed and unreachable urls";
    }

    @Override
    public void execute() {
        List<Path> texFiles = new FileSystemTasks().getFilesByExtension(".tex");

        Set<Link> links = new HashSet<>();
        Latex.with(texFiles, (line, lineNumber, file) -> links.addAll(Link.find(line, lineNumber, file)));

        final Map<String, List<Link>> urlToLink = links.stream().collect(Collectors.groupingBy(l -> l.url));
        urlToLink.forEach((url, ls) -> {
            if(ls.size() > 1) {
                System.out.format("URL %s is duplicated in %s%n", url, ls.stream().map(l -> l.file + "#" + l.location.line).collect(Collectors.joining(";")));
            }
        });

        links.stream()
                .parallel()
                .filter(link -> {
                    try {
                        link.validateUrl();
                        return true;
                    } catch (MalformedURLException e) {
                        System.out.format("%s#%4d URL %s is malformed %s%n", link.file, link.location.line, link.url, e.getMessage());
                        return false;
                    }
                })
                .forEach(link -> {
                    try {
                        final int statusCode = link.getStatusCode();
                        if (statusCode < 0) {
                            System.out.format("%s#%4d URL %s returned no response%n", link.file, link.location.line, link.url);
                        } else if (statusCode != 200) {
                            System.out.format("%s#%4d URL %s return http status code %d%n", link.file, link.location.line, link.url, statusCode);
                        }
                    } catch (IOException e) {
                        System.out.format("%s#%4d URL %s %s%n", link.file, link.location.line, link.url, e.getMessage());
                    }
                });
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
                offenses.add(new Offense(link.location, String.format("URL return http status code %d%n", statusCode)));
            }
        } catch (IOException e) {
            offenses.add(new Offense(link.location, String.format("URL %s %s%n", link.url, e.getMessage())));
        }
        return offenses;
    }

    @Override
    public List<Offense> run(Path filePath) {
        Set<Link> links = new HashSet<>();
        Latex.with(filePath, (line, lineNumber, file) -> {
            links.addAll(Link.find(line, lineNumber, file));
        });
        return links.stream()
                .parallel()
                .map(this::checkLinks)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
