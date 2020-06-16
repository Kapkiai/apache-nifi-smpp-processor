package ke.co.safaricom.processors.smpp.esmsc;

import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CreateSMPPSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateSMPPSession.class);
    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();

    private String host= "smscsim.melroselabs.com";
    private int port = 2775;
    private String systemid="277361";
    private String password= "PQPXWD";
    private String destinationSdrr="99277361";
    private String sourceAddr="MelroseLabs";//"00277361";

    public String getDestinationSdrr() {
        return destinationSdrr;
    }

    public String getSourceAddr() {
        return sourceAddr;
    }

    public SMPPSession initSession(){
        SMPPSession session =new SMPPSession();
        session.setMessageReceiverListener(new MessageReceiverListenerImpl());
        String systemId;

        try {
            systemId =session.connectAndBind(
                    host,
                    port,
                    new BindParameter(BindType.BIND_TRX, systemid, password, null,
                            TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, null)
            );

            LOGGER.info(" Connected with SMPP with system id {}", systemId);

        } catch (IOException e) {
            LOGGER.error(" I/O Error occured ", e);
            session=null;
        }

        return session;
    }

    public void sessionClose(SMPPSession session){
        if(session==null){
            session.unbindAndClose();
        }
    }

}
