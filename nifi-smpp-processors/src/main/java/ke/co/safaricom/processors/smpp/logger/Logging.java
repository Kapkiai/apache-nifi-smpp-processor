package ke.co.safaricom.processors.smpp.logger;

import ke.co.safaricom.processors.smpp.smpp.CreateSmppSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logging {
    Logger LOGGER;

    public Logging(Class callingClass){
        LOGGER = LoggerFactory.getLogger(callingClass);
    }

    public void setLOGGER(Logger LOGGER) {
        this.LOGGER = LOGGER;
    }

    public void  info(String message){

       // LOGGER.info(message);
        //System.out.println("INFO : " + message);
    }

    public void error(String message){
        //LOGGER.error(message);
        System.out.println( " ERROR : " + message);
    }

    public void debug(String message){

        //LOGGER.debug(message);
        System.out.println("DEBUG  : " + message);
    }

}
