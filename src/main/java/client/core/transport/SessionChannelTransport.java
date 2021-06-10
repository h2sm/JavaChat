package client.core.transport;

import client.core.Transport;
import client.core.exception.TransportException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static util.Logs.log;

public class SessionChannelTransport implements Transport {
    private final int timeout;
    private String clientName;
    private String password;
    private Scanner scanner = new Scanner(System.in);
    private MessagingProtocol messagingProtocol;
    private SocketChannel client;
    private InetSocketAddress address;

    public SessionChannelTransport(String host, int port, int timeout) {
        this.timeout = timeout;
        this.address = new InetSocketAddress(host, port);
    }

    @Override
    public void connect() {
        log("Type your name and password");
        clientName = scanner.next();
        password = scanner.next();
        messagingProtocol = new MessagingProtocol(clientName);
        try {
            client = SocketChannel.open(address);
            Thread serverInputThread = new Thread(this::receiveServer);
            serverInputThread.start();
            registration();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void registration(){
        byte[] msg = messagingProtocol.registrationSelector(password).getBytes();
        try {
            client.write(ByteBuffer.wrap(msg));
        }
        catch (IOException e){
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
        var sendMSG = messagingProtocol.messageSelector(message);
        var buffer = ByteBuffer.wrap((sendMSG));
        try {
            client.write(buffer);
            //buffer.flip();
        }
        catch (Exception e){
            e.getCause();
        }
        return "sent";
    }

    private void receiveServer() {
        while (client.isConnected()) {
            ByteBuffer buf = ByteBuffer.allocate(15);
            try {
                while (client.read(buf) > 0) {
                    System.out.println(new String(buf.array()));
                    buf.clear();
                }
            } catch (IOException e) {
                //System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @Override
    public void disconnect() {
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
