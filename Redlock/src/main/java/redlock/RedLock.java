package redlock;


import redlock.connection.RedisClient;
import redlock.connection.RedisSingle;
import redlock.latch.RLatchImpl;
import redlock.lock.RLock;
import redlock.lock.RLockImpl;
import redlock.semaphore.RSemaphore;
import redlock.semaphore.RSemaphoreImpl;

import java.util.UUID;

public class RedLock {

    RedisClient client;

    String id = UUID.randomUUID().toString();

    RedLock(String host, int port, String password) {
        this.client = new RedisSingle(host, port, password);
    }

    public static RedLock create() {
        return create("127.0.0.1", 6379, null);
    }

    public static RedLock create(String host, int port, String password) {
        RedLock redLock = new RedLock(host, port, password);
        return redLock;
    }

    public RLock getLock(String name) {
        RLockImpl rLock = new RLockImpl(id, name, client);
        return rLock;
    }

    public RSemaphore getSemaphore(String name) {
        RSemaphoreImpl rSemaphore = new RSemaphoreImpl(id, name, client);
        return rSemaphore;
    }

    public RLatchImpl getLatch(String name, int count) {
        RLatchImpl rLatch = new RLatchImpl(id, name, count, client);
        return rLatch;
    }

    public RLatchImpl getLatch(String name) {
        RLatchImpl rLatch = new RLatchImpl(id, name, 0, client);
        return rLatch;
    }

    public void shutdown() {
        this.client.close();
    }
}
