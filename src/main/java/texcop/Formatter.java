package texcop;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import texcop.cop.Offense;
import texcop.tasks.FileSystemTasks;

public class Formatter {

    private int offensesCount;

    public void started(int fileCount) {
        System.out.println(String.format("Inspecting %d files", fileCount));
    }

    public void fileFinished(Path filePath, List<Offense> offenses) {
        for (Offense offense : offenses) {
            reportOffense(filePath, offense);
        }
    }

    public void finished(int fileCount) {
        System.out.println();
        System.out.println(String.format("%d files inspected, %d offenses detected", fileCount, offensesCount));
    }

    private void reportOffense(Path filePath, Offense offense) {
        offensesCount++;

        System.out.format("%s:%d:%d: %s%n", FileSystemTasks.workingDirectory.relativize(filePath), offense.location.line, offense.location.column, offense.message);
        System.out.format("%s%n", offense.location.sourceLine);
        if (offense.location.column != 0) {
            System.out.format("%" + offense.location.column + "s%s\n", "", String.join("", Collections.nCopies(offense.location.length, "^")));
        } else {
            System.out.format("%s%s\n", "", String.join("", Collections.nCopies(offense.location.length, "^")));
        }
    }
}
