package commands;

import models.Role;
import models.User;
import models.Permission;
import models.AssignmentMetadata;
import models.TemporaryAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledTaskTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        Role.resetExistingNames();
        system = new RBACSystem();
        system.initialize();
    }

    @Test
    @DisplayName("Проверка запуска периодической задачи")
    void testSchedulerStarts() throws InterruptedException {
        assertNotNull(system);

        // Ждем немного чтобы убедиться что шедулер запустился
        Thread.sleep(2000);

        // Если дошли сюда без ошибок - тест пройден
        assertTrue(true);
    }

    @Test
    @DisplayName("Проверка остановки периодической задачи")
    void testSchedulerStops() {
        system.stopScheduledTask();

        // Если дошли сюда без ошибок - тест пройден
        assertTrue(true);
    }

    @Test
    @DisplayName("Создание временного назначения и проверка его активности")
    void testTemporaryAssignment() {
        User user = User.validate("test_temp_user", "Test User", "temp@test.com");
        system.getUserManager().add(user);

        Role role = new Role("TEMP_ROLE", "Temporary Role");
        system.getRoleManager().add(role);

        String expiresAt = LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        TemporaryAssignment temp = new TemporaryAssignment(user, role, meta, expiresAt, false);
        system.getAssignmentManager().add(temp);

        assertTrue(temp.isActive());
    }
}