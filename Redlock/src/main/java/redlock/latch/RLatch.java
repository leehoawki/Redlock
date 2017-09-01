package redlock.latch;


public interface RLatch {
    void countDown();

    long getCount();

    void await() throws InterruptedException;
}
