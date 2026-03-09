package utils;

import java.util.List;

public class FormatUtils {

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }

        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                if (row[i] != null && row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }

        for (int i = 0; i < columnWidths.length; i++) {
            columnWidths[i] = Math.min(columnWidths[i], 50);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("┌");
        for (int i = 0; i < columnWidths.length; i++) {
            sb.append("─".repeat(columnWidths[i] + 2));
            if (i < columnWidths.length - 1) {
                sb.append("┬");
            }
        }
        sb.append("┐\n");

        sb.append("│");
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ").append(padRight(headers[i], columnWidths[i])).append(" │");
        }
        sb.append("\n");

        sb.append("├");
        for (int i = 0; i < columnWidths.length; i++) {
            sb.append("─".repeat(columnWidths[i] + 2));
            if (i < columnWidths.length - 1) {
                sb.append("┼");
            }
        }
        sb.append("┤\n");

        for (String[] row : rows) {
            sb.append("│");
            for (int i = 0; i < headers.length; i++) {
                String value = (i < row.length && row[i] != null) ? row[i] : "";
                value = truncate(value, columnWidths[i]);
                sb.append(" ").append(padRight(value, columnWidths[i])).append(" │");
            }
            sb.append("\n");
        }

        sb.append("└");
        for (int i = 0; i < columnWidths.length; i++) {
            sb.append("─".repeat(columnWidths[i] + 2));
            if (i < columnWidths.length - 1) {
                sb.append("┴");
            }
        }
        sb.append("┘\n");

        return sb.toString();
    }

    public static String formatBox(String text) {
        String[] lines = text.split("\n");
        int maxLength = 0;
        for (String line : lines) {
            maxLength = Math.max(maxLength, line.length());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("┌").append("─".repeat(maxLength + 2)).append("┐\n");

        for (String line : lines) {
            sb.append("│ ").append(padRight(line, maxLength)).append(" │\n");
        }

        sb.append("└").append("─".repeat(maxLength + 2)).append("┘\n");

        return sb.toString();
    }

    public static String formatHeader(String text) {
        String line = "═".repeat(text.length() + 4);
        return "╔" + line + "╗\n" +
                "║  " + text + "  ║\n" +
                "╚" + line + "╝\n";
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        if (maxLength <= 3) return ".".repeat(maxLength);
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return " ".repeat(length - text.length()) + text;
    }

    public static String repeatChar(char c, int count) {
        return String.valueOf(c).repeat(Math.max(0, count));
    }
}