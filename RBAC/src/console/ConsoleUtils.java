package console;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RED = "\u001B[31m";

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(CYAN + message + ": " + RESET);
            String input = scanner.nextLine().trim();

            if (!required || !input.isEmpty()) {
                return input;
            }

            System.out.println(YELLOW + "Это поле обязательно для заполнения" + RESET);
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(CYAN + message + " [" + min + "-" + max + "]: " + RESET);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println(YELLOW + "Введите число от " + min + " до " + max + RESET);
            } catch (NumberFormatException e) {
                System.out.println(YELLOW + "Введите корректное число" + RESET);
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(CYAN + message + " (y/n/yes/no): " + RESET);
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }

            System.out.println(YELLOW + "Введите y/yes или n/no" + RESET);
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options.isEmpty()) {
            throw new IllegalArgumentException("Список выбора не может быть пустым");
        }

        while (true) {
            System.out.println("\n" + GREEN + message + RESET);
            System.out.println("─".repeat(50));

            for (int i = 0; i < options.size(); i++) {
                System.out.printf("  %d. %s\n", i + 1, options.get(i).toString());
            }
            System.out.println("─".repeat(50));

            System.out.print(CYAN + "Выберите номер (1-" + options.size() + "): " + RESET);
            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.size()) {
                    return options.get(choice - 1);
                }
                System.out.println(YELLOW + "Введите число от 1 до " + options.size() + RESET);
            } catch (NumberFormatException e) {
                System.out.println(YELLOW + "Введите корректное число" + RESET);
            }
        }
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "✓ " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "✗ " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(YELLOW + "ℹ " + message + RESET);
    }

    public static void printHeader(String message) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(CYAN + "  " + message + RESET);
        System.out.println("=".repeat(60));
    }

    public static void pressAnyKeyToContinue(Scanner scanner) {
        System.out.print(YELLOW + "\nНажмите Enter чтобы продолжить..." + RESET);
        scanner.nextLine();
    }
}