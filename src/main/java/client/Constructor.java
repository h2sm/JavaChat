package client;

import client.core.Core;
import client.core.transport.PersistSocketTransport;
import client.core.ui.ConsoleUI;

public class Constructor {
    public static Core construct() {
        return new Core(new ConsoleUI(), new PersistSocketTransport());
    }
}
