package loader;

import client.ClientStart;
import server.ServerStart;

public class Starter {
    public static void main(String[] args) throws Exception {
        new Thread(new ClientStart()).start();
        new Thread(new ServerStart()).start();
    }

}
