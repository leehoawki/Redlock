package redlock.pubsub;

import redis.clients.jedis.JedisPubSub;

public class Pubsub {
    public static final String UNLOCK_MESSAGE = "UNLOCK";

    public PubsubEntry subscribe(String channel) {
        PubsubEntry entry = new PubsubEntry();
        entry.setPubSub(new PubsubListener(pubsubCommand -> {
            if (channel.equals(pubsubCommand.getChannel()) && UNLOCK_MESSAGE.equals(pubsubCommand.getMessage())) {
                entry.countDown();
            }
        }));

        return entry;
    }

    public void unsubscribe(JedisPubSub pubSub) {
        pubSub.unsubscribe();
    }
}
