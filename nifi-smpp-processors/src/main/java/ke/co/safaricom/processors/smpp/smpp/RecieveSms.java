package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.toolbox.Buffer;
import ke.co.safaricom.processors.smpp.toolbox.Message;
import ke.co.safaricom.processors.smpp.logger.Logging;
import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.DefaultComposer;
import org.jsmpp.util.InvalidDeliveryReceiptException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class RecieveSms implements MessageReceiverListener {
    private CreateSmppSession smppSession;
    private Logging logging;
    private static final String DATASM_NOT_IMPLEMENTED= "data_sm not implemented";
    ExecutorService pool = Executors.newFixedThreadPool(20);
    Buffer buffer ;
    //ExecutorService pool = Executors.newCachedThreadPool();



    public RecieveSms(CreateSmppSession session, Logging logging,Buffer buffer){
        this.smppSession=smppSession;
        this.logging=logging;
        this.buffer=buffer;

    }

    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
       // CompletableFuture.supplyAsync(() -> {

        if(MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())){
            //This is a delivery recipt
            try{
                    DeliveryReceipt dr = deliverSm.getShortMessageAsDeliveryReceipt();
                    long id = Long.parseLong(dr.getId()) & 0xffffffff;
                    String messageId = Long.toString(id, 16).toUpperCase();
                    // String doneDate = dr.getDoneDate().toString();
                    // int delivered = dr.getDelivered();
                    // String submittedDate = dr.getSubmitDate().toString();
                    logging.info(String.format(
                            "Del receipt messageId=%s|msisdn=%s|event='Delivery Receipt Received'|da=%s|dsmsg=%s|sms="
                                    + dr.getText(),
                            messageId, deliverSm.getSourceAddr(), deliverSm.getDestAddress(), dr));
                } catch (InvalidDeliveryReceiptException e) {

                }


        }else{
            Date date = new Date();
            SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");


           CompletableFuture<String> msgJsonFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
                @Override
                public String get() {
                    Message serializedMsg= new Message(deliverSm);
                    buffer.put(serializedMsg.toJsonString());
                    return serializedMsg.toJsonString();

                }
            },pool);
            //.thenApplyAsync(jsonResult ->{

              //return jsonResult;
           // });

            logging.info(String.format("Thread ended : %s " ,df.format(date)));


        }
         //   return null;
       // }, pool);

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
