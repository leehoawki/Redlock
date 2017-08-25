package redlock.latch;


import redlock.connection.RedisClient;
import redlock.pubsub.Pubsub;

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
    }

    @Override
    public void countDown() {

    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public void await() throws InterruptedException {

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
