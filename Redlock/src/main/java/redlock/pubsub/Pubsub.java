package redlock.pubsub;

import redis.clients.jedis.JedisPubSub;
import redlock.connection.RedisClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Pubsub {
    public static final String UNLOCK_MESSAGE = "UNLOCK";

    Map<String, CountDownLatch> latchs;

    Map<String, JedisPubSub> listeners;

    public Pubsub() {
        this.latchs = new ConcurrentHashMap<>();
        this.listeners = new ConcurrentHashMap<>();
    }

    static String getKey(String channel) {
        return channel + ":" + Thread.currentThread().getId();
    }

    public CountDownLatch subscribe(String channel, RedisClient client) {
        String key = getKey(channel);
        CountDownLatch value = latchs.get(key);
        if (value != null) {
            return value;
        }
        CountDownLatch latch = new CountDownLatch(1);
        latchs.put(key, latch);
        PubsubListener listener = new PubsubListener(pubsubCommand -> {
            if (channel.equals(pubsubCommand.getChannel()) && "OK".equals(pubsubCommand.getMessage())) {
                latch.countDown();
            }
        });
        client.subscribe(channel, listener);
        listeners.put(key, listener);
        return latch;
    }

    public void unsubscribe(String channel, RedisClient client) {
        String key = getKey(channel);
        client.publish(channel, "OK");
        JedisPubSub pubSub = listeners.remove(key);
        if (pubSub != null) {
            pubSub.unsubscribe();
        }
    }
}
