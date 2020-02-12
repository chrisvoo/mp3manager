package it.chrisvoo;

import org.junit.jupiter.api.Test;

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
        System.out.println(result.toString());

        assertEquals(37421248, result.getTotalBytes());
        assertEquals(13, result.getTotalFiles());
        assertFalse(result.hasErrors());
    }
}
