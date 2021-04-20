package client;



import static util.Logs.log;

public class ClientStart implements Runnable{

    @Override
    public void run() {

        log("Client starts...");
        Constructor.construct().run();
        log("Client Stops,,,,");
    }
}
