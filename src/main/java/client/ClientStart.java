package client;
import static util.Logs.log;

public class ClientStart implements Runnable{
    private final String host;
    private final int port;
    private final String type;
    private final int timeout;
    public ClientStart(String host, int port, String type, int timeout) {
        this.host = host;
        this.port = port;
        this.type = type;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        log("Client starts...");
        Constructor.construct(host,port,type,timeout).run();
        log("Client Stops,,,,");
    }
}
