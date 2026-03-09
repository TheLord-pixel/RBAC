package commands;

import models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    private CommandParser parser;
    private RBACSystem system;
    private Scanner scanner;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        // Сбрасываем статические списки ролей
        Role.resetExistingNames();

        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        scanner = new Scanner(System.in);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    @DisplayName("Регистрация и выполнение команды")
    void testRegisterAndExecuteCommand() {
        parser.registerCommand("test", "Тестовая команда", (s, sys) -> {
            System.out.println("Test OK");
        });

        assertTrue(parser.hasCommand("test"));
        assertEquals(1, parser.getCommandCount());

        parser.executeCommand("test", scanner, system);
        assertTrue(outContent.toString().contains("Test OK"));
    }

    @Test
    @DisplayName("Выполнение неизвестной команды")
    void testUnknownCommand() {
        parser.executeCommand("unknown", scanner, system);
        assertTrue(outContent.toString().contains("Неизвестная команда"));
    }

    @Test
    @DisplayName("Парсинг и выполнение")
    void testParseAndExecute() {
        parser.registerCommand("hello", "Приветствие", (s, sys) -> {
            System.out.println("Hello!");
        });

        parser.parseAndExecute("hello", scanner, system);
        assertTrue(outContent.toString().contains("Hello!"));
    }

    @Test
    @DisplayName("Пустой ввод")
    void testEmptyInput() {
        parser.parseAndExecute("", scanner, system);
        parser.parseAndExecute("   ", scanner, system);
        assertEquals("", outContent.toString().trim());
    }

    @Test
    @DisplayName("Печать справки")
    void testPrintHelp() {
        parser.registerCommand("cmd1", "Описание 1", (s, sys) -> {});
        parser.registerCommand("cmd2", "Описание 2", (s, sys) -> {});

        parser.printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("cmd1"));
        assertTrue(output.contains("cmd2"));
        assertTrue(output.contains("Описание 1"));
        assertTrue(output.contains("Описание 2"));
    }
}