package server.postgres;

import java.util.ArrayList;

public interface DBInterface {
    boolean authenticate(String name, String password);
    void saveMessage(String name, String msg);
    ArrayList<String> loadMessages();
}
