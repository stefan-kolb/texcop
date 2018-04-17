package texcop;

import java.nio.file.Path;
import java.util.List;

import texcop.commands.Help;
import texcop.cop.Config;
import texcop.cop.CopConfig;
import texcop.cop.Offense;
import texcop.tasks.FileSystemTasks;

public class Runner {
    private final Formatter formatter = new Formatter();

    private static boolean failFast;
    private static boolean generateConfig;

    private int inspectedFiles = 0;

    private final String commandName;

    public Runner(String commandName) {
        this.commandName = commandName;
    }

    public static void setFailFast(boolean failFast) {
        Runner.failFast = failFast;
    }

    public static void generateConfig(boolean generateConfig) {
        Runner.generateConfig = generateConfig;
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
            Config config = new Config();

            formatter.started(texFiles.size());
            for (Path file: texFiles) {
                List<Offense> offenses = ((FileTask) command).execute(file);
                inspectedFiles++;
                formatter.fileFinished(file, offenses);

                if (!offenses.isEmpty() && failFast) {
                    break;
                }

                if (generateConfig) {
                    CopConfig disabled = new CopConfig();
                    disabled.setEnabled(false);
                    offenses.stream()
                            .map(o -> o.copName)
                            .distinct()
                            .forEach(copName -> config.addCop(copName, disabled));
                }
            }

            if (generateConfig) {
                config.save();
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
