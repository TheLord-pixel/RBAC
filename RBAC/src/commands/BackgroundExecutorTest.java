package commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundExecutorTest {

    @Test
    @DisplayName("Запуск задачи в фоне")
    void testSubmitTask() throws Exception {
        BackgroundExecutor executor = BackgroundExecutor.getInstance();
        AtomicBoolean executed = new AtomicBoolean(false);

        Future<?> future = executor.submit(() -> {
            executed.set(true);
        });

        future.get(5, TimeUnit.SECONDS);
        assertTrue(executed.get());
    }

    @Test
    @DisplayName("Множественные задачи")
    void testMultipleTasks() throws Exception {
        BackgroundExecutor executor = BackgroundExecutor.getInstance();
        AtomicBoolean task1 = new AtomicBoolean(false);
        AtomicBoolean task2 = new AtomicBoolean(false);

        Future<?> future1 = executor.submit(() -> task1.set(true));
        Future<?> future2 = executor.submit(() -> task2.set(true));

        future1.get(5, TimeUnit.SECONDS);
        future2.get(5, TimeUnit.SECONDS);

        assertTrue(task1.get());
        assertTrue(task2.get());
    }
}