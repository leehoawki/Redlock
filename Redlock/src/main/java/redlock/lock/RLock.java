package redlock.lock;

public interface RLock {
    boolean tryLock(long leaseTime);

    void lock(long leaseTime) throws InterruptedException;

    void unlock();

    boolean isLocked();

    default boolean tryLock() throws InterruptedException {
        return tryLock(0);
    }

    default void lock() throws InterruptedException {
        lock(0);
    }
}
