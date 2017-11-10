package textools;

import java.nio.file.Path;
import java.util.Collections;

import textools.cop.Offense;
import textools.tasks.FileSystemTasks;

public class Formatter {

    public void reportOffense(Path filePath, Offense offense) {
        System.out.format("%s:%d:%d: %s%n", FileSystemTasks.workingDirectory.relativize(filePath), offense.location.line, offense.location.column, offense.message);
        System.out.format("%s%n", offense.location.sourceLine);
        System.out.format("%" + offense.location.column + "s%s\n", "", String.join("", Collections.nCopies(offense.location.length, "^")));
    }
}
