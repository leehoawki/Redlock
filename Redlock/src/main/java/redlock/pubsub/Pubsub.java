package redlock.pubsub;

import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;

public class Pubsub {
    public static final String UNLOCK_MESSAGE = "UNLOCK";

    public PubsubEntry subscribe(String channel) {
        CountDownLatch latch = new CountDownLatch(1);
        PubsubListener listener = new PubsubListener(pubsubCommand -> {
            if (channel.equals(pubsubCommand.getChannel()) && UNLOCK_MESSAGE.equals(pubsubCommand.getMessage())) {
                latch.countDown();
            }
        });
        PubsubEntry entry = new PubsubEntry();
        entry.setLatch(latch);
        entry.setPubSub(listener);
        return entry;
    }

    public void unsubscribe(JedisPubSub pubSub) {
        pubSub.unsubscribe();
    }
}
