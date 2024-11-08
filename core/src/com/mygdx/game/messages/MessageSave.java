package com.mygdx.game.messages;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * The model for the message containing save information
 *
 * @author  Pedro Sampaio
 * @since   1.5
 */
public class MessageSave implements Serializable{

    private String character; // player name

    private int worldMap; // player current world map

    private int level;  // player level

    private long experience; // player experience

    private int posx; // player world map pos x

    private int posy; // player world map pos y

    private long gold; // player gold amount

    private String inventoryData; // player inventory data serialized in a string

    private String equipmentData; // player worn equipment data serialized in a string

    /**
     * Constructor that receives all save information
     *
     * @param worldMap          player current world map
     * @param level             player level
     * @param experience        player experience
     * @param posx              player world map pos x
     * @param posy              player world map pos y
     * @param gold              player gold amount
     * @param inventoryData     player inventory data serialized in a string
     * @param equipmentData     player worn equipment data serialized in a string
     */
    public MessageSave(String character, int worldMap, int level, long experience,
                       int posx, int posy, long gold, String inventoryData, String equipmentData) {
        this.character = character;
        this.worldMap = worldMap;
        this.level = level;
        this.experience = experience;
        this.posx = posx;
        this.posy = posy;
        this.gold = gold;
        this.inventoryData = inventoryData;
        this.equipmentData = equipmentData;
    }

    public int getWorldMap() {
        return worldMap;
    }

    public int getLevel() {
        return level;
    }

    public long getExperience() {
        return experience;
    }

    public int getPosx() {
        return posx;
    }

    public int getPosy() {
        return posy;
    }

    public long getGold() {
        return gold;
    }

    public String getInventoryData() {
        return inventoryData;
    }

    public String getEquipmentData() {
        return equipmentData;
    }

    public String getCharacter() {
        return character;
    }
}
