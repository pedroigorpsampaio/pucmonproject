package com.mygdx.game.states.game.standard.item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.battle.Attributes;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;


/**
 * Class that represents the type of item that
 * can be worn by player: equipments
 * Equipments are non-stackable and can
 * range from weapons to jewelry.
 *
 * @author  Pedro Sampaio
 * @since   1.2
 */
public class Equipment extends Item {

    /**
     * Enum that dictates the possible equipment slots to attach an equipment
     */
    public enum Slot {HELMET, ARMOR, WEAPON, SHIELD, LEGS, BOOTS, RING, AMULET}

    private Slot equipmentSlot; // the equipment slot of this equipment
    private int level; // the level of this equipment (depends on enemy that dropped it)
    private Effect effect; // the effect of this equipment
    private int uID; // the unique ID of this equipment

    /**
     * Equipment constructor
     *
     * @param id          the id of this equipment
     * @param name        the name of this equipment
     * @param description the description of this equipment
     * @param effect      the effect of this equipment
     * @param sprite      the equipment sprite
     * @param chance      the chance of dropping this equipment (0 to 100)
     * @param quality     the quality of this equipment
     */
    public Equipment(int id, String name, String description, Effect effect, TextureRegion sprite,
                     float chance, Quality quality, Slot equipmentSlot) {
        super(id, name, description, sprite, chance, false, true, 1, quality);
        this.equipmentSlot = equipmentSlot;
        this.effect = effect;
        level = 1; // base equipment level is 1
        this.uID = -1; // unique ID will be generated on factory
    }

    /**
     * Constructor to create copies of equipments
     * @param copiedEquipment the equipment to be copied
     */
    public Equipment(Equipment copiedEquipment) {
        super(copiedEquipment.getId(), copiedEquipment.getName(), copiedEquipment.getDescription(),
                copiedEquipment.getSprite(), copiedEquipment.getChance(), false, true, 1, copiedEquipment.getQuality());
        this.equipmentSlot = copiedEquipment.getEquipmentSlot();
        this.effect = new Effect();
        this.effect.attack = copiedEquipment.getEffect().attack;
        this.effect.defense = copiedEquipment.getEffect().defense;
        this.effect.autoAttack = copiedEquipment.getEffect().autoAttack;
        this.effect.maxHealth = copiedEquipment.getEffect().maxHealth;
        this.effect.critChance = copiedEquipment.getEffect().critChance;
        this.effect.critMult = copiedEquipment.getEffect().critMult;
        this.effect.autoSpeed = copiedEquipment.getEffect().autoSpeed;
        this.effect.speed = copiedEquipment.getEffect().speed;
        this.effect.rateDrop = copiedEquipment.getEffect().rateDrop;
        this.effect.rateExp = copiedEquipment.getEffect().rateExp;
        this.effect.rateGold = copiedEquipment.getEffect().rateGold;
        this.level = 1; // base equipment level is 1
        this.uID = -1; // unique ID will be generated on factory
    }

    /**
     * Scale equipment level randomly between a range based on level received in parameter
     * @param level    the level to use for the scaling of equipment
     */
    public void scale(int level) {
        float randFactor = Common.randFloat(Config.scaleItemLevelMinFactor, Config.scaleItemLevelMaxFactor);
        // applies one randomly generated factor to all attributes
        int randLevel = Math.round(level * randFactor);
        if(randLevel < 1) // clamps for safety
            randLevel = 1;
        // sets new level after scale
        this.level = randLevel;

        /* update effects based on equipment level */
        updateEffects();
    }

    /**
     * Updates equipment effects
     * based on equipment level
     */
    private void updateEffects() {
        effect.defense = Math.round(effect.defense + (Config.itemLevelFactor*this.level*effect.defense));
        effect.attack = Math.round(effect.attack + (Config.itemLevelFactor*this.level*effect.attack));
        effect.maxHealth = Math.round(effect.maxHealth + (Config.itemLevelFactor*this.level*effect.maxHealth));
        effect.autoAttack = Math.round(effect.autoAttack + (Config.itemLevelFactor*this.level*effect.autoAttack));
        effect.autoSpeed = effect.autoSpeed + (Config.itemLevelFactor*this.level*effect.autoSpeed);
        effect.critChance = effect.critChance + (Config.itemLevelFactor*this.level*effect.critChance);
        effect.critMult = effect.critMult + (Config.itemLevelFactor*this.level*effect.critMult);
        effect.speed = effect.speed + (Config.itemLevelFactor*this.level*effect.speed);
        effect.rateDrop = effect.rateDrop + (Config.itemLevelFactor*this.level*effect.rateDrop);
        effect.rateExp = effect.rateExp + (Config.itemLevelFactor*this.level*effect.rateExp);
        effect.rateGold = effect.rateGold + (Config.itemLevelFactor*this.level*effect.rateGold);
    }

