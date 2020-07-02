package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.connectionparams.Buffer;
import ke.co.safaricom.processors.smpp.connectionparams.ConnectionObj;
import ke.co.safaricom.processors.smpp.logger.Logging;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;


import java.io.IOException;

public class CreateSmppSession {

    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();

    private SMPPSession session = null;
    private ConnectionObj conParams;
    private Logging logging;
    private Buffer buffer;

    public CreateSmppSession(ConnectionObj conParams, Logging logging,Buffer buffer){
        this.conParams=conParams;
        this.logging=logging;
        this.buffer=buffer;
    }
    public SMPPSession getExistingSession(){
        return session;
    }

    private SMPPSession initSession() throws IOException{
        SMPPSession session =new SMPPSession();
        session.setEnquireLinkTimer(30000);
        session.setTransactionTimer(2000);
        session.setMessageReceiverListener(new RecieveSms(this,logging,buffer));
        session.addSessionStateListener(new StateChange(this,logging));
        logging.info("Creating a session");
        return session;
    }

    private SMPPSession getSession() throws IOException{
        if(session==null){
            session=initSession() ;
            logging.info("Initiate session for the first time session to {}:{} " + " , "+ conParams.getHost()+"," +conParams.getPort());
            return session;
        }else if(session.getSessionState().isBound()){
            logging.info("No session yet ");
        }
        return null;
    }


    public SMPPSession create(){
        try {
            session = getSession();
        } catch (IOException e) {

        }
         try {
             if(!session.getSessionState().isBound()){
                session.connectAndBind(
                        conParams.getHost(),
                        conParams.getPort(),
                        new BindParameter(BindType.BIND_RX, conParams.getSystemid(),
                                conParams.getPassword(), conParams.getSystemType(), TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN,
                                conParams.getAddressRange()));

                 logging.info("Connected with SMPP with system id {} " + "," + session.getSessionId());

               }
                return session;

            } catch (IOException e) {
                logging.error("I/O Error occured " + "," + e);
                try {
                    closeSession();
                } catch (IOException ioException) {
                    logging.error("Error closing the session");
                }
                return null;
            }

        }

    public void closeSession() throws IOException {
        if(session!=null){
            logging.info("Closing existing session  ...");
            session.unbindAndClose();
            //getSession().unbindAndClose();
        }
    }

}
