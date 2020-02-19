package it.chrisvoo;

import com.mongodb.reactivestreams.client.*;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;

/**
 *  Reactive Streams is an initiative to provide a standard for asynchronous stream processing with
 *  non-blocking back pressure.
 *  The MongoDB Reactive Streams Java Driver is built upon the MongoDB Async driver which is callback driven.
 *  All Publishers returned from the API are cold, meaning that no I/O happens until they are subscribed
 *  to and the subscription makes a request. So just creating a Publisher won’t cause any network IO.
 *  It’s not until Subscription.request() is called that the driver executes the operation.
 *
 * - Publisher: a provider of a potentially unbounded number of sequenced elements,
 *              published according to the demand received from it’s Subscriber(s).
 *
 *   1) Publisher.subscribe(Subscriber)
 *   2)
 */
public class MongoTest {
    @Test
    public void testMongoReactiveStreamAPI() throws Throwable {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/music_manager");
        MongoDatabase database = mongoClient.getDatabase("music_manager");
        MongoCollection<Document> collection = database.getCollection("test");

        Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("info", new Document("x", 203).append("y", 102));

        OperationSubscriber<Success> sub = new OperationSubscriber<Success>();
        collection.insertOne(doc).subscribe(sub);
        sub.await();

        mongoClient.close();
    }
}
