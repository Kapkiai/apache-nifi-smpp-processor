package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.GetSmpp;
import ke.co.safaricom.processors.smpp.logger.Logging;
import ke.co.safaricom.processors.smpp.toolbox.Buffer;
import ke.co.safaricom.processors.smpp.toolbox.ConnectionObj;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;

import java.util.Queue;


public class SmppClientReceiver implements SessionStateListener {

    CreateSmppSession smppClient;
    Buffer buffer;
    private Queue<String> smsQueue = null;
    private boolean stop;
    private Logging logging;
    private int retryCount;
    private volatile boolean retrying;

    public SmppClientReceiver(String host, ConnectionObj connectionParams,Logging logging,Queue<String> smsQueue) {
        smppClient = new CreateSmppSession( host, connectionParams, logging, smsQueue,this);
        this.smsQueue = smsQueue;
        this.logging=logging;

    }

    public void stop() {
        logging.info("Stopping smpp Client Receiver");
        stop = true;
        unbind();
        logging.info("Stopped smpp Client Receiver");
    }

    private void bind() {

        smppClient.initBindSession();

    }

    private void unbind() {
        smppClient.disconnectAndUnbind();
    }

    public boolean isBound() {
        return smppClient.isBound();
    }

    public boolean isStopped() {
        return stop;
    }

    public void start() {
        bind();
    }

    @Override
    public void onStateChange(SessionState newState, SessionState oldState, Session source) {
        logging.info(String.format("oldState=%s|newState=%s", oldState, newState));
        logging.info(String.format("isProducerRunning=%s|isBound=%s|isStopped=%s", GetSmpp.isProducerRunning(), isBound() , isStopped()));

        synchronized (this) {
            if (retrying) {
                logging.info("Retrying attempt already in progress");
                return;
            }
            retrying = true;
        }

        if (oldState.isBound() && (newState == SessionState.UNBOUND || newState == SessionState.CLOSED)
                && !isStopped()) {
            logging.info("Checking state change");
            unbind();
            while (GetSmpp.isProducerRunning() && !isBound() && !isStopped()) {
                retryCount++;
                try {
                    logging.info("Sleeping before retry attempt=" + retryCount);
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                logging.info(String.format("Retrying to bind attempt : %s", retryCount));

                bind();
            }
            logging.info(String.format("Success after retrying to bind attempt : %s", retryCount));
            retryCount = 0;
        }
        retrying = false;

    }

}
