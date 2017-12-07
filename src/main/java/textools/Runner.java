package textools;

import textools.commands.Help;
import textools.cop.Offense;
import textools.tasks.FileSystemTasks;

import java.nio.file.Path;
import java.util.List;

public class Runner {
    private final Formatter formatter = new Formatter();

    private static boolean failFast;
    private int inspectedFiles = 0;

    private final String commandName;

    public Runner(String commandName) {
        this.commandName = commandName;
    }

    public static void setFailFast(boolean failFast) {
        Runner.failFast = failFast;
    }

    public void run() {
        Command command = findCommandByName(commandName);

        // run actions
        if (command instanceof ActionTask) {
            ((ActionTask) command).execute();
            return;
        }

        // run inspections
        try {
            List<Path> texFiles = new FileSystemTasks().getFilesByExtension(".tex");
            formatter.started(texFiles.size());
            for (Path file: texFiles) {
                List<Offense> offenses = ((FileTask) command).execute(file);
                inspectedFiles++;
                formatter.fileFinished(file, offenses);

                if (!offenses.isEmpty() && failFast) {
                    break;
                }
            }
            formatter.finished(inspectedFiles);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static Command findCommandByName(String command) {
        for (Command task : Main.COMMANDS) {
            if (command.equals(task.getName())) {
                return task;
            }
        }
        return new Help();
    }

}
