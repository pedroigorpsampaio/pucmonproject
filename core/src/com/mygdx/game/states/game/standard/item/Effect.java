package com.mygdx.game.states.game.standard.item;

import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

/**
 * Class that represents the effect
 * of an item on an entity
 *
 * @author Pedro Sampaio
 * @since   1.3
 */
public class Effect {
    public int maxHealth;  // the effect on the maximum health of an entity
    public int attack;     // the effect on the attack attribute of an entity
    public int defense;    // the effect on the defense attribute of an entity
    public float critChance; // the effect on the chance of hitting a critical hit of an entity
    public float critMult; // the effect on the multiplier of damage in case of critical hit of an entity
    public int autoAttack; // the effect on the auto attack attribute of an entity
    public float autoSpeed; // the effect on the speed of auto attacks of an entity
    public float speed;    // the effect on the speed of an entity
    public float rateGold; // the effect on the rate of gold drop of an entity
    public float rateExp; // the effect on the rate of exp of an entity
    public float rateDrop; // the effect on the rate of drops of an entity

    /**
     * Effects constructor sets all effects to 0 (no effect)
     */
    public Effect() {
        this.maxHealth = 0;
        this.attack = 0;
        this.defense = 0;
        this.critChance = 0;
        this.critMult = 0;
        this.autoAttack = 0;
        this.autoSpeed = 0;
        this.speed = 0;
        this.rateGold = 0;
        this.rateExp = 0;
        this.rateDrop = 0;
    }

}
