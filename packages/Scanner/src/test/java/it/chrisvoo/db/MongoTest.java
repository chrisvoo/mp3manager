package it.chrisvoo.db;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.junit.jupiter.api.Assertions.*;

/**
 *  Reactive Streams is an initiative to provide a standard for asynchronous stream processing with
 *  non-blocking back pressure.
 *  The MongoDB Reactive Streams Java Driver is built upon the MongoDB Async driver which is callback driven.
 *  All Publishers returned from the API are cold, meaning that no I/O happens until they are subscribed
 *  to and the subscription makes a request. So just creating a Publisher won’t cause any network IO.
 *  It’s not until Subscription.request() is called that the driver executes the operation.
 */
public class MongoTest {

    private static MongoClient client;
    private static String collectionName = "test_reactive_driver";
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

    @Test
    public void testMongoReactiveStreamAPI() throws Throwable {
        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(collectionName);

        // write
        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("info", new Document("x", 203).append("y", 102));

        OperationSubscriber<Success> sub = new OperationSubscriber<>();
        collection.insertOne(doc).subscribe(sub);
        sub.await();

        // read
        OperationSubscriber<Document> subDoc = new OperationSubscriber<>();
        collection
                .find(eq("info.x", 203))
                .first()
                .subscribe(subDoc);
        List<Document> docsFound = subDoc.get();
        Document docFound = docsFound.get(0);

        assertNotNull(docFound.getObjectId("_id").toString());
        assertEquals("MongoDB", docFound.getString("name"));
        assertEquals(1, docFound.getInteger("count"));
        assertEquals(203, docFound.get("info", Document.class).getInteger("x"));

        // bulk write
        List<WriteModel<Document>> docs = List.of(
            new InsertOneModel<>(
                new Document("name", "MongoDB")
                    .append("type", "database")
                    .append("count", 2)
                    .append("info", new Document("x", 204).append("y", 103))
            ),
            new UpdateOneModel<>(
                new Document("_id", 1),
                new Document("$set", new Document("x", 2)),
                new UpdateOptions().upsert(true)
            ),
            new InsertOneModel<>(
                new Document("name", "MongoDB")
                    .append("type", "database")
                    .append("count", 4)
                    .append("info", new Document("x", 206).append("y", 105))
            )
        );

        OperationSubscriber<BulkWriteResult> subBulk = new OperationSubscriber<>();
        collection.bulkWrite(docs).subscribe(subBulk);
        OperationSubscriber<BulkWriteResult> result = subBulk.await();
        List<BulkWriteResult> bulkList = result.getReceived();

        BulkWriteResult writeRes = bulkList.get(0);
        assertEquals(2, writeRes.getInsertedCount());
        assertEquals(0, writeRes.getModifiedCount());
        assertEquals(1, writeRes.getUpserts().size());
        assertEquals(0, writeRes.getMatchedCount());
    }

    @Test
    public void pojoTest() throws Throwable {
        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClients.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoDatabase database = client.getDatabase(databaseName);
        MongoCollection<FileDocument> collection = database
                .getCollection(collectionName, FileDocument.class)
                .withCodecRegistry(pojoCodecRegistry);

        FileDocument doc = new FileDocument()
            .setYear("2010")
            .setAlbumTitle("An album")
            .setBitrate(320)
            .setHasCustomTag(false)
            .setFileName("/home/user/Beautiful Song.mp3")
            .setArtist("Tester")
            .setBitrateType(BitrateType.CONSTANT)
            .setDuration(125)
            .setGenre("Heavy Metal")
            .setHasID3v1Tag(false)
            .setHasID3v2Tag(true)
            .setSize(5623114)
            .setTitle("Beautiful song")
            .setAlbumImageMimeType("img/jpeg")
            .setHasAlbumImage(true)
            .setAlbumImage(new byte[]{1, 2, 3});

        OperationSubscriber<Success> sub = new OperationSubscriber<>();
        collection.insertOne(doc).subscribe(sub);
        sub.await();

        OperationSubscriber<FileDocument> subDoc = new OperationSubscriber<>();
        collection
                .find(eq("album_title", "An album"))
                .subscribe(subDoc);
        List<FileDocument> fileFound = subDoc.get();
        assertNotNull(fileFound);

        FileDocument theFile = fileFound.get(0);
        assertEquals(doc.getAlbumTitle(), theFile.getAlbumTitle());
        assertEquals(doc.getYear(), theFile.getYear());
        assertEquals(doc.getBitrate(), theFile.getBitrate());
        assertFalse(theFile.hasCustomTag());
        assertEquals(doc.getFileName(), theFile.getFileName());
        assertEquals(doc.getArtist(), theFile.getArtist());
        assertEquals(doc.getBitrateType(), theFile.getBitrateType());
        assertEquals(doc.getDuration(), theFile.getDuration());
        assertEquals(doc.getGenre(), theFile.getGenre());
        assertFalse(theFile.hasID3v1Tag());
        assertTrue(theFile.hasID3v2Tag());
        assertEquals(doc.getSize(), theFile.getSize());
        assertEquals(doc.getTitle(), theFile.getTitle());
        assertEquals(doc.getAlbumImageMimeType(), theFile.getAlbumImageMimeType());
        assertTrue(theFile.hasAlbumImage());
        assertArrayEquals(doc.getAlbumImage(), theFile.getAlbumImage());
    }
}
