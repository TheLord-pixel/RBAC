package multithread;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int NUMBER_OF_THREADS = 5;
    private static final int TOTAL_ITERATIONS = 20;
    private static final int DELAY_MS = 200;

    public static void main(String[] args) {
        DisplayManager displayManager = new DisplayManager(NUMBER_OF_THREADS);
        List<WorkerThread> threads = new ArrayList<>();

        for (int i = 1; i <= NUMBER_OF_THREADS; i++) {
            WorkerThread thread = new WorkerThread(i, TOTAL_ITERATIONS, DELAY_MS, displayManager);
            threads.add(thread);
            thread.start();
        }

        Thread displayUpdater = new Thread(() -> {
            while (!displayManager.isFinished()) {
                displayManager.refresh();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        displayUpdater.start();

        for (WorkerThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
    }
}