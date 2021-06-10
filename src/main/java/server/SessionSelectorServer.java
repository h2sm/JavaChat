package server;


import server.postgres.PostgresHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
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
    private PostgresHandler handler = PostgresHandler.getInstance("docker", "docker");

    public SessionSelectorServer(String host, int port, int timeout) {
        this.address = new InetSocketAddress(host, port);
        this.timeout = timeout*1000;
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
                selector.select(timeout);
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
                clientKey.attach(new SelectorProtocol(clientKey, timeout));
                keysList.add(clientKey);

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
        private long receivedTime = 0L;
        private long timeout;
        private static PostgresHandler postgresHandler = PostgresHandler.getInstance("docker", "docker");
        //private Thread timerTh = new Thread(this::timer);
        //private boolean firstMSGReceived = false;


        public SelectorProtocol(SelectionKey clientKey, int timeout) {
            this.key = clientKey;
            this.parser = new Parser();
            this.timeout = timeout * 1000;
            //timerTh.start();
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
            int readCount = 0;
            do {
                readBuffer.clear();
                try {
                    readCount = channel.read(readBuffer);
                }
                catch (SocketException e){
                    log("Socket was closed");
                    closeConnection();
                }
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
                receivedTime = System.currentTimeMillis();
                //firstMSGReceived = true;
                var message = new String(baos.toByteArray());
                var parsedMSG = parser.parse(message);
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
                    log("closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

//        private void timer() {
//            while (true) {
//                if (firstMSGReceived){
//                    //System.out.println(firstMSGReceived);
//                    long timeNow = System.currentTimeMillis();
//                    log(timeNow - receivedTime + " ms");
//                    if (timeNow - receivedTime > timeout) {
//                        closeConnection();
//                        log("Timeout");
//                        return;
//                    }
//                }
//            }
//        }
    }

}