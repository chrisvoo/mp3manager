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

    public List<Path> mergePaths(List<Path> paths) {
        return paths
          .stream()
          .distinct()
          .map(p -> {
              try {
                  Path realPath =  p.toRealPath();
                  // to be finished
                  return realPath;
              } catch (IOException e) {
                  System.out.printf("WARNING: %s does NOT exist and won't be considered", p.toString());
                  return null;
              }
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Integer call() throws Exception {
        Config config = ConfigFactory.parseFile(confFile);
        if (config.hasPath("scanner.threshold")) {
            threshold = config.getInt("scanner.threshold");
        }

        if (config.hasPath("scanner.paths")) {
            List<String> thePaths = config.getStringList("scanner.paths");
            paths = thePaths.stream().map(e -> Paths.get(e)).collect(Collectors.toList());
        }

        System.out.println(threshold);
        return 0;
    }
}
