package it.chrisvoo.scanner;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the result returned by the RecursiveTask.
 */
public class ScanResult {
    /**
     * Total size of all the parsed music files
     */
    private long totalBytes;

    /**
     * Total files parsed.
     */
    private int totalFiles;

    /**
     * Eventual errors thrown during the process. The key is the path, the value the error message.
     */
    private Map<String, String> errors;

    public ScanResult() {
        totalBytes = 0;
        totalFiles = 0;
        errors = new HashMap<String, String>();
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public ScanResult setTotalBytes(long bytes) {
        totalBytes = bytes;
        return this;
    }

    public ScanResult setTotalFiles(int files) {
        totalFiles = files;
        return this;
    }

    public ScanResult joinBytes(long bytes) {
        totalBytes += bytes;
        return this;
    }

    public ScanResult joinFiles(int numFiles) {
        totalFiles += numFiles;
        return this;
    }

    public ScanResult joinErrors(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            this.errors.putAll(errors);
        }
        return this;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public ScanResult setErrors(Map<String, String> errors) {
        this.errors = errors;
        return this;
    }

    /**
     * Adds an error
     * @param path A Path representing a file.
     * @param errorMessage An error message
     * @return This class
     */
    public ScanResult addError(Path path, String errorMessage) {
        errors.put(path.toString(), errorMessage);
        return this;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    /**
     * It tells if the process encountered some errors.
     * @return Boolean true if there is at least one error
     */
    public Boolean hasErrors() {
        return !errors.isEmpty();
    }

    public ScanResult joinResult(ScanResult result) {
       return this.joinErrors(result.getErrors())
            .joinFiles(result.getTotalFiles())
            .joinBytes(result.getTotalBytes());
    }

    public String toString() {
        return "Total files: " + totalFiles + ", total bytes: " + totalBytes + ". Errors: " + errors.size();
    }
}
