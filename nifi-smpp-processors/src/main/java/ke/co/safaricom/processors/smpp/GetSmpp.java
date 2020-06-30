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

import ke.co.safaricom.processors.smpp.connectionparams.Buffer;
import ke.co.safaricom.processors.smpp.connectionparams.ConnectionObj;
import ke.co.safaricom.processors.smpp.logger.Logging;
import ke.co.safaricom.processors.smpp.smpp.CreateSmppSession;
import org.apache.nifi.annotation.lifecycle.OnStopped;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            .description("SMSC host ips to bind with")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();


    public static final PropertyDescriptor PROP_SMSC_PORT = new PropertyDescriptor
            .Builder().name("SMSC PORT")
            .displayName("Port")
            .description("SMSC host port to bind to")
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
            .displayName("System id")
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

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("Success")
            .description("Successfully started receiving data from SMSC")
            .build();

    public static final Relationship REL_FAIL = new Relationship.Builder()
            .name("Failure")
            .description("Failed to bind or receive data")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private SMPPSession newSmppSession =null;
    private CreateSmppSession smppSession=null;
    private Logging logger =null ;
    private ConnectionObj connectionParams = null;
    private Buffer buffer=null;



    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(PROP_SMSC_HOST);
        descriptors.add(PROP_SMSC_PORT);
        descriptors.add(PROP_SMSC_PASSWORD);
        descriptors.add(PROP_SMSC_SYSTEM_TYPE);
        descriptors.add(PROP_SMSC_ADDRESS_RANGE);
        descriptors.add(PROP_SMSC_SYSTEM_ID);
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




    private void startSessionAndBind(
            String host,int port, String password,String addressRange,String systemType,String systemid
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
        //logger=new Logging(this.getLogger());

        if(smppSession==null){
            smppSession=new CreateSmppSession(connectionParams,logger,buffer);
         }

        newSmppSession =smppSession.getExistingSession();
        if(newSmppSession ==null){
            smppSession.create();
        }
    }


    @OnStopped
    public void stop() {
        // Close and unbind the connections
        try {
            smppSession.closeSession();
        } catch (IOException e) {
            logger.info("Closing and Unbinding Connection");
        }
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        FlowFile msgFlowFile = null;// session.create();
        // TODO implement

        String host = context.getProperty(PROP_SMSC_HOST).getValue();
        int port = context.getProperty(PROP_SMSC_PORT).asInteger();
        String password = context.getProperty(PROP_SMSC_PASSWORD).getValue();
        String systemType = context.getProperty(PROP_SMSC_SYSTEM_TYPE).getValue();
        String addressRange = context.getProperty(PROP_SMSC_ADDRESS_RANGE).getValue();
        String systemid = context.getProperty(PROP_SMSC_SYSTEM_ID).getValue();


        startSessionAndBind(host,port,password,addressRange,systemType,systemid);
        logger.info("Checking buffer.check() :  " + buffer.check());
        while(buffer.check()!=null){
            msgFlowFile=session.create();
            String msgJson=buffer.get();
            logger.info("Processor getting records :  " + msgJson);

            try{
                msgFlowFile=session.write(msgFlowFile,outputStream->{
                  outputStream.write(msgJson.getBytes(StandardCharsets.UTF_8));
                });


            }catch (ProcessException e){
                logger.error("\"Process Exception {}\"," + e );
            }
            session.transfer(msgFlowFile,REL_SUCCESS);
        }


    }
}
