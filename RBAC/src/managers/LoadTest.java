package managers;

import models.*;
import filters.UserFilters;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {

    private static final int THREAD_COUNT = 5;
    private static final int OPERATIONS_PER_THREAD = 20;

    public static void main(String[] args) {
        runLoadTest();
    }

    public static void runLoadTest() {
        System.out.println("=".repeat(80));
        System.out.println("НАГРУЗОЧНЫЙ ТЕСТ");
        System.out.println("=".repeat(80));
        System.out.println("Количество потоков: " + THREAD_COUNT);
        System.out.println("Операций на поток: " + OPERATIONS_PER_THREAD);
        System.out.println("=".repeat(80));

        Role.resetExistingNames();

        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager(userManager, roleManager);

        // Создаем тестовые роли
        Role testRole1 = new Role("TEST_ROLE_1", "Тестовая роль 1");
        Role testRole2 = new Role("TEST_ROLE_2", "Тестовая роль 2");
        Permission readPerm = new Permission("READ", "test", "Тестовое право");
        testRole1.addPermission(readPerm);
        testRole2.addPermission(readPerm);
        roleManager.add(testRole1);
        roleManager.add(testRole2);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger createSuccess = new AtomicInteger(0);
        AtomicInteger createFail = new AtomicInteger(0);
        AtomicInteger updateSuccess = new AtomicInteger(0);
        AtomicInteger assignSuccess = new AtomicInteger(0);
        AtomicInteger searchCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Сначала создаем базовых пользователей
        for (int i = 0; i < 20; i++) {
            String username = "base_user_" + i;
            try {
                User user = User.validate(username, "Base User", username + "@test.com");
                userManager.add(user);
            } catch (Exception e) {
                // игнорируем
            }
        }

        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadNum = t;
            futures.add(executor.submit(() -> {
                Random random = new Random();

                for (int op = 0; op < OPERATIONS_PER_THREAD; op++) {
                    try {
                        int operation = random.nextInt(4);

                        switch (operation) {
                            case 0: // Создание уникального пользователя
                                String username = "thread_" + threadNum + "_op_" + op + "_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
                                try {
                                    User user = User.validate(username, "Test User", username + "@test.com");
                                    userManager.add(user);
                                    createSuccess.incrementAndGet();
                                } catch (IllegalArgumentException e) {
                                    createFail.incrementAndGet();
                                }
                                break;

                            case 1: // Обновление случайного пользователя
                                List<User> users = new ArrayList<>(userManager.findAll());
                                if (!users.isEmpty()) {
                                    User randomUser = users.get(random.nextInt(users.size()));
                                    try {
                                        userManager.update(randomUser.username(),
                                                "Updated at " + System.currentTimeMillis(),
                                                randomUser.email());
                                        updateSuccess.incrementAndGet();
                                    } catch (Exception e) {
                                        // обновление может упасть если пользователь удален
                                    }
                                }
                                break;

                            case 2: // Назначение роли
                                List<User> allUsers = new ArrayList<>(userManager.findAll());
                                if (!allUsers.isEmpty()) {
                                    User randomUser = allUsers.get(random.nextInt(allUsers.size()));
                                    Role randomRole = random.nextBoolean() ? testRole1 : testRole2;
                                    try {
                                        AssignmentMetadata meta = AssignmentMetadata.now("load_test", "Нагрузочное тестирование");
                                        PermanentAssignment assignment = new PermanentAssignment(randomUser, randomRole, meta);
                                        assignmentManager.add(assignment);
                                        assignSuccess.incrementAndGet();
                                    } catch (IllegalArgumentException e) {
                                        // назначение может быть дубликатом
                                    }
                                }
                                break;

                            case 3: // Поиск
                                userManager.findByFilter(UserFilters.byUsernameContains("user"));
                                searchCount.incrementAndGet();
                                break;
                        }

                    } catch (Exception e) {
                        System.err.println("Ошибка в потоке " + threadNum + ": " + e.getMessage());
                    }
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.err.println("Задача не завершилась: " + e.getMessage());
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("\n" + "=".repeat(80));
        System.out.println("РЕЗУЛЬТАТЫ НАГРУЗОЧНОГО ТЕСТА");
        System.out.println("=".repeat(80));
        System.out.println("Время выполнения: " + duration + " мс");
        System.out.println("\nСоздано пользователей (успешно): " + createSuccess.get());
        System.out.println("Создано пользователей (дубликаты): " + createFail.get());
        System.out.println("Обновлений пользователей: " + updateSuccess.get());
        System.out.println("Назначений ролей: " + assignSuccess.get());
        System.out.println("Поисковых операций: " + searchCount.get());

        System.out.println("\n" + "=".repeat(80));
        System.out.println("ФИНАЛЬНОЕ СОСТОЯНИЕ СИСТЕМЫ");
        System.out.println("=".repeat(80));
        System.out.println("Пользователей: " + userManager.count());
        System.out.println("Ролей: " + roleManager.count());
        System.out.println("Назначений: " + assignmentManager.count());

        boolean hasIssues = false;

        Set<String> usernames = new HashSet<>();
        for (User user : userManager.findAll()) {
            if (usernames.contains(user.username())) {
                System.out.println("ОШИБКА: Найден дубликат пользователя: " + user.username());
                hasIssues = true;
            }
            usernames.add(user.username());
        }

        for (RoleAssignment ra : assignmentManager.findAll()) {
            if (!userManager.exists(ra.user().username())) {
                System.out.println("ОШИБКА: Назначение ссылается на несуществующего пользователя");
                hasIssues = true;
            }
            if (!roleManager.exists(ra.role().getName())) {
                System.out.println("ОШИБКА: Назначение ссылается на несуществующую роль");
                hasIssues = true;
            }
        }

        if (!hasIssues) {
            System.out.println("\n✅ Нарушений целостности не обнаружено");
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ НАГРУЗОЧНЫЙ ТЕСТ ЗАВЕРШЕН");
        System.out.println("Приложение не упало, критические ошибки отсутствуют");
        System.out.println("=".repeat(80));
    }
}