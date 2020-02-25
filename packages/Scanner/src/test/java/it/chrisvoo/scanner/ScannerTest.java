package it.chrisvoo.scanner;

import com.mongodb.reactivestreams.client.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.chrisvoo.db.OperationSubscriber;
import it.chrisvoo.utils.DbUtils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Scanner tests
 */
public class ScannerTest {
    private static MongoClient client;
    private static String collectionName = "test_scanner";
    private static String databaseName = "music_manager_test";

    @BeforeAll
    static void initAll() {
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        client = MongoClients.create("mongodb://localhost:27017");
    }
    
    @AfterAll
    static void tearDownAll() throws Throwable {
        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        OperationSubscriber<Success> sub = new OperationSubscriber<>();
        collection.drop().subscribe(sub);
        sub.await();

        client.close();
    }
    
    /**
     * Can collect the correct number of files from a directory and all its subdirectories
     */
    @Test
    public void canScanADirectoryTree() {
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        Config appConf = ConfigFactory.parseFile(
                Paths.get("./application.conf").toFile()
        );

        CodecRegistry pojoCodecRegistry = fromRegistries(
             MongoClients.getDefaultCodecRegistry(),
             fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoClient client = DbUtils.getClient(appConf);
        MongoDatabase database = client
                .getDatabase(databaseName)
                .withCodecRegistry(pojoCodecRegistry);

        ScanConfig config =
                new ScanConfig()
                        .setThreshold(3)
                        .setDatabase(database)
                        .setChosenPaths(
                            List.of(Paths.get("./target/test-classes/tree"))
                        );
        Scanner scanner = new Scanner(config);

        Instant start = Instant.now();
        ScanResult result = scanner.start();
        Instant finish = Instant.now();

        result.setTotalDuration(Duration.between(start, finish));

        client.close();

        if (result.hasErrors()) {
            for (Map.Entry<String,String> entry : result.getErrors().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            fail("There are " + result.getErrors().size() + " errors");
        }

        assertEquals(42656676, result.getTotalBytes());
        assertEquals(14, result.getTotalFilesScanned());
        assertEquals(14, result.getTotalFilesInserted());
        System.out.println(result.toString());
    }
}
