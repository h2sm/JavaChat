package server.settings;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface Settings {
    SocketAddress ADDRESS = new InetSocketAddress("localhost", 9753);
}
