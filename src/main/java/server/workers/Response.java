package server.workers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Response {
    private static char GS = 0x1D;

    public static String returnResponse(Request request) {
        var formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        var date = new Date();
        switch (request.getMessageType()){
            case T_MESSAGE -> {
                var msg = "(" + formatter.format(date) + ") " + request.getName()+ " says: " + request.getMessage();
                return msg;
            }
            case T_REGISTER -> {
                var msg = "(" + formatter.format(date) + ") New connected user " + request.getName();
                return msg;
            }
        }
        return "ERROR";
    }
}
