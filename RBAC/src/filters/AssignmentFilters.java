package filters;

import models.RoleAssignment;
import models.User;
import models.Role;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AssignmentFilters {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private AssignmentFilters() {
    }

    public static AssignmentFilter byUser(User user) {
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return assignment -> assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return assignment -> assignment.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return assignment -> assignment.assignmentType().equalsIgnoreCase(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        LocalDateTime targetDate;
        try {
            targetDate = LocalDateTime.parse(date, FORMATTER);
        } catch (DateTimeParseException e) {
            return assignment -> false;
        }

        return assignment -> {
            try {
                LocalDateTime assignedDate = LocalDateTime.parse(assignment.metadata().assignedAt(), FORMATTER);
                return assignedDate.isAfter(targetDate);
            } catch (DateTimeParseException e) {
                return false;
            }
        };
    }

    public static AssignmentFilter expiringBefore(String date) {
        LocalDateTime targetDate;
        try {
            targetDate = LocalDateTime.parse(date, FORMATTER);
        } catch (DateTimeParseException e) {
            return assignment -> false;
        }

        return assignment -> {
            if (!"TEMPORARY".equalsIgnoreCase(assignment.assignmentType())) {
                return false;
            }

            if (assignment instanceof models.TemporaryAssignment temp) {
                try {
                    String expiresAt = temp.getExpiresAt();
                    if (expiresAt == null) return false;
                    LocalDateTime expirationDate = LocalDateTime.parse(expiresAt, FORMATTER);
                    return expirationDate.isBefore(targetDate);
                } catch (DateTimeParseException e) {
                    return false;
                }
            }
            return false;
        };
    }
}