package redlock.semaphore;


import redlock.connection.RedisClient;
import redlock.pubsub.Pubsub;
import redlock.pubsub.PubsubEntry;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RSemaphoreImpl implements RSemaphore {

    static final String K_PREFIX = "REDLOCK.SEMAPHORE.";

    static final String C_PREFIX = "REDLOCK.SEMAPHORE.CHANNEL.";

    private String id;

    private String key;

    private String channel;

    private RedisClient client;

    static Pubsub PUBSUB = new Pubsub();

    public RSemaphoreImpl(String id, String name, RedisClient client) {
        this.id = id;
        this.key = K_PREFIX + name;
        this.channel = C_PREFIX + name;
        this.client = client;
    }

    @Override
    public void acquire(int permits) throws InterruptedException {
        if (tryAcquireInner(permits)) {
            return;
        }

        PubsubEntry entry = PUBSUB.subscribe(channel);
        CountDownLatch latch = entry.getLatch();
        client.subscribe(channel, entry.getPubSub());
        try {
            while (true) {
                if (tryAcquireInner(permits)) {
                    return;
                }
                latch.await(100, TimeUnit.MILLISECONDS);
            }
        } finally {
            if (entry.getPubSub().isSubscribed()) {
                PUBSUB.unsubscribe(entry.getPubSub());
            }
        }
    }

    @Override
    public boolean tryAcquire(int permits) {
        return tryAcquireInner(permits);
    }

    @Override
    public void release(int permits) {
        if (permits < 0) {
            return;
        }
        client.eval("local value = redis.call('incrby', KEYS[1], ARGV[1]); " +
                        "redis.call('publish', KEYS[2], value); ",
                Arrays.asList(key, channel), String.valueOf(permits));
    }

    boolean tryAcquireInner(int permits) {
        if (permits < 0) {
            return true;
        }

        String ret = client.eval("local value = redis.call('get', KEYS[1]); " +
                        "if (value ~= false and tonumber(value) >= tonumber(ARGV[1])) then " +
                        "local val = redis.call('decrby', KEYS[1], ARGV[1]); " +
                        "return 1; " +
                        "end; " +
                        "return 0;",
                Arrays.asList(key), String.valueOf(permits));
        return "1".equals(ret);
    }

    @Override
    public String toString() {
        return "RSemaphoreImpl{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
