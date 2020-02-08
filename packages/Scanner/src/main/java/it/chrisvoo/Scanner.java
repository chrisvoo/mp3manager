package it.chrisvoo;

/*
  Benchmarking lib: http://openjdk.java.net/projects/code-tools/jmh/
 */

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Scanner {
    private static ArrayList<Path> files = new ArrayList<>();

    public static void main(String[] args) throws URISyntaxException {
        Scanner scanner = new Scanner();
        File file = scanner.getFileFromResources("Under The Ice (Scene edit).mp3");
        System.out.println(file.getAbsolutePath());
//        Path dir = Paths.get("D:\\Musica");
//        listFiles(dir);
//        System.out.println("Found " + files.size() + " files");
    }

    // get file from classpath, resources folder
    private File getFileFromResources(String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }

    }

    public static void listFiles(Path path) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    listFiles(entry);
                }

                if (entry.getFileName().toString().toLowerCase().endsWith(".mp3")) {
                    files.add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
