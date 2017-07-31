package redlock.pubsub;

import redis.clients.jedis.JedisPubSub;
import redlock.lock.RLockEntry;

import java.util.concurrent.CountDownLatch;

public class Pubsub {
    public static final String UNLOCK_MESSAGE = "UNLOCK";

    public RLockEntry subscribe(String channel) {
        RLockEntry entry = new RLockEntry();
        CountDownLatch latch = new CountDownLatch(1);
        PubsubListener listener = new PubsubListener(pubsubCommand -> {
            if (channel.equals(pubsubCommand.getChannel()) && UNLOCK_MESSAGE.equals(pubsubCommand.getMessage())) {
                latch.countDown();
            }
        });
        entry.setLatch(latch);
        entry.setPubSub(listener);
        return entry;
    }

    public void unsubscribe(JedisPubSub pubSub) {
        pubSub.unsubscribe();
    }
}
