package commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {
    private Map<String, Command> commands;
    private Map<String, String> commandDescriptions;

    public CommandParser() {
        this.commands = new HashMap<>();
        this.commandDescriptions = new HashMap<>();
    }

    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        String key = commandName.toLowerCase();
        Command command = commands.get(key);

        if (command != null) {
            try {
                command.execute(scanner, system);
            } catch (Exception e) {
                System.out.println("Ошибка при выполнении команды: " + e.getMessage());
            }
        } else {
            System.out.println("Неизвестная команда: " + commandName);
            System.out.println("Введите 'help' для списка команд");
        }
    }

    public void printHelp() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("СПИСОК ДОСТУПНЫХ КОМАНД");
        System.out.println("=".repeat(60));

        commandDescriptions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    System.out.printf("%-20s - %s\n", entry.getKey(), entry.getValue());
                });

        System.out.println("=".repeat(60));
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();

        executeCommand(commandName, scanner, system);
    }

    public boolean hasCommand(String commandName) {
        return commands.containsKey(commandName.toLowerCase());
    }

    public int getCommandCount() {
        return commands.size();
    }
}