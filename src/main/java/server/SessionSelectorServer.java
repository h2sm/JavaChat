package server;

import server.settings.Settings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static util.Logs.log;

public class SessionSelectorServer implements Runnable {
    private final String host;
    private final int port;
    private final int timeout;
    public SessionSelectorServer(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout=timeout;
    }

    @Override
    public void run() {
        try (var serverChannel = ServerSocketChannel.open(); var selector = Selector.open()) {
            serverChannel.bind(new InetSocketAddress(host, port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                var iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    var key = iterator.next();

                    if (key.isValid() && key.isAcceptable()) {
                        //log("acceptable " + key);
                        performAccept(key);
                    }
                    if (key.isValid() && key.isReadable()) {
                        //log("readable " + key);
                        ((EchoProtocol) key.attachment()).read();
                    }
                    if (key.isValid() && key.isWritable()) {
                        //log("writable " + key);
                        ((EchoProtocol) key.attachment()).write();
                    }
                    iterator.remove();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performAccept(SelectionKey serverKey) {
        try {
            SocketChannel clientChannel;
            do {
                var serverChannel = (ServerSocketChannel) serverKey.channel();
                clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    clientChannel.configureBlocking(false);
                    //clientChannel.socket().setSoTimeout();
                    var clientKey = clientChannel.register(serverKey.selector(), SelectionKey.OP_READ);
                    clientKey.attach(new EchoProtocol(clientKey));
                    log("accepted " + clientChannel);
                }
            }
            while (clientChannel != null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class EchoProtocol {
        private final SelectionKey key;
        private final ByteBuffer readBuffer = ByteBuffer.allocate(8);
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private boolean isMessageReceived = false;
        private ByteBuffer writeBuffer;
        private static final char GS = 0x1D;
        private static final char RS = 0x1E;

        public EchoProtocol(SelectionKey key) {
            this.key = key;
        }

        public void read() {
            try {
                tryRead();
            }
            catch (Exception e) {
                e.printStackTrace();
                closeConnection();
            }
        }

        private void tryRead() throws Exception {
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
                    }
                    else {
                        baos.write(b);
                    }
                }
            }
            while (readCount > 0 && !isMessageReceived);

            if (isMessageReceived) {
                var message = new String(baos.toByteArray());
                //log("received from " + channel + ": " + message);
                var parsedMSG = parseMessage(message);
                writeBuffer = ByteBuffer.wrap((parsedMSG).getBytes()); //+RS
                key.interestOpsAnd(~SelectionKey.OP_READ);
                key.interestOpsOr(SelectionKey.OP_WRITE);
            }
        }
        private String parseMessage(String msg){
            var buff = msg.split(String.valueOf(GS)); //Arrays.toString(<>)
            if (buff[0].equals("T_REGISTER")) return "New connected user " + buff[1];
            return buff[1] + " says: " + buff[2];
        }

        public void write() {
            try {
                tryWrite();
            }
            catch (Exception e) {
                e.printStackTrace();
                closeConnection();
            }
        }

        private void tryWrite() throws Exception {
            var channel = (SocketChannel) key.channel();
            int writeCount;
            do {
                writeCount = channel.write(writeBuffer);
            }
            while (writeCount > 0);

            if (!writeBuffer.hasRemaining()) {
                closeConnection();
            }
        }

        private void closeConnection() {
            key.cancel();
            if (key.channel() != null) {
                try {
                    key.channel().close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
