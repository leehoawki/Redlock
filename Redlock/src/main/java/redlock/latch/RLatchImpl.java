package redlock.latch;


import redlock.connection.RedisClient;
import redlock.pubsub.Pubsub;
import redlock.pubsub.PubsubEntry;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RLatchImpl implements RLatch {

    static final String K_PREFIX = "REDLOCK.LATCH.";

    static final String C_PREFIX = "REDLOCK.LATCH.CHANNEL.";

    private String id;

    private String key;

    private String channel;

    private RedisClient client;

    private int count;

    static Pubsub PUBSUB = new Pubsub();

    public RLatchImpl(String id, String name, int count, RedisClient client) {
        this.id = id;
        this.key = K_PREFIX + name;
        this.channel = C_PREFIX + name;
        this.client = client;
        this.count = count;
        this.init();
    }

    @Override
    public void countDown() {
        client.eval("local value = redis.call('get', KEYS[1]); " +
                "if (value ~= false and tonumber(value) >= 1) then " +
                "local val = redis.call('decrby', KEYS[1], 1); " +
                "end; ", Arrays.asList(key));
    }

    @Override
    public long getCount() {
        String ret = client.get(key);
        if (ret == null) {
            return 0;
        }
        return Integer.parseInt(ret);
    }

    @Override
    public void await() throws InterruptedException {
        if (tryAcuqire()) {
            return;
        }

        PubsubEntry entry = PUBSUB.subscribe(channel);
        CountDownLatch latch = entry.getLatch();
        client.subscribe(channel, entry.getPubSub());
        try {
            while (true) {
                if (tryAcuqire()) {
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

    private void init() {
        client.eval("local value = redis.call('get', KEYS[1]); " +
                        "if (value ~= false and tonumber(value) >= 0) then " +
                        "redis.call('incrby', KEYS[1], ARGV[1]);" +
                        "return 1; " +
                        "end; " +
                        "redis.call('set', KEYS[1], ARGV[1]);" +
                        "return 0;",
                Arrays.asList(key), String.valueOf(count));
    }

    boolean tryAcuqire() {
        String ret = client.eval("local value = redis.call('get', KEYS[1]); " +
                        "if (value ~= false and tonumber(value) <= 0) then " +
                        "return 1; " +
                        "end; " +
                        "return 0;",
                Arrays.asList(key));
        return "1".equals(ret);
    }

    @Override
    public String toString() {
        return "RLatchImpl{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", channel='" + channel + '\'' +
                '}';
    }
}
