package redlock;

import junit.framework.TestCase;
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
}
