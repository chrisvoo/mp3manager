package it.chrisvoo;

/*
  Benchmarking lib: http://openjdk.java.net/projects/code-tools/jmh/
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

/**
 * Scan a directory for MP3 and store its metadata and paths inside the db
 */
public class Scanner {
    private ArrayList<Path> files = new ArrayList<>();
    private Path chosenPath;
    private int threshold;

    /**
     * Creates an instance of the scanner.
     * @param path A Path
     * @param threshold A threshold which defines when to fork a new ScanTask
     */
    public Scanner(String path, int threshold) {
        try {
            chosenPath = Paths.get(path);
            this.threshold = threshold;
        } catch (Exception e) {
            System.out.printf("Cannot read %s", path);
        }
    }

    /**
     * It creates a ForkJoinPool using the available processors to read the metadata of the
     * music files and store them inside the database.
     * @return a ScanResult instance containing the total files found, the total
     * size and eventual errors encountered during the process
     */
    public ScanResult start() {
        int nThreads = Runtime.getRuntime().availableProcessors();
        System.out.printf("Running scanner with a pool of %d threads\n", nThreads);

        boolean isListOk = listFiles(chosenPath);
        if (!isListOk) {
            return null;
        }
        System.out.println("Collected " + files.size() + " paths");

        ForkJoinPool pool = new ForkJoinPool(nThreads);
        ScanTask task = new ScanTask(files.toArray(new Path[0]), threshold);
        return pool.invoke(task);
    }

    /**
     * It recursively stores all MP3 paths found in a directory (and all its subdirs)
     * inside an ArrayList.
     * @param path A Path.
     * @return true if it was able to found the directory and read its files, false otherwise
     */
    public boolean listFiles(Path path) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listFiles(entry);
                }

                if (entry.getFileName().toString().toLowerCase().endsWith(".mp3")) {
                    files.add(entry);
                }
            }

            return true;
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }
}
