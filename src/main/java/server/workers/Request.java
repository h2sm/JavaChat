package server.workers;


public class Request {
    private String name;
    private String message;
    private MessageType messageType;
    private String rawMSG;
    private String password;

    public Request(String rawMessage) {
        this.rawMSG = rawMessage;
        decodeMessage();
    }

    private void decodeMessage() {
        messageType = Decoder.decodeType(rawMSG);
        switch (messageType) {
            case T_MESSAGE -> {
                decodeName();
                decodeText();
            }
            case T_REGISTER -> {
                decodeName();
                decodePassword();
            }
        }
    }

    private void decodeName() {
        this.name = Decoder.decodeName(rawMSG);
    }

    private void decodeText() {
        this.message = Decoder.decodeMessage(rawMSG);
    }

    private void decodePassword() {
        this.password = Decoder.decodePassword(rawMSG);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

}
