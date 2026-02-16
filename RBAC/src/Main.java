public class Main {
    public static void main(String[] args) {
        System.out.println("Тестирование record User");
        testSuccessfulCreation();
        testInvalidUsername();
        testInvalidEmail();
        testEmptyFields();

        System.out.println("\nТестирование record Permission");
        testSuccessfulPermission();
        testPermissionNormalization();
        testInvalidPermissionName();
        testInvalidPermissionResource();
        testInvalidPermissionDescription();
        testPermissionMatches();
    }


    private static void testSuccessfulCreation() {
        System.out.println("\nТест 1: Успешное создание");
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
            User user = User.validate("_", "Pudovkin Igor", "pudovkinigor@gmail.com");
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
        System.out.println("\nТест 5: Успешное создание Permission");
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
}