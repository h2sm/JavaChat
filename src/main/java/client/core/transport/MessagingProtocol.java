package client.core.transport;

import java.nio.charset.Charset;

public class MessagingProtocol {
    private static final char GS = 0x1D;
    private static final char RS = 0x1E;
    private String clientName;

    public MessagingProtocol(String name){
        this.clientName = name;
    }

    public char[] constructRegistration(){
        String output = "T_REGISTER" + GS + clientName + RS;
        char[] arr = output.toCharArray();
        return arr;
        //return "T_REGISTER" + GS + clientName + RS;
    }
    public char[] constructMessage(String msg){
        String output = "T_MESSAGE"+ GS + clientName + GS + msg + RS;
        char[] arr = output.toCharArray();
        return arr;
        //return "T_MESSAGE"+ GS + clientName + GS + msg + RS;
    }
    public String constructFinishing(){return "T_FINISH" +GS + clientName + RS;}
}
