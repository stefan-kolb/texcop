package textools.commands;

import textools.ActionTask;

public class PdfClean implements ActionTask {

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
}