package utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@(?!-)[a-zA-Z0-9-]+(?!-)(\\.[a-zA-Z0-9-]+(?!-))*\\.[a-zA-Z]{2,}$"
    );
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2})$|^(\\d{4})-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})$"
    );

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.isEmpty()) return false;

        java.util.regex.Matcher matcher = DATE_PATTERN.matcher(date);
        if (!matcher.matches()) return false;

        try {
            if (date.length() == 10) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));

                if (month < 1 || month > 12) return false;
                if (day < 1 || day > 31) return false;
                if (month == 2 && day > 29) return false;
                if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) return false;

            } else {
                int year = Integer.parseInt(matcher.group(4));
                int month = Integer.parseInt(matcher.group(5));
                int day = Integer.parseInt(matcher.group(6));
                int hour = Integer.parseInt(matcher.group(7));
                int minute = Integer.parseInt(matcher.group(8));
                int second = Integer.parseInt(matcher.group(9));

                if (month < 1 || month > 12) return false;
                if (day < 1 || day > 31) return false;
                if (month == 2 && day > 29) return false;
                if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) return false;
                if (hour < 0 || hour > 23) return false;
                if (minute < 0 || minute > 59) return false;
                if (second < 0 || second > 59) return false;
            }

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String normalizeString(String input, boolean toLowerCase, boolean toUpperCase) {
        if (input == null) return "";

        String normalized = input.trim().replaceAll("\\s+", " ");

        if (toLowerCase) {
            normalized = normalized.toLowerCase();
        } else if (toUpperCase) {
            normalized = normalized.toUpperCase();
        }

        return normalized;
    }

    public static String normalizeString(String input) {
        return normalizeString(input, true, false);
    }

    public static void requireNonNullNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }

    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " не может быть null");
        }
    }
}