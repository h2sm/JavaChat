package server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Parser {
    private final char GS = 0x1D;
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private String name;
    private String message;
    private String password;

    public String parse(String pack) {
        var date = new Date();
        var buff = pack.split(String.valueOf(GS)); //Arrays.toString(<>)
        var type = buff[0];//тип сообщения
        name = buff[1];
        if (type.equals("T_REGISTER")) {
            password = buff[2];
            return "(" + formatter.format(date) + ")" + " New connected user " + buff[1];
        }
        if (type.equals("T_MESSAGE")) {
            message = buff[2];
            return "(" + formatter.format(date) + ") " + buff[1] + " says: " + buff[2];
        }
        return "Damn an L.";
    }

    public String getName() {
        if (name!=null)
            return name;
        return null;
    }

    public String getMessage() {
        if (message != null) {
            return message;
        }
        return null;
    }
    public String getPassword(){
        if (password!=null)
            return password;
        return null;
    }
}
