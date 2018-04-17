package texcop.commands;

import texcop.ActionTask;
import texcop.tasks.FileSystemTasks;

public class CreateGitignore implements ActionTask {

    @Override
    public String getName() {
        return "create-gitignore";
    }

    @Override
    public String getDescription() {
        return "creates a latex project specific .gitignore file";
    }

    @Override
    public void execute() {
        // cannot reference .gitignore files within a jar
        new FileSystemTasks().copyResourceToFile("tex.gitignore", ".gitignore");
    }
}
