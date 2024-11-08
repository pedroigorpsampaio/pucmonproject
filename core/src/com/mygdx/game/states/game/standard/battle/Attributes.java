package com.mygdx.game.states.game.standard.battle;

import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.states.game.standard.item.Equipment;
import com.mygdx.game.util.Config;

import org.w3c.dom.Attr;

/**
 * Class that represents attributes of
 * entities that influences in battling
 *
 * @author Pedro Sampaio
 * @since 0.9
 */
public class Attributes {

    private int levelMaxHealth;   // the maximum health of an entity (only with level progression)
    private int levelAttack;  // the attack attribute of an entity (only with level progression)
    private int levelDefense; // the defense attribute of an entity (only with level progression)
    private float levelCriticalChance;    // the chance of hitting a critical hit (only with level progression)
    private float levelCriticalMultiplier;    // the multiplier of damage in case of critical hit (only with level progression)
    private int levelAutoAttack;  // the auto attack attribute of an entity (only with level progression)
    private float levelAutoSpeed; // the speed of auto attacks (only with level progression)
    private int equipMaxHealth;  // the maximum health of an entity (worn equipments effect)
    private int equipAttack;     // the attack attribute of an entity    (worn equipments effect)
    private int equipDefense;    // the defense attribute of an entity   (worn equipments effect)
    private float equipCriticalChance;     // the chance of hitting a critical hit   (worn equipments effect)
    private float equipCriticalMultiplier; // the multiplier of damage in case of critical hit   (worn equipments effect)
    private int equipAutoAttack; // the auto attack attribute of an entity   (worn equipments effect)
    private float equipAutoSpeed; // the speed of auto attacks   (worn equipments effect)
    int level;      // the level of an entity   (worn equipments effect)
    long experience; // the experience of an entity (in case of player entity)  (worn equipments effect)

    /**
     * Attributes constructor for dynamic entities with progression and equipments.
     * Only needs level and experience, all other
     * attributes will be calculated based on level
     * or currently equipped equipment.
     *
     * @param level         the level of an entity
     * @param experience    the current experience of an entity
     */
    public Attributes(int level, long experience) {
        this.level = level;
        this.experience = experience;
        // initializes level attributes initial value (they will be adjusted in update)
        this.levelMaxHealth = Config.npMaxHealth;
        this.levelAttack = Config.npAttack;
        this.levelDefense = Config.npDefense;
        this.levelCriticalChance = Config.npCriticalChance;
        this.levelCriticalMultiplier = Config.npCriticalMultiplier;
        this.levelAutoAttack = Config.npAutoAttack;
        this.levelAutoSpeed = Config.npAutoSpeed;
        // initializes equip effects with 0 (they will be updated later on update)
        this.equipMaxHealth = 0;
        this.equipAttack = 0;
        this.equipDefense = 0;
        this.equipCriticalChance = 0;
        this.equipCriticalMultiplier = 0;
        this.equipAutoAttack = 0;
        this.equipAutoSpeed = 0;
    }

    /**
     * Attributes constructor for static entities
     * with no dynamic progression and no use of equipment.
     *
     * @param maxHealth     the maximum health of an entity
     * @param attack        he attack attribute of an entity
     * @param defense       the defense attribute of an entity
     * @param criticalChance the chance of hitting a critical hit
     * @param criticalMultiplier the multiplier of damage in case of critical hit
     * @param autoAttack    the auto attack attribute of an entity
     * @param autoSpeed     the speed of auto attacks
     * @param level         the level of an entity
     * @param experience    the current experience of an entity
     */
    public Attributes(int maxHealth, int attack, int defense, float criticalChance,
                      float criticalMultiplier, int autoAttack, float autoSpeed, int level, long experience) {
        this.levelMaxHealth = maxHealth;
        this.levelAttack = attack;
        this.levelDefense = defense;
        this.levelCriticalChance = criticalChance;
        this.levelCriticalMultiplier = criticalMultiplier;
        this.levelAutoAttack = autoAttack;
        this.levelAutoSpeed = autoSpeed;
        this.level = level;
        this.experience = experience;
        // initializes equip effects with 0, no use of equipments equals no effect of equipments
        this.equipMaxHealth = 0;
        this.equipAttack = 0;
        this.equipDefense = 0;
        this.equipCriticalChance = 0;
        this.equipCriticalMultiplier = 0;
        this.equipAutoAttack = 0;
        this.equipAutoSpeed = 0;
    }

