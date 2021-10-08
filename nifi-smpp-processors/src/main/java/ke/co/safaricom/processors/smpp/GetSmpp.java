/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ke.co.safaricom.processors.smpp;

import ke.co.safaricom.processors.smpp.smpp.SmppClientReceiver;
import ke.co.safaricom.processors.smpp.smpp.SmppClientReceiverRegistry;
import ke.co.safaricom.processors.smpp.toolbox.Buffer;
import ke.co.safaricom.processors.smpp.toolbox.ConnectionObj;
import ke.co.safaricom.processors.smpp.logger.Logging;
import ke.co.safaricom.processors.smpp.smpp.CreateSmppSession;
import ke.co.safaricom.processors.smpp.toolbox.Message;
import org.apache.nifi.annotation.lifecycle.OnShutdown;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.annotation.lifecycle.OnUnscheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.jsmpp.session.SMPPSession;
import sun.util.logging.resources.logging;
import org.apache.nifi.logging.ComponentLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;


@Tags({"smpp client", "smpp" , " smpp processor"})
@CapabilityDescription("Smpp client for receiving messages from SMSC")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class GetSmpp extends AbstractProcessor {

    private static String NULL="NULL";
    public static final PropertyDescriptor PROP_SMSC_HOST = new PropertyDescriptor
            .Builder().name("SMSC host")
            .displayName("Host")
            .description("SMSC hosts to bind to comma separated lists of IPS")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();


    public static final PropertyDescriptor PROP_SMSC_PORT = new PropertyDescriptor
            .Builder().name("SMSC PORT")
            .displayName("Port")
            .description("SMSC host port to bind to. The ports should be the same across the list of IPs ")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROP_SMSC_PASSWORD = new PropertyDescriptor
            .Builder().name("Password")
            .displayName("Password")
            .description("Password for authentication")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();


    public static final PropertyDescriptor PROP_SMSC_SYSTEM_TYPE = new PropertyDescriptor
            .Builder().name("System type")
            .displayName("System type")
            .description("SMSC System type ")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .defaultValue(NULL)
            .build();

    public static final PropertyDescriptor PROP_SMSC_SYSTEM_ID = new PropertyDescriptor
            .Builder().name("System id")
            .displayName("System Id")
            .description("SMSC System id ")
            .required(true)
            .defaultValue(NULL)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROP_SMSC_ADDRESS_RANGE = new PropertyDescriptor
            .Builder().name("Address range")
            .displayName("Address range")
            .description("SMSC address range")
            .required(true)
            .defaultValue(NULL)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROP_SMSC_NO_OF_BINDS = new PropertyDescriptor
            .Builder().name("Number of Binds/IP")
            .displayName("Number of binds")
            .description("Number of binds to bind to each SMSC IP from each Smpp client IP")
            .required(true)
            .defaultValue("1")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROP_FLOWFILE_BATCH_SIZE = new PropertyDescriptor
            .Builder().name("Batch count")
            .displayName("Batch Count")
            .description("Flowfile Batch Size")
            .required(true)
            .defaultValue("100")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROP_SMSC_CONN_BIND_ATTEMPT = new PropertyDescriptor
            .Builder().name("No Attempt for failed bind")
            .displayName("Number of rebind attempt for failed bind")
            .description("Number of rebind attempt to try if there is a binding issues ")
            .required(true)
            .defaultValue("10")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PROP_SMSC_CONN_BIND_ATTEMPT_SLEEP_TIME = new PropertyDescriptor
            .Builder().name("Sleep time before retrying")
            .displayName("Sleep time attempt ")
            .description("Sleep time before making another retry attempt to rebind")
            .required(true)
            .defaultValue("10000")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully started receiving data from SMSC")
            .build();

    public static final Relationship REL_FAIL = new Relationship.Builder()
            .name("failure")
            .description("Failed to bind or receive data")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private SMPPSession newSmppSession =null;
    private CreateSmppSession smppSession=null;
    private Logging logger =null ;
    private ConnectionObj connectionParams = null;
    private Buffer buffer=null;
    private Queue<String> smsQueue = null;
    private  SMPPSession[] binds = new SMPPSession[4];
    private ExecutorService pool;
    private static volatile boolean producerRunning;
    private SmppClientReceiverRegistry smppClientReceiverRegistry;
    private int counter=0;



    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(PROP_SMSC_HOST);
        descriptors.add(PROP_SMSC_PORT);
        descriptors.add(PROP_SMSC_PASSWORD);
        descriptors.add(PROP_SMSC_SYSTEM_TYPE);
        descriptors.add(PROP_SMSC_ADDRESS_RANGE);
        descriptors.add(PROP_SMSC_SYSTEM_ID);
        descriptors.add(PROP_SMSC_NO_OF_BINDS);
        descriptors.add(PROP_FLOWFILE_BATCH_SIZE);
        descriptors.add(PROP_SMSC_CONN_BIND_ATTEMPT);
        descriptors.add(PROP_SMSC_CONN_BIND_ATTEMPT_SLEEP_TIME);

        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAIL);
        this.relationships = Collections.unmodifiableSet(relationships);
        if(logger==null){
            logger=new Logging(this.getLogger());
        }
        if(buffer==null){
            buffer=new Buffer(logger);
        }
        if(smsQueue==null){
            smsQueue=new ConcurrentLinkedQueue<String>();
        }
        setProducerRunning(false);


    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }


    public static void setProducerRunning(boolean producerRunning) {
        GetSmpp.producerRunning = producerRunning;
    }

    public static boolean isProducerRunning() {
        return GetSmpp.producerRunning;
    }







    private void startSessionAndBind(
            List<String> host, int port, String password, String addressRange, String systemType, String systemid,
            int numberOfBindsPerServer,int bindAttempt,int bindAttemptSleepTime
    ){
        /*
         TODO: Implementation of SMPP connection and data fetch
         */
        //Gson gson = new Gson();
        connectionParams = new ConnectionObj();
        connectionParams.setHost(host);
        connectionParams.setPort(port);
        connectionParams.setPassword(password);
        connectionParams.setSystemType(systemType);
        connectionParams.setAddressRange(addressRange);
        connectionParams.setSystemid(systemid);
        connectionParams.setNumberOfBindsPerServer(numberOfBindsPerServer);
        connectionParams.setBindAttempt(bindAttempt);
        connectionParams.setBindAttemptSleepTime(bindAttemptSleepTime);
        //logger=new Logging(this.getLogger());
        if(!isProducerRunning()){
            logger.info("Start server bind sequence");
             smppClientReceiverRegistry = new SmppClientReceiverRegistry(connectionParams,
                     smsQueue,logger);
            smppClientReceiverRegistry.registerReceivers();
            smppClientReceiverRegistry.startReceivers();
            setProducerRunning(true);
            logger.info("Finished bind sequence");

        }

    }



    @OnUnscheduled
    public void unScheduled(){
        setProducerRunning(false);
        smppClientReceiverRegistry.cleanUP();
        smppClientReceiverRegistry.stopThread();
    }
    @OnStopped
    public void stop() {
        // Close and unbind the connections

        setProducerRunning(false);
        smppClientReceiverRegistry.cleanUP();
        smppClientReceiverRegistry.stopThread();

    }
    @OnShutdown
    public void shutdown(){
        setProducerRunning(false);
        smppClientReceiverRegistry.cleanUP();
        smppClientReceiverRegistry.stopThread();

    }
    private void writeFlowfile(final ProcessSession session, String msgOutput){
        FlowFile smsFlowFile = session.create();
        //logger.info(String.format("Writting data to a flowfile  ..."));
        try {

            //List<String> finalMsgOutput = msgOutput;
            String finalMsgOutput = msgOutput;
            smsFlowFile = session.write(smsFlowFile, outputStream -> {
                outputStream.write(finalMsgOutput.getBytes(StandardCharsets.UTF_8));
            });

        } catch (ProcessException e) {
            session.transfer(smsFlowFile, REL_FAIL);
            logger.info(String.format("Process Exception %s ", e));
        }
        session.transfer(smsFlowFile, REL_SUCCESS);
    }



    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        FlowFile msgFlowFile = null;// session.create();
        // TODO implement

        List <String> hostList = Arrays.asList(context.getProperty(PROP_SMSC_HOST).getValue().split(","));
        int port = context.getProperty(PROP_SMSC_PORT).asInteger();
        String password = context.getProperty(PROP_SMSC_PASSWORD).getValue();
        String systemType = context.getProperty(PROP_SMSC_SYSTEM_TYPE).getValue();
        String addressRange = context.getProperty(PROP_SMSC_ADDRESS_RANGE).getValue();
        String systemid = context.getProperty(PROP_SMSC_SYSTEM_ID).getValue();
        int numberOfBindsPerServer = context.getProperty(PROP_SMSC_NO_OF_BINDS).asInteger();
        int flowfileBatchSize = context.getProperty(PROP_FLOWFILE_BATCH_SIZE).asInteger();
        int bindAttempts = context.getProperty(PROP_SMSC_CONN_BIND_ATTEMPT).asInteger();
        int bindAttemptSleepTime = context.getProperty(PROP_SMSC_CONN_BIND_ATTEMPT_SLEEP_TIME).asInteger();
        counter=2;
        startSessionAndBind(hostList,port,password,addressRange,systemType,systemid,numberOfBindsPerServer,bindAttempts,bindAttemptSleepTime);
        //logger.info(String.format("Checking for newly queued data ..."));
        List<String> msgOutput = null;
        if(smsQueue.peek()!=null) {
            int flowCount=0;
            String msgJson =null;
            while ((msgJson = smsQueue.poll()) != null) {
               // if(flowCount==flowfileBatchSize&&smsQueue.peek() != null){
                    writeFlowfile(session, msgJson);
               // }else if(smsQueue.peek()== null && flowCount<=flowfileBatchSize){
                   // writeFlowfile(session,  Arrays.asList(msgJson));
               // }
                flowCount++;
            }
        }
    }
}



