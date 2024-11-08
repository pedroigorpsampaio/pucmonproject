package com.mygdx.game.messages;

import com.mygdx.game.states.game.standard.architecture.ServerListener;

import java.io.Serializable;

/**
 * Holds the types of messages
 * that can be used in client-server
 * communication and represents
 * a message with its content and type
 *
 * @author 	Pedro Sampaio
 * @since	0.1
 */
public class MessageContent implements Serializable{

    /**
     * Enum for available types of messages between client and server
     */
    public enum Type {SIGNUP, SAVE, LOGIN, RANKING, MARKET, ACK, LOGOFF, MISSION_DATA, SENSOR}

    private MessageFlag flag; // flag to contain result flag of request

    /**
     * The listener that is waiting for server response
     */
    private String listener;

    /**
     * Content present in the message
     */
    private Serializable content;

    /**
     * Message type
     */
    Type type;

    /**
     * Constructor
     *
     * @param listener  the listener that will receive the server response
     * @param content   the serializable content to be in the message
     * @param type      the type of the message
     */
    public MessageContent(String listener, Serializable content, Type type) {
        this.listener = listener;
        this.content = content;
        this.type = type;
    }

    /**
     * Getter for the content variable
     * @return  the serializable content of the message
     */
    public Object getContent() {
        return content;
    }

    /**
     * Getter for the message type
     * @return  the message type of this message
     */
    public Type getType() {
        return type;
    }

    /**
     * Getter for the message listener
     * @return the listener of this message
     */
    public String getListener() {return listener;}

    /**
     * Getter for the message flag
     * @return the flag of this message
     */
    public MessageFlag getFlag() {return flag;}

    /**
     * Setter for the message flag
     * @param flag the flag to set in message
     */
    public void setFlag(MessageFlag flag) {this.flag = flag;}
}
