package com.mygdx.game.messages;

import java.io.Serializable;

/**
 * The model for the message containing login information
 *
 * @author  Pedro Sampaio
 * @since   1.5
 */
public class MessageLogin implements Serializable{

    private String account; // the account to login

    private String password; // the password of the account to login

    /**
     * Response data fields
     */

    private String character; // the name of the character

    private int worldMap; // player current world map

    private int level;  // player level

    private long experience; // player experience

    private int posx; // player world map pos x

    private int posy; // player world map pos y

    private long gold; // player gold amount

    private String inventoryData; // player inventory data serialized in a string

    private String equipmentData; // player worn equipment data serialized in a string

    private boolean firstLogin; // is this the first login of the player on game world?


    /**
     * Constructor for login message that sets all necessary data to retrieve login information
     * @param account       the account to login
     * @param password      the password of the account to login
     */
    public MessageLogin(String account, String password) {
        this.account = account;
        this.password = password;
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

    public String getCharacter() {return character;}

    public void setCharacter(String character) {this.character = character;}

    public int getWorldMap() {return worldMap;}

    public void setWorldMap(int worldMap) {this.worldMap = worldMap;}

    public int getLevel() {return level;}

    public void setLevel(int level) {this.level = level;}

    public long getExperience() {return experience;}

    public void setExperience(long experience) {this.experience = experience;}

    public int getPosx() {return posx;}

    public void setPosx(int posx) {this.posx = posx;}

    public int getPosy() {return posy;}

    public void setPosy(int posy) {this.posy = posy;}

    public long getGold() {return gold;}

    public void setGold(long gold) {this.gold = gold;}

    public String getInventoryData() {return inventoryData;}

    public void setInventoryData(String inventoryData) {this.inventoryData = inventoryData;}

    public String getEquipmentData() {return equipmentData;}

    public void setEquipmentData(String equipmentData) {this.equipmentData = equipmentData;}

    public boolean isFirstLogin() {return firstLogin;}

    public void setFirstLogin(boolean firstLogin) {this.firstLogin = firstLogin;}

}
