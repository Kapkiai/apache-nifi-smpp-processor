package ke.co.safaricom.processors.smpp.toolbox;


import ke.co.safaricom.processors.smpp.logger.Logging;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Buffer {
    private Logging logger;
    Queue<String> msgQueue= new ConcurrentLinkedQueue<String>();

    public Buffer(Logging logger) {
        this.logger=logger;
    }

    public void put(String serializedMsg){

        msgQueue.add(serializedMsg);
    }
    public String get(){
        return msgQueue.poll();
    }

    public String check(){
        return msgQueue.peek();
    }
}


