package com.mygdx.game.messages;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * The model for the message containing ranking information
 *
 * @author  Pedro Sampaio
 * @since   0.1
 */
public class MessageRanking implements Serializable{

    private LinkedHashMap<String, Integer> ranking; // ranking data

    private String character; // player character name

    private int position; // player character position in ranking

    /**
     * Ranking message constructor
     * @param character the name of player character
     */
    public MessageRanking(String character) {
        this.character = character;
        ranking = new LinkedHashMap<String, Integer>();
    }

    public void setRanking(LinkedHashMap<String, Integer> ranking) {
        this.ranking = ranking;
    }

    public LinkedHashMap<String, Integer> getRanking() {
        return ranking;
    }

    public String getCharacter() {return character;}

    public void setCharacter(String character) {this.character = character;}

    public int getPosition() {return position;}

    public void setPosition(int position) {this.position = position;}
}
