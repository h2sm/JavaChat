package client.core.transport;

import client.core.Transport;
import client.core.exception.TransportException;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Set;

import static util.Logs.log;

public class SessionChannelTransport implements Transport {
    private final String host;
    private final int port, timeout;
    private static final char GS = 0x1D;
    private static final char RS = 0x1E;
    private ByteBuffer rBuffer = ByteBuffer.allocate(100);
    private ByteBuffer wBuffer = ByteBuffer.allocate(100);
    private String clientName;
    private final Scanner scanner = new Scanner(System.in);
    private MessagingProtocol messagingProtocol;
    private SocketChannel client;
    private Selector selector;

    public SessionChannelTransport(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    @Override
    public void connect() {
        try {
            client = SocketChannel.open(new InetSocketAddress(host, port));
//            client.configureBlocking(false);
//            selector = Selector.open();
//            client.register(selector, SelectionKey.OP_CONNECT);
//            while (true){
//                selector.select();
//                Set<SelectionKey> selectionKeys = selector.selectedKeys();
//                for (SelectionKey key:selectionKeys){
//                    if(key.isConnectable()){
//                        SocketChannel client = (SocketChannel) key.channel();
//                        if (client.isConnectionPending()){
//                            client.finishConnect();
//                        }
//                        client.register(selector,SelectionKey.OP_READ);
//                    }
//                    // событие READ - сервер пересылает сообщение
//                    else if (key.isReadable()) {
//                        SocketChannel client = (SocketChannel) key.channel();
//                        // Считываем данные из канала
//                        //String msg = receive(client);
//                        //if (msg.isEmpty()){
//                            // Сервер неисправен, выходим из клиента
//                           // close(selector);
//                        }else {
//                            System.out.println("msg");
//                        }
//                    }
//                }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        var buffer = ByteBuffer.wrap((messagingProtocol.messageSelector(message)).getBytes());
        client.write(buffer);
        buffer = ByteBuffer.allocate(100);
        var done = false;
        var baos = new ByteArrayOutputStream();
        //while (!done) {
            buffer.clear();
            client.read(buffer);
            //done = client.read(buffer) < 0;
            buffer.flip();
            while (buffer.hasRemaining()) {
                baos.write(buffer.get());
            }
        //}
        return new String(baos.toByteArray());
    }
    private void read(){

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
