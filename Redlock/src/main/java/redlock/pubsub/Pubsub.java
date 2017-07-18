package redlock.pubsub;

import redis.clients.jedis.JedisPubSub;
import redlock.connection.RedisClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pubsub {

    ExecutorService es;

    Map<String, JedisPubSub> map;

    public Pubsub() {
        this.es = Executors.newCachedThreadPool();
        this.map = new ConcurrentHashMap<>();
    }

    public CountDownLatch subscribe(String channel, RedisClient client) {
        CountDownLatch latch = new CountDownLatch(1);
        es.submit(() -> {
            PubsubListener listener = new PubsubListener(pubsubCommand -> {
                if (channel.equals(pubsubCommand.getChannel()) && "OK".equals(pubsubCommand.getMessage())) {
                    latch.countDown();
                }
            });
            JedisPubSub ret = map.putIfAbsent(channel, listener);
            if (ret == null) {
                client.subscribe(listener, channel);
            }
        });
        return latch;
    }

    public void unsubscribe(String channel, RedisClient client) {
        client.publish(channel, "OK");
        JedisPubSub pubSub = map.get(channel);
        if (pubSub != null) {
            pubSub.unsubscribe();
            map.remove(channel);
        }
    }

    public void shutdown() {
        es.shutdownNow();
    }
}
