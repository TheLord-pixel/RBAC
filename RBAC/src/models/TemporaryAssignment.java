package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt, boolean autoRenew) {
        super(user, role, metadata);

        if (expiresAt == null || expiresAt.isBlank()) {
            throw new IllegalArgumentException("expiresAt не может быть пустым");
        }

        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return now.isBefore(expires);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isActive(LocalDateTime currentTime) {
        try {
            LocalDateTime expires = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return currentTime.isBefore(expires);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        if (newExpirationDate == null || newExpirationDate.isBlank()) {
            throw new IllegalArgumentException("newExpirationDate не может быть пустым");
        }
        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        return !isActive();
    }

    public String getTimeRemaining() {
        if (!isActive()) {
            return "Истекло";
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expires = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            Duration duration = Duration.between(now, expires);

            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();

            return String.format("%d ч %d мин", hours, minutes);
        } catch (Exception e) {
            return "Неизвестно";
        }
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    @Override
    public String summary() {
        String status = isActive() ? "ACTIVE" : "EXPIRED";
        return String.format("[%s] %s assigned to %s by %s at %s Expires: %s Reason: %s Status: %s",
                assignmentType(),
                role().getName(),
                user().username(),
                metadata().assignedBy(),
                metadata().assignedAt(),
                expiresAt,
                metadata().reason() != null ? metadata().reason() : "—",
                status);
    }
}