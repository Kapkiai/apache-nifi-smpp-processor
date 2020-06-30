package ke.co.safaricom.processors.smpp.smpp.mock;

import ke.co.safaricom.processors.smpp.GetSmpp;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;

public class MockSMPPServer {
    @Mock
    GetSmpp mockClient = mock(GetSmpp.class);

   @Test
    void testShouldSendSms(){
       // Create a class to send SMS and connect to mock server

   }



}
