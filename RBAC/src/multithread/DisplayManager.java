package multithread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisplayManager {
    private Map<Integer, ProgressBar> progressBars;
    private int totalThreads;
    private boolean headerPrinted;
    private boolean finished;
    private long lastUpdateTime;
    private static final long UPDATE_INTERVAL = 100;

    public DisplayManager(int totalThreads) {
        this.totalThreads = totalThreads;
        this.progressBars = new ConcurrentHashMap<>();
        this.headerPrinted = false;
        this.finished = false;
        this.lastUpdateTime = 0;
    }

    public void registerThread(int threadNumber, ProgressBar bar) {
        progressBars.put(threadNumber, bar);
    }

    public void refresh() {
        if (finished) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        lastUpdateTime = currentTime;

        if (!headerPrinted && progressBars.size() == totalThreads) {
            System.out.println("Количество потоков: " + totalThreads);
            System.out.println("Длина расчёта: 20 шагов");
            System.out.println("=".repeat(80));
            headerPrinted = true;
        }

        System.out.print("\033[" + totalThreads + "A");

        for (int i = 1; i <= totalThreads; i++) {
            ProgressBar bar = progressBars.get(i);
            if (bar != null) {
                System.out.print("\033[2K");
                System.out.println(bar.getDisplayString());
            }
        }

        boolean allFinished = true;
        for (int i = 1; i <= totalThreads; i++) {
            ProgressBar bar = progressBars.get(i);
            if (bar == null || !bar.isFinished()) {
                allFinished = false;
                break;
            }
        }

        if (allFinished && !finished) {
            finished = true;
            System.out.println("=".repeat(80));
            System.out.println("Все потоки завершили работу!");
        }
    }

    public boolean isFinished() {
        return finished;
    }
}