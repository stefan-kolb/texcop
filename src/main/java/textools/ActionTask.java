package textools;

public interface ActionTask extends Command {
    /**
     * Executes the command in the current directory.
     */
    void execute();
}
