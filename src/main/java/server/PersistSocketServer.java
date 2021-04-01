package server;

import server.settings.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import static util.Logs.log;

public class PersistSocketServer implements Runnable {
    protected static ArrayList<Socket> clients;
    @Override
    public void run() {
        //var pool = Executors.newCachedThreadPool();
        try (var serverSocket = new ServerSocket()) {
            serverSocket.bind(Settings.ADDRESS);
            clients = new ArrayList<>();
            //clients = Collections.synchronizedList(new ArrayList<>());
            while (true) {
                    var clientSocket = serverSocket.accept();
                    log("connected " + clientSocket);
                    var echoProtocol = new EchoProtocol(clients,clientSocket);
                    var thread = new Thread(echoProtocol);
                    thread.start();
                    clients.add(clientSocket);
                    //new EchoProtocol(clientSocket).start();
                    //clients.add(clientSocket);
                    //pool.submit(new EchoProtocol(clientSocket,clients));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class EchoProtocol implements Runnable{
        protected static ArrayList<Socket> clients;
        private final Socket socket;

        public EchoProtocol(ArrayList<Socket> clients, Socket socket) {
            this.socket = socket;
            this.clients=clients;
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

            var in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8));


            var msg = "";
                while (msg != null) {
                    msg = in.readLine();
                    log("received from " + socket + ": " + msg);
                    for (Socket x : clients) {
                        var out = new PrintWriter(x.getOutputStream(), true,
                                StandardCharsets.UTF_8);
                        out.println("SERVER ECHO: " + msg);

                    }
                            //out.println("server echo: " + msg);
                        }
                    }
        }

    }

