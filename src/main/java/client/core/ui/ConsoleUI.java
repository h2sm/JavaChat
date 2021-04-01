package client.core.ui;

import client.core.UI;
import client.core.command.Command;
import client.core.command.Connect;
import client.core.command.Disconnect;
import client.core.command.Send;

import java.util.Scanner;

import static util.Logs.log;

public class ConsoleUI implements UI {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public Command getCommand() {
        Command command = null;
        while (command == null) {
            System.out.print("> ");
            String input = scanner.nextLine();
            command = parseCommand(input);

        }
        return command;
    }

    private Command parseCommand(String line) {
        line = line.strip();
        if (line.equals("connect")) {
            return parseConnect(line);
        }
        else if (line.startsWith("send ")) {
            return parseSend(line);
        }
        else if (line.equals("disconnect")) {
            return parseDisconnect(line);
        }
        else {
            System.out.println("unknown command");
            return null;
        }
    }

    private Connect parseConnect(String line) {
        return new Connect();
    }

    private Send parseSend(String line) {
        line = line.substring("send ".length());
        return new Send(line);
    }

    private Disconnect parseDisconnect(String line) {
        return new Disconnect();
    }

    @Override
    public void showMessage(String message) {
        System.out.println(message);
    }


}
