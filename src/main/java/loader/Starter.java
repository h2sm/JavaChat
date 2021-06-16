package loader;

import client.ClientStart;
import server.PersistSocketServer;
import server.SessionSelectorServer;

import java.io.IOException;

import static util.Logs.log;

public class Starter {
    public static void main(String[] args) throws Exception {
        var config = new Configuration();
        int port = config.getPort();
        String host = config.getHostname();
        String type = config.getType();
        int timeout = config.getTimer();

        new Thread(new ClientStart(host,port,type,timeout)).start();

        try {
            if (type.equals("socket")){
                new Thread(new PersistSocketServer(host,port,timeout)).start();
            }
            else if (type.equals("selector")){
                new Thread(new SessionSelectorServer(host,port,timeout)).start();//здесь создать таймер для сообщений
            }
            else {
                throw new IOException("Explicit server configuration: " + type);
            }
        }
        catch (Exception e){
            log("No configuration file found.");
            throw new IOException("No configuration file found");
        }
    }

}
