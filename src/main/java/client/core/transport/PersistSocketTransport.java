package client.core.transport;

import client.core.Transport;
import client.core.exception.*;
import client.core.settings.Settings;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static util.Logs.log;

public class PersistSocketTransport implements Transport {
    private Socket socket;
    private String name;
    private final Scanner scanner = new Scanner(System.in);
    private PrintWriter out;
    private MessagingProtocol messagingProtocol;
    private final String host;
    private final int port, timeout;
    public PersistSocketTransport(String host, int port, int timeout){
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    @Override
    public void connect() {
        try {
            tryConnect();
        } catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private void tryConnect() throws Exception {
        var name = askForName();
        messagingProtocol = new MessagingProtocol(name);
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port));
        socket.setSoTimeout(timeout*1000);
        registerOnServer();
        readMessages();
    }

    private String askForName() {
        log("Enter your name");
        name = scanner.next();
        return name;
    }

    private void registerOnServer() throws IOException {
        if (socket.isConnected()) {
            out = new PrintWriter(socket.getOutputStream(), true,
                    StandardCharsets.UTF_8);//отдаем
            out.println(messagingProtocol.constructRegistration());
        }
    }

    private void closeSocketIfRequired() throws Exception {
        if (socket != null && socket.isConnected()) {
            socket.shutdownOutput();
            socket.close();
        }
    }

    @Override
    public String converse(String message) {//один поток заведует инпутом, второй - оутпутом
        if (socket == null || !socket.isConnected())
            throw new TransportException("connection required");
        if (message == null) throw new TransportException("msg is null");
        try {
            return tryConverse(message);
        } catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private String tryConverse(String message) throws Exception {
        if (message != null)
            out.println(messagingProtocol.constructMessage(message));
        return "sent!";
    }

    private void readMessages() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                            StandardCharsets.UTF_8));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String line;
                try {
                    while (!socket.isClosed()) {
                        line = in.readLine();
                        if (!line.equals("null"))
                            System.out.println(line );
                    }
                } catch (Exception e) {
                    try {
                        tryConnectAgain();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                }
            }
        });
        thread.start();
    }

    @Override
    public void disconnect() {
        try {
            tryDisconnect();
        } catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private void tryDisconnect() throws Exception {
        closeSocketIfRequired();
    }
    private void tryConnectAgain() throws Exception {
        socket.close();
        /*Thread.sleep(900);
        tryConnect();*/
    }

}