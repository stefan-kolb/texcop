package textools.commands;

import textools.FileTask;
import textools.commands.acronym.Acronym;
import textools.commands.latex.Latex;
import textools.cop.Offense;
import textools.tasks.FileSystemTasks;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Find acronyms defined in the acronym package but that are not yet included.
 */
public class ValidateAcronym implements FileTask {

    @Override
    public String getName() {
        return "validate-acronym";
    }

    @Override
    public String getDescription() {
        return "detects unmarked acronyms in text";
    }

    public void execute() {
        List<Path> texFiles = new FileSystemTasks().getFilesByExtension(".tex");

        Set<Acronym> acronyms = new HashSet<>();
        Latex.with(texFiles, (line, lineNumber, file) ->  Acronym.find(line).ifPresent(acronyms::add));

        Latex.with(texFiles, (line, lineNumber, file) ->  {
            for (Acronym acro : acronyms) {
                if (acro.isAbbreviationInLine(line)) {
                    System.out.format("%s:%d:%s%n", file.toString(), lineNumber, acro.getName());
                } else if(acro.isLongInLine(line)) {
                    System.out.format("%s:%d:%s%n", file.toString(), lineNumber, acro.getLongName());
                }
            }
        });
    }

    @Override
    public List<Offense> execute(Path file) {
        return null;
    }
}
