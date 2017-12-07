package textools;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import textools.commands.Cites;
import textools.commands.Clean;
import textools.commands.CreateGitignore;
import textools.commands.Help;
import textools.commands.MinifyBibtexAuthors;
import textools.commands.MinifyBibtexOptionals;
import textools.commands.Pdf;
import textools.commands.PdfClean;
import textools.commands.PrintLinksSorted;
import textools.commands.Texlipse;
import textools.commands.Texniccenter;
import textools.commands.Validate;
import textools.commands.ValidateAcronym;
import textools.commands.ValidateBibtex;
import textools.commands.ValidateLabels;
import textools.commands.ValidateLatex;
import textools.commands.ValidateLinks;
import textools.commands.Version;
import textools.cop.Offense;
import textools.tasks.FileSystemTasks;

public class Runner {
    private static final Command DEFAULT = new Help();

    private final Formatter formatter = new Formatter();

    private static boolean failFast;
    private int inspectedFiles = 0;

    private final String commandName;

    public static final List<Command> COMMANDS = Stream.of(
            new CreateGitignore(),
            new Clean(),
            new Cites(),
            new Texlipse(),
            new Texniccenter(),
            new Validate(),
            new ValidateBibtex(),
            new ValidateLatex(),
            new ValidateAcronym(),
            new ValidateLabels(),
            new MinifyBibtexOptionals(),
            new MinifyBibtexAuthors(),
            new Pdf(),
            new PdfClean(),
            new ValidateLinks(),
            new Version(),
            new PrintLinksSorted(),
            DEFAULT
    ).sorted(Comparator.comparing(Command::getName)).collect(Collectors.toList());

    public Runner(String commandName) {
        this.commandName = commandName;
    }

    public static void setFailFast(boolean failFast) {
        Runner.failFast = failFast;
    }

    public void run() {
        Command command = findCommandByName(commandName);
        try {
            List<Path> texFiles = new FileSystemTasks().getFilesByExtension(".tex");
            formatter.started(texFiles.size());
            for (Path file: texFiles) {
                List<Offense> offenses = command.run(file);
                inspectedFiles++;
                formatter.fileFinished(file, offenses);

                if (!offenses.isEmpty() && failFast) {
                    break;
                }
            }
            formatter.finished(inspectedFiles);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static Command findCommandByName(String command) {
        for (Command task : COMMANDS) {
            if (command.equals(task.getName())) {
                return task;
            }
        }
        return DEFAULT;
    }

}
