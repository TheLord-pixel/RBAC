package commands;

import models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        // Сбрасываем статические списки ролей перед каждым тестом
        Role.resetExistingNames();
        system = new RBACSystem();
        system.initialize();
    }

    @Test
    @DisplayName("Проверка инициализации системы")
    void testInitialize() {
        assertNotNull(system.getUserManager());
        assertNotNull(system.getRoleManager());
        assertNotNull(system.getAssignmentManager());
        assertEquals("admin", system.getCurrentUser());

        assertTrue(system.getUserManager().exists("admin"));
        assertTrue(system.getRoleManager().exists("ADMIN"));
        assertTrue(system.getRoleManager().exists("MANAGER"));
        assertTrue(system.getRoleManager().exists("VIEWER"));
    }

    @Test
    @DisplayName("Проверка геттеров")
    void testGetters() {
        assertNotNull(system.getUserManager());
        assertNotNull(system.getRoleManager());
        assertNotNull(system.getAssignmentManager());
    }

    @Test
    @DisplayName("Проверка setCurrentUser")
    void testSetCurrentUser() {
        system.setCurrentUser("testuser");
        assertEquals("testuser", system.getCurrentUser());
    }

    @Test
    @DisplayName("Проверка генерации статистики")
    void testGenerateStatistics() {
        String stats = system.generateStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("Пользователей:"));
        assertTrue(stats.contains("Ролей:"));
    }
}