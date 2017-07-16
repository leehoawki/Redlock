package redlock;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class RedLockTest extends TestCase {

    RedLock redLock;

    @Override
    public void setUp() {
        redLock = RedLock.create();
    }

    @Test
    public void testLock() throws InterruptedException {
        Lock lock = redLock.lock("test");

        Thread t = new Thread() {
            @Override
            public void run() {
                long ts1 = new Date().getTime();
                redLock.lock("test");
                long ts2 = new Date().getTime();
                Assert.assertTrue(ts2 - ts1 > 9999);
            }
        };

        Thread.sleep(10000);
        redLock.unlock(lock);
    }
}
