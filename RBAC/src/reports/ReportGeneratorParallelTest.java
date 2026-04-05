package reports;

import managers.UserManager;
import managers.RoleManager;
import managers.AssignmentManager;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorParallelTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        Role.resetExistingNames();
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        reportGenerator = new ReportGenerator();

        // Добавляем тестовые данные
        for (int i = 0; i < 10; i++) {
            User user = User.validate("user" + i, "User " + i, "user" + i + "@test.com");
            userManager.add(user);
        }

        Role role = new Role("TEST_ROLE", "Test Role");
        Permission perm = new Permission("READ", "docs", "Read docs");
        role.addPermission(perm);
        roleManager.add(role);
    }

    @Test
    @DisplayName("Параллельная генерация отчета по пользователям")
    void testGenerateUserReportParallel() {
        String report = reportGenerator.generateUserReportParallel(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ (parallel)"));
        assertTrue(report.contains("user0"));
        assertTrue(report.contains("user9"));
    }

    @Test
    @DisplayName("Параллельная генерация матрицы прав")
    void testGeneratePermissionMatrixParallel() {
        String matrix = reportGenerator.generatePermissionMatrixParallel(userManager, assignmentManager);

        assertNotNull(matrix);
        assertTrue(matrix.contains("МАТРИЦА ПРАВ ДОСТУПА (parallel)"));
    }
}