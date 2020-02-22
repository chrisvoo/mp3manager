package it.chrisvoo;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.chrisvoo.scanner.ScanConfig;
import it.chrisvoo.scanner.ScanResult;
import it.chrisvoo.scanner.Scanner;
import it.chrisvoo.utils.DbUtils;
import it.chrisvoo.utils.FileSystemUtils;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Main implements Callable<Integer> {
    @Option(names = {"-c", "--configuration"}, paramLabel = "FILE", description = "Configuration file's path")
    private File confFile;

    @Option(names = "--help", usageHelp = true, description = "display this help and exit")
    boolean help;

    @Option(names = {"-d", "--dry-run"}, description = "do not save anything into the database")
    boolean dryRun;

    private MongoClient client;

    public static void main(String[] args) {
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        int exitCode = new CommandLine(new Main())
                .setStopAtUnmatched(true)
                .execute(args);
        System.exit(exitCode);
    }

    /**
     * Creates a MongoClient using the specified configuration file-
     * @param config The configuration instance
     * @return The {@link MongoClient} instance or null if this CLI is running in dry run mode
     */
    private MongoDatabase getDatabase(Config config) {
        if (!dryRun) {
            String musicManagerDbName = config.hasPath("scanner.db.dbname")
                    ? config.getString("scanner.db.dbname")
                    : "music_manager";

            client = DbUtils.getClient(config);

            CodecRegistry pojoCodecRegistry = fromRegistries(
                    MongoClients.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );

            return client.getDatabase(musicManagerDbName).withCodecRegistry(pojoCodecRegistry);
        }

        return null;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result or if don't pass any path.
     */
    @Override
    public Integer call() throws Exception {
        List<Path> paths;
        Config config = ConfigFactory.parseFile(confFile);

        int threshold = (config.hasPath("scanner.threshold"))
                    ? config.getInt("scanner.threshold")
                    : -1;

        if (config.hasPath("scanner.music_paths")) {
            List<String> thePaths = config.getStringList("scanner.music_paths");
            paths = FileSystemUtils.mergePaths(thePaths);
        } else {
            System.out.println("'paths' property cannot be empty");
            return -1;
        }

        ScanConfig scanConfig =
                new ScanConfig()
                    .setChosenPaths(paths)
                    .setThreshold(threshold)
                    .setDatabase(getDatabase(config));

        Instant start = Instant.now();
        Scanner scanner = new Scanner(scanConfig);
        ScanResult result = scanner.start();
        Instant finish = Instant.now();

        result.setTotalTimeElapsed(Duration.between(start, finish));

        client.close();
        System.out.println(result.toString());

        return 0;
    }
}
