package server;

import server.settings.Settings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import static util.Logs.log;

public class SessionSocketServer implements Runnable {
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
            var out = new PrintWriter(socket.getOutputStream(), true,
                    StandardCharsets.UTF_8);
            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8));

            var msg = in.readLine();
            log("received from " + socket + ": " + msg);
            out.println("server echo: " + msg);
        }
    }
}
