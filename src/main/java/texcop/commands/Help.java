package texcop.commands;

import texcop.ActionTask;
import texcop.Command;

import static texcop.Main.COMMANDS;

public class Help implements ActionTask {

    private static final int COMMAND_SHORT_LENGTH = 30;

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "prints usage information";
    }

    @Override
    public void execute() {
        System.out.format("texcop [command]%n%n");
        for (Command command : COMMANDS) {
            StringBuilder firstPart = new StringBuilder();
            firstPart.append(' ');
            firstPart.append(command.getName());
            while (firstPart.length() < COMMAND_SHORT_LENGTH) {
                firstPart.append(' ');
            }
            System.out.format("%s%s%n", firstPart, command.getDescription());
        }
    }
}
