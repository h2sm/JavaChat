package loader;

import client.ClientStart;
import server.PersistSocketServer;
import server.SessionSelectorServer;

import java.io.IOException;

import static util.Logs.log;

public class Starter {
    public static void main(String[] args) throws Exception {
        int port;
        String host;
        String type;
        int timeout;
        try {
            var config = new Configuration();
            port = config.getPort();
            host = config.getHostname();
            type = config.getType();
            timeout = config.getTimer();
            new Thread(new ClientStart(host,port,type,timeout)).start();
            if (type.equals("socket")){
                new Thread(new PersistSocketServer(host,port,timeout)).start();
            }
            else if (type.equals("selector")){
                new Thread(new SessionSelectorServer(host,port,timeout)).start();
            }
            else {
                throw new IOException("Explicit server configuration: " + type);
            }
        }
        catch (Exception e){
            log("No configuration file found.");
            //e.printStackTrace();
            throw new IOException("No configuration file found");
        }
    }

}
