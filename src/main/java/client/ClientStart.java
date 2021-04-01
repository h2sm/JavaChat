package client;



import static util.Logs.log;

public class ClientStart implements Runnable{
//    public static void main(String[] args) {
//        log("Client starts...");
//        Constructor.construct().run();
//        log("Client Stops,,,,");
//    }

    @Override
    public void run() {

        log("Client starts...");
        Constructor.construct().run();
        log("Client Stops,,,,");
    }
}
