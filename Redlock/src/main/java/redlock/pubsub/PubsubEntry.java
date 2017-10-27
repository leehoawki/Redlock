package redlock.pubsub;

import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;

public class PubsubEntry {
    CountDownLatch latch;

    JedisPubSub pubSub;

    PubsubEntry(){
        this.latch = new CountDownLatch(1);
    }

    public void countDown(){
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        if (latch.getCount() == 0) {
            latch = new CountDownLatch(1);
            return latch;
        }
        return latch;
    }

    public JedisPubSub getPubSub() {
        return pubSub;
    }

    public void setPubSub(JedisPubSub pubSub) {
        this.pubSub = pubSub;
    }
}
