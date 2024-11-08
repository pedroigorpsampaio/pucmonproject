package com.mygdx.game.states.game.standard.battle;

import com.mygdx.game.states.game.standard.player.Player;
import com.mygdx.game.util.Config;

/**
 * Class that hold level calculation formulas
 *
 * @author  Pedro Sampaio
 * @since   1.1
 */
public class Level {

    /**
     * Applies a logarithm curve to levelling progress
     * @param exp   the current amount of exp
     * @return  the level of the current amount of exp in the logarithm curve
     */
    public static int levelLog(long exp) {
        return (int)Math.floor(Math.max( Math.floor(Config.logConstA * Math.log( exp + Config.logConstC ) + Config.logConstB ), 1));
    }

    /**
     * Gets the necessary experience to achieve
     * a certain level on the logarithm curve
     * @param level the level to get experience needed
     * @return the experience needed to achieve level received in parameter
     */
    public static long expLog(int level) {
        return (long)Math.floor(Math.exp((level - Config.logConstB) / Config.logConstA) - Config.logConstC);
    }

    /**
     * Calculates the current percentage of experience
     * player has in current level based on next level needed exp
     *
     * @param player the player reference to calculate exp percent
     * @return the percentage player has in current level in relation to the next level
     */
    public static float expPercent(Player player) {
        // next level needed experience
        float expToNextLevel = Level.expLog(player.getAttributes().getLevel()+1) -
                Level.expLog(player.getAttributes().getLevel());
        // exp gained in current level
        float expGainedInCurrLvl = player.getAttributes().getExp() - Level.expLog(player.getAttributes().getLevel());
        // calculates player experience percentage
        float expPercent = expGainedInCurrLvl / (expToNextLevel+1);

        // return exp percent
        return expPercent;
    }
}
