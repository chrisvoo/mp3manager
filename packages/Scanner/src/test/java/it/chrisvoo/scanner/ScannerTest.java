package it.chrisvoo.scanner;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.chrisvoo.utils.DbUtils;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Scanner tests
 */
public class ScannerTest {
    /**
     * Can collect the correct number of files from a directory and all its subdirectories
     */
    @Test
    public void canScanADirectoryTree() {
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.FINE);
        Config appConf = ConfigFactory.parseFile(
                Paths.get("./application.conf").toFile()
        );

        CodecRegistry pojoCodecRegistry = fromRegistries(
             MongoClients.getDefaultCodecRegistry(),
             fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoClient client = DbUtils.getClient(appConf);
        MongoDatabase database = client
                .getDatabase("music_manager_test")
                .withCodecRegistry(pojoCodecRegistry);

        ScanConfig config =
                new ScanConfig()
                        .setThreshold(3)
                        .setDatabase(database)
                        .setChosenPaths(
                            List.of(Paths.get("./target/test-classes/tree"))
                        );
        Scanner scanner = new Scanner(config);
        ScanResult result = scanner.start();
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
    }
}
