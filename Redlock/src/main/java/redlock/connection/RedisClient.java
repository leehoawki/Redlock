package redlock.connection;


import redis.clients.jedis.JedisPubSub;

import java.util.List;

public interface RedisClient {

    String set(String key, String value, String nxxx, String expx, long time);

    String set(String key, String value, String nxxx);

    String get(String key);

    String hGet(String key, String field);

    String eval(String script, List<String> keys, String... params);

    void subscribe(String channel, JedisPubSub listener);

    void publish(String channel, String ok);

    void close();
}
