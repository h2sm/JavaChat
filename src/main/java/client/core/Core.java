package client.core;

import client.core.command.*;
import client.core.exception.UnexpectedCommandException;


public class Core implements Runnable {
    private final UI ui;
    private final Transport transport;

    public Core(UI ui, Transport transport) {
        this.ui = ui;
        this.transport = transport;
    }

    @Override
    public void run() {
        try {
            runCommandLoop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runCommandLoop() {
        Command command;
        do {
            command = ui.getCommand();
            perform(command);
        }
        while (command.getClass() != Disconnect.class);
    }

    private void perform(Command command) {
        try {
            performSelection(command);
        }
        catch (Exception e) {
            ui.showMessage(e.getMessage());
        }
    }

    private void performSelection(Command command) {
        if (command.getClass() == Connect.class) {
            performConnect((Connect) command);
        }
        else if (command.getClass() == Send.class) {
            performSend((Send) command);
        }
        else if (command.getClass() == Disconnect.class) {
            performDisconnect((Disconnect) command);
        }
        else {
            throw new UnexpectedCommandException("unexpected " + command.getClass().getName());
        }
    }

    private void performConnect(Connect command) {
        transport.connect();
    }

    private void performSend(Send command) {
        var answer = transport.converse(command.getMessage());
        ui.showMessage(answer);
    }

    private void performDisconnect(Disconnect command) {
        transport.disconnect();
    }

}
