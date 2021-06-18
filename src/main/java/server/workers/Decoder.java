package server.workers;

public class Decoder {
    private final static char GS = 0x1D;
    private static final char RS = 0x1E;

    public static MessageType decodeType(String raw) throws IllegalArgumentException {
        var buff = split(raw);
        MessageType type = null;
        try {
            type = MessageType.get(raw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }

    public static String decodeMessage(String raw) {
        var buff = split(raw);
        return buff[2];
    }

    public static String decodeName(String raw) {
        var buff = split(raw);
        return buff[1];
    }

    public static String decodePassword(String raw) {
        var buff = split(raw);
//        var pass = buff[2];
//        int pointerRS = pass.indexOf(RS);
//        var goodPass = pass.substring(0, pointerRS);
        //var goodPass = deleteChar(buff[2]);
        System.out.println(buff[2]);
        return buff[2];
    }

    private static String[] split(String raw) {
        return raw.split(String.valueOf(GS));
    }

    private static String deleteChar(String raw) {
        int pointerRS = raw.indexOf(RS);
        System.out.println(raw + pointerRS);
        return raw.substring(0,pointerRS);
    }
}
