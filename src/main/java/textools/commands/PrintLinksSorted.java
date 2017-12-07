package textools.commands;

import textools.FileTask;
import textools.commands.latex.Latex;
import textools.commands.latex.Link;
import textools.cop.Offense;
import textools.tasks.FileSystemTasks;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Find acronyms defined in the acronym package but that are not yet included.
 */
public class PrintLinksSorted implements FileTask {

    @Override
    public String getName() {
        return "print-links";
    }

    @Override
    public String getDescription() {
        return "prints all used urls";
    }

    public void execute() {
        List<Path> texFiles = new FileSystemTasks().getFilesByExtension(".tex");

        Set<Link> links = new HashSet<>();
        Latex.with(texFiles, (line, lineNumber, file) -> links.addAll(Link.find(line, lineNumber, file)));

        links.stream().map(l -> l.url).sorted().forEach(System.out::println);
    }

    @Override
    public List<Offense> execute(Path file) {
        return null;
    }
}
