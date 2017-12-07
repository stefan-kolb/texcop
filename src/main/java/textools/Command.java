package textools;

import java.nio.file.Path;
import java.util.List;

import textools.cop.Offense;

public interface Command {

    /**
     * The name of the command is used to identify it and call it.
     *
     * @return the command name
     */
    String getName();

    /**
     * The command description. Describes the functioning of the command in one line.
     *
     * @return the command description
     */
    String getDescription();

    /**
     * Executes the command.
     */
    void execute();

    public List<Offense> run(Path file);
}
