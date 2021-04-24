package server;

import static util.Logs.log;

public class ServerStart implements Runnable{
    @Override
    public void run() {
        log("Server starting...");
        //new SessionSelectorServer().run();
        new PersistSocketServer().run();
        log("Server finished");
    }
}
