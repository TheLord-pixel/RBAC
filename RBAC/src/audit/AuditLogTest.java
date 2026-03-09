package audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;
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
    void testLog() {
        auditLog.log("CREATE_USER", "admin", "john", "Создан пользователь john");
        auditLog.log("DELETE_ROLE", "admin", "user_role", "Роль удалена");

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(2, entries.size());

        AuditEntry first = entries.get(0);
        assertEquals("CREATE_USER", first.action());
        assertEquals("admin", first.performer());
        assertEquals("john", first.target());
        assertEquals("Создан пользователь john", first.details());
    }

    @Test
    @DisplayName("Поиск по исполнителю")
    void testGetByPerformer() {
        auditLog.log("CREATE_USER", "admin", "john", "desc1");
        auditLog.log("CREATE_ROLE", "admin", "role1", "desc2");
        auditLog.log("CREATE_USER", "user1", "jane", "desc3");

        List<AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        assertEquals(2, adminEntries.size());

        List<AuditEntry> userEntries = auditLog.getByPerformer("user1");
        assertEquals(1, userEntries.size());

        List<AuditEntry> unknownEntries = auditLog.getByPerformer("unknown");
        assertTrue(unknownEntries.isEmpty());
    }

    @Test
    @DisplayName("Поиск по действию")
    void testGetByAction() {
        auditLog.log("CREATE_USER", "admin", "john", "desc1");
        auditLog.log("CREATE_USER", "admin", "jane", "desc2");
        auditLog.log("DELETE_USER", "admin", "john", "desc3");

        List<AuditEntry> createEntries = auditLog.getByAction("CREATE_USER");
        assertEquals(2, createEntries.size());

        List<AuditEntry> deleteEntries = auditLog.getByAction("DELETE_USER");
        assertEquals(1, deleteEntries.size());

        List<AuditEntry> unknownEntries = auditLog.getByAction("UNKNOWN");
        assertTrue(unknownEntries.isEmpty());
    }

    @Test
    @DisplayName("Сохранение в файл")
    void testSaveToFile(@TempDir Path tempDir) throws IOException {
        auditLog.log("CREATE_USER", "admin", "john", "desc1");
        auditLog.log("CREATE_ROLE", "admin", "role1", "desc2");

        Path filePath = tempDir.resolve("audit.log");
        auditLog.saveToFile(filePath.toString());

        assertTrue(filePath.toFile().exists());

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String header = reader.readLine();
            assertEquals("Timestamp,Action,Performer,Target,Details", header);

            String line1 = reader.readLine();
            assertNotNull(line1);
            assertTrue(line1.contains("CREATE_USER"));
            assertTrue(line1.contains("admin"));
            assertTrue(line1.contains("john"));
        }
    }
}