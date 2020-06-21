package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.logger.Logging;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;

public class StateChange implements SessionStateListener {

    private long reconnectInterval=3000;
    private Logging logging;
    private CreateSmppSession smppSession;



    public StateChange(CreateSmppSession smppSession ,Logging logging) {
        this.smppSession=smppSession;
        this.logging=logging;
    }

    private void reconnectAfter(long timeMillis){
        new Thread(){
            @Override
            public void run() {
                logging.info("\"Scheduling reconnect after {} millis \"" + timeMillis );
                try {
                    Thread.sleep(timeMillis);
                } catch (InterruptedException e) {
                    logging.error(" \" Something went wrong when trying to reconnect \" " + e);
                }

                int attempt = 0;
                while (smppSession.getExistingSession() == null || smppSession.getExistingSession().getSessionState().equals(SessionState.CLOSED)){
                    logging.info(" \" Reconnecting attempt #{} ... \"" + "," + ++attempt);
                    smppSession.create();
                    // Wait for sec
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }

                }

            }
        }.start();
    }

    @Override
    public void onStateChange(SessionState newSate, SessionState oldState, Session session) {
        reconnectAfter(reconnectInterval);
    }
}
