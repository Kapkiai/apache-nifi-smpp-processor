package ke.co.safaricom.processors.smpp.smpp.mock;

import ke.co.safaricom.processors.smpp.SmppClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockSMPPServer {
    @Mock
    SmppClient mockClient = mock(SmppClient.class);

   @Test
    void testShouldSendSms(){
       // Create a class to send SMS and connect to mock server

   }



}
