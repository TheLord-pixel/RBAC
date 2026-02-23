package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    public AssignmentMetadata {
        if (assignedBy == null || assignedBy.isBlank()) {
            throw new IllegalArgumentException("assignedBy не может быть пустым");
        }
        if (assignedAt == null || assignedAt.isBlank()) {
            throw new IllegalArgumentException("assignedAt не может быть пустым");
        }
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new AssignmentMetadata(assignedBy, now, reason);
    }

    public String format() {
        if (reason == null || reason.isBlank()) {
            return String.format("Назначено: %s в %s", assignedBy, assignedAt);
        }
        return String.format("Назначено: %s в %s, Причина: %s", assignedBy, assignedAt, reason);
    }

    @Override
    public String toString() {
        return format();
    }
}