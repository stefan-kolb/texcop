package textools;

import textools.cop.Offense;

import java.nio.file.Path;
import java.util.List;

public interface FileTask extends Command {
    /**
     * Executes the command on a file.
     */
    List<Offense> execute(Path file);
}
