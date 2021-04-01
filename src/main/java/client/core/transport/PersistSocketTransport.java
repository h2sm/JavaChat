package client.core.transport;

import client.core.Transport;
import client.core.exception.*;
import client.core.settings.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static util.Logs.log;

public class PersistSocketTransport implements Transport {
    private Socket socket;
    private String name;
    private final Scanner scanner = new Scanner(System.in);

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
        askForName();
        closeSocketIfRequired();
        socket = new Socket();
        socket.connect(Settings.ADDRESS);
    }
    private void askForName() throws Exception{
        log("Enter your name");
        name = scanner.next();
    }

    private void closeSocketIfRequired() throws Exception {
        if (socket != null && socket.isConnected()) {
            socket.close();
        }
    }

    @Override
    public String converse(String message) {
        if (socket == null || !socket.isConnected())
            throw new TransportException("connection required");
        try {
            return tryConverse(message);
        }
        catch (Exception e) {
            throw new TransportException(e);
        }
    }

    private String tryConverse(String message) throws Exception {
        var out = new PrintWriter(socket.getOutputStream(), true,
                StandardCharsets.UTF_8);
        var in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                StandardCharsets.UTF_8));

        out.println("[" + name+ "]:" + message);
        return in.readLine();
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
