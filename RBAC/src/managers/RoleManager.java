package managers;

import filters.RoleFilter;
import models.Role;
import models.Permission;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> storageById;
    private final Map<String, Role> storageByName;

    public RoleManager() {
        this.storageById = new HashMap<>();
        this.storageByName = new HashMap<>();
    }

    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }
        if (storageByName.containsKey(role.getName())) {
            throw new IllegalArgumentException("Роль с именем '" + role.getName() + "' уже существует");
        }
        storageById.put(role.getId(), role);
        storageByName.put(role.getName(), role);
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;
        Role removed = storageById.remove(role.getId());
        if (removed != null) {
            storageByName.remove(removed.getName());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(storageById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(storageById.values());
    }

    @Override
    public int count() {
        return storageById.size();
    }

    @Override
    public void clear() {
        storageById.clear();
        storageByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(storageByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) return findAll();
        return storageById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        if (sorter == null) {
            throw new IllegalArgumentException("Сортировка не может быть null");
        }
        return storageById.values().stream()
                .filter(filter != null ? filter::test : r -> true)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String name) {
        return storageByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = storageByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Роль '" + roleName + "' не найдена");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = storageByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Роль '" + roleName + "' не найдена");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return storageById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleManager that = (RoleManager) o;
        return Objects.equals(storageById, that.storageById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storageById);
    }
}