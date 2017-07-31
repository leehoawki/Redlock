package redlock;


import redlock.connection.RedisClient;
import redlock.connection.RedisSingle;
import redlock.lock.RLock;
import redlock.lock.RLockImpl;

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

    public void shutdown() {
        this.client.close();
    }
}
