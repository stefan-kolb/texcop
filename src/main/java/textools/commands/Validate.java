package textools.commands;

import textools.FileTask;
import textools.cop.Offense;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Validate implements FileTask {

    @Override
    public String getName() {
        return "validate";
    }

    @Override
    public String getDescription() {
        return "executes validate-latex and validate-bibtex commands in sequence";
    }

    @Override
    public List<Offense> execute(Path file) {
        List<Offense> lists = new ArrayList<>();
        lists.addAll(new ValidateBibtex().execute(file));
        lists.addAll(new ValidateLatex().execute(file));
        return lists;
    }
}
