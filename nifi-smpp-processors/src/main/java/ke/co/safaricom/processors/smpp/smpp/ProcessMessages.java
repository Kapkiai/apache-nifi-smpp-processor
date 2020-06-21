package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.connectionparams.Message;
import ke.co.safaricom.processors.smpp.logger.Logging;

import org.jsmpp.bean.DeliverSm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class ProcessMessages implements Callable<String> {

    private Logging logging;
    private Message msgObj;
    public ProcessMessages(Logging logging) {
        this.logging=logging;
    }

    public ProcessMessages(DeliverSm deliverSm) {
        msgObj=new Message();
        msgObj.setMsgContent(new String(deliverSm.getShortMessage()));
        msgObj.setMsgSource(deliverSm.getSourceAddr());
        msgObj.setMsgDestination(deliverSm.getDestAddress());

    }

    public String  getMsgAsJsonString(){
        logging.info("\" Recieving SMS \"" + ", " + msgObj.toJsonString());
        return msgObj.toJsonString();
    }


    @Override
    public String call() throws Exception {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
        logging.info("Initialization time for task " + df.format(date));
        String json= getMsgAsJsonString();
        logging.info("End of task time  " + df.format(date));
        return json;
    }
}

