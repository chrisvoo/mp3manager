package it.chrisvoo;

import com.mongodb.MongoTimeoutException;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OperationSubscriber<T> implements Subscriber<T> {
    private final List<T> received;
    private final List<Throwable> errors;
    private final CountDownLatch latch;
    private volatile Subscription subscription;
    private volatile boolean completed;

    public OperationSubscriber() {
        this.received = new ArrayList<T>();
        this.errors = new ArrayList<Throwable>();
        this.latch = new CountDownLatch(1);
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        System.out.println("onSubscribe: " + subscription.toString());
        this.subscription = subscription;
        this.subscription.request(Integer.MAX_VALUE);
    }

    /**
     * Once the document has been inserted the onNext method will be called and it will print “Inserted!”
     * followed by the onComplete method
     * @param item An object
     */
    @Override
    public void onNext(T item) {
        System.out.println("Inserted");
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.printf("Failed: %s", throwable.getMessage());
    }

    @Override
    public void onComplete() {
        latch.countDown();
        System.out.println("Done");
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public List<T> getReceived() {
        return received;
    }

    public Throwable getError() {
        if (errors.size() > 0) {
            return errors.get(0);
        }
        return null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public List<T> get(final long timeout, final TimeUnit unit) throws Throwable {
        return await(timeout, unit).getReceived();
    }

    public OperationSubscriber<T> await() throws Throwable {
        return await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public OperationSubscriber<T> await(final long timeout, final TimeUnit unit) throws Throwable {
        subscription.request(Integer.MAX_VALUE);
        if (!latch.await(timeout, unit)) {
            throw new MongoTimeoutException("Publisher onComplete timed out");
        }
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
        return this;
    }
}
