package com.mygdx.game.messages;

import java.io.Serializable;

/**
 * The model for the message containing sign up information
 *
 * @author  Pedro Sampaio
 * @since   1.5
 */
public class MessageSignUp implements Serializable{

    private String account; // the account to sign up

    private String password; // the password of the account to sign up

    private String character; // the name of the character to create in sign up account

    /**
     * Constructor for sign up message that sets all necessary sign up data
     * @param account       the account to sign up
     * @param password      the password of the account to sign up
     * @param character     the name of the character to create in sign up account
     */
    public MessageSignUp(String account, String password, String character) {
        this.account = account;
        this.password = password;
        this.character = character;
    }

    /**
     * Getters and Setters
     */

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

}
