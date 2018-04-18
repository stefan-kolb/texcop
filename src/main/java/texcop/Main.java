package texcop;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import texcop.commands.Cites;
import texcop.commands.Clean;
import texcop.commands.CreateGitignore;
import texcop.commands.Help;
import texcop.commands.MinifyBibtexAuthors;
import texcop.commands.MinifyBibtexOptionals;
import texcop.commands.Pdf;
import texcop.commands.PdfClean;
import texcop.commands.PrintLinksSorted;
import texcop.commands.Texlipse;
import texcop.commands.TexnicCenter;
import texcop.commands.Validate;
import texcop.commands.ValidateAcronym;
import texcop.commands.ValidateBibtex;
import texcop.commands.ValidateLabels;
import texcop.commands.ValidateLatex;
import texcop.commands.ValidateLinks;
import texcop.commands.Version;

public class Main {

    public static final List<Command> COMMANDS = Stream.of(
            new CreateGitignore(),
            new Clean(),
            new Cites(),
            new Texlipse(),
            new TexnicCenter(),
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
            new Help()
    ).sorted(Comparator.comparing(Command::getName)).collect(Collectors.toList());

    public static void main(String... args) {
        if (args == null || args.length == 0) {
            new Help().execute();
            System.exit(0);
        }

        List<String> arguments = Arrays.asList(args);
        // Command
        String commandName = arguments.get(0);
        Runner runner = new Runner(commandName);
        // Options
        if (arguments.contains("-F") || arguments.contains("--fail-fast")) {
            runner.setFailFast(true);
        }
        if (arguments.contains("--auto-gen-config")) {
            runner.generateConfig(true);
        }

        runner.run();
    }

    private Main() {}
}
