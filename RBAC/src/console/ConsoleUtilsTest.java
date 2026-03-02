package console;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {

    @Test
    @DisplayName("Запрос строки")
    void testPromptString() {
        String input = "test\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        String result = ConsoleUtils.promptString(scanner, "Enter name", false);
        assertEquals("test", result);
    }

    @Test
    @DisplayName("Запрос строки с валидацией")
    void testPromptStringRequired() {
        String input = "\n\ntest\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        String result = ConsoleUtils.promptString(scanner, "Enter name", true);
        assertEquals("test", result);
    }

    @Test
    @DisplayName("Запрос числа")
    void testPromptInt() {
        String input = "5\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = ConsoleUtils.promptInt(scanner, "Enter number", 1, 10);
        assertEquals(5, result);
    }

    @Test
    @DisplayName("Запрос числа с валидацией")
    void testPromptIntInvalid() {
        String input = "abc\n15\n5\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int result = ConsoleUtils.promptInt(scanner, "Enter number", 1, 10);
        assertEquals(5, result);
    }

    @Test
    @DisplayName("Запрос подтверждения")
    void testPromptYesNo() {
        String input = "y\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        boolean result = ConsoleUtils.promptYesNo(scanner, "Continue?");
        assertTrue(result);
    }

    @Test
    @DisplayName("Запрос подтверждения с валидацией")
    void testPromptYesNoInvalid() {
        String input = "maybe\nyes\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        boolean result = ConsoleUtils.promptYesNo(scanner, "Continue?");
        assertTrue(result);
    }

    @Test
    @DisplayName("Выбор из списка")
    void testPromptChoice() {
        String input = "2\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        List<String> options = Arrays.asList("Option1", "Option2", "Option3");
        String result = ConsoleUtils.promptChoice(scanner, "Choose", options);

        assertEquals("Option2", result);
    }

    @Test
    @DisplayName("Выбор из списка с валидацией")
    void testPromptChoiceInvalid() {
        String input = "5\n0\n2\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        List<String> options = Arrays.asList("Option1", "Option2", "Option3");
        String result = ConsoleUtils.promptChoice(scanner, "Choose", options);

        assertEquals("Option2", result);
    }
}