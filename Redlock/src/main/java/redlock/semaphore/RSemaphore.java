package redlock.semaphore;


public interface RSemaphore {
    default void acquire() throws InterruptedException {
        acquire(1);
    }

    void acquire(int permits) throws InterruptedException;

    default boolean tryAcquire() {
        return tryAcquire(1);
    }

    boolean tryAcquire(int permits);

    default void release() {
        release(1);
    }

    void release(int permits);
}
