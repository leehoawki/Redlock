package redlock.lock;

import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;

public class RLockEntry {
    CountDownLatch latch;

    JedisPubSub pubSub;

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public JedisPubSub getPubSub() {
        return pubSub;
    }

    public void setPubSub(JedisPubSub pubSub) {
        this.pubSub = pubSub;
    }
}
