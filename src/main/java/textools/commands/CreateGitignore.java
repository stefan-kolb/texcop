package textools.commands;

import java.nio.file.Path;
import java.util.List;

import textools.Command;
import textools.cop.Offense;
import textools.tasks.FileSystemTasks;

public class CreateGitignore implements Command {

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

    @Override
    public List<Offense> run(Path file) {
        return null;
    }
}