    /**
     * Gets description of this equipment based on effects
     * that depends on equipments level
     * @return a string build containing the equipment general description and effects
     */
    public String getEquipmentDescription() {
        // string that will hold the built description
        StringBuilder desc = new StringBuilder("").append(getDescription());
        // breaks line to start concatenating effects
        desc.append("\n");
        // concatenate effects alternating break lines
        if(effect.attack != 0) {
            String effName = Main.getInstance().getLang().get("statsAtk");
            desc.append(effName).append(" + ").append(effect.attack);
            desc.append("\n");
        }
        if(effect.defense != 0) {
            String effName = Main.getInstance().getLang().get("statsDef");
            desc.append(effName).append(" + ").append(effect.defense);
            desc.append("\n");
        }
        if(effect.maxHealth != 0) {
            String effName = Main.getInstance().getLang().get("statsMaxHealth");
            desc.append(effName).append(" + ").append(effect.maxHealth);
            desc.append("\n");
        }
        if(effect.critChance != 0) {
            String effName = Main.getInstance().getLang().get("statsCritChance");
            desc.append(effName).append(" + ").append(String.format("%.2f", effect.critChance)).append("%");
            desc.append("\n");
        }
        if(effect.critMult != 0) {
            String effName = Main.getInstance().getLang().get("statsCritMult");
            desc.append(effName).append(" + ").append(String.format("%.2f", effect.critMult)).append(" x ");
            desc.append("\n");
        }
        if(effect.autoAttack != 0) {
            String effName = Main.getInstance().getLang().get("statsAutoAtk");
            desc.append(effName).append(" + ").append(effect.autoAttack);
            desc.append("\n");
        }
        if(effect.autoSpeed != 0) {
            String effName = Main.getInstance().getLang().get("statsAutoAtkSpd");
            desc.append(effName).append(" + ").append(String.format("%.2f", 10f/effect.autoSpeed));
            desc.append("\n");
        }
        if(effect.speed != 0) {
            String effName = Main.getInstance().getLang().get("statsSpeed");
            desc.append(effName).append(" + ").append(String.format("%.2f", effect.speed));
            desc.append("\n");
        }
        if(effect.rateDrop != 0) {
            String effName = Main.getInstance().getLang().get("statsDropRate");
            desc.append(effName).append(" + ").append(String.format("%.2f", effect.rateDrop));
            desc.append("\n");
        }
        if(effect.rateGold != 0) {
            String effName = Main.getInstance().getLang().get("statsGoldRate");
            desc.append(effName).append(" + ").append(String.format("%.2f", effect.rateGold));
            desc.append("\n");
        }
        if(effect.rateExp != 0) {
            String effName = Main.getInstance().getLang().get("statsExpRate");
            desc.append(effName).append(" + ").append(String.format("%.2f", effect.rateExp));
            desc.append("\n");
        }

        // return the built string with added effects to description
        return desc.toString();
    }

    /**
     * Returns the scaled effect of this equipment
     * @return  the scaled effects of this equipment based on its level
     */
    public Effect getEffect() {
        return effect;
    }

    /**
     * Getters and Setters
     */

    public Slot getEquipmentSlot() {
        return equipmentSlot;
    }

    public void setEquipmentSlot(Slot equipmentSlot) {
        this.equipmentSlot = equipmentSlot;
    }

    public int getLevel(){return level;}

    public int getUniqueID() {
        return uID;
    }

    public void setUniqueID(int uID) {
        this.uID = uID;
    }

    public void setLevel(int level) {this.level = level; updateEffects();}
}
