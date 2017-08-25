package redlock.latch;

import junit.framework.TestCase;
import org.junit.Test;
import redlock.RedLock;
import redlock.semaphore.RSemaphore;

public class RLatchTest extends TestCase {

    RedLock redLock;

    RLatch latch;

    @Override
    public void setUp() {
        redLock = RedLock.create();
    }

    public void tearDown() throws InterruptedException {
        redLock.shutdown();
    }

    @Test
    public void testAcquireAtMultiThread() throws Throwable {

    }
}
