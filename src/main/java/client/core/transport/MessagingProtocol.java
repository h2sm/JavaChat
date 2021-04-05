package client.core.transport;

public class MessagingProtocol {
    private String clientName;
    public MessagingProtocol(String name){
        this.clientName = name;
    }

    public String constructRegistration(){
        return "T_REGISTER^]" + clientName+"^^";
    }
    public String constructMessage(String msg){
        return "T_MESSAGE^]" + clientName + "^]" + msg + "^^";
    }
    public String constructFinishing(){return "T_FINISH^]" + clientName + "^^";}
}
