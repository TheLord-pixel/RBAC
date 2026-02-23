package models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Role {
    private final String id;
    private final String name;
    private String description;
    private Set<Permission> permissions;

    private static Set<String> existingNames = new HashSet<>();

    public Role(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название роли не может быть пустым");
        }

        String trimmedName = name.trim();
        if (existingNames.contains(trimmedName)) {
            throw new IllegalArgumentException("Роль с именем '" + trimmedName + "' уже существует");
        }

        this.id = UUID.randomUUID().toString();
        this.name = trimmedName;
        this.description = (description != null) ? description.trim() : "";
        this.permissions = new HashSet<>();
        existingNames.add(trimmedName);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    public void addPermission(Permission permission) {
        if (permission != null) {
            permissions.add(permission);
        }
    }

    public void removePermission(Permission permission) {
        if (permission != null) {
            permissions.remove(permission);
        }
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        if (permissionName == null || resource == null) {
            return false;
        }

        for (Permission p : permissions) {
            if (p.name().equals(permissionName.toUpperCase()) &&
                    p.resource().equals(resource.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public Set<Permission> getPermissions() {
        return Set.copyOf(permissions); // неизменяемая копия
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("models.Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");

        if (permissions.isEmpty()) {
            sb.append(" - нет прав\n");
        } else {
            for (Permission p : permissions) {
                sb.append(" - ").append(p.format()).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "models.Role{id='" + id + "', name='" + name + "', permissions=" + permissions.size() + "}";
    }

    public static void resetExistingNames() {
        existingNames.clear();
    }
}