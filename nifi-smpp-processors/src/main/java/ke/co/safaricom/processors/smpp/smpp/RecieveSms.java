package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.connectionparams.Buffer;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class RecieveSms implements MessageReceiverListener {
    private CreateSmppSession smppSession;
    private Logging logging;
    private static final String DATASM_NOT_IMPLEMENTED= "data_sm not implemented";
    //ExecutorService pool = Executors.newFixedThreadPool(10);
    Buffer buffer ;
    ExecutorService pool = Executors.newCachedThreadPool();



    public RecieveSms(CreateSmppSession session, Logging logging,Buffer buffer){
        this.smppSession=smppSession;
        this.logging=logging;
        this.buffer=buffer;

    }

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        if(MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())){
            //This is a delivery recipt

        }else{
            logging.info("\"Recieving SMS \"" + ", " + new String(deliverSm.getShortMessage()));
            logging.info("Starting  thread ...  ");
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");
            logging.info("Initialization time for task " + df.format(date));

            CompletableFuture<String> msgJsonFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
                @Override
                public String get() {
                    Message serializeMsg= new Message(deliverSm);
                    return serializeMsg.toJsonString();
                }
            },pool);

            CompletableFuture<String> resultJsonFuture = msgJsonFuture.thenApply(jsonResult ->{
                logging.info("we have received Result json  :  \n " + jsonResult);
                return jsonResult;
            });

            try {
                buffer.put(resultJsonFuture.get());

                // logging.info(" We have the result finally ...  \n  " + resultJsonFuture.get());
            } catch (InterruptedException e) {
                logging.error("Interrupted Exception : " + e);
            } catch (ExecutionException e) {
                logging.error("Execution Exception : " + e);
            }
            logging.info("End of task time  " + df.format(date));
            logging.info("Thread Completed");
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
