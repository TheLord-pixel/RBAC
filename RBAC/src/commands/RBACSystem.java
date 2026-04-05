package commands;

import managers.UserManager;
import managers.RoleManager;
import managers.AssignmentManager;
import models.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class RBACSystem {
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private String currentUser;
    private ScheduledExecutorService scheduler;
    private boolean schedulerRunning;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.currentUser = "system";
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.schedulerRunning = false;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        Permission readDocs = new Permission("READ", "documents", "Чтение документов");
        Permission writeDocs = new Permission("WRITE", "documents", "Запись документов");
        Permission deleteDocs = new Permission("DELETE", "documents", "Удаление документов");

        Permission readUsers = new Permission("READ", "users", "Просмотр пользователей");
        Permission writeUsers = new Permission("WRITE", "users", "Управление пользователями");
        Permission deleteUsers = new Permission("DELETE", "users", "Удаление пользователей");

        Permission readReports = new Permission("READ", "reports", "Просмотр отчетов");
        Permission writeReports = new Permission("WRITE", "reports", "Создание отчетов");

        Role adminRole = new Role("ADMIN", "Полный доступ ко всем ресурсам");
        adminRole.addPermission(readDocs);
        adminRole.addPermission(writeDocs);
        adminRole.addPermission(deleteDocs);
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readReports);
        adminRole.addPermission(writeReports);
        roleManager.add(adminRole);

        Role managerRole = new Role("MANAGER", "Управление документами и просмотр пользователей");
        managerRole.addPermission(readDocs);
        managerRole.addPermission(writeDocs);
        managerRole.addPermission(readUsers);
        managerRole.addPermission(readReports);
        roleManager.add(managerRole);

        Role viewerRole = new Role("VIEWER", "Только просмотр");
        viewerRole.addPermission(readDocs);
        viewerRole.addPermission(readReports);
        roleManager.add(viewerRole);

        User admin = User.validate("admin", "System Administrator", "admin@system.com");
        userManager.add(admin);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "Начальная настройка");
        PermanentAssignment assignment = new PermanentAssignment(admin, adminRole, meta);
        assignmentManager.add(assignment);

        setCurrentUser("admin");

        // Запускаем периодическую задачу
        startScheduledTask();
    }

    private void startScheduledTask() {
        if (schedulerRunning) return;

        schedulerRunning = true;

        // Задача выполняется каждые 30 секунд
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Находим истекшие временные назначения
                List<RoleAssignment> allAssignments = assignmentManager.findAll();
                List<TemporaryAssignment> expiredAssignments = new ArrayList<>();

                for (RoleAssignment ra : allAssignments) {
                    if (ra instanceof TemporaryAssignment temp && ra.isActive()) {
                        if (!temp.isActive()) {
                            expiredAssignments.add(temp);
                        }
                    }
                }

                // Помечаем истекшие как неактивные (если есть)
                if (!expiredAssignments.isEmpty()) {
                    for (TemporaryAssignment temp : expiredAssignments) {
                        // Отзываем истекшее назначение
                        assignmentManager.revokeAssignment(temp.assignmentId());
                    }

                    System.out.println("[Scheduler] Помечено как неактивные " + expiredAssignments.size() + " истекших назначений");
                }

                // Логируем статистику
                System.out.println("[Scheduler] Статистика: Пользователей=" + userManager.count() +
                        ", Ролей=" + roleManager.count() +
                        ", Назначений=" + assignmentManager.count());

            } catch (Exception e) {
                System.err.println("[Scheduler] Ошибка: " + e.getMessage());
            }
        }, 10, 30, TimeUnit.SECONDS); // задержка 10 сек, интервал 30 сек
    }

    public void stopScheduledTask() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            schedulerRunning = false;
            System.out.println("[Scheduler] Периодическая задача остановлена");
        }
    }

    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();

        int userCount = userManager.count();
        int roleCount = roleManager.count();
        int assignmentCount = assignmentManager.count();

        List<RoleAssignment> allAssignments = assignmentManager.findAll();
        long activeCount = allAssignments.stream().filter(RoleAssignment::isActive).count();
        long expiredCount = allAssignments.stream().filter(a -> !a.isActive()).count();

        Map<Role, Long> rolePopularity = new HashMap<>();
        for (RoleAssignment ra : allAssignments) {
            if (ra.isActive()) {
                rolePopularity.put(ra.role(), rolePopularity.getOrDefault(ra.role(), 0L) + 1);
            }
        }

        List<Map.Entry<Role, Long>> topRoles = rolePopularity.entrySet().stream()
                .sorted(Map.Entry.<Role, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        double avgRolesPerUser = userCount > 0 ? (double) activeCount / userCount : 0;

        sb.append("=".repeat(60)).append("\n");
        sb.append("СТАТИСТИКА СИСТЕМЫ\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("Пользователей: %d\n", userCount));
        sb.append(String.format("Ролей: %d\n", roleCount));
        sb.append(String.format("Назначений: всего %d (активных: %d, истекших: %d)\n",
                assignmentCount, activeCount, expiredCount));
        sb.append(String.format("Среднее количество ролей на пользователя: %.2f\n", avgRolesPerUser));

        if (!topRoles.isEmpty()) {
            sb.append("\nТоп-3 самых популярных ролей:\n");
            for (int i = 0; i < topRoles.size(); i++) {
                Map.Entry<Role, Long> entry = topRoles.get(i);
                sb.append(String.format("  %d. %s (%d назначений)\n",
                        i + 1, entry.getKey().getName(), entry.getValue()));
            }
        }
        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }
}