package com.mygdx.game.messages;


/**
 * Possible message flags to help
 * server communication
 */
public enum MessageFlag {
    OKIDOKI, // everything went ok flag
    GENERAL_ERROR, // general error flag
    ACCOUNT_TAKEN, // account name already taken flag
    CHARACTER_NAME_TAKEN, // character name already taken flag
    ACCOUNT_PASSWORD_DO_NOT_MATCH, // account and password mismatch flag
    CHARACTER_ALREADY_ONLINE, // character already online flag
    EMPTY_MARKET, // no items are being sold currently on market flag
    ITEM_ALREADY_BOUGHT, // item already bought flag
    ITEM_ALREADY_SOLD, // item already sold flag
    NO_ITEMS_SOLD_BY_PLAYER, // no items being sold by client player flag
    ITEM_ALREADY_COLLECTED // item already collected flag
}
