package server;

import static util.Logs.log;

public class App implements Runnable{
    @Override
    public void run() {
        log("Server starting...");
        new PersistSocketServer().run();
        log("Server finished");
    }
    //    public static void main(String[] args) {
//        log("Server starting...");
//        new PersistSocketServer().run();
////        new SessionSocketServer().run();
////        new SessionChannelServer().run();
////        new SessionSerializeServer().run();
////        new SessionSelectorServer().run();
//        log("Server finished");
}
