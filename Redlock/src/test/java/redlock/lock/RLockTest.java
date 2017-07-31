package redlock.lock;

import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import org.junit.Assert;
import org.junit.Test;
import redlock.RedLock;

import java.util.Date;

public class RLockTest extends TestCase {

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
        Assert.assertFalse(lock.isLocked());
        lock.lock();
        lock.lock();
        Assert.assertTrue(lock.isLocked());
        lock.unlock();
        Assert.assertTrue(lock.isLocked());
        lock.unlock();
        Assert.assertFalse(lock.isLocked());
    }

    @Test
    public void testTrylock() throws InterruptedException {
        boolean r1 = lock.tryLock();
        boolean r2 = lock.tryLock();
        Assert.assertTrue(lock.isLocked());
        Assert.assertTrue(r1);
        Assert.assertTrue(r2);
        lock.unlock();
        Assert.assertTrue(lock.isLocked());
        lock.unlock();
        Assert.assertFalse(lock.isLocked());
    }

    @Test
    public void testUnlock() throws InterruptedException {
        Assert.assertFalse(lock.isLocked());
        lock.lock();
        Assert.assertTrue(lock.isLocked());
        lock.unlock();
        Assert.assertFalse(lock.isLocked());
        lock.unlock();
        Assert.assertFalse(lock.isLocked());
        lock.unlock();
        Assert.assertFalse(lock.isLocked());
    }

    @Test
    public void testForceUnlock() throws InterruptedException {
        Assert.assertFalse(lock.isLocked());
        lock.lock();
        lock.lock();
        Assert.assertTrue(lock.isLocked());
        lock.forceUnlock();
        Assert.assertFalse(lock.isLocked());
    }

    @Test
    public void testLockAtMultiThread() throws Throwable {
        int runnerCount = 4;
        TestRunnable[] trs = new TestRunnable[runnerCount];
        for (int i = 0; i < runnerCount; i++) {
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    RLock lock = redLock.getLock("test");
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":LOCKING.");
                    lock.lock();
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":LOCKED.");
                    lock.unlock();
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":UNLOCKED.");
                }
            };
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }

    @Test
    public void testLockAtMultiThread2() throws Throwable {
        int runnerCount = 4;
        TestRunnable[] trs = new TestRunnable[runnerCount];
        for (int i = 0; i < runnerCount; i++) {
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":LOCKING.");
                    lock.lock(1000);
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":LOCKED.");
                }
            };
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }

    @Test
    public void testLockAtMultiThread3() throws Throwable {
        int runnerCount = 4;
        TestRunnable[] trs = new TestRunnable[runnerCount];
        for (int i = 0; i < runnerCount; i++) {
            trs[i] = new TestRunnable() {
                @Override
                public void runTest() throws Throwable {
                    RLock lock = redLock.getLock("test");
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":LOCKING.");
                    lock.lock();
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":LOCKED.");
                    Thread.sleep(1000);
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":UNLOCKING.");
                    lock.unlock();
                    System.out.println(new Date() + ":" + Thread.currentThread().getName() + ":UNLOCKED.");
                }
            };
        }
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
        mttr.runTestRunnables();
    }
}
