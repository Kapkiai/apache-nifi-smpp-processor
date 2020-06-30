package ke.co.safaricom.processors.smpp.connectionparams;


import org.jsmpp.bean.DeliverSm;

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

    public String getMsgDestination() {
        return msgDestination;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String toJsonString (){
        return "{\"source\":\""+ getMsgSource() +"\"," +
                " \"recipient\":\"" + getMsgDestination() + "\", " +
                " \"message\":\"" + getMsgContent() +"\"" +
                "}";
    }
}