package utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTime {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        try {
            if (date1.length() == 10 && date2.length() == 10) {
                LocalDate d1 = LocalDate.parse(date1, DATE_FORMATTER);
                LocalDate d2 = LocalDate.parse(date2, DATE_FORMATTER);
                return d1.isBefore(d2);
            } else {
                LocalDateTime d1 = LocalDateTime.parse(date1, DATETIME_FORMATTER);
                LocalDateTime d2 = LocalDateTime.parse(date2, DATETIME_FORMATTER);
                return d1.isBefore(d2);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isAfter(String date1, String date2) {
        try {
            if (date1.length() == 10 && date2.length() == 10) {
                LocalDate d1 = LocalDate.parse(date1, DATE_FORMATTER);
                LocalDate d2 = LocalDate.parse(date2, DATE_FORMATTER);
                return d1.isAfter(d2);
            } else {
                LocalDateTime d1 = LocalDateTime.parse(date1, DATETIME_FORMATTER);
                LocalDateTime d2 = LocalDateTime.parse(date2, DATETIME_FORMATTER);
                return d1.isAfter(d2);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isEqual(String date1, String date2) {
        try {
            if (date1.length() == 10 && date2.length() == 10) {
                LocalDate d1 = LocalDate.parse(date1, DATE_FORMATTER);
                LocalDate d2 = LocalDate.parse(date2, DATE_FORMATTER);
                return d1.isEqual(d2);
            } else {
                LocalDateTime d1 = LocalDateTime.parse(date1, DATETIME_FORMATTER);
                LocalDateTime d2 = LocalDateTime.parse(date2, DATETIME_FORMATTER);
                return d1.isEqual(d2);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String addDays(String date, int days) {
        try {
            if (date.length() == 10) {
                LocalDate d = LocalDate.parse(date, DATE_FORMATTER);
                return d.plusDays(days).format(DATE_FORMATTER);
            } else {
                LocalDateTime d = LocalDateTime.parse(date, DATETIME_FORMATTER);
                return d.plusDays(days).format(DATETIME_FORMATTER);
            }
        } catch (Exception e) {
            return date;
        }
    }

    public static String formatRelativeTime(String date) {
        try {
            LocalDateTime target;
            if (date.length() == 10) {
                target = LocalDate.parse(date, DATE_FORMATTER).atStartOfDay();
            } else {
                target = LocalDateTime.parse(date, DATETIME_FORMATTER);
            }

            LocalDateTime now = LocalDateTime.now();

            if (target.isBefore(now)) {
                long days = ChronoUnit.DAYS.between(target, now);
                if (days == 0) {
                    long hours = ChronoUnit.HOURS.between(target, now);
                    if (hours == 0) {
                        long minutes = ChronoUnit.MINUTES.between(target, now);
                        if (minutes == 0) {
                            return "только что";
                        }
                        return minutes + " минут назад";
                    }
                    return hours + " часов назад";
                }
                if (days == 1) return "вчера";
                if (days < 7) return days + " дней назад";
                if (days < 30) return (days / 7) + " недель назад";
                if (days < 365) return (days / 30) + " месяцев назад";
                return (days / 365) + " лет назад";

            } else if (target.isAfter(now)) {
                long days = ChronoUnit.DAYS.between(now, target);
                if (days == 0) {
                    long hours = ChronoUnit.HOURS.between(now, target);
                    if (hours == 0) {
                        long minutes = ChronoUnit.MINUTES.between(now, target);
                        return "через " + minutes + " минут";
                    }
                    return "через " + hours + " часов";
                }
                if (days == 1) return "завтра";
                if (days < 7) return "через " + days + " дней";
                if (days < 30) return "через " + (days / 7) + " недель";
                if (days < 365) return "через " + (days / 30) + " месяцев";
                return "через " + (days / 365) + " лет";

            } else {
                return "сейчас";
            }

        } catch (Exception e) {
            return date;
        }
    }

    public static String parseDate(String date) {
        try {
            if (date.length() == 10) {
                LocalDate d = LocalDate.parse(date, DATE_FORMATTER);
                return d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } else {
                LocalDateTime d = LocalDateTime.parse(date, DATETIME_FORMATTER);
                return d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            }
        } catch (Exception e) {
            return date;
        }
    }
}