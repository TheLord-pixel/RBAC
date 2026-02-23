package models;

public record User(String username, String fullName, String email) {
    public static User validate(String username, String fullName, String email) {
        validateNotEmpty(username, "Имя пользователя");
        validateNotEmpty(fullName, "Полное имя");
        validateNotEmpty(email, "Email");
        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new IllegalArgumentException(
                    "Имя пользователя должно содержать только латинские буквы, цифры и подчеркивание, " +
                            "и быть длиной от 3 до 20 символов. Получено: '" + username + "'"
            );
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(
                    "Email должен содержать символ @ и точку после @. Получено: '" + email + "'"
            );
        }
        return new User(username, fullName, email);
    }
    private static void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }
    private static boolean isValidEmail(String email) {
        return email != null &&
                !email.isBlank() &&
                email.contains("@") &&
                email.indexOf('@') < email.lastIndexOf('.') &&
                !email.contains(" ");
    }
    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
    @Override
    public String toString() {
        return format();
    }
}