package client.core.settings;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Properties;

public interface Settings {
    SocketAddress ADDRESS = new InetSocketAddress("localhost", 9753);
    //Properties x = new Properties(); // getOrDefault

}
