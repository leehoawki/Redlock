package redlock;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;

public class RedisClient {

    JedisPool pool;

    public RedisClient(JedisPool pool) {
        this.pool = pool;
    }

    public String set(String key, String value, String nxxx, String expx, long time) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.set(key, value, nxxx, expx, time);
        }
    }

    public String set(String key, String value, String nxxx) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.set(key, value, nxxx);
        }
    }

    public Object eval(String script, String key, String arg) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.eval(script, Arrays.asList(key), Arrays.asList(arg));
        }
    }
}
