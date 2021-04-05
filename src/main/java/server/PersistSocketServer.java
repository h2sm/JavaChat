package server;

import server.settings.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static util.Logs.log;

public class PersistSocketServer implements Runnable {
    private static ArrayList<EchoProtocol> clients  = new ArrayList<>();
    private static ExecutorService pool = Executors.newCachedThreadPool();
    @Override
    public void run() {
        try (var serverSocket = new ServerSocket()) {
            serverSocket.bind(Settings.ADDRESS);
            while (true) {
                    var clientSocket = serverSocket.accept();
                    log("connected " + clientSocket);
                    var echoProtocol = new EchoProtocol(clients,clientSocket);
                    clients.add(echoProtocol);
                    pool.submit(echoProtocol);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class EchoProtocol implements Runnable{
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        protected static ArrayList<EchoProtocol> clients;
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public EchoProtocol(ArrayList<EchoProtocol> echoclients, Socket socket) throws Exception {
            this.socket = socket;
            clients=echoclients;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        }

        @Override
        public void run() {
            try (socket) {
                tryRun();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log("finished" + socket);
        }

        private void tryRun() throws Exception {
            try {
                var msg = "";
                while (msg!=null){
                    msg = in.readLine();
                    log("received from " + socket + ": " + msg);
                    toAll(msg);
                }
            }
            finally {
//                out.close();
//                in.close();
            }


        }

        private void toAll(String msg) {
            if (msg != null){
                for (EchoProtocol client : clients) {
                   client.out.println(formatter.format(date) + " " + msg);
                }
            }
        }
    }
}

