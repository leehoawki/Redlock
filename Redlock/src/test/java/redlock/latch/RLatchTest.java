package redlock.latch;

import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Assert;
import org.junit.Test;
import redlock.RedLock;

import java.util.Date;

public class RLatchTest extends TestCase {

    RedLock redLock;

    @Override
    public void setUp() {
        redLock = RedLock.create();
    }

    public void tearDown() throws InterruptedException {
        redLock.shutdown();
    }

    @Test
    public void testCountdown() {
        RLatch latch = redLock.getLatch("test", 2);
        Assert.assertEquals(2, latch.getCount());
        latch.countDown();
        Assert.assertEquals(1, latch.getCount());
        latch.countDown();
        Assert.assertEquals(0, latch.getCount());
    }

    @Test
    public void testAwaitAtMultiThread() throws Throwable {
        int runnerCount = 80;
        TestRunnable[] trs = new TestRunnable[runnerCount + 1];
        RLatch latch = redLock.getLatch("test", runnerCount);
        for (int i = 0; i < runnerCount; i++) {
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":AWATING.");
                    latch.await();
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":PASS.");
                }
            };
        }

        trs[runnerCount] = new TestRunnable() {
            @Override
            public void runTest() throws Throwable {
                for (int i = 0; i < runnerCount; i++) {
                    Thread.sleep(10);
                    System.out.println(new Date() + ":" + "COUNTDOWN.");
                    latch.countDown();
                }
            }
        };

        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }


    @Test
    public void testAwaitAtInstance() throws Throwable {
        int runnerCount = 80;
        TestRunnable[] trs = new TestRunnable[runnerCount + 1];
        RLatch latch = redLock.getLatch("test", runnerCount);
        for (int i = 0; i < runnerCount; i++) {
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    RedLock rl = RedLock.create();
                    RLatch l = redLock.getLatch("test");
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":AWATING.");
                    l.await();
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":PASS.");
                    rl.shutdown();
                }
            };
        }

        trs[runnerCount] = new TestRunnable() {
            @Override
            public void runTest() throws Throwable {
                for (int i = 0; i < runnerCount; i++) {
                    RedLock rl = RedLock.create();
                    Thread.sleep(10);
                    RLatch l = redLock.getLatch("test");
                    System.out.println(new Date() + ":" + "COUNTDOWN.");
                    l.countDown();
                    rl.shutdown();
                }
            }
        };

        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }
}
