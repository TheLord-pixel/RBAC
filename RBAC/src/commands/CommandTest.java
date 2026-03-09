package commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    @Test
    @DisplayName("Проверка функционального интерфейса Command")
    void testCommandInterface() {
        Command testCommand = (scanner, system) -> {
            System.out.println("Test command executed");
        };

        assertNotNull(testCommand);
    }
}