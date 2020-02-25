package it.chrisvoo.scanner;

import it.chrisvoo.utils.FileSystemUtils;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the result returned by the RecursiveTask.
 */
public class ScanResult {
    @BsonId
    private ObjectId id;

    /**
     * Total size of all the parsed music files
     */
    @BsonProperty(value = "total_bytes")
    private long totalBytes;

    /**
     * Total files parsed.
     */
    @BsonProperty(value = "total_files_scanned")
    private int totalFilesScanned;

    /**
     * Total files inserted into MongoDB
     */
    @BsonProperty(value = "total_files_inserted")
    private int totalFilesInserted;

    @BsonIgnore
    private Duration totalDuration;

    @BsonProperty(value = "total_elapsed_time")
    private String elapsedTime;

    /**
     * Eventual errors thrown during the process. The key is the path, the value the error message.
     */
    @BsonIgnore
    private Map<String, String> errors;

    /**
     * Just init the class fields.
     */
    public ScanResult() {
        totalBytes = 0;
        totalFilesScanned = 0;
        totalFilesInserted = 0;
        errors = new HashMap<String, String>();
    }

    /**
     * It returns the total number of bytes of all files found by this instance.
     * @return The total bytes for this instance
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * It sets the total number of bytes
     * @param bytes Total number of bytes
     * @return This instance.
     */
    public ScanResult setTotalBytes(long bytes) {
        totalBytes = bytes;
        return this;
    }

    /**
     * It returns the total number of files found in this instance.
     * @param files Total number of files found
     * @return This instance.
     */
    public ScanResult setTotalFilesScanned(int files) {
        totalFilesScanned = files;
        return this;
    }

    /**
     * It merges the total number of bytes of all the files found by this result with the one of another result.
     * @param bytes The total bytes
     * @return This instance.
     */
    public ScanResult joinBytes(long bytes) {
        totalBytes += bytes;
        return this;
    }

    /**
     * It merges the total number of audio files found by the scanner in this result with the one of another result.
     * @param numFiles Total number of files.
     * @return This instance.
     */
    public ScanResult joinScannedFiles(int numFiles) {
        totalFilesScanned += numFiles;
        return this;
    }

    /**
     * It merges the total number of files inserted into the db by this instance with the one of another instance.
     * @param numFiles Total number of files.
     * @return This instance.
     */
    public ScanResult joinInsertedFiles(int numFiles) {
        totalFilesInserted += numFiles;
        return this;
    }

    /**
     * It merges the current errors occurred for this result with the ones of another result.
     * @param errors A map of errors
     * @return This instance.
     */
    public ScanResult joinErrors(Map<String, String> errors) {
        if (errors == null) {
            return this;
        }

        if (!errors.isEmpty()) {
            this.errors.putAll(errors);
        }
        return this;
    }

    /**
     * Returns the errors for this result.
     * @return A map of errors.
     */
    public Map<String, String> getErrors() {
        return errors;
    }

    /**
     * It directly sets some errors details for this result.
     * @param errors A map of errors
     * @return This instance.
     */
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

    /**
     * It returns the total number of files inserted for this instance.
     * @return The total num of files inserted into db.
     */
    public int getTotalFilesInserted() {
        return totalFilesInserted;
    }

    /**
     * It directly sets the total number of files inserted into the database.
     * @param totalFilesInserted Total num. of files inserted.
     * @return This instance
     */
    public ScanResult setTotalFilesInserted(int totalFilesInserted) {
        this.totalFilesInserted = totalFilesInserted;
        return this;
    }

    /**
     * Total files scanned.
     * @return The total number of audio files found by this result.
     */
    public int getTotalFilesScanned() {
        return totalFilesScanned;
    }

    /**
     * It tells if the process encountered some errors.
     * @return Boolean true if there is at least one error
     */
    public Boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Merge the results of this instance with the ones of another one, performing sums.
     * @param result Another result
     * @return This instance updated with the results of another instance
     */
    public ScanResult joinResult(ScanResult result) {
       if (result == null) {
           return this;
       }

       // we do not consider the total time elapsed, we just use that field in the Main class
       return this.joinErrors(result.getErrors())
            .joinScannedFiles(result.getTotalFilesScanned())
            .joinInsertedFiles(result.getTotalFilesInserted())
            .joinBytes(result.getTotalBytes());
    }

    /**
     * It returns the total duration of a scanning activity
     * @return The duration
     */
    public String formatTotalDuration() {
        long minElapsed = totalDuration.toMinutesPart();
        long secsElapsed = totalDuration.toSecondsPart();
        long millisElapsed = totalDuration.toMillisPart();

        String durationFormatted = "";
        if (minElapsed != 0) {
            durationFormatted += minElapsed + " minutes";
        }

        if (secsElapsed != 0) {
            durationFormatted += minElapsed != 0 ? ", " : "";
            durationFormatted += secsElapsed + " seconds";
        }

        durationFormatted += (minElapsed != 0 || secsElapsed != 0) ? " and " : "";
        durationFormatted += millisElapsed + " milliseconds";
        return durationFormatted;
    }

    /**
     * Sets the total duration of a scanning activity. It's used in {@link it.chrisvoo.Main}
     * @param totalDuration The total duration
     * @return This instance
     */
    public ScanResult setTotalDuration(Duration totalDuration) {
        this.totalDuration = totalDuration;
        elapsedTime = formatTotalDuration();
        return this;
    }

    public ObjectId getId() {
        return id;
    }

    public ScanResult setId(ObjectId id) {
        this.id = id;
        return this;
    }

    public String getElapsedTime() {
        return elapsedTime;
    }

    @BsonProperty(value = "total_errors")
    public int getTotalErrors() {
        return errors.size();
    }

    @BsonProperty(value = "scanning_date")
    public Date getScanningDate() {
        return new Date();
    }

    /**
     * Prints details about the parsing activity
     * @return Stats about the scanner activity
     */
    public String toString() {
        String durationFormatted = formatTotalDuration();
        String result = String.format("%nScanning finished in " + durationFormatted + ". Results: %n" +
               "\t- total files scanned: " + totalFilesScanned + "%n" +
               "\t- total files inserted: " + totalFilesInserted +
                " (" + (totalFilesInserted * 100 / totalFilesScanned) + " %%) %n" +
               "\t- total bytes: " + FileSystemUtils.formatSize(totalBytes) + "%n" +
               "\t- total errors: " + errors.size() + "%n%n"
        );

        if (hasErrors()) {
            for (Map.Entry<String,String> entry : getErrors().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println();
        }

        return result;
    }
}
