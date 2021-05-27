package server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Parser {
    private final char GS = 0x1D;
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    public String parse(String pack) {
        var date = new Date();
        var buff = pack.split(String.valueOf(GS)); //Arrays.toString(<>)
        var type = buff[0];
        if (type.equals("T_REGISTER"))
            return "(" + formatter.format(date) + ")" + " New connected user " + buff[1];
        if (type.equals("T_MESSAGE"))
            return "(" + formatter.format(date) + ") " + buff[1] + " says: " + buff[2];
        return "Damn an L.";
    }

}
