package server.workers;

public enum MessageType {
    T_REGISTER, T_MESSAGE;

    public static MessageType get(String msg) throws Exception {
        if (msg.contains("T_REGISTER")) return T_REGISTER;
        else if (msg.contains("T_MESSAGE")) return T_MESSAGE;
        else {
            throw new Exception("No Mathcing type of message");
        }
    }
}
