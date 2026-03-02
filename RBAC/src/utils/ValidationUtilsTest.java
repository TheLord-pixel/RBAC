package utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    @DisplayName("Валидные имена пользователей")
    void testValidUsernames() {
        assertTrue(ValidationUtils.isValidUsername("john_doe"));
        assertTrue(ValidationUtils.isValidUsername("user123"));
        assertTrue(ValidationUtils.isValidUsername("abc_123"));
        assertTrue(ValidationUtils.isValidUsername("a1b2c3"));
        assertTrue(ValidationUtils.isValidUsername("admin"));
    }

    @Test
    @DisplayName("Невалидные имена пользователей")
    void testInvalidUsernames() {
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("very_long_username_1234567890"));
        assertFalse(ValidationUtils.isValidUsername("user@name"));
        assertFalse(ValidationUtils.isValidUsername("user-name"));
        assertFalse(ValidationUtils.isValidUsername("user name"));
        assertFalse(ValidationUtils.isValidUsername("user.name"));
        assertFalse(ValidationUtils.isValidUsername(""));
        assertFalse(ValidationUtils.isValidUsername("   "));
        assertFalse(ValidationUtils.isValidUsername(null));
    }

    @Test
    @DisplayName("Валидные email адреса")
    void testValidEmails() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
        assertTrue(ValidationUtils.isValidEmail("john.doe@company.co.uk"));
        assertTrue(ValidationUtils.isValidEmail("user+label@gmail.com"));
        assertTrue(ValidationUtils.isValidEmail("user_name@domain.org"));
        assertTrue(ValidationUtils.isValidEmail("user123@test-domain.com"));
    }

    @Test
    @DisplayName("Невалидные email адреса")
    void testInvalidEmails() {
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
        assertFalse(ValidationUtils.isValidEmail("user@"));
        assertFalse(ValidationUtils.isValidEmail("@domain.com"));
        assertFalse(ValidationUtils.isValidEmail("user@domain"));
        assertFalse(ValidationUtils.isValidEmail("user@.com"));
        assertFalse(ValidationUtils.isValidEmail("user@domain."));
        assertFalse(ValidationUtils.isValidEmail("user name@domain.com"));
        assertFalse(ValidationUtils.isValidEmail("user@domain..com"));
        assertFalse(ValidationUtils.isValidEmail("user@-domain.com"));
        assertFalse(ValidationUtils.isValidEmail("user@domain.c"));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    @DisplayName("Валидные даты")
    void testValidDates() {
        assertTrue(ValidationUtils.isValidDate("2024-01-15"));
        assertTrue(ValidationUtils.isValidDate("2024-12-31"));
        assertTrue(ValidationUtils.isValidDate("2024-01-15 14:30:00"));
        assertTrue(ValidationUtils.isValidDate("2024-02-29 23:59:59"));
    }

    @Test
    @DisplayName("Невалидные даты")
    void testInvalidDates() {
        assertFalse(ValidationUtils.isValidDate("2024/01/15"));
        assertFalse(ValidationUtils.isValidDate("15-01-2024"));
        assertFalse(ValidationUtils.isValidDate("2024-1-15"));
        assertFalse(ValidationUtils.isValidDate("2024-13-15"));
        assertFalse(ValidationUtils.isValidDate("2024-01-40"));
        assertFalse(ValidationUtils.isValidDate("2024-01-15 25:30:00"));
        assertFalse(ValidationUtils.isValidDate("2024-01-15 14:60:00"));
        assertFalse(ValidationUtils.isValidDate("2024-01-15 14:30:60"));
        assertFalse(ValidationUtils.isValidDate("invalid-date"));
        assertFalse(ValidationUtils.isValidDate(""));
        assertFalse(ValidationUtils.isValidDate(null));
    }

    @Test
    @DisplayName("Нормализация строк")
    void testNormalizeString() {
        assertEquals("hello world", ValidationUtils.normalizeString("  Hello   World  "));
        assertEquals("john doe", ValidationUtils.normalizeString("  John   Doe  "));
        assertEquals("", ValidationUtils.normalizeString(null));
        assertEquals("", ValidationUtils.normalizeString("   "));
    }

    @Test
    @DisplayName("Нормализация с регистром")
    void testNormalizeStringWithCase() {
        assertEquals("HELLO WORLD",
                ValidationUtils.normalizeString("  Hello   World  ", false, true));
        assertEquals("JOHN DOE",
                ValidationUtils.normalizeString("  John   Doe  ", false, true));
        assertEquals("hello world",
                ValidationUtils.normalizeString("  Hello   World  ", true, false));
    }

    @Test
    @DisplayName("Проверка на пустые строки")
    void testRequireNonNullNotEmpty() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireNonNullNotEmpty("test", "field"));
        assertDoesNotThrow(() ->
                ValidationUtils.requireNonNullNotEmpty("  hello  ", "field"));

        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonNullNotEmpty("", "field"));
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonNullNotEmpty("   ", "field"));
        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonNullNotEmpty(null, "field"));
    }

    @Test
    @DisplayName("Проверка на null")
    void testRequireNonNull() {
        assertDoesNotThrow(() ->
                ValidationUtils.requireNonNull(new Object(), "field"));
        assertDoesNotThrow(() ->
                ValidationUtils.requireNonNull("test", "field"));

        assertThrows(IllegalArgumentException.class, () ->
                ValidationUtils.requireNonNull(null, "field"));
    }
}