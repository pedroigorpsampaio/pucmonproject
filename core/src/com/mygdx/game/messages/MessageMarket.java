package com.mygdx.game.messages;

import com.mygdx.game.states.game.standard.market.MarketItem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The model for the message containing market information
 *
 * @author  Pedro Sampaio
 * @since   1.8
 */
public class MessageMarket implements Serializable {
    // Types of actions that can be requested to server in this message
    public enum Action {RETRIEVE_ITEMS, BUY_ITEM, REGISTER_ITEM, REMOVE_ITEM, SHOW_LISTINGS, COLLECT}

    private Action action; // the action to request to server in this message

    private String character; // the name of player's character

    private ArrayList<MarketItem> items; // list of market items in case of retrieves (retrieve_items and show_listings)

    private MarketItem item; // item data in case of item actions (buy_item, register_item, remove_item)

    /**
     * Market message constructor
     * @param character the name of player character
     * @param action the action to request to server in this message
     */
    public MessageMarket(String character, Action action) {
        this.character = character;
        this.action = action;
    }

    /**
     * Getters and Setters
     */

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public ArrayList<MarketItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<MarketItem> items) {
        this.items = items;
    }

    public MarketItem getItem() {
        return item;
    }

    public void setItem(MarketItem item) {
        this.item = item;
    }
}
