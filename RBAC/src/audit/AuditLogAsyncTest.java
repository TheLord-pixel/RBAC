package audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogAsyncTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    @DisplayName("Асинхронное добавление записей в лог")
    void testAsyncLog() throws InterruptedException {
        auditLog.log("TEST_ACTION", "tester", "target", "test details");

        // Даем время потоку-обработчику обработать запись
        Thread.sleep(100);

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());

        AuditEntry entry = entries.get(0);
        assertEquals("TEST_ACTION", entry.action());
        assertEquals("tester", entry.performer());
    }

    @Test
    @DisplayName("Множественные асинхронные записи")
    void testMultipleAsyncLogs() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            auditLog.log("ACTION_" + i, "tester", "target", "details " + i);
        }

        Thread.sleep(200);

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(10, entries.size());
    }

    @Test
    @DisplayName("Синхронное добавление записей")
    void testSyncLog() {
        auditLog.logSync("SYNC_ACTION", "tester", "target", "sync details");

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());
    }
}