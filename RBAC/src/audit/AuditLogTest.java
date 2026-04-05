package audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    @DisplayName("Добавление записей в лог")
    void testLog() throws InterruptedException {
        auditLog.log("CREATE_USER", "admin", "john", "Создан пользователь john");
        auditLog.log("DELETE_ROLE", "admin", "user_role", "Роль удалена");

        // Даем время асинхронному обработчику
        Thread.sleep(100);

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(2, entries.size());

        AuditEntry first = entries.get(0);
        assertEquals("CREATE_USER", first.action());
        assertEquals("admin", first.performer());
        assertEquals("john", first.target());
    }

    @Test
    @DisplayName("Поиск по исполнителю")
    void testGetByPerformer() throws InterruptedException {
        auditLog.log("CREATE_USER", "admin", "john", "desc1");
        auditLog.log("CREATE_ROLE", "admin", "role1", "desc2");
        auditLog.log("CREATE_USER", "user1", "jane", "desc3");

        Thread.sleep(100);

        List<AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        assertEquals(2, adminEntries.size());

        List<AuditEntry> userEntries = auditLog.getByPerformer("user1");
        assertEquals(1, userEntries.size());

        List<AuditEntry> unknownEntries = auditLog.getByPerformer("unknown");
        assertTrue(unknownEntries.isEmpty());
    }

    @Test
    @DisplayName("Поиск по действию")
    void testGetByAction() throws InterruptedException {
        auditLog.log("CREATE_USER", "admin", "john", "desc1");
        auditLog.log("CREATE_USER", "admin", "jane", "desc2");
        auditLog.log("DELETE_USER", "admin", "john", "desc3");

        Thread.sleep(100);

        List<AuditEntry> createEntries = auditLog.getByAction("CREATE_USER");
        assertEquals(2, createEntries.size());

        List<AuditEntry> deleteEntries = auditLog.getByAction("DELETE_USER");
        assertEquals(1, deleteEntries.size());

        List<AuditEntry> unknownEntries = auditLog.getByAction("UNKNOWN");
        assertTrue(unknownEntries.isEmpty());
    }

    @Test
    @DisplayName("Синхронное добавление записей")
    void testLogSync() {
        auditLog.logSync("CREATE_USER", "admin", "john", "desc1");
        auditLog.logSync("CREATE_ROLE", "admin", "role1", "desc2");

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(2, entries.size());
    }
}