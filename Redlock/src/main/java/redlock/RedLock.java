package redlock;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.UUID;

public class RedLock {

    static final String prefix = "REDLOCK.";

    static final String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";

    JedisPool pool;

    public RedLock(String host, int port) {
        this.pool = new JedisPool(host, port);
    }

    public Lock lock(Object lock) {
        return lock(lock, 0);
    }

    public Lock lock(Object lock, long ttl) {
        String key = prefix + String.valueOf(lock.hashCode());
        String value = UUID.randomUUID().toString();

        try (Jedis jedis = pool.getResource()) {
            String ret;
            if (ttl > 0) {
                ret = jedis.set(key, value, "NX", "EX", ttl);
            } else {
                ret = jedis.set(key, value, "NX");
            }
            if ("OK".equals(ret))
                return new Lock(key, value);
            else return null;
        }
    }

    public void unlock(Lock lock) {
        try (Jedis jedis = pool.getResource()) {
            jedis.eval(script, Arrays.asList(lock.getKey()), Arrays.asList(lock.getValue()));
        }
    }

    public static RedLock create() {
        return new RedLock("127.0.0.1", 6379);
    }
}
