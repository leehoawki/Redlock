package redlock;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class RedLockTest extends TestCase {

    RedLock redLock;

    @Override
    public void setUp() {
        redLock = RedLock.create();
    }

    public void tearDown() {
        redLock.shutdown();
    }

    @Test
    public void testLock() throws InterruptedException {
        Lock lock = redLock.lock("test");
        redLock.unlock(lock);
    }

    @Test
    public void testTrylock() throws InterruptedException {
        Lock lock1 = redLock.tryLock("test");
        Lock lock2 = redLock.tryLock("test");
        Assert.assertTrue(lock1 != null);
        Assert.assertTrue(lock2 == null);
        redLock.unlock(lock1);
    }
}
