package texcop.commands;

import texcop.ActionTask;
import texcop.Constants;
import texcop.tasks.FileSystemTasks;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static texcop.tasks.ConsoleTasks.executeWithLog;

public class Pdf implements ActionTask {

    private static final String TEXTOOLS_PDF_LOG = "texcop-pdf.log";

    @Override
    public String getName() {
        return "pdf";
    }

    @Override
    public String getDescription() {
        return "creates pdf with pdflatex, including bibtex; logs to " + TEXTOOLS_PDF_LOG;
    }

    @Override
    public void execute() {
        String mainLatexFile = getMainLatexFileWithoutExtension();
        System.out.println("Using " + mainLatexFile);

        new FileSystemTasks().deleteFile(Paths.get(TEXTOOLS_PDF_LOG));

        pdflatex(mainLatexFile);
        bibtex(mainLatexFile);
        pdflatex(mainLatexFile);
        pdflatex(mainLatexFile);
        pdflatex(mainLatexFile);
    }

    private void bibtex(String mainLatexFile) {
        if (mainLatexFile.contains(".tex")) {
            mainLatexFile = mainLatexFile.substring(0, mainLatexFile.lastIndexOf(".tex"));
        }
        executeWithLog("bibtex " + mainLatexFile, TEXTOOLS_PDF_LOG);
    }

    private void pdflatex(String mainLatexFile) {
        executeWithLog("pdflatex -interaction=nonstopmode " + mainLatexFile, TEXTOOLS_PDF_LOG);
    }

    private String getMainLatexFileWithoutExtension() {
        List<String> mainTexFiles = new ArrayList<>();
        mainTexFiles.add(Texlipse.getMainTexFile(Paths.get(".")));
        mainTexFiles.add(TexnicCenter.getMainTexFile(Paths.get(".")));
        mainTexFiles.add(Constants.MAIN_LATEX_FILE);

        // remove all NOT_FOUND entries
        while (mainTexFiles.contains(Constants.NOT_FOUND)) {
            mainTexFiles.remove(Constants.NOT_FOUND);
        }

        // return first element
        return mainTexFiles.get(0);
    }

}
