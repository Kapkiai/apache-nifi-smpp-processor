package ke.co.safaricom.processors.smpp.smpp;

import ke.co.safaricom.processors.smpp.logger.Logging;
import ke.co.safaricom.processors.smpp.toolbox.Buffer;
import ke.co.safaricom.processors.smpp.toolbox.ConnectionObj;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SmppClientReceiverRegistry {


    Logging logging;
    List<SmppClientReceiver> smppClientReceivers;
    private int numberOfBindsPerServer;
    private Buffer buffer;
    private Queue<String> smsQueue = null;
    private ExecutorService executor;
    private ConnectionObj connectionParams;
    private int numberOfSmppServers;

    public SmppClientReceiverRegistry(ConnectionObj connectionParams, Queue<String> smsQueue,Logging logging) {
        this.numberOfSmppServers = numberOfSmppServers < 0 ? 1 : connectionParams.getHost().size();
        this.numberOfBindsPerServer = connectionParams.getNumberOfBindsPerServer() < 0 ? 1 : connectionParams.getNumberOfBindsPerServer();
        executor = Executors.newFixedThreadPool( numberOfBindsPerServer*numberOfSmppServers);
        this.smsQueue = smsQueue;
        smppClientReceivers = new ArrayList<>();
        this.connectionParams=connectionParams;
        this.logging=logging;
    }

    public void registerReceivers() {
        logging.info("Registering smpp client receivers for " + numberOfSmppServers + " Servers");
           for(int i =0 ; i <numberOfSmppServers; i++){
               for (int j = 0; j < numberOfBindsPerServer; j++) {
               SmppClientReceiver smppClientReceiver = new SmppClientReceiver(connectionParams.getHost().get(i),connectionParams,
                       logging,smsQueue);
               smppClientReceivers.add(smppClientReceiver);
             }
           }
        logging.info("Registration done for " + smppClientReceivers.size() + " Servers");
    }

    public void startReceivers() {
        logging.info("Starting smpp client receivers");
        smppClientReceivers.forEach(i -> executor.submit(() -> i.start()));
    }

    public void stopReceivers() {
        logging.info("Stopping smpp client receivers");
        smppClientReceivers.forEach(SmppClientReceiver::stop);
    }
    public void  cleanUP(){
        logging.info("Stopping smpp client receivers");
        for (SmppClientReceiver smppReciever: smppClientReceivers
             ) {
            if(smppReciever.isBound()){
                smppReciever.stop();
            }
        }
        logging.info("All SMPP Clients Stopped ");
    }

    public void stopThread() {

        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logging.error("Error interrupting thread");
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
    }

}
