package server;

import server.postgres.DBFactory;
import server.postgres.DBInterface;
import server.workers.Request;
import server.workers.Response;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static util.Logs.log;

public class PersistSocketServer implements Runnable {
    private static ArrayList<EchoProtocol> clients = new ArrayList<>();
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private final String host;
    private final int port;
    private final int timeout;

    public PersistSocketServer(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try (var serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(host, port));
            while (true) {
                var clientSocket = serverSocket.accept();
                log("connected " + clientSocket);
                clientSocket.setSoTimeout(timeout * 1000);
                var echoProtocol = new EchoProtocol(clients, clientSocket);
                clients.add(echoProtocol);
                pool.submit(echoProtocol);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class EchoProtocol implements Runnable {
        private static final char RS = 0x1E;

        protected static ArrayList<EchoProtocol> clients;
        private final Socket socket;
        private final BufferedInputStream in;
        private final PrintWriter out;
        private final DBInterface postgresHandler = DBFactory.getInstance();
        private boolean isAuthorized = false;
        private static ArrayList<String> stack = new ArrayList<>();

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
                } catch (SocketTimeoutException e) {
                    socket.close();
                    throw new SocketException("Server Socket Timeout. No Messages were received.");
                }
                if (chInt == -1) throw new IOException("Socket has been closed by a client outage." +
                        "Please reconnect.");
                ch = (char) chInt;
                baos.write(ch);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }

        private void tryRun() throws Exception {
            while (true) {
                var receivedString = readInputStream();
                var req = new Request(receivedString);
                switch (req.getMessageType()) {
                    case T_REGISTER -> registering(req);
                    case T_MESSAGE -> messaging(req);
                }
            }
        }

        private void registering(Request r) {
            if (checkUser(r.getName(), r.getPassword())) {
                receiveHistory();
                stack.add(Response.returnResponse(r));
                sendMessage(r);
            } else closeConnection();
        }

        private void messaging(Request r) {
            saveToSQL(r.getName(), r.getMessage());
            stack.add(Response.returnResponse(r));
            sendMessage(r);
        }

        private void sendMessage(Request request) {
            switch (request.getMessageType()) {
                case T_REGISTER -> {
                    stack.forEach(out::println);
                    stack.clear();
                }
                case T_MESSAGE -> {
                    stack.forEach(msg -> clients.forEach(client -> client.out.println(msg)));
                    stack.clear();
                }
            }
        }

        private void saveToSQL(String name, String msg) {
            postgresHandler.saveMessage(name, msg);
        }

        private boolean checkUser(String name, String pass) {
            if (!isAuthorized) {
                if (postgresHandler.authenticate(name, pass))
                    isAuthorized = true;
            }
            return isAuthorized;
        }

        private void receiveHistory() {
            stack.addAll(postgresHandler.loadMessages());
        }

        private void closeConnection() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}