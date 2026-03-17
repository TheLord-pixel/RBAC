package multithread;

public class WorkerThread extends Thread {
    private int threadNumber;
    private ProgressBar progressBar;
    private int totalIterations;
    private int delayMs;
    private DisplayManager displayManager;
    private volatile boolean running;

    public WorkerThread(int threadNumber, int totalIterations, int delayMs, DisplayManager displayManager) {
        this.threadNumber = threadNumber;
        this.totalIterations = totalIterations;
        this.delayMs = delayMs;
        this.displayManager = displayManager;
        this.running = true;
    }

    @Override
    public void run() {
        progressBar = new ProgressBar(threadNumber, Thread.currentThread().getId(), totalIterations);
        displayManager.registerThread(threadNumber, progressBar);

        for (int i = 1; i <= totalIterations && running; i++) {
            try {
                Thread.sleep(delayMs);
                if (running) {
                    progressBar.update(i);
                }
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
    }

    public void stopThread() {
        running = false;
        this.interrupt();
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}