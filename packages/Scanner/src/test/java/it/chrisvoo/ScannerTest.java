package it.chrisvoo;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scanner tests
 */
public class ScannerTest {
    /**
     * Can collect the correct number of files from a directory and all its subdirectories
     */
    @Test
    public void canScanADirectoryTree() {
        Scanner scanner = new Scanner("./target/test-classes/tree", 3);
        ScanResult result = scanner.start();

        assertEquals(37421248, result.getTotalBytes());
        assertEquals(13, result.getTotalFiles());
        assertFalse(result.hasErrors());
    }

    @Test
    public void canMergePaths() {
        List<String> paths = List.of(
          "./src/main/java",
          "./target",
          "./src/main"
        );

        Main main = new Main();
        List<Path> result = main.mergePaths(paths);
        assertEquals(2, result.size());
        assertTrue(result.get(0).endsWith("main") || result.get(0).endsWith("target"));
        assertTrue(result.get(1).endsWith("main") || result.get(0).endsWith("target"));
    }
}
