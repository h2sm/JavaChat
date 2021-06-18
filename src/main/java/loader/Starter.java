package loader;

import client.ClientStart;
import server.PersistSocketServer;
import server.SessionSelectorServer;

import java.io.IOException;

public class Starter {
    public static void main(String[] args) throws Exception {
        var config = new Configuration();
        int port = config.getPort();
        var host = config.getHostname();
        var type = config.getType();
        int timeout = config.getTimer();

        new Thread(new ClientStart(host, port, type, timeout)).start();

        switch (type) {
            case "socket" -> new Thread(new PersistSocketServer(host, port, timeout)).start();
            case "selector" -> new Thread(new SessionSelectorServer(host, port, timeout)).start();
            default -> throw new IOException("Explicit server configuration: " + type);
        }

    }

}
