package redlock.semaphore;

import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Assert;
import org.junit.Test;
import redlock.RedLock;

import java.util.Date;

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
    public void testTryAcquire() {
        Assert.assertFalse(semaphore.tryAcquire());
        semaphore.release();
        Assert.assertFalse(semaphore.tryAcquire(2));
        Assert.assertTrue(semaphore.tryAcquire());
        semaphore.release();
        semaphore.release();
        Assert.assertTrue(semaphore.tryAcquire(2));
        Assert.assertFalse(semaphore.tryAcquire());
    }

    @Test
    public void testAcquire() throws InterruptedException {
        semaphore.release();
        semaphore.acquire();
        semaphore.release();
        semaphore.acquire();
        semaphore.release();
        semaphore.release();
        semaphore.acquire();
        semaphore.acquire();
    }

    @Test
    public void testAcquireAtMultiThread() throws Throwable {
        int runnerCount = 8;
        TestRunnable[] trs = new TestRunnable[runnerCount + 1];
        for (int i = 0; i < runnerCount; i++) {
            final int permit = 1;
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":ACQUIRING.");
                    semaphore.acquire(permit);
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":ACQUIRED.");
                }
            };
        }

        trs[runnerCount] = new TestRunnable() {
            @Override
            public void runTest() throws Throwable {
                for (int i = 0; i < runnerCount; i++) {
                    Thread.sleep(1000);
                    semaphore.release(1);
                }
            }
        };

        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }

    @Test
    public void testAcquireAtInstance() throws Throwable {
        int runnerCount = 8;
        TestRunnable[] trs = new TestRunnable[runnerCount + 1];
        for (int i = 0; i < runnerCount; i++) {
            final int permit = 1;
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    RedLock rl = RedLock.create();
                    RSemaphore s = rl.getSemaphore("test");
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":ACQUIRING.");
                    s.acquire(permit);
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":ACQUIRED.");
                    rl.shutdown();
                }
            };
        }

        trs[runnerCount] = new TestRunnable() {
            @Override
            public void runTest() throws Throwable {
                for (int i = 0; i < runnerCount; i++) {
                    Thread.sleep(1000);
                    RedLock rl = RedLock.create();
                    RSemaphore s = rl.getSemaphore("test");
                    s.release(1);
                    rl.shutdown();
                }
            }
        };

        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }
}
