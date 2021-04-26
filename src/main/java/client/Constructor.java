package client;

import client.core.Core;
import client.core.transport.PersistSocketTransport;
import client.core.transport.SessionChannelTransport;
import client.core.ui.ConsoleUI;

public class Constructor {
    public static Core construct(String host, int port, String type, int timeout) {
        if (type.equals("socket")){
            return new Core(new ConsoleUI(), new PersistSocketTransport(host,port,timeout));
        }
        else {
            return new Core(new ConsoleUI(), new SessionChannelTransport(host,port,timeout));
        }
    }
}
