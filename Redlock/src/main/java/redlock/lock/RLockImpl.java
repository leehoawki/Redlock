package redlock.lock;

import redlock.connection.RedisClient;
import redlock.pubsub.Pubsub;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RLockImpl implements RLock {

    static final String K_PREFIX = "REDLOCK.";

    static final String C_PREFIX = "REDLOCK.CHANNEL.";

    static final String UNLOCK_SCRIPT = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";

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

    public String getKey() {
        return key;
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
        String ret;
        String value = getValue();
        if (leaseTime > 0) {
            ret = client.set(key, value, "NX", "EX", leaseTime);
        } else {
            ret = client.set(key, value, "NX");
        }
        if ("OK".equals(ret)) {
            return true;
        }
        return false;
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
        String value = getValue();
        client.eval(UNLOCK_SCRIPT, key, value);
        PUBSUB.unsubscribe(channel, client);
    }

    @Override
    public boolean isLocked() {
        String ret = client.get(key);
        return ret != null;
    }

    boolean tryAcuqire(long leaseTime) {
        String ret;
        String value = getValue();
        if (leaseTime > 0) {
            ret = client.set(key, value, "NX", "EX", leaseTime);
        } else {
            ret = client.set(key, value, "NX");
        }
        if ("OK".equals(ret)) {
            return true;
        }
        return false;
    }
}
