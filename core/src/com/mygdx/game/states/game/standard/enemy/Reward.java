package com.mygdx.game.states.game.standard.enemy;

/**
 * Class that represents an enemy`s reward
 * that can be gold, experience and items.
 *
 * @author  Pedro Sampaio
 * @since   1.1
 */
public class Reward {
    private int experience;     // the amount of experience this enemy gives
    private int gold;           // the amount of gold this enemy gives

    // items will be added soon

    /**
     * Reward constructor
     * @param experience    the amount of experience this enemy gives
     * @param gold          the amount of gold this enemy gives
     */
    public Reward(int experience, int gold) {
        this.experience = experience;
        this.gold = gold;
    }

    /**
     * Getters and Setters
     */

    public int getExp() {
        return experience;
    }

    public void setExp(int experience) {
        this.experience = experience;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }
}
