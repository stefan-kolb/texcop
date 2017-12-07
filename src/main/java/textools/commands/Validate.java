package textools.commands;

import java.nio.file.Path;
import java.util.List;

import textools.Command;
import textools.cop.Offense;

public class Validate implements Command {

    @Override
    public String getName() {
        return "validate";
    }

    @Override
    public String getDescription() {
        return "executes validate-latex and validate-bibtex commands in sequence";
    }

    @Override
    public void execute() {
        new ValidateBibtex().execute();
        new ValidateLatex().execute();
    }

    @Override
    public List<Offense> run(Path file) {
        return null;
    }
}
