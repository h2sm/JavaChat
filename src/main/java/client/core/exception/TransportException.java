package client.core.exception;

public class TransportException extends RuntimeException {
    public TransportException(Throwable cause) {
        super(cause);
    }

    public TransportException(String message) {
        super(message);
    }
}
