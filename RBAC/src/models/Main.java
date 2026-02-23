package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("Тестирование record models.User");
        testSuccessfulCreation();
        testInvalidUsername();
        testInvalidEmail();
        testEmptyFields();

        System.out.println("\nТестирование record models.Permission");
        testSuccessfulPermission();
        testPermissionNormalization();
        testInvalidPermissionName();
        testInvalidPermissionResource();
        testInvalidPermissionDescription();
        testPermissionMatches();

        System.out.println("\nТестирование class models.Role");
        testSuccessfulRole();
        testDuplicateRoleName();
        testAddRemovePermissions();
        testHasPermission();
        testGetPermissions();
        testEqualsAndHashCode();
        testFormat();

        System.out.println("\nТестирование models.AssignmentMetadata");
        testAssignmentMetadata();

        System.out.println("\nТестирование models.PermanentAssignment");
        testPermanentAssignment();

        System.out.println("\nТестирование models.TemporaryAssignment");
        testTemporaryAssignment();
    }


    private static void testSuccessfulCreation() {
        System.out.println("\nТест 1: Успешное создание models.User");
        try {
            User user = User.validate("pudovkinigor", "Pudovkin Igor", "pudovkinigor@gmail.com");
            System.out.println("Успех: " + user.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void testInvalidUsername() {
        System.out.println("\nТест 2: Неверный username");
        try {
            User user = User.validate("_", "John Doe", "john@example.com");
            System.out.println("Успех: " + user.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    private static void testInvalidEmail() {
        System.out.println("\nТест 3: Неверный email");
        try {
            User user = User.validate("pudovkinigor", "Pudovkin Igor", "pudovkinigor.gmail.com");
            System.out.println("Успех: " + user.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    private static void testEmptyFields() {
        System.out.println("\nТест 4: Пустые поля");
        try {
            User user = User.validate("", "Pudovkin Igor", "pudovkinigor@gmail.com");
            System.out.println("Успех: " + user.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }


    private static void testSuccessfulPermission() {
        System.out.println("\nТест 5: Успешное создание models.Permission");
        try {
            Permission perm1 = new Permission("READ", "users", "Может читать пользователей");
            Permission perm2 = new Permission("  write  ", "  REPORTS  ", "  Может писать отчеты  ");

            System.out.println("Успех: " + perm1.format());
            System.out.println("Успех (с пробелами): " + perm2.format());
            System.out.println("name стал: " + perm2.name());
            System.out.println("resource стал: " + perm2.resource());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void testPermissionNormalization() {
        System.out.println("\nТест 6: Проверка нормализации");
        try {
            Permission perm = new Permission("read", "USERS", "описание");
            System.out.println("name: " + perm.name() + " (был read)");
            System.out.println("resource: " + perm.resource() + " (был USERS)");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void testInvalidPermissionName() {
        System.out.println("\nТест 7: Неверное название (с пробелом)");
        try {
            Permission perm = new Permission("READ WRITE", "users", "Описание");
            System.out.println("Успех: " + perm.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }

        System.out.println("\nТест 8: Пустое название");
        try {
            Permission perm = new Permission("   ", "users", "Описание");
            System.out.println("Успех: " + perm.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    private static void testInvalidPermissionResource() {
        System.out.println("\nТест 9: Пустой ресурс");
        try {
            Permission perm = new Permission("READ", "   ", "Описание");
            System.out.println("Успех: " + perm.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    private static void testInvalidPermissionDescription() {
        System.out.println("\nТест 10: Пустое описание");
        try {
            Permission perm = new Permission("READ", "users", "   ");
            System.out.println("Успех: " + perm.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    private static void testPermissionMatches() {
        System.out.println("\nТест 11: Метод matches()");

        Permission perm = new Permission("READ", "users", "Чтение пользователей");
        System.out.println("Право: " + perm.format());

        System.out.println("Поиск по 'READ': " + perm.matches("READ", null));
        System.out.println("Поиск по 'WRITE': " + perm.matches("WRITE", null));
        System.out.println("Поиск по 'users': " + perm.matches(null, "users"));
        System.out.println("Поиск по 'reports': " + perm.matches(null, "reports"));
        System.out.println("Поиск по 'READ' и 'users': " + perm.matches("READ", "users"));
        System.out.println("Поиск по 'READ' и 'reports': " + perm.matches("READ", "reports"));
        System.out.println("Поиск по части 'REA': " + perm.matches("REA", null));
        System.out.println("Поиск по части 'ser': " + perm.matches(null, "ser"));
    }


    private static void testSuccessfulRole() {
        System.out.println("\nТест 12: Успешное создание models.Role");
        try {
            Role.resetExistingNames();

            Role role1 = new Role("Administrator", "Полный доступ к системе");
            Role role2 = new Role("Manager", "Доступ к управлению");

            System.out.println("Успех: " + role1.getName() + " с ID: " + role1.getId());
            System.out.println("Успех: " + role2.getName() + " с ID: " + role2.getId());
            System.out.println("ID разные: " + !role1.getId().equals(role2.getId()));
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void testDuplicateRoleName() {
        System.out.println("\nТест 13: Дубликат имени роли");
        try {
            Role.resetExistingNames();

            Role role1 = new Role("Admin", "Первый админ");
            System.out.println("Создана: " + role1.getName());

            Role role2 = new Role("Admin", "Второй админ");
            System.out.println("Создана: " + role2.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемая ошибка: " + e.getMessage());
        }
    }

    private static void testAddRemovePermissions() {
        System.out.println("\nТест 14: Добавление и удаление прав");
        try {
            Role.resetExistingNames();

            Role role = new Role("Editor", "Редактор");
            Permission read = new Permission("READ", "articles", "Читать статьи");
            Permission write = new Permission("WRITE", "articles", "Писать статьи");
            Permission delete = new Permission("DELETE", "articles", "Удалять статьи");

            System.out.println("Роль: " + role.getName());
            System.out.println("Прав до добавления: " + role.getPermissions().size());

            role.addPermission(read);
            role.addPermission(write);
            System.out.println("После добавления 2 прав: " + role.getPermissions().size());

            role.removePermission(read);
            System.out.println("После удаления read: " + role.getPermissions().size());

            role.addPermission(delete);
            System.out.println("После добавления delete: " + role.getPermissions().size());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void testHasPermission() {
        System.out.println("\nТест 15: Проверка наличия прав");
        try {
            Role.resetExistingNames();

            Role role = new Role("Viewer", "Просмотрщик");
            Permission readUsers = new Permission("READ", "users", "Читать пользователей");
            Permission readReports = new Permission("READ", "reports", "Читать отчеты");

            role.addPermission(readUsers);

            System.out.println("Есть READ на users: " +
                    role.hasPermission(new Permission("READ", "users", "любое описание")));
            System.out.println("Есть READ на reports: " +
                    role.hasPermission("READ", "reports"));
            System.out.println("Есть WRITE на users: " +
                    role.hasPermission("WRITE", "users"));

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void testGetPermissions() {
        System.out.println("\nТест 16: Неизменяемая копия прав");
        try {
            Role.resetExistingNames();

            Role role = new Role("Tester", "Тестировщик");
            Permission perm = new Permission("TEST", "all", "Тестировать всё");

            role.addPermission(perm);

            Set<Permission> perms = role.getPermissions();
            System.out.println("Размер копии: " + perms.size());

            try {
                perms.add(new Permission("NEW", "new", "новое"));
                System.out.println("Удалось добавить (не должно быть)");
            } catch (UnsupportedOperationException e) {
                System.out.println("Нельзя изменить копию - ожидаемо");
            }

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void testEqualsAndHashCode() {
        System.out.println("\nТест 17: equals и hashCode");
        try {
            Role.resetExistingNames();

            Role role1 = new Role("Role1", "Описание 1");
            Role role2 = new Role("Role2", "Описание 2");

            System.out.println("role1.equals(role2): " + role1.equals(role2));
            System.out.println("role1.equals(role1): " + role1.equals(role1));
            System.out.println("hashCode role1: " + role1.hashCode());
            System.out.println("hashCode role2: " + role2.hashCode());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void testFormat() {
        System.out.println("\nТест 18: Метод format() у models.Role");
        try {
            Role.resetExistingNames();

            Role role = new Role("SuperAdmin", "Супер администратор");

            Permission p1 = new Permission("READ", "users", "Просмотр пользователей");
            Permission p2 = new Permission("WRITE", "users", "Редактирование пользователей");
            Permission p3 = new Permission("DELETE", "users", "Удаление пользователей");

            role.addPermission(p1);
            role.addPermission(p2);
            role.addPermission(p3);

            System.out.println("\n" + role.format());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }


    private static void testAssignmentMetadata() {
        System.out.println("\nТест 19: models.AssignmentMetadata");

        try {
            AssignmentMetadata meta1 = new AssignmentMetadata("admin", "2026-02-17T01:00:00", "Тестовое назначение");
            System.out.println("meta1: " + meta1.format());

            AssignmentMetadata meta2 = AssignmentMetadata.now("pudovkinigor", "Срочное назначение");
            System.out.println("meta2 (now): " + meta2.format());

            AssignmentMetadata meta3 = new AssignmentMetadata("admin", "2026-02-17T02:00:00", null);
            System.out.println("meta3 (без причины): " + meta3.format());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }


    private static void testPermanentAssignment() {
        System.out.println("\nТест 20: models.PermanentAssignment");

        try {
            Role.resetExistingNames();

            User user = User.validate("pudovkinigor", "Pudovkin Igor", "igor@mail.com");
            Role role = new Role("Admin", "Администратор");
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Назначение админа");

            PermanentAssignment pa = new PermanentAssignment(user, role, meta);
            System.out.println("Создано: " + pa.summary());
            System.out.println("isActive(): " + pa.isActive());
            System.out.println("type: " + pa.assignmentType());

            pa.revoke();
            System.out.println("После revoke(): " + pa.isActive());
            System.out.println("isRevoked(): " + pa.isRevoked());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }


    private static void testTemporaryAssignment() {
        System.out.println("\nТест 21: models.TemporaryAssignment");

        try {
            Role.resetExistingNames();

            User user = User.validate("pudovkinigor", "Pudovkin Igor", "igor@mail.com");
            Role role = new Role("Editor", "Редактор");
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Временный доступ");

            String expires = LocalDateTime.now().plusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            TemporaryAssignment ta = new TemporaryAssignment(user, role, meta, expires, false);
            System.out.println("Создано: " + ta.summary());
            System.out.println("isActive(): " + ta.isActive());
            System.out.println("Осталось: " + ta.getTimeRemaining());

            // Проверка с конкретным временем
            LocalDateTime future = LocalDateTime.now().plusHours(3);
            System.out.println("Через 3 часа будет активно? " + ta.isActive(future));

            // Продление
            String newExpires = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            ta.extend(newExpires);
            System.out.println("После продления: " + ta.summary());

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}