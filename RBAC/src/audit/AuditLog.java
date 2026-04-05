package audit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AuditLog {
    private List<AuditEntry> entries;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Для асинхронного логирования
    private BlockingQueue<AuditEntry> queue;
    private Thread consumerThread;
    private AtomicBoolean running;
    private static final int QUEUE_CAPACITY = 1000;

    public AuditLog() {
        this.entries = new ArrayList<>();
        this.queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.running = new AtomicBoolean(true);
        startConsumerThread();
    }

    private void startConsumerThread() {
        consumerThread = new Thread(() -> {
            while (running.get()) {
                try {
                    AuditEntry entry = queue.take();
                    synchronized (entries) {
                        entries.add(entry);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);

        // Добавляем в очередь для асинхронной обработки
        boolean offered = queue.offer(entry);
        if (!offered) {
            System.err.println("Очередь аудит лога переполнена, запись потеряна: " + entry);
        }
    }

    public void logSync(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        synchronized (entries) {
            entries.add(entry);
        }
    }

    public List<AuditEntry> getAll() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public List<AuditEntry> getByPerformer(String performer) {
        synchronized (entries) {
            return entries.stream()
                    .filter(entry -> entry.performer().equals(performer))
                    .collect(Collectors.toList());
        }
    }

    public List<AuditEntry> getByAction(String action) {
        synchronized (entries) {
            return entries.stream()
                    .filter(entry -> entry.action().equals(action))
                    .collect(Collectors.toList());
        }
    }

    public void printLog() {
        synchronized (entries) {
            if (entries.isEmpty()) {
                System.out.println("Аудит лог пуст");
                return;
            }

            System.out.println("=" + "=".repeat(98) + "=");
            System.out.printf("| %-20s | %-15s | %-20s | %-20s | %-10s |\n",
                    "Timestamp", "Action", "Performer", "Target", "Details");
            System.out.println("|" + "-".repeat(20) + "|" + "-".repeat(15) + "|" + "-".repeat(20) + "|" + "-".repeat(20) + "|" + "-".repeat(10) + "|");

            for (AuditEntry entry : entries) {
                String details = entry.details();
                if (details.length() > 10) {
                    details = details.substring(0, 7) + "...";
                }
                System.out.printf("| %-20s | %-15s | %-20s | %-20s | %-10s |\n",
                        entry.timestamp(),
                        entry.action(),
                        entry.performer(),
                        entry.target(),
                        details);
            }
            System.out.println("=" + "=".repeat(98) + "=");
        }
    }

    public void saveToFile(String filename) {
        synchronized (entries) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("Timestamp,Action,Performer,Target,Details");
                for (AuditEntry entry : entries) {
                    writer.printf("%s,%s,%s,%s,%s%n",
                            entry.timestamp(),
                            entry.action(),
                            entry.performer(),
                            entry.target(),
                            entry.details().replace(",", ";"));
                }
                System.out.println("Аудит лог сохранен в файл: " + filename);
            } catch (IOException e) {
                System.err.println("Ошибка при сохранении аудит лога: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        running.set(false);
        consumerThread.interrupt();
    }
}