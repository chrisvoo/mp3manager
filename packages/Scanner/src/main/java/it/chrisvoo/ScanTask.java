package it.chrisvoo;

import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.RecursiveTask;

/**
 * It process by default up to 100 files. If it receives more than this,
 * the work gets split.
 */
public class ScanTask extends RecursiveTask<ScanResult> {
    private int threshold;
    private Path[] paths;

    public ScanTask(Path[] paths) {
        this(paths, 100);
    }

    public ScanTask(Path[] paths, int threshold) {
        this.paths = paths;
        this.threshold = threshold;
    }

    /**
     * The main computation performed by this task.
     *
     * @return the result of the computation
     */
    @Override
    protected ScanResult compute() {
        ScanResult result = new ScanResult();

        if (paths == null || paths.length == 0) {
            return result;
        }

        if (paths.length < threshold) {
            for (Path path : paths) {
                try {
                    Mp3File mp3 = new Mp3File(path);
                    result.joinFiles(1).joinBytes(path.toFile().length());
                } catch (Exception e) {
                    result.addError(path, e.getMessage());
                }
            }
        } else {
            Path[] subset1 = Arrays.copyOfRange(paths, 0, paths.length / 2);
            ScanTask subTaskOne = new ScanTask(subset1, threshold);

            Path[] subset2 = Arrays.copyOfRange(paths, paths.length / 2, paths.length);
            ScanTask subTaskTwo = new ScanTask(subset2, threshold);

            subTaskOne.fork();
            subTaskTwo.fork();

            result.joinResult(subTaskOne.join());
            result.joinResult(subTaskTwo.join());
        }

        return result;
    }
}
