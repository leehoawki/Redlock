package redlock;


import redis.clients.jedis.JedisPool;
import redlock.pubsub.Pubsub;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RedLock {

    static final String kPrefix = "REDLOCK.";

    static final String cPrefix = "REDLOCK.CHANNEL.";

    static final String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";

    RedisClient client;

    Pubsub pubsub;

    RedLock(String host, int port) {
        JedisPool pool = new JedisPool(host, port);
        this.pubsub = new Pubsub(pool);
        this.client = new RedisClient(pool);
    }

    public Lock tryLock(Object lock) {
        String key = kPrefix + String.valueOf(lock.hashCode());
        String value = UUID.randomUUID().toString();
        String ret = client.set(key, value, "NX");
        if ("OK".equals(ret)) {
            return new Lock(key, value);
        }
        return null;
    }

    public Lock lock(Object lock) throws InterruptedException {
        return lock(lock, 0);
    }

    public Lock lock(Object lock, long ttl) throws InterruptedException {
        String key = kPrefix + String.valueOf(lock.hashCode());
        String value = UUID.randomUUID().toString();
        String ret;
        String channel = cPrefix + lock.hashCode();
        CountDownLatch latch = pubsub.subscribe(channel);
        while (true) {
            if (ttl > 0) {
                ret = client.set(key, value, "NX", "EX", ttl);
            } else {
                ret = client.set(key, value, "NX");
            }
            if ("OK".equals(ret)) {
                pubsub.unsubscribe(channel);
                return new Lock(key, value);
            } else {
                latch.await(100, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void unlock(Lock lock) {
        String channel = cPrefix + lock.hashCode();
        client.eval(script, lock.getKey(), lock.getValue());
        pubsub.unsubscribe(channel);
    }

    public static RedLock create() {
        return new RedLock("127.0.0.1", 6379);
    }

    public static RedLock create(String host, int port) {
        return new RedLock(host, port);
    }

    public void shutdown() {
        this.pubsub.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        RedLock redLock = RedLock.create();
        Thread t = new Thread(() -> {
            try {
                Lock lock = redLock.lock("test");
                System.out.println("UNLOCKING");
                Thread.sleep(10000);
                System.out.println("UNLOCKED");
                redLock.unlock(lock);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        t.start();
        Thread.sleep(1000);
        System.out.println(new Date().getTime());
        Lock lock = redLock.lock("test");
        System.out.println(new Date().getTime());
        redLock.unlock(lock);
        redLock.shutdown();
    }
}
