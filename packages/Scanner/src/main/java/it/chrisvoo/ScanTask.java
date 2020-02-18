package it.chrisvoo;

import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * It process by default up to 100 files. If it receives more than this,
 * the work gets split.
 */
public class ScanTask extends RecursiveTask<ScanResult> {
    private ScanConfig config;
    private List<Path> paths;

    /**
     * An intance of a RecursiveTask subclass.
     * @param paths List of paths to be scanned
     * @param config Configuration which will be passed to every {@link ScanTask} instance
     */
    public ScanTask(List<Path> paths, ScanConfig config) {
        this.paths = paths;
        this.config = config;
    }

    /**
     * The main computation performed by this task.
     *
     * @return the result of the computation
     */
    @Override
    protected ScanResult compute() {
        ScanResult result = new ScanResult();

        if (paths == null || paths.isEmpty()) {
            return result;
        }

        // it directly parse the list...
        if (paths.size() < config.getThreshold()) {
            for (Path path : paths) {
                try {
                    Mp3File mp3 = new Mp3File(path);
                    result
                        .joinFiles(1)
                        .joinBytes(mp3.getLength());
                } catch (Exception e) {
                    result.addError(path, e.getMessage());
                }
            }
        } else {
            // otherwise it split the job in two tasks
            List<Path> subset1 = paths.subList(0, paths.size() / 2);
            ScanTask subTaskOne = new ScanTask(subset1, config);

            List<Path> subset2 = paths.subList(paths.size() / 2, paths.size());
            ScanTask subTaskTwo = new ScanTask(subset2, config);

            invokeAll(subTaskOne, subTaskTwo);

            result.joinResult(subTaskOne.join());
            result.joinResult(subTaskTwo.join());
        }

        return result;
    }
}
