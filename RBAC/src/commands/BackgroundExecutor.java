package commands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BackgroundExecutor {
    private static BackgroundExecutor instance;
    private ExecutorService executor;

    private BackgroundExecutor() {
        this.executor = Executors.newCachedThreadPool();
    }

    public static synchronized BackgroundExecutor getInstance() {
        if (instance == null) {
            instance = new BackgroundExecutor();
        }
        return instance;
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}