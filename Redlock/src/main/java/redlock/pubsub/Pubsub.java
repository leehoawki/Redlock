package redlock.pubsub;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pubsub {

    JedisPool pool;

    ExecutorService es = Executors.newCachedThreadPool();

    Map<String, JedisPubSub> map;

    public Pubsub(JedisPool pool) {
        this.pool = pool;
        this.map = new ConcurrentHashMap<>();
    }

    public CountDownLatch subscribe(String channel) {
        CountDownLatch latch = new CountDownLatch(1);
        es.submit(() -> {
            try (Jedis jedis = pool.getResource()) {
                PubsubListener listener = new PubsubListener(pubsubCommand -> {
                    if (channel.equals(pubsubCommand.getChannel()) && "OK".equals(pubsubCommand.getMessage())) {
                        latch.countDown();
                    }
                });
                map.put(channel, listener);
                jedis.subscribe(listener, channel);
            }
        });
        return latch;
    }

    public void unsubscribe(String channel) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, "OK");
            JedisPubSub pubSub = map.get(channel);
            if (pubSub != null) {
                pubSub.unsubscribe();
                map.remove(channel);
            }
        }
    }

    public void shutdown() {
        this.es.shutdownNow();
    }
}
