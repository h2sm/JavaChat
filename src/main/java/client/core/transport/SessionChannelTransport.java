package client.core.transport;

import client.core.Transport;
import client.core.exception.TransportException;
import client.core.settings.Settings;
import util.Logs.*;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

import static util.Logs.log;

public class SessionChannelTransport implements Transport {
    private final String host;
    private final int port, timeout;
    private static final char GS = 0x1D;
    private static final char RS = 0x1E;
    private String clientName;
    private final Scanner scanner = new Scanner(System.in);
    private MessagingProtocol messagingProtocol;

    public SessionChannelTransport(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    @Override
    public void connect() {
        log("Type your name");
        clientName = scanner.next();
        messagingProtocol = new MessagingProtocol(clientName);
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
            var buffer = ByteBuffer.wrap((messagingProtocol.messageSelector(message)).getBytes());
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
