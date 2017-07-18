package redlock.lock;

public interface RLock {
    boolean tryLock();

    default void lock() throws InterruptedException {
        lock(0);
    }

    void lock(long leaseTime) throws InterruptedException;

    void unlock();

    boolean isLocked();
}
