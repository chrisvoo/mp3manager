package it.chrisvoo;

/*
  Benchmarking lib: http://openjdk.java.net/projects/code-tools/jmh/
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Scan a directory for MP3 and store its metadata and paths inside the db
 */
public class Scanner {
    private ArrayList<Path> files = new ArrayList<>();
    private ScanConfig config;

    /**
     * Creates an instance of the scanner.
     * @param config Configuration params
     */
    public Scanner(ScanConfig config) {
        this.config = config;
    }

    /**
     * It creates a ForkJoinPool using the available processors to read the metadata of the
     * music files and store them inside the database.
     * @return a ScanResult instance containing the total files found, the total
     * size and eventual errors encountered during the process
     */
    public ScanResult start() {
        List<Path> chosenPaths = config.getChosenPaths();
        for (Path chosenPath : chosenPaths) {
            boolean isListOk = listFiles(chosenPath);
            if (!isListOk) {
                return null;
            }
        }

        System.out.println("Collected " + files.size() + " paths");

        int nThreads = Runtime.getRuntime().availableProcessors();
        boolean usingProcessors = false;

        // let's calculate the threshold dividing the total number of files by the total available processors.
        if (config.getThreshold() == -1) {
            config.setThreshold(files.size() / nThreads);
            usingProcessors = true;
        } else if (config.getThreshold() < files.size()) {
            usingProcessors = true;
        }

        if (usingProcessors) {
            System.out.printf("Running scanner with a pool of %d threads\n", nThreads);
        } else {
            System.out.println("Running scanner in single-threaded mode");
        }

        ForkJoinPool pool = new ForkJoinPool(nThreads);
        ScanTask task = new ScanTask(files, config);
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