    /**
     * Update equipment effects to always match
     * the effects of currently equipped items
     * and current level (for dynamic entities)
     *
     * @param wornEquipment the array containing the current worn equipments
     */
    public void update(Equipment[] wornEquipment) {
        // resets effects
        this.equipMaxHealth = 0;
        this.equipAttack = 0;
        this.equipDefense = 0;
        this.equipCriticalChance = 0;
        this.equipCriticalMultiplier = 0;
        this.equipAutoAttack = 0;
        this.equipAutoSpeed = 0;
        // for all equipments effects, update
        for(int i = 0; i < wornEquipment.length; i++) {
            Equipment equip = wornEquipment[i];
            if(equip != null) { // if slot has equipment
                // updates with equipment effect
                this.equipMaxHealth += equip.getEffect().maxHealth;
                this.equipAttack += equip.getEffect().attack;
                this.equipDefense += equip.getEffect().defense;
                this.equipCriticalChance += equip.getEffect().critChance;
                this.equipCriticalMultiplier += equip.getEffect().critMult;
                this.equipAutoAttack += equip.getEffect().autoAttack;
                this.equipAutoSpeed += equip.getEffect().autoSpeed;
            }
        }
        // updates attributes based on level
        levelAttack = Math.round(Config.npAttack+ (Config.levelUpAtkInc * Config.levelUpScaleLevel * level));
        levelDefense = Math.round(Config.npDefense +  (Config.levelUpDefInc * Config.levelUpScaleLevel * level));
        levelMaxHealth = Math.round(Config.npMaxHealth + (Config.levelUpHealthInc * Config.levelUpScaleLevel * level));
        levelAutoAttack = Math.round(Config.npAutoAttack  + (Config.levelUpAutoAtkInc * Config.levelUpScaleLevel * level));
        levelAutoSpeed = Config.npAutoSpeed -  (Config.levelUpAutoSpdInc* Config.levelUpScaleLevel * level);
        levelCriticalChance = Config.npCriticalChance + (Config.levelUpCritChanceInc * Config.levelUpScaleLevel * level);
        levelCriticalMultiplier = Config.npCriticalMultiplier + (Config.levelUpCritInc * Config.levelUpScaleLevel * level);
    }

    /**
     * Getters and setters
     */

    public int getMaxHealth() {
        return levelMaxHealth+equipMaxHealth;
    }

    public int getAttack() {
        return levelAttack+equipAttack;
    }

    public int getDefense() {
        return levelDefense+equipDefense;
    }

    public int getAutoAttack() {
        return levelAutoAttack+equipAutoAttack;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public float getCriticalChance() {
        // clamp critical chances
        return MathUtils.clamp(levelCriticalChance+equipCriticalChance, 0f, 100f);
    }

    public float getCriticalMultiplier() {
        return levelCriticalMultiplier+equipCriticalMultiplier;
    }

    public float getAutoSpeed() {
        float autoSpeed = levelAutoSpeed - equipAutoSpeed;

        // clamp auto speed to obey min limit
        if(autoSpeed < Config.minAutoAtkSpeed)
            autoSpeed = Config.minAutoAtkSpeed;

        return autoSpeed;
    }

    public long getExp() {return experience; }

    /**
     * Adds experience to player's current experience amount
     * and checks if player has achieved new level
     * @param amount the amount of experience to be added
     */
    public void addExp(long amount) {
        experience+=amount; //adds experience
        int newLevel = Level.levelLog(experience); // calculates level
        // if new level is different, do level up operations
        if(level != newLevel) {
            levelUp(newLevel); // do level up operations
        }
        // updates level
        level = newLevel;
    }

    /**
     * Perform level up operations
     * @param level the newly achieved level
     */
    public void levelUp(int level) {
        //TODO - level up screen/notification,etc
        System.out.println("LEVEL UP !!!!! GRATZ !!!");
    }

    public void setExp(long experience) {this.experience = experience;}

    /**
     * Creates new player attributes and returns it
     * @return the attributes of a new player
     */
    public static Attributes createNewPlayer() {
        return new Attributes(Config.npLevel, Config.npExp);
    }
}
