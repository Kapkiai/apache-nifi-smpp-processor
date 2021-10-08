package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.toolbox.Buffer;
import ke.co.safaricom.processors.smpp.toolbox.ConnectionObj;
import ke.co.safaricom.processors.smpp.logger.Logging;
import ke.co.safaricom.processors.smpp.toolbox.Message;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.*;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.DefaultComposer;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.jsmpp.util.TimeFormatter;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.apache.nifi.logging.ComponentLog;

public class CreateSmppSession {

    private static final TimeFormatter TIME_FORMATTER = new AbsoluteTimeFormatter();
    ExecutorService pool = Executors.newFixedThreadPool(20);
    //Buffer buffer ;
    //ExecutorService pool = Executors.newCachedThreadPool();


    private SMPPSession session = null;
    private ConnectionObj conParams;
    private Logging logging;
    private Buffer buffer;
    private Queue<String> smsQueue=null;
    private SessionStateListener sessionStateListener;
    private String host;
    private int bindAttempt=0;

    public CreateSmppSession(String host, ConnectionObj conParams, Logging logging, Queue<String> smsQueue, SessionStateListener sessionStateListener){
        this.conParams=conParams;
        this.host=host;
        this.logging=logging;
        this.smsQueue=smsQueue;
        this.sessionStateListener = sessionStateListener;
    }
    public SMPPSession getExistingSession(){
        return session;
    }

    public void initBindSession(){
        logging.info(String.format("Trying to connect and bind to host > %s : %s", this.host, conParams.getPort()));
        session =new SMPPSession();
        session.setEnquireLinkTimer(30000);
        //session.setTransactionTimer(2000);
        //session.setMessageReceiverListener(new RecieveSms(this,logging,buffer));
        session.addSessionStateListener(sessionStateListener);
        initSMSReceiver();
        logging.info("Creating a session");

        try {
            session.connectAndBind(
                    this.host,
                    conParams.getPort(),
                    new BindParameter(BindType.BIND_RX, conParams.getSystemid(),
                            conParams.getPassword(), conParams.getSystemType(), TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.ISDN,
                            conParams.getAddressRange()));

            logging.info(String.format("Success: connecting and binding to host > %s : %s", this.host, conParams.getPort()));

        } catch (IOException e) {
            if(bindAttempt<conParams.getBindAttempt()){
                //Cleaning up the bind in preparation for a rebind
                disconnectAndUnbind();
                try {
                    Thread.sleep(conParams.getBindAttemptSleepTime());
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                logging.error(String.format("Error binding to %s:%s trying to rebind attempt %s", this.host, conParams.getPort(),bindAttempt));
                //Creating a new bind
                initBindSession();
                bindAttempt++;
            }else{
                logging.error(String.format("Error: connecting and binding to host > %s : %s", this.host, conParams.getPort()));

            }
        }
    }



    public Boolean isBound(){
        return session.getSessionState().isBound();
    }



    private void initSMSReceiver() {
        // Receive Message
        // Set listener to receive deliver_sm
        session.setMessageReceiverListener(new MessageReceiverListener() {


            @Override
            public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
                return null;
            }

            @Override
            public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
              //CompletableFuture.supplyAsync(() -> {

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
                        smsQueue.add(new Message(deliverSm).toJsonString());

                       /* CompletableFuture<String> msgJsonFuture = CompletableFuture.supplyAsync(new Supplier<String>() {
                            @Override
                            public String get() {
                                Message serializedMsg= new Message(deliverSm);
                                buffer.put(serializedMsg.toJsonString());
                                return serializedMsg.toJsonString();

                            }
                        },pool);*/
                        //.thenApplyAsync(jsonResult ->{

                        //return jsonResult;
                        // });

                        //logging.info(String.format("Thread ended : %s " ,df.format(date)));


                    }
               //  return null;
              //  }, pool);
            }



            @Override
            public void onAcceptAlertNotification(AlertNotification alertNotification) {
                // Default implementation is acceptable
            }

        });
    }

    public void disconnectAndUnbind() {
        logging.info(String.format("Closing existing session %s  ..." , session.getSessionId()));
        session.unbindAndClose();
    }

    public void closeSession()  {
        if(session!=null){
            logging.info(String.format("Closing existing session  ..."));
            session.unbindAndClose();

        }
    }

}
