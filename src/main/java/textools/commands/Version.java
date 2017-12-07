package textools.commands;

import textools.ActionTask;

import java.util.ResourceBundle;

/**
 * Prints the current version of textools on the console.
 */
public class Version implements ActionTask {

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
}
