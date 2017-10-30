package redlock.pubsub;

import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.Semaphore;

public class PubsubEntry {
    Semaphore latch;

    JedisPubSub pubSub;

    PubsubEntry(){
        this.latch = new Semaphore(0);
    }

    public void countDown(){
        latch.release();
    }

    public Semaphore getLatch() {
        return latch;
    }

    public JedisPubSub getPubSub() {
        return pubSub;
    }

    public void setPubSub(JedisPubSub pubSub) {
        this.pubSub = pubSub;
    }
}
