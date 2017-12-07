package textools.commands;

import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

import textools.Command;
import textools.cop.Offense;

/**
 * Prints the current version of textools on the console.
 */
public class Version implements Command {

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getDescription() {
        return "prints the current version";
    }

    @Override
    public void execute() {
        System.out.println("Version: " + ResourceBundle.getBundle("textools").getString("version"));
        System.out.println("Build Date: " + ResourceBundle.getBundle("textools").getString("build.date"));
    }

    @Override
    public List<Offense> run(Path file) {
        return null;
    }
}
