package client.core.transport;

import client.core.Transport;
import client.core.exception.*;
import client.core.settings.Settings;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static util.Logs.log;

public class PersistSocketTransport implements Transport {
    private Socket socket;
    private String name;
    private final Scanner scanner = new Scanner(System.in);
    //private PerformMessages runnable;
    private PrintWriter out;
//    private BufferedReader in;
    private MessagingProtocol messagingProtocol;

    @Override
    public void connect() {
        try {
            tryConnect();
        }
        catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private void tryConnect() throws Exception {
        var name = askForName();
        messagingProtocol = new MessagingProtocol(name);
        socket = new Socket();
        socket.connect(Settings.ADDRESS);
        registerOnServer();
        readMessages();
//        runnable = new PerformMessages(socket);
//        runnable.start();
    }

    private String askForName() {
        log("Enter your name");
        name = scanner.next();
        return name;
    }
    private void registerOnServer() throws IOException {
        if (socket.isConnected()){
            out = new PrintWriter(socket.getOutputStream(), true,
                    StandardCharsets.UTF_8);//отдаем
           out.println(messagingProtocol.constructRegistration());
        }
    }

    private void closeSocketIfRequired() throws Exception {
        if (socket != null && socket.isConnected()) {
            socket.shutdownOutput();
            socket.close();
            System.out.println("is closed? "+socket.isClosed());

        }
    }

    @Override
    public String converse(String message) {//один поток заведует инпутом, второй - оутпутом
        if (socket == null || !socket.isConnected())
            throw new TransportException("connection required");
        if (message == null) throw new TransportException("msg is null");
        try {
            return tryConverse(message);
        }
        catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private String tryConverse(String message) throws Exception {
         /*out = new PrintWriter(socket.getOutputStream(), true,
                StandardCharsets.UTF_8);//отдаем*/
        if (message != null)
            out.println(messagingProtocol.constructMessage(message));
        return "sent!";
    }
    private void readMessages(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                            StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String line;
                    try {
                        while (!socket.isClosed()){
                            line=in.readLine();
                            System.out.println(line);
                            }
                        }
                     catch (Exception e) {
                        //e.printStackTrace();
                    }
            }
        });
        thread.start();
    }

    @Override
    public void disconnect() {
        try {
            tryDisconnect();
        }
        catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private void tryDisconnect() throws Exception {
        closeSocketIfRequired();
    }

}