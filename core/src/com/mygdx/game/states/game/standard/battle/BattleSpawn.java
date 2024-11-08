package com.mygdx.game.states.game.standard.battle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.enemy.Enemy;
import com.mygdx.game.states.game.standard.enemy.NameGenerator;
import com.mygdx.game.states.game.standard.enemy.Reward;
import com.mygdx.game.states.game.standard.player.Player;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

/**
 * Spawns a battle with a random enemy.
 * Random enemies are scaled to player`s current level
 *
 * Pattern:
 * Singleton
 *
 * @author Pedro Sampaio
 * @since 0.9
 */
public class BattleSpawn {
    private NameGenerator enemyNameGen; // enemy name generator
    private static BattleSpawn instance = null; // singleton instance

    /**
     * Battle spawn constructor defeats external instantiation
     */
    private BattleSpawn() {
        // initializes enemy name generator
        enemyNameGen = new NameGenerator("enemies", 1, 3);

    }

    /**
     * Gets BattleSpawn singleton instance
     * @return battlespawn singleton instance
     */
    public static BattleSpawn getInstance() {
        if(instance == null)
            instance = new BattleSpawn();

        return instance;
    }

    /**
     * Spawns a battle scaled to player`s current level
     * @param player    the player's reference
     * @param enemySheet the reference to enemies sprite sheet
     * @return the battle spawned
     */
    public static Battle spawn(Player player, Texture enemySheet) {
        // generates enemy attributes based on player's attribute
        float randFactor = Common.randFloat(Config.eScaleMinFactor, Config.eScaleMaxFactor);
        // applies randomly generated factor to level
        float eLevel = player.getAttributes().level * randFactor * Config.eScaleLevel;
        // creates attributes with generated level as base
        float eMaxHealth = Config.eBaseMaxHealth + (eLevel * Config.eScaleLevelFactor * randFactor * Config.eMaxHealthScale);
        float eAutoAttack = Config.eBaseAutoAtk + (eLevel * Config.eScaleLevelFactor * randFactor * Config.eAtkScale);
        float eDefense =  Config.eBaseDefense + (eLevel * Config.eScaleLevelFactor * randFactor * Config.eDefScale);
        float eAutoSpeed = Config.eBaseAtkSpeed - (randFactor * eLevel * Config.eAtkSpeedScale * Config.eScaleLevelFactor);

        // clamp minimum level of 1
        if(eLevel < 1)
            eLevel = 1;

        // creates enemy attributes
        Attributes enemyAttr = new Attributes((int) eMaxHealth, 0, (int) eDefense, 0, 1,
                                                (int)eAutoAttack, eAutoSpeed, (int) eLevel, 0);

        // calculates enemy sprite scale based on enemy level
        Vector2 sprScale = new Vector2(Config.eSprInitialScale + (eLevel * Config.eSprLevelScale),
                                        Config.eSprInitialScale + (eLevel * Config.eSprLevelScale));

        // generates an name for the enemy
        String name = getInstance().enemyNameGen.nextName();

        // clamp max name size
        if(name.length() > Config.maxEnemyNameChars)
            name = name.substring(0, Config.maxEnemyNameChars);

        // generates enemy reward
        int exp = MathUtils.round((randFactor * Config.baseExpAmount +
                                    (eLevel * Config.levelExpFactor)) * Config.expRate);
        int gold = MathUtils.round((randFactor * Config.baseGoldAmount +
                                    (eLevel * Config.levelGoldFactor)) * Config.goldRate);

        Reward reward = new Reward(exp, gold);

        // Randomly chooses a type of enemy
        Enemy.EnemyType eType = Common.randomEnum(Enemy.EnemyType.class);

        // creates enemy with random properties generated
        Enemy enemy = new Enemy(enemyAttr, name, reward, enemySheet, eType, sprScale);

        // randomly chose a background
        int bgIndex = Common.randInt(0, Resource.battleBGs.size()-1);

        // returns a newly created battle with generated data
        return new Battle(enemy, player, bgIndex);
    }

}
