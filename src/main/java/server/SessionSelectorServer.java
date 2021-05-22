package server;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static util.Logs.log;

public class SessionSelectorServer implements Runnable {
    private final int timeout;
    private final InetSocketAddress address;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer writeBuffer;
    private boolean isMessageReceived = false;
    private static final char GS = 0x1D;
    private static final char RS = 0x1E;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private List<SocketChannel> clients = new ArrayList<>();


    public SessionSelectorServer(String host, int port, int timeout) {
        this.address = new InetSocketAddress(host, port);
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            serverChannel = ServerSocketChannel.open();
            selector = Selector.open();
            serverChannel.bind(address);
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> itr = selectedKeys.iterator();
                while (itr.hasNext()) {
                    var key = (SelectionKey) itr.next();

                    if (key.isValid() && key.isAcceptable()) {
                        var clientSocket = serverChannel.accept();
                        if (clientSocket != null) {
                            clientSocket.configureBlocking(false);
                            clientSocket.register(selector, SelectionKey.OP_READ);
                            clients.add(clientSocket);
                            log("accepted " + clientSocket);
                        }
                    }
                    if (key.isValid() && key.isReadable()) {
                        //log("readable " + key);
                        read(key);
                    }
                    if (key.isValid() && key.isWritable()) {
                        //log("writable " + key);
                        write(key);
                    }
                    itr.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void read(SelectionKey key) {
        try {
            tryRead(key);
        } catch (Exception e) {
            e.printStackTrace();
            closeConnection(key);
        }
    }

    private void tryRead(SelectionKey key) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        var channel = (SocketChannel) key.channel();
        int readCount;
        do {
            readBuffer.clear();
            readCount = channel.read(readBuffer);
            readBuffer.flip();
            while (readBuffer.hasRemaining() && !isMessageReceived) {
                var b = readBuffer.get();
                if (b == RS) {
                    isMessageReceived = true;
                } else {
                    baos.write(b);
                }
            }
        }
        while (readCount > 0 && !isMessageReceived);

        if (isMessageReceived) {
            var message = new String(baos.toByteArray());
            System.out.println(message + " message");
            var parsedMSG = Parser.parse(message);
            //System.out.println(parsedMSG + " parsed");
            writeBuffer = ByteBuffer.wrap((parsedMSG).getBytes()); //+RS
            //System.out.println(new String(writeBuffer.array()) + " kek?");
            key.interestOps(SelectionKey.OP_WRITE);
            isMessageReceived = false;
        }
    }

    public void write(SelectionKey key) {
        try {
            tryWrite(key);
        } catch (Exception e) {
            e.printStackTrace();
            closeConnection(key);
        }
    }

    private void tryWrite(SelectionKey key) throws Exception {
        try {
            //writeBuffer.clear();
            for (SocketChannel channel : clients) {
                int writeCount;
                do {
                    writeCount = channel.write(writeBuffer);
                }
                while (writeCount > 0);
                //channel.write(writeBuffer);
                key.interestOps(SelectionKey.OP_READ);
            }
            //writeBuffer.flip();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeConnection(SelectionKey key) {
        key.cancel();
        if (key.channel() != null) {
            try {
                key.channel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Parser {
        private static final char GS = 0x1D;
        private static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        public static String parse(String pack) {
            var date = new Date();
            var buff = pack.split(String.valueOf(GS)); //Arrays.toString(<>)
            var type = buff[0];
            if (type.equals("T_REGISTER"))
                return "(" + formatter.format(date) + ")" + " New connected user " + buff[1];
            if (type.equals("T_MESSAGE"))
                return "(" + formatter.format(date) + ") " + buff[1] + " says: " + buff[2];
            return "Damn an L.";
        }
    }
}