package redlock.connection;


import redis.clients.jedis.JedisPubSub;

public interface RedisClient {

    String set(String key, String value, String nxxx, String expx, long time);

    String set(String key, String value, String nxxx);

    String get(String key);

    Object eval(String script, String key, String arg);

    void subscribe(String channel, JedisPubSub listener);

    void publish(String channel, String ok);

    void close();
}
