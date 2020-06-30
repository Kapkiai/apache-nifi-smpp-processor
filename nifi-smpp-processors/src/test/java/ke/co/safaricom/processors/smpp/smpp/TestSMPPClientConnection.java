package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.connectionparams.ConnectionObj;
import ke.co.safaricom.processors.smpp.logger.Logging;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestSMPPClientConnection {

    public static CreateSmppSession smppSession;
    public static ConnectionObj conParams;
    public static Logging logging;


    private String host= "smscsim.melroselabs.com";
    private int port = 2775;
    private String systemid="277361";
    private String password= "PQPXWD";
    private String destinationSdrr="99277361";
    private String sourceAddr="MelroseLabs";//"00277361";
    private String systemType = "smpp";

    @BeforeAll
    static void setup(){

        conParams = new ConnectionObj();
        //logging = new Logging(this.getLogger());

    }

    @Test
    void TestConnectionToSMSC(){

        conParams.setHost(host);
        conParams.setPort(port);
        conParams.setPassword(password);
        conParams.setSystemType(systemType);
        conParams.setAddressRange(null);
        conParams.setSystemid(systemid);
        //smppSession = new CreateSmppSession(conParams,logging);
        //smppSession.create();
    }

}
