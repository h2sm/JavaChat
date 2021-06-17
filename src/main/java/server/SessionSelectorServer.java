package server;

import server.workers.Request;
import server.workers.Response;

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

    public SessionSelectorServer(String host, int port, int timeout) {
        this.address = new InetSocketAddress(host, port);
        this.timeout = timeout * 1000;
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
                        ((SelectorProtocol) key.attachment()).process();
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
                var sp = new SelectorProtocol(clientKey, timeout);
                clientKey.attach(sp);
                keysList.add(clientKey);
                //log("accepted " + clientChannel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SelectorProtocol {
        private final SelectionKey key;
        private static ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        private static ByteBuffer writeBuffer;
        private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private boolean isMessageReceived = false;
        private static final char RS = 0x1E;
        private long timeout;
        private long lastSent = 0l;
        //private static PostgresHandler postgresHandler = PostgresHandler.getInstance("docker", "docker");
        private boolean isAuthorized = true;//false!!!!
        private Thread time = new Thread(this::timer);
        private boolean connected = true;
        private static ArrayList<String> messagesStack = new ArrayList<>();
        private static int counter = 0;

        public SelectorProtocol(SelectionKey clientKey, int timeout) {
            this.key = clientKey;
            this.timeout = timeout;
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
            lastSent = System.currentTimeMillis();
            if (!time.isAlive()) time.start();
            if (baos.size() != 0) baos.reset();
            var channel = (SocketChannel) key.channel();
            int readCount = 0;
            do {
                readBuffer.clear();
                try {
                    readCount = channel.read(readBuffer);
                } catch (SocketException e) {
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
        }

        private void writeMethod() throws Exception {//наш клиент начинает писать в свой канал
            var channel = (SocketChannel) key.channel();
            ++counter;
            for (String b : messagesStack) {
                writeBuffer = ByteBuffer.wrap(b.getBytes());
                channel.write(writeBuffer);
            }
            if (keysList.size() == counter) {
                log("cleared");
                messagesStack.clear();
                counter = 0;
            }
            key.interestOps(SelectionKey.OP_READ);
            writeBuffer.flip();
        }

        private void process() {
            var message = new String(baos.toByteArray());
            var userRequest = new Request(message);
            switch (userRequest.getMessageType()) {
                case T_REGISTER -> {
                    if (checkUser(userRequest.getName(), userRequest.getPassword())) {
                        //receiveHistory();
                        openWriting();
                        messagesStack.add(Response.returnResponse(userRequest));
                        log("USER RECEIVED HISTORY!");
                    } else closeConnection();
                    isMessageReceived = false;
                    break;
                }
                case T_MESSAGE -> {
                    //saveToSQL(userRequest.getName(), userRequest.getMessage());
                    log("MESSAGE SAVED TO SQL");
                    messagesStack.add(Response.returnResponse(userRequest));
                    //messagesStack.add(ByteBuffer.wrap(userRequest.getMessage().getBytes()));
                    openWriting();
                    isMessageReceived = false;
                    break;
                }
            }
        }

        private void openWriting() {
            for (SelectionKey sk : keysList)
                sk.interestOps(SelectionKey.OP_WRITE);//говорим другим клиентам, что у нас тут есть интересный контент

        }

        private boolean checkUser(String name, String pass) {
//            if (!isAuthorized) {//если клиент пока не авторизован
//                if (postgresHandler.authenticate(name, pass)) {//мы обращаемся к базе данных
//                    isAuthorized = true;//если клиент найден, то он авторизован
//                } else {
//                    //closeConnection();//если нет клиента, то прощаемся с ним
//                }
//            }
            System.out.println("USER CONNECTED !");
            return isAuthorized;//такие дела
        }

        private void saveToSQL(String name, String msg) {
            try {
                //postgresHandler.saveMessage(name, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void timer() {
            while (connected) {
                var time = System.currentTimeMillis();
                //System.out.println(time - lastSent + " =" + timeout);
                if (time - lastSent > timeout && lastSent != 0L) {
                    log("timeout");
                    closeConnection();
                    connected = false;
                }
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        private void receiveHistory() {
            //var msges = postgresHandler.returnLast20Messages();
            var msges = new ArrayList<String>();
            for (String s : msges) {
                //messagesStack.add(ByteBuffer.wrap(s.getBytes()));
            }
            //return msges;
        }

        private void closeConnection() {
            key.cancel();
            time.interrupt();
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
    }

}