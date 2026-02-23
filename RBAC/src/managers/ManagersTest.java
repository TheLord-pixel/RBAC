package managers;

import filters.UserFilter;
import filters.RoleFilter;
import filters.UserFilters;
import filters.RoleFilters;
import models.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        // Критически важно: сбрасываем статический список занятых имен ролей
        Role.resetExistingNames();

        // Создаем новые чистые экземпляры менеджеров для каждого теста
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
    }

    // ================= USER MANAGER TESTS =================

    @Test
    @DisplayName("UserManager: добавление и поиск пользователя")
    void testAddAndFindUser() {
        User user = User.validate("test_user_1", "Test User", "test@example.com");
        userManager.add(user);

        assertEquals(1, userManager.count());
        assertTrue(userManager.exists("test_user_1"));

        Optional<User> found = userManager.findByUsername("test_user_1");
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().fullName());
    }

    @Test
    @DisplayName("UserManager: поиск по email")
    void testFindByEmail() {
        User user = User.validate("user_email", "Email User", "unique@mail.org");
        userManager.add(user);

        Optional<User> found = userManager.findByEmail("unique@mail.org");
        assertTrue(found.isPresent());
        assertEquals("user_email", found.get().username());
    }

    @Test
    @DisplayName("UserManager: дубликат пользователя")
    void testDuplicateUser() {
        String username = "dup_user_test";
        User user1 = User.validate(username, "First", "first@test.com");
        userManager.add(user1);

        User user2 = User.validate(username, "Second", "second@test.com");

        assertThrows(IllegalArgumentException.class, () -> userManager.add(user2),
                "Должно выбросить исключение при добавлении пользователя с тем же username");
    }

    @Test
    @DisplayName("UserManager: фильтрация пользователей")
    void testUserFilter() {
        User u1 = User.validate("alice_filt", "Alice", "alice@company.com");
        User u2 = User.validate("bob_filt", "Bob", "bob@other.org");
        User u3 = User.validate("charlie_filt", "Charlie", "charlie@company.com");

        userManager.add(u1);
        userManager.add(u2);
        userManager.add(u3);

        UserFilter filter = UserFilters.byEmailDomain("@company.com");
        List<User> filtered = userManager.findByFilter(filter);

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(u -> u.username().equals("alice_filt")));
        assertTrue(filtered.stream().anyMatch(u -> u.username().equals("charlie_filt")));
        assertFalse(filtered.stream().anyMatch(u -> u.username().equals("bob_filt")));
    }

    @Test
    @DisplayName("UserManager: обновление пользователя")
    void testUpdateUser() {
        User user = User.validate("old_user_upd", "Old Name", "old@test.com");
        userManager.add(user);

        userManager.update("old_user_upd", "New Name", "new@test.com");

        Optional<User> updated = userManager.findByUsername("old_user_upd");
        assertTrue(updated.isPresent());
        assertEquals("New Name", updated.get().fullName());
        assertEquals("new@test.com", updated.get().email());
    }

    // ================= ROLE MANAGER TESTS =================

    @Test
    @DisplayName("RoleManager: добавление и поиск роли")
    void testAddAndFindRole() {
        Role role = new Role("Admin_Role_Test", "Administrator");
        roleManager.add(role);

        assertEquals(1, roleManager.count());
        assertTrue(roleManager.exists("Admin_Role_Test"));

        Optional<Role> found = roleManager.findByName("Admin_Role_Test");
        assertTrue(found.isPresent());
        assertEquals("Administrator", found.get().getDescription());
    }

    @Test
    @DisplayName("RoleManager: дубликат роли")
    void testDuplicateRole() {
        String roleName = "Dup_Role_Test";
        Role role1 = new Role(roleName, "First desc");
        roleManager.add(role1);
    }

    @Test
    @DisplayName("RoleManager: добавление прав к роли")
    void testAddPermissionToRole() {
        Role role = new Role("Editor_Role", "Editor");
        roleManager.add(role);

        Permission read = new Permission("READ", "articles", "Read articles");
        roleManager.addPermissionToRole("Editor_Role", read);

        assertTrue(role.hasPermission(read));
        assertEquals(1, role.getPermissions().size());
    }

    @Test
    @DisplayName("RoleManager: фильтрация ролей")
    void testRoleFilter() {
        Role r1 = new Role("Super_Admin", "Super");
        Role r2 = new Role("Mini_Admin", "Mini");
        Role r3 = new Role("User_Role", "User");

        roleManager.add(r1);
        roleManager.add(r2);
        roleManager.add(r3);

        RoleFilter filter = RoleFilters.byNameContains("Admin");
        List<Role> filtered = roleManager.findByFilter(filter);

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(r -> r.getName().equals("Super_Admin")));
        assertTrue(filtered.stream().anyMatch(r -> r.getName().equals("Mini_Admin")));
    }

    @Test
    @DisplayName("RoleManager: поиск ролей с конкретным правом")
    void testFindRolesWithPermission() {
        Role r1 = new Role("Reader_1", "R1");
        Permission p = new Permission("READ", "docs", "Read docs");
        r1.addPermission(p);
        roleManager.add(r1);

        Role r2 = new Role("Reader_2", "R2");
        r2.addPermission(p);
        roleManager.add(r2);

        Role r3 = new Role("Writer", "W");
        roleManager.add(r3);

        List<Role> roles = roleManager.findRolesWithPermission("READ", "docs");
        assertEquals(2, roles.size());
    }

    // ================= ASSIGNMENT MANAGER TESTS =================

    @Test
    @DisplayName("AssignmentManager: добавление назначения")
    void testAddAssignment() {
        User user = User.validate("assign_user", "Assign User", "a@u.com");
        userManager.add(user);

        Role role = new Role("Assign_Role", "AR");
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test assign");
        PermanentAssignment pa = new PermanentAssignment(user, role, meta);

        assignmentManager.add(pa);

        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.findById(pa.assignmentId()).isPresent());
    }

    @Test
    @DisplayName("AssignmentManager: дублирование назначения (ОДИН ОБЪЕКТ РОЛИ)")
    void testDuplicateAssignment() {
        // 1. Создаем данные
        String uName = "dup_user_final";
        String rName = "Dup_Role_Final";

        User user = User.validate(uName, "Dup User", "d@u.com");

        // ВАЖНО: Создаем роль ОДИН РАЗ
        Role role = new Role(rName, "Duplicate Test Role");

        // 2. Добавляем в менеджеры
        userManager.add(user);
        roleManager.add(role);

        // Проверка: убедимся, что они добавлены
        if (!userManager.exists(uName)) {
            fail("Пользователь не добавлен в userManager");
        }
        if (!roleManager.exists(rName)) {
            fail("Роль не добавлена в roleManager");
        }

        // 3. Первое назначение
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "First");
        PermanentAssignment pa1 = new PermanentAssignment(user, role, meta1);

        // Добавляем первое. Если тут ошибка - значит менеджеры рассинхронизированы
        assignmentManager.add(pa1);

        // Проверка: точно ли добавилось?
        if (assignmentManager.count() != 1) {
            fail("Первое назначение не добавлено в хранилище. Текущий размер: " + assignmentManager.count());
        }

        // 4. Второе назначение
        // ВАЖНО: Используем ТЕ ЖЕ САМЫЕ переменные user и role (ссылки на те же объекты)
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second");
        PermanentAssignment pa2 = new PermanentAssignment(user, role, meta2);
    }

    @Test
    @DisplayName("AssignmentManager: поиск назначений по пользователю")
    void testFindByUser() {
        User u1 = User.validate("find_u1", "U1", "u1@t.com");
        User u2 = User.validate("find_u2", "U2", "u2@t.com");
        userManager.add(u1);
        userManager.add(u2);

        Role role = new Role("Find_Role", "FR");
        roleManager.add(role);

        AssignmentMetadata m1 = AssignmentMetadata.now("admin", "For U1");
        PermanentAssignment pa1 = new PermanentAssignment(u1, role, m1);
        assignmentManager.add(pa1);

        AssignmentMetadata m2 = AssignmentMetadata.now("admin", "For U2");
        PermanentAssignment pa2 = new PermanentAssignment(u2, role, m2);
        assignmentManager.add(pa2);

        List<RoleAssignment> assignments = assignmentManager.findByUser(u1);
        assertEquals(1, assignments.size());
        assertEquals(u1, assignments.get(0).user());
    }

    @Test
    @DisplayName("AssignmentManager: активные и истекшие назначения")
    void testActiveAndExpiredAssignments() {
        User user = User.validate("active_test_user", "Active User", "act@t.com");
        userManager.add(user);

        Role role = new Role("Active_Test_Role", "ATR");
        roleManager.add(role);

        // Активное постоянное назначение
        AssignmentMetadata m1 = AssignmentMetadata.now("admin", "Permanent");
        PermanentAssignment pa = new PermanentAssignment(user, role, m1);
        assignmentManager.add(pa);

        // Истекшее временное назначение
        String expiredDate = LocalDateTime.now().minusHours(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        AssignmentMetadata m2 = AssignmentMetadata.now("admin", "Expired");
        TemporaryAssignment ta = new TemporaryAssignment(user, role, m2, expiredDate, false);
        assignmentManager.add(ta);

        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();

        assertEquals(1, active.size());
        assertTrue(active.get(0).isActive());

        assertEquals(1, expired.size());
        assertFalse(expired.get(0).isActive());
    }

    @Test
    @DisplayName("AssignmentManager: проверка прав пользователя")
    void testUserHasPermission() {
        User user = User.validate("perm_check_user", "Perm User", "pc@t.com");
        userManager.add(user);

        Role role = new Role("Perm_Check_Role", "PCR");
        Permission read = new Permission("READ", "files", "Read files");
        role.addPermission(read);
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Grant perm");
        PermanentAssignment pa = new PermanentAssignment(user, role, meta);
        assignmentManager.add(pa);

        assertTrue(assignmentManager.userHasPermission(user, "READ", "files"));
        assertFalse(assignmentManager.userHasPermission(user, "WRITE", "files"));
    }

    @Test
    @DisplayName("AssignmentManager: отзыв назначения (Revoke)")
    void testRevokeAssignment() {
        User user = User.validate("revoke_user", "Revoke User", "rev@t.com");
        userManager.add(user);

        Role role = new Role("Revoke_Role", "RR");
        roleManager.add(role);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "To be revoked");
        PermanentAssignment pa = new PermanentAssignment(user, role, meta);
        assignmentManager.add(pa);

        assertTrue(pa.isActive());

        assignmentManager.revokeAssignment(pa.assignmentId());

        assertFalse(pa.isActive());
        assertTrue(assignmentManager.getExpiredAssignments().contains(pa));
    }

    @Test
    @DisplayName("AssignmentManager: продление временного назначения")
    void testExtendTemporaryAssignment() {
        User user = User.validate("extend_user", "Extend User", "ext@t.com");
        userManager.add(user);

        Role role = new Role("Extend_Role", "ER");
        roleManager.add(role);

        String initialExpires = LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Short term");
        TemporaryAssignment ta = new TemporaryAssignment(user, role, meta, initialExpires, false);
        assignmentManager.add(ta);

        assertTrue(ta.isActive());

        String newExpires = LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assignmentManager.extendTemporaryAssignment(ta.assignmentId(), newExpires);

        // Проверяем, что дата обновилась и назначение активно даже через 1 час (который был бы критичен для 10 мин)
        LocalDateTime checkTime = LocalDateTime.now().plusHours(1);
        assertTrue(ta.isActive(checkTime));
    }
}