package redlock.semaphore;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import redlock.RedLock;

public class RSemaphoreTest extends TestCase {

    RedLock redLock;

    RSemaphore semaphore;

    @Override
    public void setUp() {
        redLock = RedLock.create();
        semaphore = redLock.getSemaphore("test");
    }

    public void tearDown() throws InterruptedException {
        redLock.shutdown();
    }

    @Test
    public void testAcquire() {
        Assert.assertFalse(semaphore.tryAcquire());
        semaphore.release();
        Assert.assertTrue(semaphore.tryAcquire());
    }
}
