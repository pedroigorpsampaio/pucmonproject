package sddl.server;

import com.mygdx.game.messages.MessageContent;
import com.mysql.fabric.Server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;

import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.serialization.Serialization;
import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;

/**
 * The main class for the game server. Uses ContextNet middleware
 * and its SDDL layer to perform remote communications
 * Pattern:
 * Singleton
 *
 * @author  Pedro Sampaio
 * @since   0.1
 */
public class PucmonServer implements UDIDataReaderListener<ApplicationObject> {

    /** DEBUG */
    private static final String TAG = PucmonServer.class.getSimpleName();

    SddlLayer  core;        // core of SDDL layer

    private int svUpdateTick = 100; // server update tick in milliseconds

    int        counter;

    /** Mobile Hubs Data */
    private static final Map<UUID, UUID> mMobileHubs = new HashMap<UUID, UUID>();

    // List of keys (UUID of the M-Hubs)
    List<UUID> nodes;

    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.OFF);

        new PucmonServer(); // initializes server
    }

    /**
     * Protected to defeat external instantiation.
     * Establishes the game server, as seen on
     * contextNet documentation.
     *
     */
    protected PucmonServer() {
        // Create a layer and participant
        core = UniversalDDSLayerFactory.getInstance();
        core.createParticipant(UniversalDDSLayerFactory.CNET_DOMAIN);
        // Receive and write topics to domain
        core.createPublisher();
        core.createSubscriber();
        // ClientLib Events
        Object receiveMessageTopic = core.createTopic(Message.class, Message.class.getSimpleName());
        core.createDataReader(this, receiveMessageTopic);
        // To ClientLib
        Object toMobileNodeTopic = core.createTopic(PrivateMessage.class, PrivateMessage.class.getSimpleName());
        core.createDataWriter(toMobileNodeTopic);

        counter = 0;

        // List of keys (UUID of the M-Hubs)
        nodes = new ArrayList<UUID>( mMobileHubs.keySet() );

        startPlayerManagement();

        System.out.println("=== Server Started (Listening) ===");
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts player management
     * using another thread
     */
    private void startPlayerManagement() {
        final Thread pManageThread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        // each tick
                        sleep(svUpdateTick);
                        ServerState.getInstance().update();
                    } catch (InterruptedException e) {
                        System.err.println("Thread exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        };
        pManageThread.start();
    }

    enum MessageTag {SensorData, EventData, ReplyData, ErrorData}

    /**
     * SDDL callback for receiving messages.
     * pass control for the handler of server messages
     *
     * @param topic the message received
     */
    @Override
    public void onNewData(ApplicationObject topic) {
        Message message = (Message) topic;
       // System.out.println("Message Received: "+Serialization.fromJavaByteStream(message.getContent()));

        /**
         * mobile hub tests
         */
        Message msg = null;
        MessageTag msgTag = null;

        if( topic instanceof Message ) {
            msg = (Message) topic;
            UUID nodeId = msg.getSenderId();
            UUID gatewayId = msg.getGatewayId();

            if (!mMobileHubs.containsKey(nodeId)) {
                mMobileHubs.put(nodeId, gatewayId);
                System.out.println(">>" + TAG + ": Client " + nodeId + " connected with gateway: " + gatewayId);
                // List of keys (UUID of the M-Hubs)
                nodes = new ArrayList<UUID>( mMobileHubs.keySet() );

                for( int i = 0; i < nodes.size(); ++i )
                    System.out.println( i + ": " + nodes.get( i ) );
            }

            String content = new String( msg.getContent() );
            JSONParser parser = new JSONParser();

            try {
                JSONObject object = (JSONObject) parser.parse( content );
                String tag = (String) object.get( "tag" );
                msgTag = MessageTag.valueOf(tag);

                switch( msgTag ) {
                    case SensorData:
                        String sensorUUID = (String) object.get( "uuid" );
                        String sensorSource = (String) object.get( "source" );
                        String sensorName = (String) object.get( "sensor_name" );
                        JSONArray sensorData  = (JSONArray) object.get( "sensor_value" );
                        System.out.println("\nSensor UUID: "+ sensorUUID + "\nSensor Source: " + sensorSource +
                                                "\nSensor Name: " + sensorName + "\nSensor Data: "  + sensorData);
                        break;

                    case EventData:
                        System.out.println("EventData-> tag: " + tag);
                        //handleEvent( label, data );
                        break;

                    case ReplyData:
                        System.out.println("ReplyData-> tag: " + tag);
                    case ErrorData:
                        //handleMessage( tag, object );
                        System.out.println("ErrorData-> tag: " + tag);
                        break;
                }
            } catch( Exception ex ) {
                // casts received message to the agreement model for game server communications
                Serializable rawContent = Serialization.fromJavaByteStream(message.getContent());
                if( rawContent instanceof MessageContent ) {
                    MessageContent msgContent = (MessageContent) rawContent;

                    // handles the message received
                    MessageHandler.getInstance().handleMessage(core, message, msgContent);
                }
            }
        }

        // m-hub test msg
//        UUID mHubID = UUID.fromString("fce983d3-6191-453e-86c4-458c7e31a1e4");
//        if (mMobileHubs.containsKey(mHubID)) {
//            // test message
//            String tstMsg = "{\n" +
//                    " \"MEPAQuery\": {\n" +
//                    "  \"type\":\"add\",\n" +
//                    "  \"label\":\"AVGTemp\",\n" +
//                    "  \"object\":\"event\",\n" +
//                    "  \"rule\":\"SELECT avg(sensorValue[1]) as value FROM \n" +
//                    "      SensorData(sensorName='Temperature')\n" +
//                    "      .win:time_batch(10 sec)\",\n" +
//                    "  \"target\":\"local\"\n" +
//                    " }\n" +
//                    "}";
//            // Send the message
//            ApplicationMessage appMsg = new ApplicationMessage();
//            appMsg.setPayloadType( PayloadSerialization.JSON );
//            appMsg.setContentObject( "[" + tstMsg + "]" );
//            //sendUnicastMSG(appMsg, mHubID);
//        }

        synchronized (core) {
            counter++;
        }

    }

    /**
     * Sends a message to a unique component (UNICAST)
     * @param appMSG The application message (e.g. a String message)
     * @param nodeID The UUID of the receiver
     */
    public void sendUnicastMSG( ApplicationMessage appMSG, UUID nodeID ) {
        PrivateMessage privateMSG = new PrivateMessage();
        privateMSG.setGatewayId( UniversalDDSLayerFactory.BROADCAST_ID );
        privateMSG.setNodeId( nodeID );
        privateMSG.setMessage( Serialization.toProtocolMessage( appMSG ) );

        sendMessage( privateMSG );
    }

    /**
     * Sends a private message containing
     * information needed for the shipment
     */
    public void sendMessage(PrivateMessage privateMessage) {
        core.writeTopic(PrivateMessage.class.getSimpleName(), privateMessage);
    }
}