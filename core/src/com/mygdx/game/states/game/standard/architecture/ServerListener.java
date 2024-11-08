package com.mygdx.game.states.game.standard.architecture;

import com.mygdx.game.messages.MessageContent;

import java.io.Serializable;

/**
 * Interface for the modules that will
 * be communicating with server containing
 * methods that deal with server messages
 *
 * @author Pedro Sampaio
 * @since   1.5
 */
public interface ServerListener {

    /**
     * All classes that implements this interface
     * should subscribe to server messages to be
     * able to receive messages.
     * Suggested implementation:
     * ServerMessages.getInstance().subscribe(this)
     */
    void subscribeToServer();

    /**
     * Called when a server message is received
     * @param msg   the message received form the server on the form of MessageContent
     */
    void handleServerMessage(MessageContent msg);

}
