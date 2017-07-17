package redlock;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

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

        Thread t = new Thread(() -> {
            long ts1 = new Date().getTime();
            try {
                redLock.lock("test");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long ts2 = new Date().getTime();
            Assert.assertTrue(ts2 - ts1 > 9999);
        });

        Thread.sleep(10000);
        redLock.unlock(lock);
    }
}
