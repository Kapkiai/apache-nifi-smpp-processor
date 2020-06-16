package ke.co.safaricom.processors.smpp.esmsc;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.*;
import org.jsmpp.util.InvalidDeliveryReceiptException;

import java.io.IOException;

public class RecieveSms {
    private CreateSMPPSession session;
    private MessageReceiverListenerImpl receiverListener;

    public RecieveSms(CreateSMPPSession session,MessageReceiverListenerImpl receiverListener){
        this.session=session;
        this.receiverListener=receiverListener;
    }

public void getSMS(){
   //receiverListener.onAcceptDeliverSm();

}



}
