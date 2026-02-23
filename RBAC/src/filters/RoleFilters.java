package filters;

import models.Role;
import models.Permission;

public class RoleFilters {

    private RoleFilters() {
    }

    public static RoleFilter byName(String name) {
        return role -> name.equals(role.getName());
    }

    public static RoleFilter byNameContains(String substring) {
        return role -> {
            String roleName = role.getName();
            return roleName != null && roleName.toLowerCase().contains(substring.toLowerCase());
        };
    }

    public static RoleFilter hasPermission(Permission permission) {
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        return role -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        return role -> role.getPermissions().size() >= n;
    }
}