package redlock;


import redlock.connection.RedisClient;
import redlock.connection.RedisSingle;
import redlock.lock.RLock;
import redlock.lock.RLockImpl;
import redlock.pubsub.Pubsub;

import java.util.Date;

public class RedLock {

    RedisClient client;

    Pubsub pubsub;

    RedLock(String host, int port) {
        this.client = new RedisSingle(host, port);
    }

    public static RedLock create() {
        return create("127.0.0.1", 6379);
    }

    public static RedLock create(String host, int port) {
        RedLock redLock = new RedLock(host, port);
        Pubsub pubsub = new Pubsub();
        redLock.setPubsub(pubsub);
        return redLock;
    }

    public RLock getLock(Object target) {
        RLockImpl rLock = new RLockImpl(target, client, pubsub);
        return rLock;
    }

    public void shutdown() {
        this.pubsub.shutdown();
    }

    public void setPubsub(Pubsub pubsub) {
        this.pubsub = pubsub;
    }

    public static void main(String[] args) throws InterruptedException {
        RedLock redLock = RedLock.create();
        RLock lock1 = redLock.getLock("test");
        Thread t = new Thread(() -> {
            try {
                RLock lock2 = redLock.getLock("test");
                System.out.println("LOCK2,LOCKING:" + new Date());
                lock2.lock();
                System.out.println("LOCK2,LOCKED:" + new Date());
                Thread.sleep(10000);
                System.out.println("LOCK2,UNLOCKING:" + new Date());
                lock2.unlock();
                System.out.println("LOCK2,UNLOCKED:" + new Date());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        t.start();
        System.out.println("LOCK1,LOCKING:" + new Date());
        lock1.lock();
        System.out.println("LOCK1,LOCKED:" + new Date());
        Thread.sleep(10000);
        System.out.println("LOCK1,UNLOCKING:" + new Date());
        lock1.unlock();
        System.out.println("LOCK1,UNLOCKED:" + new Date());
        redLock.shutdown();
    }
}
