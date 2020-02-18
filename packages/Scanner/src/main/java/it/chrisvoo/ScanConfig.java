package it.chrisvoo;

import java.nio.file.Path;
import java.util.List;

/**
 * Configuration to be passed to {@link Scanner} class
 */
public class ScanConfig {
    /**
     * Paths passed via command line
     */
    private List<Path> chosenPaths;

    /**
     * Threshold over which tasks will be split
     */
    private int threshold;

    /**
     * Mongo connection string
     */
    private String mongoConnectionUri;

    public List<Path> getChosenPaths() {
        return chosenPaths;
    }

    public ScanConfig setChosenPaths(List<Path> chosenPaths) {
        this.chosenPaths = chosenPaths;
        return this;
    }

    public int getThreshold() {
        return threshold;
    }

    public ScanConfig setThreshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    public String getMongoConnectionUri() {
        return mongoConnectionUri;
    }

    public ScanConfig setMongoConnectionUri(String mongoConnectionUri) {
        this.mongoConnectionUri = mongoConnectionUri;
        return this;
    }
}
