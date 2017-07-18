package redlock.connection;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redlock.pubsub.PubsubListener;

import java.util.Arrays;

public class RedisSingle implements RedisClient {

    JedisPool pool;

    public RedisSingle(String host, int port) {
        pool = new JedisPool(host, port);
    }

    public RedisSingle(String host, int port, String password) {
        pool = new JedisPool(new JedisPoolConfig(), host, port, 2000, password);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.set(key, value, nxxx, expx, time);
        }
    }

    @Override
    public String set(String key, String value, String nxxx) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.set(key, value, nxxx);
        }
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public Object eval(String script, String key, String arg) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.eval(script, Arrays.asList(key), Arrays.asList(arg));
        }
    }

    @Override
    public void subscribe(PubsubListener listener, String channel) {
        try (Jedis jedis = pool.getResource()) {
            jedis.subscribe(listener, channel);
        }
    }

    @Override
    public void publish(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        }
    }
}
