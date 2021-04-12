package server;

import server.settings.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
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
//            in = socket.getInputStream();
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

        private String readInputStream(byte [] pack) throws Exception{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (byte b: pack){
                if ((char) b != 0x1E) baos.write(b);
            }
//            String message = "";
//            while (in.available()!=0){//получаем из потока сообщение побайтово
//                int piece = in.read();
//                //byte[] mas = in.readAllBytes();
//                if ((char)piece!=0x1E){
//                    baos.write(piece);
//                }
//            }
            return baos.toString(Charset.defaultCharset());
            //return message;
            //пока из in не прочитаем FS считывать очередной байт в массив байтов
            //потом разделить массив на комманды и сообщение
            //ByteArrayOutputStream -> bytearray -> string (utf8)
            //в цикле пока байт не равен RS

            //return ;
        }

        private void tryRun() throws Exception {
            while (true){
                byte[] pack = in.readLine().getBytes();
                //if (pack.length!=0){
                    var receivedString = readInputStream(pack);
                    var parsedMessage = parseMessage(receivedString);
                    log("Received from " + socket + ": " + parsedMessage);
                    sendMessageToAll(parsedMessage);
                //}
            }

        }

        private String parseMessage(String pack){//лучше parseMessage, возвращать message
            var buff = pack.split(String.valueOf((char) 0x1D));
            String command = buff[0];
            String name = buff[1];
            if (command.equals("T_MESSAGE")) {
                var msg = buff[2];
                return name + " says: " + msg;
            }
            else return "New Connected user " + name;

        }

        private void sendMessageToAll(String msg) {
            for (EchoProtocol client : clients) {
                client.out.println(formatter.format(date) + " "+ msg);
            }
        }
    }
}