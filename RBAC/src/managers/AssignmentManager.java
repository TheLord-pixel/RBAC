package managers;

import filters.AssignmentFilter;
import models.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> storage;
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.storage = new ConcurrentHashMap<>();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment item) {
        if (item == null) {
            throw new IllegalArgumentException("Назначение не может быть null");
        }
        RoleAssignment previous = storage.putIfAbsent(item.assignmentId(), item);
        if (previous != null) {
            throw new IllegalArgumentException("Назначение с ID '" + item.assignmentId() + "' уже существует");
        }

        User user = item.user();
        Role role = item.role();

        if (!userManager.exists(user.username())) {
            storage.remove(item.assignmentId());
            throw new IllegalArgumentException("Пользователь '" + user.username() + "' не существует");
        }
        if (!roleManager.exists(role.getName())) {
            storage.remove(item.assignmentId());
            throw new IllegalArgumentException("Роль '" + role.getName() + "' не существует");
        }
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) return false;
        return storage.remove(item.assignmentId(), item);
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new CopyOnWriteArrayList<>(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }

    @Override
    public void clear() {
        storage.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return storage.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return storage.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) return findAll();
        return storage.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        if (sorter == null) {
            throw new IllegalArgumentException("Сортировка не может быть null");
        }
        return storage.values().stream()
                .filter(filter != null ? filter::test : a -> true)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return storage.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return storage.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return storage.values().stream()
                .filter(RoleAssignment::isActive)
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role));
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        Set<Permission> permissions = getUserPermissions(user);
        for (Permission p : permissions) {
            if (p.matches(permissionName, resource)) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> permissions = ConcurrentHashMap.newKeySet();
        storage.values().stream()
                .filter(RoleAssignment::isActive)
                .filter(a -> a.user().equals(user))
                .forEach(a -> permissions.addAll(a.role().getPermissions()));
        return permissions;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = storage.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение с ID '" + assignmentId + "' не найдено");
        }
        if (assignment instanceof PermanentAssignment pa) {
            pa.revoke();
        } else if (assignment instanceof TemporaryAssignment ta) {
            ta.extend(LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = storage.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение с ID '" + assignmentId + "' не найдено");
        }
        if (!(assignment instanceof TemporaryAssignment ta)) {
            throw new IllegalArgumentException("Назначение не является временным");
        }
        ta.extend(newExpirationDate);
    }

    public RoleAssignment createPermanentAssignment(User user, Role role, String assignedBy, String reason) {
        AssignmentMetadata metadata = AssignmentMetadata.now(assignedBy, reason);
        PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
        add(assignment);
        return assignment;
    }

    public RoleAssignment createTemporaryAssignment(User user, Role role, String assignedBy, String reason, String expiresAt, boolean autoRenew) {
        AssignmentMetadata metadata = AssignmentMetadata.now(assignedBy, reason);
        TemporaryAssignment assignment = new TemporaryAssignment(user, role, metadata, expiresAt, autoRenew);
        add(assignment);
        return assignment;
    }

    private boolean hasActiveAssignment(User user, Role role) {
        for (RoleAssignment assignment : storage.values()) {
            if (!assignment.isActive()) {
                continue;
            }
            if (assignment.user().equals(user) && assignment.role().equals(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentManager that = (AssignmentManager) o;
        return Objects.equals(storage, that.storage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storage);
    }
}