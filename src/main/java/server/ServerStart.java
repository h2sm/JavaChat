package server;

import server.settings.ServerConfiguration;

import java.io.IOException;

import static util.Logs.log;

public class ServerStart implements Runnable{
    @Override
    public void run() {
        int port;
        String host;
        String type;
        int timeout;
        try {
            var config = new ServerConfiguration();
            port = config.getPort();
            host = config.getHostname();
            type = config.getType();
            timeout = config.getTimer();
            if (type.equals("socket")){
                log("Socket Server starting");
                new PersistSocketServer(host,port,timeout).run();
                log("Socket Server closing");
            }
            if (type.equals("selector")){
                log("Selector server starting");
                new SessionSelectorServer().run();
                log("Selector Server closing");
            }
            else {
                throw new IOException("Explicit server configuration");
            }
        } catch (Exception e) {
            log("No configuration file found.");
            e.printStackTrace();
        }
        //log("Server starting...");
        //new SessionSelectorServer().run();
        //new PersistSocketServer(host,port,timeout).run();
        //log("Server finished");
    }
}
