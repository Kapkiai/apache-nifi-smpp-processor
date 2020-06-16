package ke.co.safaricom.processors.smpp.esmsc;

import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.*;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageReceiverListenerImpl implements MessageReceiverListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiverListenerImpl.class);
    private static final String DATASM_NOT_IMPLEMENTED= "data_sm not implemented";



    @Override
    public void onAcceptDeliverSm(DeliverSm deliverSm) throws ProcessRequestException {
        LOGGER.info("getting here");
        if(MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())){
            try {
                DeliveryReceipt delReciept =deliverSm.getShortMessageAsDeliveryReceipt();
                long id = Long.parseLong(delReciept.getId()) &  0xffffffff;
                String messageId =Long.toString(id,16).toUpperCase();
                LOGGER.info(" Recieving delivery receipt for message '{}' from {} to {}: {} ",
                        messageId,deliverSm.getSourceAddr(), deliverSm.getDestAddress(),delReciept);
            } catch (InvalidDeliveryReceiptException e) {
                LOGGER.error("Failed getting delivery receipt " , e);
            }
        }

    }

    @Override
    public void onAcceptAlertNotification(AlertNotification alertNotification) {
        LOGGER.info(" AlertNotification not implemented ");

    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session session) throws ProcessRequestException {
        LOGGER.info("Datasm not implemented ");
        throw new ProcessRequestException(DATASM_NOT_IMPLEMENTED, SMPPConstant.STAT_ESME_RINVCMDID);
    }
}
