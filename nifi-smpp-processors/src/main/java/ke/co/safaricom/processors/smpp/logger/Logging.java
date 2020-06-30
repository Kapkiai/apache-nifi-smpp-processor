package ke.co.safaricom.processors.smpp.logger;


import org.apache.nifi.logging.ComponentLog;



public class Logging {
    ComponentLog LOGGER;

    public Logging(ComponentLog logger){
        LOGGER = logger;
    }

    public void  info(String message){
       LOGGER.info(message);
       //System.out.println("INFO : " + message);
    }

    public void error(String message){
        LOGGER.error(message);
        //System.out.println( " ERROR : " + message);
    }

    public void debug(String message){
        LOGGER.debug(message);
       // System.out.println("DEBUG  : " + message);
    }

}
