package redlock.pubsub;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pubsub {

    JedisPool pool;

    ExecutorService es = Executors.newCachedThreadPool();

    public Pubsub(JedisPool pool) {
        this.pool = pool;
    }

    public CountDownLatch subscribe(String channel) {
        CountDownLatch latch = new CountDownLatch(1);
        es.execute(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(new PubsubListener(pubsubCommand -> {
                    if (channel.equals(pubsubCommand.getChannel()) && "OK".equals(pubsubCommand.getMessage())) {
                        latch.countDown();
                    }
                }), channel);
            }
        });
        return latch;
    }

    public void unsubscribe(String channel) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, "OK");
        }
    }

    public void shutdown() {
        this.es.shutdownNow();
    }
}
