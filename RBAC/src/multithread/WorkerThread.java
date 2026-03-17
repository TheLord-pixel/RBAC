package multithread;

public class WorkerThread extends Thread {
    private int threadNumber;
    private ProgressBar progressBar;
    private int totalIterations;
    private int delayMs;

    public WorkerThread(int threadNumber, int totalIterations, int delayMs) {
        this.threadNumber = threadNumber;
        this.totalIterations = totalIterations;
        this.delayMs = delayMs;
    }

    @Override
    public void run() {
        progressBar = new ProgressBar(threadNumber, Thread.currentThread().getId(), totalIterations);

        for (int i = 1; i <= totalIterations; i++) {
            try {
                Thread.sleep(delayMs);
                progressBar.update(i);

            } catch (InterruptedException e) {
                System.err.println("Поток " + threadNumber + " был прерван");
                break;
            }
        }
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}