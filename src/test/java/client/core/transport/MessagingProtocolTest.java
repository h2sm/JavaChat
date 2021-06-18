package client.core.transport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessagingProtocolTest {

    @Test
    void constructRegistration() {
        var msg = "Test";
        var x  = MessagingProtocol.messageSelector(msg, msg);
        System.out.println(x);
    }

    @Test
    void constructMessage() {
    }

    @Test
    void registrationSelector() {
    }

    @Test
    void messageSelector() {
    }
}