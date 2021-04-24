package server;

import server.settings.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static util.Logs.log;

public class PersistSocketServer implements Runnable {
    private static ArrayList<EchoProtocol> clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newCachedThreadPool();

    @Override
    public void run() {
        try (var serverSocket = new ServerSocket()) {
            serverSocket.bind(Settings.ADDRESS);
            while (true) {
                var clientSocket = serverSocket.accept();
                log("connected " + clientSocket);
                clientSocket.setSoTimeout(5*1000);
                var echoProtocol = new EchoProtocol(clients, clientSocket);
                clients.add(echoProtocol);
                pool.submit(echoProtocol);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class EchoProtocol implements Runnable {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        private static final char GS = 0x1D;
        private static final char RS = 0x1E;

        protected static ArrayList<EchoProtocol> clients;
        private final Socket socket;
        private BufferedInputStream in;
        private PrintWriter out;

        public EchoProtocol(ArrayList<EchoProtocol> echoclients, Socket socket) throws Exception {
            this.socket = socket;
            clients = echoclients;
            in = new BufferedInputStream(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        }

        @Override
        public void run() {
            try (socket) {
                tryRun();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log("Finished socket " + socket);
        }

        private String readInputStream() throws Exception {
                var baos = new ByteArrayOutputStream();
                char ch = ' ';
                while (ch != RS) {
                    int chInt = 0;
                    try {
                        chInt = in.read();
                    }
                    catch (Exception e){
                        socket.close();
                        throw new SocketException("Server Socket Timeout. No Messages were received.");
                        //isTimeout=true;
                        //e.printStackTrace();
                        //log("socket timeout");
                    }
                    if (chInt == -1) throw new IOException("Socket has been closed by a client outage. Reconnect.");
                    ch = (char) chInt;
                    baos.write(ch);
                }
                return baos.toString(StandardCharsets.UTF_8);
        }

        private void tryRun() throws Exception {
            while (true) {
                var receivedString = readInputStream();
                log("RECEIVED STRING " + receivedString);
                var parsedMessage = parseMessage(receivedString);
                //log("Received from " + socket + ": " + parsedMessage);
                sendMessageToAll(parsedMessage);
                }
            }

        private String parseMessage(String pack) {
            var buff = pack.split(String.valueOf(GS)); //Arrays.toString(<>)
            if (buff[0].equals("T_REGISTER")) return "New connected user " + buff[1];
            return buff[1] + " says: " + buff[2];
        }

        private void sendMessageToAll(String msg) {
            for (EchoProtocol client : clients) {
                client.out.println(formatter.format(date) + " " + msg);
            }
        }

        private void testTimer() {
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    runTask();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 3, TimeUnit.SECONDS);
        }

        private void runTask() {
            String unit = "T_TEST" + RS;
            sendMessageToAll(unit);
        }
    }
}