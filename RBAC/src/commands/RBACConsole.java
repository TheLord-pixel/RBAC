package commands;

import console.ConsoleUtils;

import java.util.Scanner;

public class RBACConsole {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        ConsoleUtils.printHeader("RBAC SYSTEM");
        System.out.println("Добро пожаловать в систему управления доступом!");

        RBACSystem system = new RBACSystem();
        system.initialize();

        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        ConsoleUtils.printSuccess("Система инициализирована. Введите 'help' для списка команд.");
        System.out.println("Текущий пользователь: " + system.getCurrentUser());

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            parser.parseAndExecute(input, scanner, system);
        }
    }
}