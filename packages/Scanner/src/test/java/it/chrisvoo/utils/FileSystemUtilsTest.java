package it.chrisvoo.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemUtilsTest {

    @Test
    public void canMergePaths() {
        List<String> paths = List.of(
                "./src/main/java",
                "./target",
                "./src/main"
        );

        List<Path> result = FileSystemUtils.mergePaths(paths);
        assertEquals(2, result.size());
        assertTrue(result.get(0).endsWith("main") || result.get(0).endsWith("target"));
        assertTrue(result.get(1).endsWith("main") || result.get(0).endsWith("target"));
    }

    @Test
    public void canDisplayHumanBytes() {
        long theBytes = 12256987;
        assertEquals("11.7 MB", FileSystemUtils.formatSize(theBytes));
    }
}
