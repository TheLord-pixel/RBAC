package multithread;

public class ProgressBar {
    private static final int BAR_LENGTH = 50;
    private int threadNumber;
    private long threadId;
    private int progress;
    private int total;
    private long startTime;
    private long endTime;
    private boolean finished;

    public ProgressBar(int threadNumber, long threadId, int total) {
        this.threadNumber = threadNumber;
        this.threadId = threadId;
        this.total = total;
        this.progress = 0;
        this.startTime = System.currentTimeMillis();
        this.finished = false;
    }

    public synchronized void update(int progress) {
        this.progress = progress;
        if (progress >= total) {
            this.finished = true;
            this.endTime = System.currentTimeMillis();
        }
        print();
    }

    private void print() {
        int percent = (progress * 100) / total;
        int filledLength = (progress * BAR_LENGTH) / total;

        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i = 0; i < BAR_LENGTH; i++) {
            if (i < filledLength) {
                bar.append("=");
            } else if (i == filledLength) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");

        String timeInfo = "";
        if (finished) {
            long elapsedTime = endTime - startTime;
            timeInfo = String.format(" - Завершен за %d мс", elapsedTime);
        }

        System.out.printf("\rПоток #%d [ID: %d] %s %d%%%s",
                threadNumber, threadId, bar.toString(), percent, timeInfo);
    }

    public boolean isFinished() {
        return finished;
    }
}