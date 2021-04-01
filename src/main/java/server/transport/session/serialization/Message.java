package server.transport.session.serialization;

import server.settings.Settings;

import java.io.Serializable;
import java.net.SocketAddress;
import java.time.LocalDateTime;

public class Message implements Serializable {
    public static final long serialVersionUID = 42L;

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final SocketAddress target = Settings.ADDRESS;
    private final String message;

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public SocketAddress getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }

    public Message(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "timestamp=" + timestamp +
                ", target=" + target +
                ", message='" + message + '\'' +
                '}';
    }
}
