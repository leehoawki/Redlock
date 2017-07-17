package redlock.pubsub;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class Pubsub {

    JedisPool pool;

    public Pubsub(JedisPool pool) {
        this.pool = pool;
    }

    public CountDownLatch subscribe(String channel) {
        CountDownLatch latch = new CountDownLatch(1);
        try (Jedis jedis = pool.getResource()) {
            jedis.subscribe(new PubsubListener(new Consumer<PubsubCommand>() {
                @Override
                public void accept(PubsubCommand pubsubCommand) {
                    if (channel.equals(pubsubCommand.getChannel()) && "RELEASE".equals(pubsubCommand.getMessage())) {
                        latch.countDown();
                    }
                }
            }), channel);
        }
        return latch;
    }

    public void unsubscribe(String channel) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, "RELEASE");
        }
    }
}
