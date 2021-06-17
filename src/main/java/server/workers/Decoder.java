package server.workers;

public class Decoder {
    private final static char GS = 0x1D;
    public static MessageType decodeType(String raw) throws IllegalArgumentException {
        var buff = split(raw);
        MessageType type = null;
        try {
            type = MessageType.get(raw);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return type;
    }
    public static String decodeMessage(String raw){
        var buff = split(raw);
        return buff[2];
    }
    public static String decodeName(String raw){
        var buff = split(raw);
        return buff[1];
    }
    public static String decodePassword(String raw){
        var buff = split(raw);
        return buff[2];
    }
    private static String[] split(String raw){
        return raw.split(String.valueOf(GS));
    }
}
