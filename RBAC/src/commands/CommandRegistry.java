package commands;

import console.ConsoleUtils;
import managers.AssignmentManager;
import models.*;
import utils.ValidationUtils;
import utils.FormatUtils;
import utils.DateTime;

import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerAllCommands(CommandParser parser) {

        // ==================== КОМАНДЫ УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ ====================

        parser.registerCommand("user-list", "Список всех пользователей", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();

            if (users.isEmpty()) {
                System.out.println("Пользователей не найдено");
                return;
            }

            String[] headers = {"Username", "Full Name", "Email"};
            List<String[]> rows = new ArrayList<>();

            for (User user : users) {
                rows.add(new String[]{user.username(), user.fullName(), user.email()});
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            ConsoleUtils.printHeader("СОЗДАНИЕ НОВОГО ПОЛЬЗОВАТЕЛЯ");

            String username = ConsoleUtils.promptString(scanner, "Введите username (3-20 символов, буквы, цифры, _)", true);

            if (system.getUserManager().exists(username)) {
                ConsoleUtils.printError("Пользователь с таким username уже существует");
                return;
            }

            if (!ValidationUtils.isValidUsername(username)) {
                ConsoleUtils.printError("Некорректный username. Используйте только буквы, цифры и _, длина 3-20");
                return;
            }

            String fullName = ConsoleUtils.promptString(scanner, "Введите полное имя", true);
            String email = ConsoleUtils.promptString(scanner, "Введите email", true);

            if (!ValidationUtils.isValidEmail(email)) {
                ConsoleUtils.printError("Некорректный email");
                return;
            }

            try {
                User user = User.validate(username, fullName, email);
                system.getUserManager().add(user);
                ConsoleUtils.printSuccess("Пользователь " + username + " успешно создан");
            } catch (IllegalArgumentException e) {
                ConsoleUtils.printError("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            User user = userOpt.get();

            System.out.println("\n" + FormatUtils.formatBox("ИНФОРМАЦИЯ О ПОЛЬЗОВАТЕЛЕ"));
            System.out.println("Username: " + user.username());
            System.out.println("Полное имя: " + user.fullName());
            System.out.println("Email: " + user.email());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("Роли: не назначены");
            } else {
                System.out.println("\nРоли:");
                String[] headers = {"Роль", "Тип", "Статус", "Назначена"};
                List<String[]> rows = new ArrayList<>();

                for (RoleAssignment ra : assignments) {
                    String type = ra instanceof TemporaryAssignment ? "Временная" : "Постоянная";
                    String status = ra.isActive() ? "Активна" : "Неактивна";
                    String assignedAt = ra.metadata().assignedAt();

                    rows.add(new String[]{ra.role().getName(), type, status, assignedAt});
                }

                System.out.println(FormatUtils.formatTable(headers, rows));
            }

            Set<Permission> allPermissions = new HashSet<>();
            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    allPermissions.addAll(ra.role().getPermissions());
                }
            }

            if (!allPermissions.isEmpty()) {
                System.out.println("\nПрава доступа:");
                for (Permission p : allPermissions) {
                    System.out.println("  • " + p.format());
                }
            }
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите username пользователя для обновления", true);

            if (!system.getUserManager().exists(username)) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            ConsoleUtils.printInfo("Оставьте поле пустым, если не хотите менять значение");

            String newFullName = ConsoleUtils.promptString(scanner, "Новое полное имя", false);
            String newEmail = ConsoleUtils.promptString(scanner, "Новый email", false);

            if (newFullName.isEmpty() && newEmail.isEmpty()) {
                ConsoleUtils.printInfo("Изменений не внесено");
                return;
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String finalFullName = newFullName.isEmpty() ? user.fullName() : newFullName;
                String finalEmail = newEmail.isEmpty() ? user.email() : newEmail;

                system.getUserManager().update(username, finalFullName, finalEmail);
                ConsoleUtils.printSuccess("Пользователь обновлен");
            }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите username для удаления", true);

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            User user = userOpt.get();

            System.out.println("Пользователь: " + user.username() + " (" + user.fullName() + ")");

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (!assignments.isEmpty()) {
                System.out.println("У пользователя есть назначения:");
                for (RoleAssignment ra : assignments) {
                    System.out.println("  • " + ra.role().getName() + " - " + (ra.isActive() ? "активна" : "неактивна"));
                }
            }

            boolean confirm = ConsoleUtils.promptYesNo(scanner, "Вы уверены, что хотите удалить пользователя " + username + "?");

            if (confirm) {
                for (RoleAssignment ra : assignments) {
                    system.getAssignmentManager().revokeAssignment(ra.assignmentId());
                }
                ConsoleUtils.printSuccess("Пользователь " + username + " удален");
            } else {
                ConsoleUtils.printInfo("Удаление отменено");
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            ConsoleUtils.printHeader("ПОИСК ПОЛЬЗОВАТЕЛЕЙ");

            List<String> filterOptions = Arrays.asList(
                    "По username (содержит)",
                    "По email (содержит)",
                    "По домену email",
                    "По полному имени (содержит)"
            );

            String choice = ConsoleUtils.promptChoice(scanner, "Выберите тип фильтра", filterOptions);

            String value = ConsoleUtils.promptString(scanner, "Введите значение для поиска", true);

            List<User> results = new ArrayList<>();

            if (choice.equals("По username (содержит)")) {
                results = system.getUserManager().findByFilter(user ->
                        user.username().toLowerCase().contains(value.toLowerCase())
                );
            } else if (choice.equals("По email (содержит)")) {
                results = system.getUserManager().findByFilter(user ->
                        user.email().toLowerCase().contains(value.toLowerCase())
                );
            } else if (choice.equals("По домену email")) {
                results = system.getUserManager().findByFilter(user ->
                        user.email().toLowerCase().endsWith(value.toLowerCase())
                );
            } else if (choice.equals("По полному имени (содержит)")) {
                results = system.getUserManager().findByFilter(user ->
                        user.fullName().toLowerCase().contains(value.toLowerCase())
                );
            }

            if (results.isEmpty()) {
                ConsoleUtils.printInfo("Пользователи не найдены");
                return;
            }

            System.out.println("\nНайдено пользователей: " + results.size());
            String[] headers = {"Username", "Full Name", "Email"};
            List<String[]> rows = new ArrayList<>();

            for (User user : results) {
                rows.add(new String[]{user.username(), user.fullName(), user.email()});
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        // ==================== КОМАНДЫ УПРАВЛЕНИЯ РОЛЯМИ ====================

        parser.registerCommand("role-list", "Список всех ролей", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();

            if (roles.isEmpty()) {
                System.out.println("Ролей не найдено");
                return;
            }

            String[] headers = {"Название", "Описание", "Прав"};
            List<String[]> rows = new ArrayList<>();

            for (Role role : roles) {
                rows.add(new String[]{
                        role.getName(),
                        role.getDescription(),
                        String.valueOf(role.getPermissions().size())
                });
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            ConsoleUtils.printHeader("СОЗДАНИЕ НОВОЙ РОЛИ");

            String name = ConsoleUtils.promptString(scanner, "Введите название роли", true);

            if (system.getRoleManager().exists(name)) {
                ConsoleUtils.printError("Роль с таким названием уже существует");
                return;
            }

            String description = ConsoleUtils.promptString(scanner, "Введите описание роли", true);

            try {
                Role role = new Role(name, description);
                system.getRoleManager().add(role);
                ConsoleUtils.printSuccess("Роль " + name + " создана");

                boolean addMore = ConsoleUtils.promptYesNo(scanner, "Добавить права к роли?");
                while (addMore) {
                    System.out.println("\n--- Добавление права ---");
                    String permName = ConsoleUtils.promptString(scanner, "Название права (READ/WRITE/DELETE)", true);
                    String resource = ConsoleUtils.promptString(scanner, "Ресурс", true);
                    String permDesc = ConsoleUtils.promptString(scanner, "Описание права", true);

                    try {
                        Permission perm = new Permission(permName, resource, permDesc);
                        role.addPermission(perm);
                        ConsoleUtils.printSuccess("Право добавлено");
                    } catch (Exception e) {
                        ConsoleUtils.printError("Ошибка: " + e.getMessage());
                    }

                    addMore = ConsoleUtils.promptYesNo(scanner, "Добавить еще право?");
                }

            } catch (IllegalArgumentException e) {
                ConsoleUtils.printError("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "Просмотр информации о роли", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Введите название роли", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();
            System.out.println(role.format());
        });

        parser.registerCommand("role-update", "Обновить роль", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Введите название роли для обновления", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();

            ConsoleUtils.printInfo("Оставьте поле пустым, если не хотите менять значение");

            String newDescription = ConsoleUtils.promptString(scanner, "Новое описание", false);

            if (newDescription.isEmpty()) {
                ConsoleUtils.printInfo("Изменений не внесено");
                return;
            }

            try {
                Role updatedRole = new Role(role.getName(), newDescription);

                for (Permission p : role.getPermissions()) {
                    updatedRole.addPermission(p);
                }

                system.getRoleManager().add(updatedRole);

                List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
                for (RoleAssignment ra : assignments) {
                    if (ra.isActive()) {
                        system.getAssignmentManager().revokeAssignment(ra.assignmentId());

                        AssignmentMetadata meta = AssignmentMetadata.now(
                                system.getCurrentUser(),
                                "Автоматическое обновление роли"
                        );

                        if (ra instanceof TemporaryAssignment) {
                            String expires = ((TemporaryAssignment) ra).getExpiresAt();
                            TemporaryAssignment newRa = new TemporaryAssignment(
                                    ra.user(), updatedRole, meta, expires, false
                            );
                            system.getAssignmentManager().add(newRa);
                        } else {
                            PermanentAssignment newRa = new PermanentAssignment(
                                    ra.user(), updatedRole, meta
                            );
                            system.getAssignmentManager().add(newRa);
                        }
                    }
                }

                ConsoleUtils.printSuccess("Роль обновлена");

            } catch (Exception e) {
                ConsoleUtils.printError("Ошибка при обновлении роли: " + e.getMessage());
            }
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Введите название роли для удаления", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("Роль назначена следующим пользователям:");
                for (RoleAssignment ra : assignments) {
                    System.out.println("  • " + ra.user().username() + " (" + (ra.isActive() ? "активна" : "неактивна") + ")");
                }

                boolean confirm = ConsoleUtils.promptYesNo(scanner, "Вы уверены, что хотите удалить роль? Все назначения будут отозваны");
                if (!confirm) {
                    ConsoleUtils.printInfo("Удаление отменено");
                    return;
                }

                for (RoleAssignment ra : assignments) {
                    system.getAssignmentManager().revokeAssignment(ra.assignmentId());
                }
            }

            ConsoleUtils.printSuccess("Роль " + name + " удалена");
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Введите название роли", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();

            String permName = ConsoleUtils.promptString(scanner, "Название права (READ/WRITE/DELETE)", true);
            String resource = ConsoleUtils.promptString(scanner, "Ресурс", true);
            String description = ConsoleUtils.promptString(scanner, "Описание права", true);

            try {
                Permission perm = new Permission(permName, resource, description);
                role.addPermission(perm);
                ConsoleUtils.printSuccess("Право добавлено к роли " + name);
            } catch (Exception e) {
                ConsoleUtils.printError("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Введите название роли", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();
            Set<Permission> permissions = role.getPermissions();

            if (permissions.isEmpty()) {
                ConsoleUtils.printInfo("У роли нет прав");
                return;
            }

            List<Permission> permList = new ArrayList<>(permissions);
            List<String> options = permList.stream()
                    .map(p -> p.name() + " на " + p.resource() + " (" + p.description() + ")")
                    .collect(Collectors.toList());

            String choice = ConsoleUtils.promptChoice(scanner, "Выберите право для удаления", options);
            int index = options.indexOf(choice);

            if (index >= 0) {
                Permission permToRemove = permList.get(index);
                role.removePermission(permToRemove);
                ConsoleUtils.printSuccess("Право удалено из роли");
            }
        });

        parser.registerCommand("role-search", "Поиск ролей", (scanner, system) -> {
            ConsoleUtils.printHeader("ПОИСК РОЛЕЙ");

            List<String> filterOptions = Arrays.asList(
                    "По названию (содержит)",
                    "По наличию права",
                    "По минимальному количеству прав"
            );

            String choice = ConsoleUtils.promptChoice(scanner, "Выберите тип фильтра", filterOptions);

            List<Role> results = new ArrayList<>();

            if (choice.equals("По названию (содержит)")) {
                String name = ConsoleUtils.promptString(scanner, "Введите часть названия", true);
                results = system.getRoleManager().findAll().stream()
                        .filter(r -> r.getName().toLowerCase().contains(name.toLowerCase()))
                        .collect(Collectors.toList());
            } else if (choice.equals("По наличию права")) {
                String permName = ConsoleUtils.promptString(scanner, "Введите название права", true);
                String resource = ConsoleUtils.promptString(scanner, "Введите ресурс", true);
                results = system.getRoleManager().findAll().stream()
                        .filter(r -> r.hasPermission(permName, resource))
                        .collect(Collectors.toList());
            } else if (choice.equals("По минимальному количеству прав")) {
                int minCount = ConsoleUtils.promptInt(scanner, "Минимальное количество прав", 0, 100);
                results = system.getRoleManager().findAll().stream()
                        .filter(r -> r.getPermissions().size() >= minCount)
                        .collect(Collectors.toList());
            }

            if (results.isEmpty()) {
                ConsoleUtils.printInfo("Роли не найдены");
                return;
            }

            System.out.println("\nНайдено ролей: " + results.size());
            String[] headers = {"Название", "Описание", "Прав"};
            List<String[]> rows = new ArrayList<>();

            for (Role role : results) {
                rows.add(new String[]{
                        role.getName(),
                        role.getDescription(),
                        String.valueOf(role.getPermissions().size())
                });
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        // ==================== КОМАНДЫ УПРАВЛЕНИЯ НАЗНАЧЕНИЯМИ ====================

        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            ConsoleUtils.printHeader("НАЗНАЧЕНИЕ РОЛИ");

            String username = ConsoleUtils.promptString(scanner, "Введите username пользователя", true);

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            User user = userOpt.get();

            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                ConsoleUtils.printError("Нет доступных ролей");
                return;
            }

            List<String> roleNames = roles.stream()
                    .map(r -> r.getName() + " - " + r.getDescription())
                    .collect(Collectors.toList());

            String choice = ConsoleUtils.promptChoice(scanner, "Выберите роль", roleNames);
            int index = roleNames.indexOf(choice);
            Role role = roles.get(index);

            List<String> assignmentTypes = Arrays.asList("Постоянное", "Временное");
            String typeChoice = ConsoleUtils.promptChoice(scanner, "Выберите тип назначения", assignmentTypes);
            boolean isTemporary = typeChoice.equals("Временное");

            String reason = ConsoleUtils.promptString(scanner, "Причина назначения", true);

            try {
                AssignmentMetadata meta = AssignmentMetadata.now(system.getCurrentUser(), reason);
                RoleAssignment assignment;

                if (isTemporary) {
                    String expiresAt = ConsoleUtils.promptString(scanner, "Дата истечения (YYYY-MM-DD)", true);
                    if (!ValidationUtils.isValidDate(expiresAt)) {
                        ConsoleUtils.printError("Некорректный формат даты");
                        return;
                    }
                    assignment = new TemporaryAssignment(user, role, meta, expiresAt, false);
                } else {
                    assignment = new PermanentAssignment(user, role, meta);
                }

                system.getAssignmentManager().add(assignment);
                ConsoleUtils.printSuccess("Роль успешно назначена пользователю " + username);

            } catch (Exception e) {
                ConsoleUtils.printError("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите username пользователя", true);

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            User user = userOpt.get();

            List<RoleAssignment> activeAssignments = system.getAssignmentManager().findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());

            if (activeAssignments.isEmpty()) {
                ConsoleUtils.printInfo("У пользователя нет активных назначений");
                return;
            }

            List<String> options = new ArrayList<>();
            for (RoleAssignment ra : activeAssignments) {
                String type = ra instanceof TemporaryAssignment ? "Временная" : "Постоянная";
                options.add(ra.role().getName() + " (" + type + ") - " + ra.metadata().reason());
            }

            String choice = ConsoleUtils.promptChoice(scanner, "Выберите назначение для отзыва", options);
            int index = options.indexOf(choice);

            if (index >= 0) {
                RoleAssignment ra = activeAssignments.get(index);
                system.getAssignmentManager().revokeAssignment(ra.assignmentId());
                ConsoleUtils.printSuccess("Назначение отозвано");
            }
        });

        parser.registerCommand("assignment-list-user", "Назначения пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);

            if (assignments.isEmpty()) {
                System.out.println("У пользователя нет назначений");
                return;
            }

            String[] headers = {"Роль", "Тип", "Статус", "Назначена", "Причина"};
            List<String[]> rows = new ArrayList<>();

            for (RoleAssignment ra : assignments) {
                String type = ra instanceof TemporaryAssignment ? "Временная" : "Постоянная";
                String status = ra.isActive() ? "Активна" : "Неактивна";
                rows.add(new String[]{
                        ra.role().getName(),
                        type,
                        status,
                        ra.metadata().assignedAt(),
                        ra.metadata().reason()
                });
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        parser.registerCommand("assignment-list-role", "Список пользователей с ролью", (scanner, system) -> {
            String roleName = ConsoleUtils.promptString(scanner, "Введите название роли", true);

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                ConsoleUtils.printError("Роль не найдена");
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);

            if (assignments.isEmpty()) {
                System.out.println("Роль не назначена никому");
                return;
            }

            String[] headers = {"Пользователь", "Тип", "Статус", "Назначена"};
            List<String[]> rows = new ArrayList<>();

            for (RoleAssignment ra : assignments) {
                String type = ra instanceof TemporaryAssignment ? "Временная" : "Постоянная";
                String status = ra.isActive() ? "Активна" : "Неактивна";
                rows.add(new String[]{
                        ra.user().username(),
                        type,
                        status,
                        ra.metadata().assignedAt()
                });
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        parser.registerCommand("assignment-active", "Активные назначения", (scanner, system) -> {
            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();

            if (active.isEmpty()) {
                System.out.println("Нет активных назначений");
                return;
            }

            String[] headers = {"Пользователь", "Роль", "Тип", "Назначена"};
            List<String[]> rows = new ArrayList<>();

            for (RoleAssignment ra : active) {
                String type = ra instanceof TemporaryAssignment ? "Временная" : "Постоянная";
                rows.add(new String[]{
                        ra.user().username(),
                        ra.role().getName(),
                        type,
                        ra.metadata().assignedAt()
                });
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        parser.registerCommand("assignment-expired", "Истекшие назначения", (scanner, system) -> {
            List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments();

            if (expired.isEmpty()) {
                System.out.println("Нет истекших назначений");
                return;
            }

            String[] headers = {"Пользователь", "Роль", "Назначена", "Истекла"};
            List<String[]> rows = new ArrayList<>();

            for (RoleAssignment ra : expired) {
                if (ra instanceof TemporaryAssignment) {
                    rows.add(new String[]{
                            ra.user().username(),
                            ra.role().getName(),
                            ra.metadata().assignedAt(),
                            ((TemporaryAssignment) ra).getExpiresAt()
                    });
                }
            }

            System.out.println(FormatUtils.formatTable(headers, rows));
        });

        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, system) -> {
            String assignmentId = ConsoleUtils.promptString(scanner, "Введите ID назначения", true);

            Optional<RoleAssignment> assignmentOpt = system.getAssignmentManager().findById(assignmentId);
            if (assignmentOpt.isEmpty()) {
                ConsoleUtils.printError("Назначение не найдено");
                return;
            }

            RoleAssignment ra = assignmentOpt.get();
            if (!(ra instanceof TemporaryAssignment)) {
                ConsoleUtils.printError("Можно продлевать только временные назначения");
                return;
            }

            String newExpiresAt = ConsoleUtils.promptString(scanner, "Новая дата истечения (YYYY-MM-DD)", true);
            if (!ValidationUtils.isValidDate(newExpiresAt)) {
                ConsoleUtils.printError("Некорректный формат даты");
                return;
            }

            system.getAssignmentManager().extendTemporaryAssignment(assignmentId, newExpiresAt);
            ConsoleUtils.printSuccess("Назначение продлено");
        });

        // ==================== КОМАНДЫ ПРОСМОТРА ПРАВ ====================

        parser.registerCommand("permissions-user", "Все права пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);

            Map<String, Set<String>> permissionsByResource = new HashMap<>();

            for (RoleAssignment ra : assignments) {
                if (ra.isActive()) {
                    for (Permission p : ra.role().getPermissions()) {
                        permissionsByResource
                                .computeIfAbsent(p.resource(), k -> new HashSet<>())
                                .add(p.name());
                    }
                }
            }

            if (permissionsByResource.isEmpty()) {
                System.out.println("У пользователя нет прав");
                return;
            }

            System.out.println("\nПрава пользователя " + username + ":");
            for (Map.Entry<String, Set<String>> entry : permissionsByResource.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + String.join(", ", entry.getValue()));
            }
        });

        parser.registerCommand("permissions-check", "Проверить наличие права у пользователя", (scanner, system) -> {
            String username = ConsoleUtils.promptString(scanner, "Введите username", true);

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                ConsoleUtils.printError("Пользователь не найден");
                return;
            }

            User user = userOpt.get();

            String permName = ConsoleUtils.promptString(scanner, "Название права (READ/WRITE/DELETE)", true);
            String resource = ConsoleUtils.promptString(scanner, "Ресурс", true);

            boolean hasPermission = system.getAssignmentManager().userHasPermission(user, permName, resource);

            if (hasPermission) {
                ConsoleUtils.printSuccess("Пользователь имеет это право");

                List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
                for (RoleAssignment ra : assignments) {
                    if (ra.isActive()) {
                        for (Permission p : ra.role().getPermissions()) {
                            if (p.name().equals(permName) && p.resource().equals(resource)) {
                                System.out.println("  • Из роли: " + ra.role().getName());
                            }
                        }
                    }
                }
            } else {
                ConsoleUtils.printError("Пользователь не имеет этого права");
            }
        });

        parser.registerCommand("report-users-async", "Запустить генерацию отчета в фоне", (scanner, system) -> {
            ConsoleUtils.printInfo("Запуск генерации отчета в фоновом режиме...");

            BackgroundExecutor.getInstance().submit(() -> {
                try {
                    reports.ReportGenerator generator = new reports.ReportGenerator();
                    String report = generator.generateUserReport(system.getUserManager(), system.getAssignmentManager());
                    generator.exportToFile(report, "user_report_async.txt");
                    ConsoleUtils.printSuccess("Отчет сохранен в файл: user_report_async.txt");
                } catch (Exception e) {
                    ConsoleUtils.printError("Ошибка при генерации отчета: " + e.getMessage());
                }
            });

            ConsoleUtils.printSuccess("Задача отправлена в фоновый режим");
        });

        parser.registerCommand("save-async", "Сохранить данные в файл в фоне", (scanner, system) -> {
            String filename = ConsoleUtils.promptString(scanner, "Введите имя файла для сохранения", true);

            ConsoleUtils.printInfo("Сохранение данных в фоновом режиме...");

            BackgroundExecutor.getInstance().submit(() -> {
                try {
                    // Сохраняем пользователей
                    java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename + "_users.txt"));
                    for (User user : system.getUserManager().findAll()) {
                        writer.println(user.username() + "," + user.fullName() + "," + user.email());
                    }
                    writer.close();

                    ConsoleUtils.printSuccess("Данные сохранены в файл: " + filename + "_users.txt");
                } catch (Exception e) {
                    ConsoleUtils.printError("Ошибка при сохранении: " + e.getMessage());
                }
            });

            ConsoleUtils.printSuccess("Задача сохранения отправлена в фоновый режим");
        });

        // ==================== СЛУЖЕБНЫЕ КОМАНДЫ ====================

        parser.registerCommand("help", "Показать справку по командам", (scanner, system) -> {
            parser.printHelp();
        });

        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            boolean confirm = ConsoleUtils.promptYesNo(scanner, "Вы уверены, что хотите выйти?");
            if (confirm) {
                System.out.println("До свидания!");
                System.exit(0);
            }
        });

        parser.registerCommand("report-users-async", "Запустить генерацию отчета в фоне", (scanner, system) -> {
            ConsoleUtils.printInfo("Запуск генерации отчета в фоновом режиме...");

            BackgroundExecutor.getInstance().submit(() -> {
                try {
                    reports.ReportGenerator generator = new reports.ReportGenerator();
                    String report = generator.generateUserReport(system.getUserManager(), system.getAssignmentManager());
                    generator.exportToFile(report, "user_report_async.txt");
                    ConsoleUtils.printSuccess("Отчет сохранен в файл: user_report_async.txt");
                } catch (Exception e) {
                    ConsoleUtils.printError("Ошибка при генерации отчета: " + e.getMessage());
                }
            });

            ConsoleUtils.printSuccess("Задача отправлена в фоновый режим");
        });

        parser.registerCommand("save-async", "Сохранить данные в файл в фоне", (scanner, system) -> {
            String filename = ConsoleUtils.promptString(scanner, "Введите имя файла для сохранения", true);

            ConsoleUtils.printInfo("Сохранение данных в фоновом режиме...");

            BackgroundExecutor.getInstance().submit(() -> {
                try {
                    // Сохраняем пользователей
                    java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename + "_users.txt"));
                    for (User user : system.getUserManager().findAll()) {
                        writer.println(user.username() + "," + user.fullName() + "," + user.email());
                    }
                    writer.close();

                    ConsoleUtils.printSuccess("Данные сохранены в файл: " + filename + "_users.txt");
                } catch (Exception e) {
                    ConsoleUtils.printError("Ошибка при сохранении: " + e.getMessage());
                }
            });

            ConsoleUtils.printSuccess("Задача сохранения отправлена в фоновый режим");
        });
    }
}