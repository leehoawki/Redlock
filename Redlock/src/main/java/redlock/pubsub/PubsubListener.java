package redlock.pubsub;

import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

public class PubsubListener extends JedisPubSub {

    private Consumer<PubsubCommand> callback;

    public PubsubListener(Consumer<PubsubCommand> callback) {
        this.callback = callback;
    }

    @Override
    public void onMessage(String channel, String message) {
        callback.accept(new PubsubCommand(channel, message));
    }
}