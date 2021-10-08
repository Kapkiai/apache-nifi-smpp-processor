package ke.co.safaricom.processors.smpp.toolbox;

import java.util.List;

public class ConnectionObj {
    private List<String> host;
    private int port;
    private String systemType;
    private String systemid;
    private String password;
    private String addressRange;
    private int numberOfBindsPerServer;
    private int bindAttemptSleepTime;
    private int bindAttempt;

    public String getAddressRange() {
        return addressRange;
    }

    public void setAddressRange(String addressRange) {
        this.addressRange = addressRange;
    }

    public List<String> getHost() {
        return host;
    }

    public void setHost(List<String> host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public String getSystemid() {
        return systemid;
    }

    public void setSystemid(String systemid) {
        this.systemid = systemid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNumberOfBindsPerServer(int numberOfBindsPerServer) {
        this.numberOfBindsPerServer=numberOfBindsPerServer;
    }

    public int getNumberOfBindsPerServer() {
        return numberOfBindsPerServer;
    }

    public void setBindAttempt(int bindAttempt) {
        this.bindAttempt=bindAttempt;
    }

    public void setBindAttemptSleepTime(int bindAttemptSleepTime) {
        this.bindAttemptSleepTime=bindAttemptSleepTime;
    }

    public int getBindAttemptSleepTime() {
        return bindAttemptSleepTime;
    }

    public int getBindAttempt() {
        return bindAttempt;
    }
}
