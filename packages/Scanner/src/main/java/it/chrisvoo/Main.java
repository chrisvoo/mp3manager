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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class Main implements Callable<Integer> {
    @Option(names = {"-c", "--configuration"}, description = "Configuration file's path")
    private File confFile;

    public static void main(String[] args) {
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
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

        if (config.hasPath("scanner.paths")) {
            List<String> thePaths = config.getStringList("scanner.paths");
            paths = FileSystemUtils.mergePaths(thePaths);
        } else {
            System.out.println("'paths' property cannot be empty");
            return -1;
        }

        String musicManagerDbName = config.hasPath("scanner.db.dbname")
                ? config.getString("scanner.db.dbname")
                : "music_manager";

        MongoClient client = DbUtils.getClient(config);

        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClients.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoDatabase database = client.getDatabase(musicManagerDbName).withCodecRegistry(pojoCodecRegistry);

        ScanConfig scanConfig =
                new ScanConfig()
                    .setChosenPaths(paths)
                    .setThreshold(threshold)
                    .setDatabase(database);

        Scanner scanner = new Scanner(scanConfig);
        ScanResult result = scanner.start();

        client.close();
        System.out.println(result.toString());

        return 0;
    }
}
