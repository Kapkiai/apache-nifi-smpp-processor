package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.connectionparams.Message;
import ke.co.safaricom.processors.smpp.logger.Logging;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;

import java.util.concurrent.*;

public class RecieveSms implements MessageReceiverListener {
    private CreateSmppSession smppSession;
    private Logging logging;
    private static final String DATASM_NOT_IMPLEMENTED= "data_sm not implemented";
    private Message msgObj = null;
    ExecutorService pool = Executors.newFixedThreadPool(10);



    public RecieveSms(CreateSmppSession session, Logging logging){
        this.smppSession=smppSession;
        this.logging=logging;
        this.msgObj = new Message();


    }

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        if(MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())){
            //This is a delivery recipt

        }else{
            msgObj.setMsgContent(new String(deliverSm.getShortMessage()));
            msgObj.setMsgSource(deliverSm.getSourceAddr());
            msgObj.setMsgDestination(deliverSm.getDestAddress());

            // This is a normal short message
            logging.info("\" Recieving SMS \"" + ", " + msgObj.toJsonString());
            Future<String > msgResult = pool.submit(new ProcessMessages(deliverSm));

            try {
                String msgJson = msgResult.get(10, TimeUnit.MILLISECONDS);
                logging.info(" Data returned " + msgJson);
            } catch (InterruptedException e) {
                logging.error("Thread pool interruption : " + e);
                msgResult.cancel(true);
            } catch (ExecutionException e) {
                logging.error("Thread pool Execution Exception : " + e);
                msgResult.cancel(true);
            } catch (TimeoutException e) {
                logging.error("Thread pool Timeout Exception : " + e);

            }


        }

    }

    @Override
    public void onAcceptAlertNotification(AlertNotification alertNotification) {
        logging.info(" Alert Notification not implemented ");
    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {

        logging.info(" Not implemented ");
        throw new ProcessRequestException(DATASM_NOT_IMPLEMENTED, SMPPConstant.STAT_ESME_RINVCMDID);
    }
}
