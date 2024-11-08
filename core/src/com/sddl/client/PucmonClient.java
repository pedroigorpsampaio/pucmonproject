package com.sddl.client;

import com.mygdx.game.messages.MessageContent;
import com.mygdx.game.messages.ServerMessages;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.NodeConnectionListener;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.serialization.Serialization;

/***
 * Works as an interface to server requests
 * Pattern:
 * Singleton
 *
 * @author  Pedro Sampaio
 * @since   0.1
 */
public class PucmonClient implements NodeConnectionListener {

//    private static String		gatewayIP   = "177.84.31.229";   // server IP (WAN)
    private static String		gatewayIP   = "192.168.0.66";   // server IP (LAN)
//    private static String		gatewayIP   = "localhost";   // server IP (LOCALHOST)
    private static int		gatewayPort = 7171;                 // server Port
    private MrUdpNodeConnection	connection;                     // connection object
    private volatile boolean isConnected;                       // connectivity status

    private static PucmonClient instance = null;                // singleton instance
    private InetSocketAddress sv_address;                       // server address
    private volatile boolean checkingConnection = false;        // if it is checking connection status
    private volatile boolean ackComplete = false;               // if ack message was sent and connection is ok

    /**
     * Protected constructor for singleton initialization
     * Creates connection with the server, storing
     * connection information in the connection variable
     */
    protected PucmonClient() {
        isConnected = false;    // starts as non-connected

        // start connection with server
        connect();
    }

    /**
     * Creates the PucmonClient instance if does not exist yet
     * obeying the singleton pattern
     * @return returns PucmonClient instance
     */
    public static PucmonClient getInstance() {
        if (instance == null) // if instance is not created
            instance = new PucmonClient(); // instantiate it

        return instance; // return instance
    }

    /**
     * Connects this client to the server
     */
    private void connect() {

        // creates thread to keep retrying connection
        final Thread connThread = new Thread() {
            @Override
            public void run() {
                try {
                    if (!isConnected) {
                        // init socket address with server and port information if not instantiated yet
                        sv_address = new InetSocketAddress(gatewayIP, gatewayPort);

                        // instantiate connection object if no instantiated yet
                        try {
                            connection = new MrUdpNodeConnection();
                        } catch (IOException e) {
                            System.err.println("Could not create connection: " + e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        // add PucmonClient object as a connection node listener in connection object
                        connection.addNodeConnectionListener(instance);

                        // connect with address built from the server data
                        connection.connect(sv_address);
                        //sleep(5000);
                        // starts checking connectivity with the server
                        // if not checking yet
                        if(!checkingConnection) {
                            synchronized (this) {
                                checkingConnection = true;
                                keepCheckingConnection();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Thread exception: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        connThread.start();
    }

    /**
     * Keeps checking in a different thread if
     * client is connected to the server
     */
    private void keepCheckingConnection () {
        // creates thread to keep retrying connection
        final Thread checkThread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        sendAckMessage(); // sends ack message
                        sleep(5000);    // wait for timeout
                        // checks if message was sent and connection is ok
                        if(ackComplete) {
                            synchronized (this) {
                                isConnected = true; // connection is ok
                                ackComplete = false; // sets false to continue checking
                            }
                        } else { // connection not ok
                            synchronized (this) {
                                isConnected = false;
                            }
                        }
                    } catch (InterruptedException e) {
                        System.err.println("Thread exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        };
        checkThread.start();
    }

    /**
     * Sends ack message to server to keep connectivity status updated
     * Message received callback updates status of connectivity
     */
    public void sendAckMessage () {
        ApplicationMessage message = new ApplicationMessage();
        message.setContentObject(new MessageContent(null, "ack", MessageContent.Type.ACK));

        try {
            connection.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            isConnected = false;
            System.err.println("Could not send ack message to the server"+e.getMessage());
        }
    }

    /**
     * Method that controls message sending from
     * client to the server (only if connection is ok)
     */
    public void sendMessage(MessageContent msg) {

        // connection is okay, send message
        if(isConnected) {
            ApplicationMessage message = new ApplicationMessage();
            message.setContentObject(msg);

            try {
                connection.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Could not send message to the server"+e.getMessage());
            }
        }
        else {  // could not establish a connection to the server
            System.err.println("Could not connect to the server");
        }
    }

    /***
     * SDDL callback for connection with the server event
     * updates status of the client to mirror current connectivity
     *
     * @param remoteCon remote node connection object
     */
    @Override
    public void connected(NodeConnection remoteCon) {
        // is connected is set to true if connection is successfully made
        isConnected = true;
    }

    /**
     * SDDL callback for receiving messages
     *
     * @param remoteCon remote node connection object sender
     * @param message   message received
     */
    @Override
    public void newMessageReceived(NodeConnection remoteCon, lac.cnclib.sddl.message.Message message) {

        // gets the message content
        MessageContent msgContent = (MessageContent)
                Serialization.fromJavaByteStream(message.getContent());

        // checks if message type equals ack
        if(msgContent.getType() == MessageContent.Type.ACK) {
            synchronized (this) {
                ackComplete = true;  // ack was completed
            }
        }
        else // if it not an ack message, stores the message to the game observable server messages class
            ServerMessages.getInstance().addMessage(message);
    }

    // other methods

    @Override
    public void reconnected(NodeConnection remoteCon, SocketAddress endPoint, boolean wasHandover, boolean wasMandatory) {
        // updates connectivity status
        synchronized (this) {
            isConnected = true;
        }
    }

    @Override
    public void disconnected(NodeConnection remoteCon) {
        // updates connectivity status
        synchronized (this) {
            isConnected = false;
        }
    }

    @Override
    public void unsentMessages(NodeConnection remoteCon, List<lac.cnclib.sddl.message.Message> unsentMessages) {}

    @Override
    public void internalException(NodeConnection remoteCon, Exception e) {}

    /**
     * getter for connection status
     */
    public synchronized boolean isConnected() {
        return isConnected;
    }
}