package texcop;

import texcop.cop.Offense;

import java.nio.file.Path;
import java.util.List;

public interface FileTask extends Command {
    /**
     * Executes the command on a file.
     */
    List<Offense> execute(Path file);

    /**
     * Returns the file extension this file task can be used with.
     * Defaults to ".tex" files.
     *
     * @return the file extension this file task can be used with
     */
    default String getFileExtension() {
        return ".tex";
    }
}
