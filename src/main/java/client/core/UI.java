package client.core;

import client.core.command.Command;

public interface UI {
    Command getCommand();
    void showMessage(String message);
}
