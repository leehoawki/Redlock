package redlock;


import redis.clients.jedis.JedisPool;
import redlock.pubsub.Pubsub;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RedLock {

    static final String prefix = "REDLOCK.";

    static final String channel = "REDLOCK.CHANNEL.";

    static final String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";

    RedisClient client;

    Pubsub pubsub;

    public RedLock(String host, int port) {
        JedisPool pool = new JedisPool(host, port);
        this.pubsub = new Pubsub(pool);
        this.client = new RedisClient(pool);
    }

    public Lock tryLock(Object lock) {
        return null;
    }

    public Lock lock(Object lock) throws InterruptedException {
        return lock(lock, 0);
    }

    public Lock lock(Object lock, long ttl) throws InterruptedException {
        String key = prefix + String.valueOf(lock.hashCode());
        String value = UUID.randomUUID().toString();
        String ret;
        while (true) {
            if (ttl > 0) {
                ret = client.set(key, value, "NX", "EX", ttl);
            } else {
                ret = client.set(key, value, "NX");
            }
            if ("OK".equals(ret)) {
                return new Lock(key, value);
            } else {
                CountDownLatch latch = pubsub.subscribe(channel + lock.hashCode());
                latch.await(100, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void unlock(Lock lock) {
        client.eval(script, lock.getKey(), lock.getValue());
        pubsub.unsubscribe(channel + lock.hashCode());
    }

    public static RedLock create() {
        return new RedLock("127.0.0.1", 6379);
    }
}
