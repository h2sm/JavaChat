package client.core.transport;

import client.core.Transport;
import client.core.exception.TransportException;
import client.core.settings.Settings;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SessionChannelTransport implements Transport {
    private final String host;
    private final int port, timeout;

    public SessionChannelTransport(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    @Override
    public void connect() {

    }

    @Override
    public String converse(String message) {
        try {
            return tryConverse(message);
        } catch (Exception e) {
            throw new TransportException(e);
        }
    }


    private String tryConverse(String message) throws Exception {
        try (var channel = SocketChannel.open(new InetSocketAddress(host, port))) {
            var buffer = ByteBuffer.wrap((message + "\n").getBytes());
            channel.write(buffer);

            buffer = ByteBuffer.allocate(8);
            var done = false;
            var baos = new ByteArrayOutputStream();
            while (!done) {
                buffer.clear();
                done = channel.read(buffer) < 0;
                buffer.flip();

                while (buffer.hasRemaining()) {
                    baos.write(buffer.get());
                }
            }
            return new String(baos.toByteArray());
        }
    }

    @Override
    public void disconnect() {

    }
}
