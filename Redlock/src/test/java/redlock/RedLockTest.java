package redlock;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import redlock.lock.RLock;

public class RedLockTest extends TestCase {

    RedLock redLock;

    RLock lock;

    @Override
    public void setUp() {
        redLock = RedLock.create();
        lock = redLock.getLock("test");
    }

    public void tearDown() throws InterruptedException {
        redLock.shutdown();
    }

    @Test
    public void testLock() throws InterruptedException {
        lock.lock();
        lock.unlock();
    }

    @Test
    public void testTrylock() throws InterruptedException {
        boolean r1 = lock.tryLock();
        boolean r2 = lock.tryLock();
        Assert.assertTrue(r1);
        Assert.assertTrue(!r2);
        lock.unlock();
    }
}
