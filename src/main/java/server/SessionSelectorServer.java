package server;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static util.Logs.log;

public class SessionSelectorServer implements Runnable {
    private final int timeout;
    private final InetSocketAddress address;
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8);
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private boolean isMessageReceived = false;
    private ByteBuffer writeBuffer;
    private static final char GS = 0x1D;
    private static final char RS = 0x1E;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    public SessionSelectorServer(String host, int port, int timeout) {
        this.address = new InetSocketAddress(host, port);
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            serverChannel=ServerSocketChannel.open();
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
                        //log("acceptable " + key);
                        //performAccept(key);
                        var clientSocket = serverChannel.accept();
                        if (clientSocket!=null){
                            clientSocket.configureBlocking(false);
                            clientSocket.register(selector,SelectionKey.OP_READ);
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

    private void performAccept(SelectionKey serverKey) {
        try {
            SocketChannel clientChannel;
            do {
                var serverChannel = (ServerSocketChannel) serverKey.channel();
                clientChannel = serverChannel.accept();
                if (clientChannel != null) {
                    clientChannel.configureBlocking(false);
                    var clientKey = clientChannel.register(serverKey.selector(), SelectionKey.OP_READ);
                    //clientKey.attach(new EchoProtocol(clientKey));
                    log("accepted " + clientChannel);
                }
            }
            while (clientChannel != null);
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
            //log("received from " + channel + ": " + message);
            var parsedMSG = parseMessage(message);
            writeBuffer = ByteBuffer.wrap((formatter.format(date) + " " + parsedMSG).getBytes()); //+RS
            //key.interestOpsAnd(~SelectionKey.OP_READ);
            key.interestOps(SelectionKey.OP_WRITE);
            //channel.register(key.selector(), SelectionKey.OP_WRITE);

        }
    }

    private String parseMessage(String msg) {
        var buff = msg.split(String.valueOf(GS)); //Arrays.toString(<>)
        if (buff[0].equals("T_REGISTER")) return "New connected user " + buff[1];
        return buff[1] + " says: " + buff[2];
    }

    public void write(SelectionKey key) {
        try {
            tryWrite();
        } catch (Exception e) {
            e.printStackTrace();
            closeConnection(key);
        }
    }

    private void tryWrite() throws Exception {
        try {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();

            while(iter.hasNext()) {
                SelectionKey key = (SelectionKey) iter.next();
                if (key.isWritable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    //ByteBuffer buf = Charset.forName("ISO-8859-2").encode(msg);

                    while(writeBuffer.hasRemaining())
                        clientChannel.write(writeBuffer);

                    writeBuffer.clear();
                }
            }
        } catch(Exception e) {
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
}


    /*private static class EchoProtocol {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
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
                var parsedMSG = parseMessage( message);
                writeBuffer = ByteBuffer.wrap((formatter.format(date) + " " + parsedMSG).getBytes()); //+RS
                //key.interestOpsAnd(~SelectionKey.OP_READ);
                key.interestOps(SelectionKey.OP_WRITE);
                //channel.register(key.selector(), SelectionKey.OP_WRITE);

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
            //System.out.println(key.isWritable() + " is writable");
            int writeCount;
            do {
                writeCount = channel.write(writeBuffer);
            }
            while (writeCount > 0);

            if (!writeBuffer.hasRemaining()) {
                key.interestOps(SelectionKey.OP_READ);
                //channel.register(key.selector(), SelectionKey.OP_READ);
                //closeConnection();
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
*/