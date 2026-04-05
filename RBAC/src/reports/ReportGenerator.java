package reports;

import managers.UserManager;
import managers.RoleManager;
import managers.AssignmentManager;
import models.User;
import models.Role;
import models.RoleAssignment;
import models.Permission;
import models.TemporaryAssignment;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(100)).append("\n");
        sb.append("ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ\n");
        sb.append("=".repeat(100)).append("\n\n");

        List<User> users = userManager.findAll();

        for (User user : users) {
            sb.append("┌─ ").append(user.username()).append("\n");
            sb.append("│   Полное имя: ").append(user.fullName()).append("\n");
            sb.append("│   Email: ").append(user.email()).append("\n");

            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            if (assignments.isEmpty()) {
                sb.append("│   Роли: нет назначенных ролей\n");
            } else {
                sb.append("│   Роли:\n");
                for (RoleAssignment ra : assignments) {
                    String status = ra.isActive() ? "активна" : "истекла";
                    sb.append("│     • ").append(ra.role().getName())
                            .append(" (").append(status).append(")");

                    if (ra instanceof TemporaryAssignment) {
                        sb.append(" - истекает: ").append(((TemporaryAssignment) ra).getExpiresAt());
                    }
                    sb.append("\n");
                }
            }
            sb.append("└─\n\n");
        }

        sb.append("=".repeat(100)).append("\n");
        sb.append("Всего пользователей: ").append(users.size()).append("\n");
        sb.append("=".repeat(100)).append("\n");

        return sb.toString();
    }

    // НОВЫЙ МЕТОД: параллельная генерация отчета по пользователям
    public String generateUserReportParallel(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(100)).append("\n");
        sb.append("ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ (parallel)\n");
        sb.append("=".repeat(100)).append("\n\n");

        List<User> users = userManager.findAll();

        List<String> userReports = users.parallelStream()
                .map(user -> {
                    StringBuilder userSb = new StringBuilder();
                    userSb.append("┌─ ").append(user.username()).append("\n");
                    userSb.append("│   Полное имя: ").append(user.fullName()).append("\n");
                    userSb.append("│   Email: ").append(user.email()).append("\n");

                    List<RoleAssignment> assignments = assignmentManager.findByUser(user);
                    if (assignments.isEmpty()) {
                        userSb.append("│   Роли: нет назначенных ролей\n");
                    } else {
                        userSb.append("│   Роли:\n");
                        for (RoleAssignment ra : assignments) {
                            String status = ra.isActive() ? "активна" : "истекла";
                            userSb.append("│     • ").append(ra.role().getName())
                                    .append(" (").append(status).append(")");

                            if (ra instanceof TemporaryAssignment) {
                                userSb.append(" - истекает: ").append(((TemporaryAssignment) ra).getExpiresAt());
                            }
                            userSb.append("\n");
                        }
                    }
                    userSb.append("└─\n\n");
                    return userSb.toString();
                })
                .collect(Collectors.toList());

        for (String report : userReports) {
            sb.append(report);
        }

        sb.append("=".repeat(100)).append("\n");
        sb.append("Всего пользователей: ").append(users.size()).append("\n");
        sb.append("=".repeat(100)).append("\n");

        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(100)).append("\n");
        sb.append("ОТЧЕТ ПО РОЛЯМ\n");
        sb.append("=".repeat(100)).append("\n\n");

        List<Role> roles = roleManager.findAll();

        for (Role role : roles) {
            sb.append("┌─ ").append(role.getName()).append("\n");
            sb.append("│   Описание: ").append(role.getDescription()).append("\n");

            Set<Permission> permissions = role.getPermissions();
            if (permissions.isEmpty()) {
                sb.append("│   Права: нет прав\n");
            } else {
                sb.append("│   Права:\n");
                for (Permission p : permissions) {
                    sb.append("│     • ").append(p.name())
                            .append(" на ").append(p.resource())
                            .append(" (").append(p.description()).append(")\n");
                }
            }

            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            long activeCount = assignments.stream().filter(RoleAssignment::isActive).count();

            sb.append("│   Назначений: всего ").append(assignments.size())
                    .append(" (активных: ").append(activeCount).append(")\n");
            sb.append("└─\n\n");
        }

        sb.append("=".repeat(100)).append("\n");
        sb.append("Всего ролей: ").append(roles.size()).append("\n");
        sb.append("=".repeat(100)).append("\n");

        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(120)).append("\n");
        sb.append("МАТРИЦА ПРАВ ДОСТУПА\n");
        sb.append("=".repeat(120)).append("\n\n");

        List<User> users = userManager.findAll();

        Set<String> allResources = new TreeSet<>();
        Map<String, Set<String>> userPermissions = new HashMap<>();

        for (User user : users) {
            Set<String> resources = new HashSet<>();
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    for (Permission p : ra.role().getPermissions()) {
                        String key = p.resource() + ":" + p.name();
                        resources.add(key);
                        allResources.add(p.resource());
                    }
                }
            }
            userPermissions.put(user.username(), resources);
        }

        List<String> resourceList = new ArrayList<>(allResources);

        String header = "| Пользователь | " + resourceList.stream()
                .map(r -> String.format("%-15s", r))
                .collect(Collectors.joining(" | ")) + " |";

        sb.append(header).append("\n");
        sb.append("|" + "-".repeat(13) + "|");
        for (String r : resourceList) {
            sb.append("-".repeat(17));
        }
        sb.append("|\n");

        for (User user : users) {
            sb.append(String.format("| %-12s | ", user.username()));

            Set<String> perms = userPermissions.getOrDefault(user.username(), new HashSet<>());

            for (String resource : resourceList) {
                boolean hasRead = perms.contains(resource + ":READ") || perms.contains(resource + ":*");
                boolean hasWrite = perms.contains(resource + ":WRITE") || perms.contains(resource + ":*");
                boolean hasDelete = perms.contains(resource + ":DELETE") || perms.contains(resource + ":*");

                String permStr = "";
                if (hasRead) permStr += "R";
                if (hasWrite) permStr += "W";
                if (hasDelete) permStr += "D";

                if (permStr.isEmpty()) permStr = "-";

                sb.append(String.format(" %-15s | ", permStr));
            }
            sb.append("\n");
        }

        sb.append("=".repeat(120)).append("\n");
        sb.append("R - чтение, W - запись, D - удаление, * - все права\n");
        sb.append("=".repeat(120)).append("\n");

        return sb.toString();
    }

    // НОВЫЙ МЕТОД: параллельная генерация матрицы прав
    public String generatePermissionMatrixParallel(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(120)).append("\n");
        sb.append("МАТРИЦА ПРАВ ДОСТУПА (parallel)\n");
        sb.append("=".repeat(120)).append("\n\n");

        List<User> users = userManager.findAll();

        Set<String> allResources = ConcurrentHashMap.newKeySet();
        Map<String, Set<String>> userPermissions = new ConcurrentHashMap<>();

        users.parallelStream().forEach(user -> {
            Set<String> resources = ConcurrentHashMap.newKeySet();
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    for (Permission p : ra.role().getPermissions()) {
                        String key = p.resource() + ":" + p.name();
                        resources.add(key);
                        allResources.add(p.resource());
                    }
                }
            }
            userPermissions.put(user.username(), resources);
        });

        List<String> resourceList = new ArrayList<>(allResources);
        Collections.sort(resourceList);

        String header = "| Пользователь | " + resourceList.stream()
                .map(r -> String.format("%-15s", r))
                .collect(Collectors.joining(" | ")) + " |";

        sb.append(header).append("\n");
        sb.append("|" + "-".repeat(13) + "|");
        for (String r : resourceList) {
            sb.append("-".repeat(17));
        }
        sb.append("|\n");

        for (User user : users) {
            sb.append(String.format("| %-12s | ", user.username()));

            Set<String> perms = userPermissions.getOrDefault(user.username(), new HashSet<>());

            for (String resource : resourceList) {
                boolean hasRead = perms.contains(resource + ":READ") || perms.contains(resource + ":*");
                boolean hasWrite = perms.contains(resource + ":WRITE") || perms.contains(resource + ":*");
                boolean hasDelete = perms.contains(resource + ":DELETE") || perms.contains(resource + ":*");

                String permStr = "";
                if (hasRead) permStr += "R";
                if (hasWrite) permStr += "W";
                if (hasDelete) permStr += "D";

                if (permStr.isEmpty()) permStr = "-";

                sb.append(String.format(" %-15s | ", permStr));
            }
            sb.append("\n");
        }

        sb.append("=".repeat(120)).append("\n");
        sb.append("R - чтение, W - запись, D - удаление, * - все права\n");
        sb.append("=".repeat(120)).append("\n");

        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
            System.out.println("Отчет сохранен в файл: " + filename);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении отчета: " + e.getMessage());
        }
    }
}