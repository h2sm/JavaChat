package client.core.transport;

public class MessagingProtocol {
    private static final char GS = 0x1D;
    private static final char RS = 0x1E;

    public static char[] registrationSocket(String name, String pass) {
        String output = "T_REGISTER" + GS + name + GS + pass + RS;
        return output.toCharArray();
    }

    public static char[] messageSocket(String name, String msg) {
        String output = "T_MESSAGE" + GS + name + GS + msg + RS;
        return output.toCharArray();
    }

    public static byte[] registrationSelector(String clientName, String password) {
        var out = "T_REGISTER" + GS + clientName + GS + password + RS;
        return out.getBytes();
    }

    public static byte[] messageSelector(String clientName, String msg) {
        String str = "T_MESSAGE" + GS + clientName + GS + msg + RS;
        return str.getBytes();
    }
//    public static String finishSelector(String msg){
//        return "T_FINISH" + GS + clientName+RS;
//    }
    //public static String constructFinishing(){return "T_FINISH" +GS + clientName + RS;}

}
