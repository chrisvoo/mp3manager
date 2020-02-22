package it.chrisvoo.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utilities for merging paths, checks if a directory is child of another directory
 */
public class FileSystemUtils {
    /**
     * It tells if a Path is a child of the other specified
     * @param child A Path to check
     * @param possibleParent The path that may be the parent
     * @return true if it's a child of possibleParent, false otherise or if they're equal
     */
    public static boolean isChild(Path child, Path possibleParent) {
        if (child.equals(possibleParent)) {
            return false;
        }

        Path parent = possibleParent.normalize().toAbsolutePath();
        return child.startsWith(parent);
    }

    /**
     * Takes a list of strings representing paths and filters out duplicates and directories which are
     * children of other directories in the list.
     * @param paths The paths to scan
     * @return A list of Path instances
     */
    public static List<Path> mergePaths(List<String> paths) {
        return paths
                .stream()
                .distinct()
                .map(p -> {
                    try {
                        return Paths.get(p).toRealPath();
                    } catch (IOException e) {
                        System.out.printf("WARNING: %s does NOT exist and won't be considered", p.toString());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(p -> paths.stream().noneMatch(path -> isChild(p, Paths.get(path).normalize().toAbsolutePath())))
                .collect(Collectors.toList());
    }

    /**
     * It gives a human readable representation of the total number of bytes passed as arguments.
     * @param v a long representing the amount of bytes to show
     * @return A human readable representation of the bytes.
     */
    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format(Locale.ENGLISH, "%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
