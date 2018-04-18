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

    public int run() {
        Command command = findCommandByName(commandName);

        // run actions
        if (command instanceof ActionTask) {
            ((ActionTask) command).execute();
            return 0;
        }

        // run inspections
        boolean offensesFound = false;
        try {
            List<Path> texFiles = new FileSystemTasks().getFilesByExtension(".tex");
            Config config = new Config();

            formatter.started(texFiles.size());
            for (Path file: texFiles) {
                List<Offense> offenses = ((FileTask) command).execute(file);
                inspectedFiles++;
                formatter.fileFinished(file, offenses);

                if (!offenses.isEmpty()) {
                    offensesFound = true;

                    if (failFast) {
                        break;
                    }
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

            return offensesFound ? 1 : 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return 2;
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
