package it.chrisvoo;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Main implements Callable<Integer> {
    /**
     * Threshold over which the work will be split in two tasks.
     * - If null (not specified), if will be automatically calculated divided the total amount of files by the
     *   number of available processors.
     * - If -1, if will use a value greater than the total amount of files to sequentially process the files (useful if
     *   you don't have your music on an SDD. An HDD will worsen the performance).
     */
    private Integer threshold = null;

    /**
     * Mongo connection string
     */
    private String mongoConnectionString;

    /**
     * Paths to be scanned for music. If there's an intersection between path, they will be normalized. For example if
     * path A includes path B, path B will be discarded. This avoids useless duplicates.
     */
    private List<Path> paths;

    @Option(names = {"-c", "--configuration"}, description = "Configuration file's path")
    private File confFile;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    /**
     * It tells if a Path is a child of the other specified
     * @param child A Path to check
     * @param possibleParent The path that may be the parent
     * @return true if it's a child of possibleParent, false otherise or if they're equal
     */
    private boolean isChild(Path child, Path possibleParent) {
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
    public List<Path> mergePaths(List<String> paths) {
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
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result or if don't pass any path.
     */
    @Override
    public Integer call() throws Exception {
        Config config = ConfigFactory.parseFile(confFile);

        threshold = (config.hasPath("scanner.threshold"))
                    ? config.getInt("scanner.threshold")
                    : -1;

        if (config.hasPath("scanner.paths")) {
            List<String> thePaths = config.getStringList("scanner.paths");
            paths = mergePaths(thePaths);
        } else {
            System.out.println("'paths' property cannot be empty");
            return -1;
        }

        mongoConnectionString =  (config.hasPath("scanner.db"))
                                 ? config.getString("scanner.db")
                                 : "mongodb://localhost:27017";
        return 0;
    }
}
