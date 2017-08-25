package redlock.latch;


import java.util.concurrent.TimeUnit;

public interface RLatch {
    void countDown();

    long getCount();

    boolean await(long timeout, TimeUnit unit);

    void await() throws InterruptedException;
}
