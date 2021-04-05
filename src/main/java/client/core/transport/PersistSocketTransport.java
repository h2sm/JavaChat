package client.core.transport;

import client.core.Transport;
import client.core.exception.*;
import client.core.settings.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static util.Logs.log;

public class PersistSocketTransport implements Transport {
    private Socket socket;
    private String name;
    private final Scanner scanner = new Scanner(System.in);
    private PerformMessages runnable;
    private PrintWriter out;
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
        setPrintWriter();
        registerOnServer();
        runnable = new PerformMessages(socket);
        runnable.start();
    }
    private void setPrintWriter() throws Exception {
        out = new PrintWriter(socket.getOutputStream(), true,
                StandardCharsets.UTF_8);//отдаем
    }
    private String askForName() throws Exception{
        log("Enter your name");
        name = scanner.next();
        return name;
    }
    private void registerOnServer(){
        if (socket.isConnected()){
           out.println(messagingProtocol.constructRegistration());
        }
    }

    private void closeSocketIfRequired() throws Exception {
        if (socket != null && socket.isConnected()) {
//            socket.shutdownInput();
//            socket.shutdownOutput();
            runnable.interrupt();
            runnable.join();
        }
    }

    @Override
    public String converse(String message) {
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
//        var out = new PrintWriter(socket.getOutputStream(), true,
//                StandardCharsets.UTF_8);//отдаем
        if (message != null)
            out.println(messagingProtocol.constructMessage(message));
            //out.println("[" + name+ "]:" + message);
        return "sent!";
    }

    @Override
    public void disconnect() {
        try {
            runnable.terminate();
            tryDisconnect();
        }
        catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private void tryDisconnect() throws Exception {
        socket.shutdownOutput();
        closeSocketIfRequired();
    }
}
