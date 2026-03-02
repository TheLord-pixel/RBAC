package reports;

import managers.UserManager;
import managers.RoleManager;
import managers.AssignmentManager;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

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
    }

    @Test
    @DisplayName("Генерация отчета по пользователям")
    void testGenerateUserReport() {
        User user = User.validate("testuser", "Test User", "test@test.com");
        userManager.add(user);

        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("testuser"));
        assertTrue(report.contains("Test User"));
        assertTrue(report.contains("test@test.com"));
    }

    @Test
    @DisplayName("Генерация отчета по ролям")
    void testGenerateRoleReport() {
        Role role = new Role("TEST_ROLE", "Test Description");
        roleManager.add(role);

        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertNotNull(report);
        assertTrue(report.contains("TEST_ROLE"));
        assertTrue(report.contains("Test Description"));
    }

    @Test
    @DisplayName("Генерация матрицы прав")
    void testGeneratePermissionMatrix() {
        User user = User.validate("testuser", "Test User", "test@test.com");
        userManager.add(user);

        Role role = new Role("TEST_ROLE", "Test Role");
        Permission perm = new Permission("READ", "docs", "Read permission");
        role.addPermission(perm);
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);

        String matrix = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertNotNull(matrix);
        assertTrue(matrix.contains("testuser"));
        assertTrue(matrix.contains("docs"));
    }
}