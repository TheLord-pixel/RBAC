package commands;

import models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    private CommandParser parser;
    private RBACSystem system;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        // Сбрасываем статические списки ролей
        Role.resetExistingNames();

        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        CommandRegistry.registerAllCommands(parser);
    }

    @Test
    @DisplayName("Проверка регистрации всех команд")
    void testAllCommandsRegistered() {
        assertTrue(parser.hasCommand("help"));
        assertTrue(parser.hasCommand("stats"));
        assertTrue(parser.hasCommand("user-list"));
        assertTrue(parser.hasCommand("user-create"));
        assertTrue(parser.hasCommand("user-view"));
        assertTrue(parser.hasCommand("user-update"));
        assertTrue(parser.hasCommand("user-delete"));
        assertTrue(parser.hasCommand("user-search"));
        assertTrue(parser.hasCommand("role-list"));
        assertTrue(parser.hasCommand("role-create"));
        assertTrue(parser.hasCommand("role-view"));
        assertTrue(parser.hasCommand("role-update"));
        assertTrue(parser.hasCommand("role-delete"));
        assertTrue(parser.hasCommand("role-add-permission"));
        assertTrue(parser.hasCommand("role-remove-permission"));
        assertTrue(parser.hasCommand("role-search"));
        assertTrue(parser.hasCommand("assign-role"));
        assertTrue(parser.hasCommand("revoke-role"));
        assertTrue(parser.hasCommand("assignment-list-user"));
        assertTrue(parser.hasCommand("assignment-list-role"));
        assertTrue(parser.hasCommand("assignment-active"));
        assertTrue(parser.hasCommand("assignment-expired"));
        assertTrue(parser.hasCommand("assignment-extend"));
        assertTrue(parser.hasCommand("permissions-user"));
        assertTrue(parser.hasCommand("permissions-check"));
        assertTrue(parser.hasCommand("clear"));
        assertTrue(parser.hasCommand("exit"));
    }

    @Test
    @DisplayName("Команда help")
    void testHelpCommand() {
        parser.executeCommand("help", new Scanner(System.in), system);
        assertTrue(outContent.toString().contains("СПИСОК ДОСТУПНЫХ КОМАНД"));
    }

    @Test
    @DisplayName("Команда stats")
    void testStatsCommand() {
        parser.executeCommand("stats", new Scanner(System.in), system);
        String output = outContent.toString();
        assertTrue(output.contains("СТАТИСТИКА СИСТЕМЫ"));
    }

    @Test
    @DisplayName("Команда user-list")
    void testUserListCommand() {
        parser.executeCommand("user-list", new Scanner(System.in), system);
        String output = outContent.toString();
        assertTrue(output.contains("admin") || output.contains("Пользователей не найдено"));
    }

    @Test
    @DisplayName("Команда role-list")
    void testRoleListCommand() {
        parser.executeCommand("role-list", new Scanner(System.in), system);
        String output = outContent.toString();
        assertTrue(output.contains("ADMIN") || output.contains("Ролей не найдено"));
    }
}