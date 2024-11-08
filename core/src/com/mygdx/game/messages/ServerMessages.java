package com.mygdx.game.messages;

import com.mygdx.game.states.game.standard.architecture.ServerListener;

import java.util.ArrayList;
import java.util.Observable;

import lac.cnclib.sddl.message.Message;
import lac.cnclib.sddl.serialization.Serialization;

/**
 * Observable class that will contain
 * server received messages
 * Pattern:
 * Singleton
 * Observer
 *
 * @author  Pedro Sampaio
 * @since   0.1
 */
public class ServerMessages extends Observable {

    // list of existing server messages
    private ArrayList<Message> messages = null;

    // list of existing server listeners
    private ArrayList<ServerListener> listeners = null;

    // reference to the singleton reference for this class
    private static ServerMessages instance = null;

    /**
     * Constructor to defeat instantiation
     */
    protected ServerMessages () {
        // initializes list of server messages
        messages = new ArrayList<Message>();
        // initializes list of server listeners
        listeners = new ArrayList<ServerListener>();
    }

    /**
     * Returns the instance of ServerMessages if
     * it exists, otherwise creates the instance and
     * then proceed to return it, singleton pattern
     *
     * @return the ServerMessages singleton instance
     */
    public static ServerMessages getInstance() {
        if(instance == null)
            instance = new ServerMessages();

        return instance;
    }

    /**
     * dispatch changes in server messages for observers
     */
    public void dispatchChanges() {
        setChanged();
        notifyObservers();
    }

    /**
     * @return the list of current server messages
     */
    public ArrayList<Message> getMessages() {
        return messages;
    }

    /**
     * Adds a message to the list of server messages
     * notifying observers of the change
     *
     * @param msg   the message that will be added to the list of server messages
     */
    public void addMessage(Message msg) {
        // gets content of each message
        MessageContent msgContent = (MessageContent)
                Serialization.fromJavaByteStream(msg.getContent());

        // searches for listener to send received message
        for(int i = 0; i < listeners.size(); i++) {
            // found the listener
            if(listeners.get(i).getClass().toString().equals(msgContent.getListener())) {
                listeners.get(i).handleServerMessage(msgContent);
                break;
            }
        }
    }

    /**
     * Adds a listener to the list of server listeners
     * that listen to server messages and can receive them
     * @param listener the listener to add to the list of server listeners
     */
    public void subscribe(ServerListener listener) {this.listeners.add(listener);}

    /**
     * Removes a listener from list of server listeners
     * @param listener  the listener to be removed
     */
    public void unsubscribe(ServerListener listener) {this.listeners.remove(listener);}
}
