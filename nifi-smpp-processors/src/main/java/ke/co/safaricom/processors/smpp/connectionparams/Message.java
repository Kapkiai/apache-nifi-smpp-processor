package ke.co.safaricom.processors.smpp.connectionparams;



public class Message {
    private String msgSource;
    private String msgDestination;
    private String msgContent;

    public void setMsgSource(String msgSource) {
        this.msgSource = msgSource;
    }

    public void setMsgDestination(String msgDestination) {
        this.msgDestination = msgDestination;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
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

