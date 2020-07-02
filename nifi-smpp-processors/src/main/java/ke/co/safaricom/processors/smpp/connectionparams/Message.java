package ke.co.safaricom.processors.smpp.connectionparams;


import org.jsmpp.bean.DeliverSm;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private String msgSource;
    private String msgDestination;
    private String msgContent;

    public Message(DeliverSm deliverSm){
        this.msgContent= new String(deliverSm.getShortMessage());
        this.msgSource=deliverSm.getSourceAddr();
        this.msgDestination=deliverSm.getDestAddress();

    }
    public String getMsgSource() {
        return msgSource;
    }

    public String getDate() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        return df.format(date);
    }

    public String getMsgDestination() {
        return msgDestination;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String toJsonString (){
        return "{\"timestamp \":\""+ getDate() + "\"," +
                "\"source\":\""+ getMsgSource() +"\"," +
                " \"recipient\":\"" + getMsgDestination() + "\", " +
                " \"message\":\"" + getMsgContent() +"\"" +
                "}";
    }
}