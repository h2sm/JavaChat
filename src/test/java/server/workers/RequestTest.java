package server.workers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;


class RequestTest {
    private final char GS = 0x1D;
    private final char RS = 0x1E;

    @Test
    void init(){

    }

    @Test
    void getMessageType() {
        String rawMSG = "T_REGISTER" + GS + "EGOR" + GS + "EGOR";
        var request = new Request(rawMSG);
        var x = request.getMessageType();
        assertThat(x).isEqualTo(MessageType.T_REGISTER);
        getName(rawMSG);
        getPassword(rawMSG);
    }

    @Test
    void getMessage() {
        var rawMSG = "T_MESSAGE"+ GS + "clientName" + GS + "msg";
        var req = new Request(rawMSG).getMessage();
        assertThat(req).isEqualTo("msg");
    }

    @Test
    void getName(String rawmsg) {
        var name = Decoder.decodeName(rawmsg);
        assertThat(name).isEqualTo("EGOR");
    }

    @Test
    void getPassword(String rawmsg) {
        var pass = Decoder.decodePassword(rawmsg);
        assertThat(pass).isEqualTo("EGOR");
    }
}