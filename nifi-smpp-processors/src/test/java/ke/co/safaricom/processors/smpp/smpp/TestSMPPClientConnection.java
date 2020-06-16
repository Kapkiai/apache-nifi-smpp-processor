package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.esmsc.CreateSMPPSession;
import ke.co.safaricom.processors.smpp.esmsc.RecieveSms;
import ke.co.safaricom.processors.smpp.esmsc.SendMessages;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestSMPPClientConnection {
    public static SendMessages sendMessage;
    public static CreateSMPPSession session;


    @BeforeAll
    static void setup(){
        session = new CreateSMPPSession();
        sendMessage =new SendMessages(session);
    }

    @Test
    void TestConnectionToSMSC(){
        sendMessage.sendPeerToPeer();
    }

}
