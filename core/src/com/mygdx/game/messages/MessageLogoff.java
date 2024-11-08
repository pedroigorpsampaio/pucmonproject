package com.mygdx.game.messages;

import java.io.Serializable;

/**
 * The model for the message for logging off server
 *
 * @author  Pedro Sampaio
 * @since   1.6
 */
public class MessageLogoff implements Serializable {

    private String character; // the name of the character to logoff

    /**
     * Constructor for logoff message that receives the name of character to logoff
     * @param character the name of the character to logoff of server
     */
    public MessageLogoff(String character) {
        this.character = character;
    }

    public String getCharacter() {return character;}

    public void setCharacter(String character) {this.character = character;}
}
