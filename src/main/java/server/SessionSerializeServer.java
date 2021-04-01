package server;

import server.settings.Settings;
import server.transport.session.serialization.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

import static util.Logs.log;

public class SessionSerializeServer implements Runnable {
    @Override
    public void run() {
        var pool = Executors.newCachedThreadPool();
        try (var serverSocket = new ServerSocket()) {
            serverSocket.bind(Settings.ADDRESS);
            while (true) {
                var clientSocket = serverSocket.accept();
                log("connected " + clientSocket);
                pool.submit(new EchoProtocol(clientSocket));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class EchoProtocol implements Runnable {
        private final Socket socket;

        public EchoProtocol(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (socket) {
                tryRun();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            log("finished" + socket);
        }

        private void tryRun() throws Exception {
            var out = new ObjectOutputStream(socket.getOutputStream());
            var in = new ObjectInputStream(socket.getInputStream());



            Message msg = (Message) in.readObject();
            log("received from " + socket + ": " + msg);
            out.writeObject(new Message("server echo:" + msg.getMessage()));
        }
    }
}
