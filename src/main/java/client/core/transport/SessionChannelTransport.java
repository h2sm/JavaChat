package client.core.transport;

import client.core.Transport;
import client.core.exception.TransportException;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;

import static util.Logs.log;

public class SessionChannelTransport implements Transport {
    private final String host;
    private final int port, timeout;
    private static final char GS = 0x1D;
    private static final char RS = 0x1E;
    private String clientName;
    private final Scanner scanner = new Scanner(System.in);
    private MessagingProtocol messagingProtocol;
    private SocketChannel client;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

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
        try {
            client = SocketChannel.open(new InetSocketAddress(host, port));
            startReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        var buffer = ByteBuffer.wrap((messagingProtocol.messageSelector(message)).getBytes());
        client.write(buffer);
//        buffer = ByteBuffer.allocate(100);
//        var baos = new ByteArrayOutputStream();
//        buffer.clear();
//        client.read(buffer);
//        buffer.flip();
//        while (buffer.hasRemaining()) {
//            baos.write(buffer.get());
//        }
        return "sent";
        //return new String(baos.toByteArray());
    }

    private void startReceiver(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    ByteBuffer buffer = ByteBuffer.allocate(100);
                    var baos = new ByteArrayOutputStream();
                    try {
                        client.read(buffer);
                        while (buffer.hasRemaining()) {
                            baos.write(buffer.get());
                        }
                        log(baos.toString(StandardCharsets.UTF_8));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });
        thread.start();

    }


    @Override
    public void disconnect() {
        try {
            client.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
