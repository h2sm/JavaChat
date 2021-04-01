package server;

import server.settings.Settings;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

import static util.Logs.log;

public class SessionChannelServer implements Runnable {

    @Override
    public void run() {
        var pool = Executors.newCachedThreadPool();
        try (var serverChannel = ServerSocketChannel.open()) {
            serverChannel.configureBlocking(true);
            serverChannel.bind(Settings.ADDRESS);
            while (true) {
                var clientChannel = serverChannel.accept();
                log("connected " + clientChannel);
                pool.submit(new EchoProtocol(clientChannel));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class EchoProtocol implements Runnable {
        private final SocketChannel channel;

        public EchoProtocol(SocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void run() {
            try (channel) {
                tryRun();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log("finished " + channel);
        }

        private void tryRun() throws Exception {
            var buffer = ByteBuffer.allocate(8);
            var done = false;
            var baos = new ByteArrayOutputStream();
            while (!done) {
                buffer.clear();
                channel.read(buffer);
                buffer.flip();

                while (buffer.hasRemaining() && !done) {
                    var b = buffer.get();
                    if (b == '\n') {
                        done = true;
                    }
                    else {
                        baos.write(b);
                    }
                }
            }
            var message = new String(baos.toByteArray());
            log("received from " + channel + ": " + message);

            buffer = ByteBuffer.wrap(("server echo: " + message + "\n").getBytes());
            channel.write(buffer);
        }
    }
}
