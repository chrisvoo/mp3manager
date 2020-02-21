package it.chrisvoo.scanner;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import it.chrisvoo.utils.DbUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        Logger.getLogger( "org.mongodb.driver" ).setLevel(Level.WARNING);
        Config appConf = ConfigFactory.parseFile(
                Paths.get("./application.conf").toFile()
        );
        String databaseName = appConf.getString("scanner.db.dbname");
        assertEquals("music_manager", appConf.getString("scanner.db.dbname"));

        MongoClient client = DbUtils.getClient(appConf);
        MongoDatabase database = client.getDatabase(databaseName);

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

        assertEquals(37421248, result.getTotalBytes());
        assertEquals(13, result.getTotalFilesScanned());

    }
}
