package redlock.connection;


import redlock.pubsub.PubsubListener;

public interface RedisClient {

    String set(String key, String value, String nxxx, String expx, long time);

    String set(String key, String value, String nxxx);

    String get(String key);

    Object eval(String script, String key, String arg);

    void subscribe(PubsubListener listener, String channel);

    void publish(String channel, String ok);
}
