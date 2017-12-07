package textools.commands;

import java.nio.file.Path;
import java.util.List;

import textools.Command;
import textools.cop.Offense;

public class PdfClean implements Command {

    @Override
    public String getName() {
        return "pdfclean";
    }

    @Override
    public String getDescription() {
        return "executes pdf and clean commands in sequence";
    }

    @Override
    public void execute() {
        new Pdf().execute();
        new Clean().execute();
    }

    @Override
    public List<Offense> run(Path file) {
        return null;
    }
}