package redlock.connection;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisSingle implements RedisClient {
    JedisPool pool;

    ExecutorService es;

    public RedisSingle(String host, int port, String password) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1000);
        pool = new JedisPool(config, host, port, 2000, password);
        this.es = Executors.newCachedThreadPool();
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
    public boolean exists(String key) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(key).booleanValue();
        }
    }

    @Override
    public String hGet(String key, String field) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.hget(key, field);
        }
    }

    @Override
    public String eval(String script, List<String> keys, String... params) {
        try (Jedis jedis = pool.getResource()) {
            Object ret = jedis.eval(script, keys, Arrays.asList(params));
            if (ret == null) return null;
            return ret.toString();
        }
    }

    @Override
    public void subscribe(String channel, JedisPubSub listener) {
        es.execute(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(listener, channel);
            }
        });
    }

    @Override
    public void publish(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    @Override
    public void close() {
        this.es.shutdown();
        this.pool.close();
    }
}
