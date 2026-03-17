package multithread;

import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int NUMBER_OF_THREADS = 5;
    private static final int TOTAL_ITERATIONS = 20;
    private static final int DELAY_MS = 200; // задержка между шагами в миллисекундах

    public static void main(String[] args) {
        System.out.println("Запуск многопоточного расчёта");
        System.out.println("Количество потоков: " + NUMBER_OF_THREADS);
        System.out.println("Длина расчёта: " + TOTAL_ITERATIONS + " шагов");
        System.out.println("Задержка между шагами: " + DELAY_MS + " мс");
        System.out.println("=".repeat(80));

        List<WorkerThread> threads = new ArrayList<>();

        for (int i = 1; i <= NUMBER_OF_THREADS; i++) {
            WorkerThread thread = new WorkerThread(i, TOTAL_ITERATIONS, DELAY_MS);
            threads.add(thread);
            thread.start();
        }

        for (WorkerThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Ошибка при ожидании потока: " + e.getMessage());
            }
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("Все потоки завершили работу!");
    }
}