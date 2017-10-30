package redlock.pubsub;

public class PubsubCommand {
    String channel;

    String message;

    public PubsubCommand(String channel, String message) {
        this.channel = channel;
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }
}
