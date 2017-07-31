package redlock.lock;

import redlock.connection.RedisClient;
import redlock.pubsub.Pubsub;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RLockImpl implements RLock {

    static final String K_PREFIX = "REDLOCK.";

    static final String C_PREFIX = "REDLOCK.CHANNEL.";

    String id;

    String key;

    String channel;

    RedisClient client;

    static Pubsub PUBSUB = new Pubsub();

    public RLockImpl(String id, String name, RedisClient client) {
        this.id = id;
        this.key = K_PREFIX + name;
        this.channel = C_PREFIX + name;
        this.client = client;
    }

    public String getValue() {
        return id + ":" + Thread.currentThread().getId();
    }

    @Override
    public String toString() {
        return "RLockImpl{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }

    @Override
    public boolean tryLock(long leaseTime) {
        String ttl = tryAcuqire(leaseTime);
        return ttl == null;
    }

    @Override
    public void lock(long leaseTime) throws InterruptedException {
        if (tryLock(leaseTime)) {
            return;
        }

        CountDownLatch latch = PUBSUB.subscribe(channel, client);
        String ret;
        String value = getValue();
        while (latch.getCount() > 0) {
            if (leaseTime > 0) {
                ret = client.set(key, value, "NX", "EX", leaseTime);
            } else {
                ret = client.set(key, value, "NX");
            }
            if ("OK".equals(ret)) {
                PUBSUB.unsubscribe(channel, client);
                return;
            } else {
                latch.await(100, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void unlock() {
        Object ret = client.eval("if (redis.call('exists', KEYS[1]) == 0) then " +
                        "redis.call('publish', KEYS[2], ARGV[1]); " +
                        "return 1; " +
                        "end;" +
                        "if (redis.call('hexists', KEYS[1], ARGV[2]) == 0) then " +
                        "return nil;" +
                        "end; " +
                        "local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1); " +
                        "if (counter > 0) then " +
                        "return 0; " +
                        "else " +
                        "redis.call('del', KEYS[1]); " +
                        "redis.call('publish', KEYS[2], ARGV[1]); " +
                        "return 1; " +
                        "end; ",
                Arrays.asList(key, channel), Pubsub.UNLOCK_MESSAGE, getValue());
        if (ret == null) {
            throw new IllegalMonitorStateException("Not locked by current thread, node id: " + id + " thread-id: " + Thread.currentThread().getId());
        }
    }

    @Override
    public void forceUnlock() {
        Object ret = client.eval("if (redis.call('exists', KEYS[1]) == 0) then " +
                        "redis.call('publish', KEYS[2], ARGV[1]); " +
                        "return 1; " +
                        "end;" +
                        "if (redis.call('hexists', KEYS[1], ARGV[2]) == 0) then " +
                        "return nil;" +
                        "end; " +
                        "redis.call('del', KEYS[1]); " +
                        "redis.call('publish', KEYS[2], ARGV[1]); " +
                        "return 1; " +
                        "end; ",
                Arrays.asList(key, channel), Pubsub.UNLOCK_MESSAGE, getValue());
        if (ret == null) {
            throw new IllegalMonitorStateException("Not locked by current thread, node id: " + id + " thread-id: " + Thread.currentThread().getId());
        }
    }

    @Override
    public int getHoldCount() {
        String ret = client.hGet(key, getValue());
        if (ret == null) {
            return 0;
        }
        return Integer.parseInt(ret);
    }

    @Override
    public boolean isLocked() {
        return client.get(key) != null;
    }

    String tryAcuqire(long leaseTime) {
        Object ret;
        if (leaseTime > 0) {
            ret = client.eval("if (redis.call('exists', KEYS[1]) == 0) then " +
                    "redis.call('hset', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                    "end; " +
                    "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
                    "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
                    "redis.call('pexpire', KEYS[1], ARGV[1]); " +
                    "return nil; " +
                    "end; " +
                    "return redis.call('pttl', KEYS[1]);", Arrays.asList(key), String.valueOf(leaseTime), getValue());
        } else {
            ret = client.eval("if (redis.call('exists', KEYS[1]) == 0) then " +
                    "redis.call('hset', KEYS[1], ARGV[1], 1); " +
                    "return nil; " +
                    "end; " +
                    "if (redis.call('hexists', KEYS[1], ARGV[1]) == 1) then " +
                    "redis.call('hincrby', KEYS[1], ARGV[1], 1); " +
                    "return nil; " +
                    "end; " +
                    "return redis.call('pttl', KEYS[1]);", Arrays.asList(key), getValue());
        }
        if (ret == null) {
            return null;
        }
        return ret.toString();
    }
}
