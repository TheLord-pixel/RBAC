public class Main {
    public static void main(String[] args) {
        System.out.println("Тестирование record user");
        testSuccessfulCreation();
        testInvalidUsername();
        testInvalidEmail();
        testEmptyFields();
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
}