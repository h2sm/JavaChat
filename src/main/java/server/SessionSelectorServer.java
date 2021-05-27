package server;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
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
    private static List<SelectionKey> keysList = new ArrayList<>();
    private static List<ByteBuffer> byteBufferList = new ArrayList<>();
    private static int clientCounter = 0;

    public SessionSelectorServer(String host, int port, int timeout) {
        this.address = new InetSocketAddress(host, port);
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            var serverChannel = ServerSocketChannel.open();
            var selector = Selector.open();
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
                        log("acceptable " + key);
                        accept(key);
                    }
                    if (key.isValid() && key.isReadable()) {
                        log("readable " + key);
                        ((SelectorProtocol) key.attachment()).read();
                    }
                    if (key.isValid() && key.isWritable()) {
                        log("writable " + key);
                        ((SelectorProtocol) key.attachment()).write();
                    }
                    itr.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void accept(SelectionKey key) {
        try {
            var serverChannel = (ServerSocketChannel) key.channel();
            var clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                clientChannel.configureBlocking(false);
                var clientKey = clientChannel.register(key.selector(), SelectionKey.OP_READ);
                clientKey.attach(new SelectorProtocol(clientKey));
                keysList.add(clientKey);
                ++clientCounter;

                //log("accepted " + clientChannel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SelectorProtocol {
        private Parser parser;
        private final SelectionKey key;
        private static ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        private static ByteBuffer writeBuffer;
        private boolean isMessageReceived = false;
        private static final char RS = 0x1E;
        private static int innerCounter = 0;
        //private static List<ByteBuffer> buffers = new ArrayList<>();

        public SelectorProtocol(SelectionKey clientKey) {
            this.key = clientKey;
            this.parser = new Parser();
        }

        public void read() {
            try {
                readMethod();
            } catch (Exception e) {
                e.printStackTrace();
                closeConnection();
            }
        }

        public void write() {
            try {
                writeMethod();
            } catch (Exception e) {
                e.printStackTrace();
                //closeConnection();
            }
        }

        private void readMethod() throws Exception {
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
                var parsedMSG = parser.parse(message);
                //System.out.println(parsedMSG + " parsedmsg");
                writeBuffer = ByteBuffer.wrap((parsedMSG).getBytes()); //+RS
                for (SelectionKey sk : keysList) {
                    if (!key.equals(sk))
                        sk.interestOps(SelectionKey.OP_WRITE);//говорим другим клиентам, что у нас тут есть интересный контент
                }
                key.interestOps(SelectionKey.OP_WRITE);//нашему тоже говорим
                isMessageReceived = false;
            }
        }

        private void writeMethod() throws Exception {//наш клиент начинает писать в свой канал
            var channel = (SocketChannel) key.channel();
            int writeCount;
            do {
                writeCount = channel.write(writeBuffer);
                //channel.write(writeBuffer);
            }
            while (writeCount > 0);
            key.interestOps(SelectionKey.OP_READ);
            writeBuffer.flip();
        }

        private void closeConnection() {
            key.cancel();
            if (key.channel() != null) {
                try {
                    key.channel().close();
                    keysList.remove(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static class MessageStack {
            public void addMessage() {
            }

            public void returnStack() {
            }
        }

    }

}