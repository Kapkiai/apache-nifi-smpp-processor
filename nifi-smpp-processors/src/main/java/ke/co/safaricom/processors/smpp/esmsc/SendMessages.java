package ke.co.safaricom.processors.smpp.esmsc;

import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.SMPPSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SendMessages {
    private CreateSMPPSession createSession;
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMessages.class);

    public SendMessages(CreateSMPPSession createSession){
        this.createSession=createSession;
    }

    public void broadCastMesseges(){

    }

    public void sendPeerToPeer() {
        String message = "Hello testing ";
        String result;
        SMPPSession session = createSession.initSession();
        final RegisteredDelivery registeredDelivery = new RegisteredDelivery();
        registeredDelivery.setSMSCDeliveryReceipt(SMSCDeliveryReceipt.SUCCESS_FAILURE);
        if(session!=null){
            try {
                result = session.submitShortMessage("",
                        TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, createSession.getSourceAddr(),
                        TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, createSession.getDestinationSdrr(),
                        new ESMClass(), (byte) 0, (byte) 1, null,
                        //TIME_FORMATTER.format(new Date()),
                        null,
                        registeredDelivery,
                        (byte) 0, new GeneralDataCoding(Alphabet.ALPHA_DEFAULT, MessageClass.CLASS1, false), (byte) 0,
                        message.getBytes());
                LOGGER.info("Message  submitted, results {} ", result);
            } catch (PDUException e) {
                // Invalid PDU parameter
                LOGGER.error("Invalid PDU parameter", e);
            } catch (ResponseTimeoutException e) {
                // Response timeout
                LOGGER.error("Response timeout", e);
            } catch (InvalidResponseException e) {
                // Invalid response
                LOGGER.error("Receive invalid response", e);
            } catch (NegativeResponseException e) {
                // Receiving negative response (non-zero command_status)
                LOGGER.error("Receive negative response ", e);
            } catch (IOException e) {
                LOGGER.error("IO error occured", e);
            }
        }


        createSession.sessionClose(session);
    }


}
