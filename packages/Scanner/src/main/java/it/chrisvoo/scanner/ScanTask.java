package it.chrisvoo.scanner;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.*;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mpatric.mp3agic.Mp3File;
import it.chrisvoo.db.FileDocument;
import it.chrisvoo.db.OperationSubscriber;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import static com.mongodb.client.model.Filters.eq;

/**
 * It process by default up to 100 files. If it receives more than this,
 * the work gets split.
 */
public class ScanTask extends RecursiveTask<ScanResult> {
    private ScanConfig config;
    private List<Path> paths;

    /**
     * An intance of a RecursiveTask subclass.
     * @param paths List of paths to be scanned
     * @param config Configuration which will be passed to every {@link ScanTask} instance
     */
    public ScanTask(List<Path> paths, ScanConfig config) {
        this.paths = paths;
        this.config = config;
    }

    /**
     * The main computation performed by this task.
     *
     * @return the result of the computation
     */
    @Override
    protected ScanResult compute() {
        ScanResult result = new ScanResult();

        if (paths == null || paths.isEmpty()) {
            return result;
        }

        // it directly parse the list...
        if (paths.size() < config.getThreshold()) {

            MongoCollection<FileDocument> collection = config
                    .getDatabase()
                    .getCollection("files", FileDocument.class);

            OperationSubscriber<BulkWriteResult> subBulk = new OperationSubscriber<>();

            List<WriteModel<FileDocument>> docs = new ArrayList<>();

            for (Path path : paths) {
                try {
                    FileDocument audioFile = new FileDocument(new Mp3File(path));

                    docs.add(
                        new ReplaceOneModel<>(
                            eq( "filename", audioFile.getFileName()),
                            audioFile,
                            new ReplaceOptions().upsert(true)
                        )
                    );

                    result
                        .joinScannedFiles(1)
                        .joinBytes(audioFile.getSize());
                } catch (Exception e) {
                    result.addError(path, e.getMessage());
                }
            }

            collection.bulkWrite(docs).subscribe(subBulk);
            OperationSubscriber<BulkWriteResult> bulkResult = null;
            try {
                bulkResult = subBulk.await();
                List<BulkWriteResult> bulkList = bulkResult.getReceived();
                BulkWriteResult writeRes = bulkList.get(0);
                result.joinInsertedFiles(
                        writeRes.getInsertedCount() +
                        writeRes.getUpserts().size() +
                        writeRes.getMatchedCount()
                );
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            // otherwise it split the job in two tasks
            List<Path> subset1 = paths.subList(0, paths.size() / 2);
            ScanTask subTaskOne = new ScanTask(subset1, config);

            List<Path> subset2 = paths.subList(paths.size() / 2, paths.size());
            ScanTask subTaskTwo = new ScanTask(subset2, config);

            invokeAll(subTaskOne, subTaskTwo);

            result.joinResult(subTaskOne.join());
            result.joinResult(subTaskTwo.join());
        }

        return result;
    }
}
