package com.mygdx.game.states.game.standard.enemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.architecture.GameLoop;
import com.mygdx.game.states.game.standard.battle.Attributes;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

/**
 * Class that represents an enemy with all
 * its information and stats
 *
 * @author Pedro Sampaio
 * @since 0.9
 */
public class Enemy implements GameLoop {

    Attributes attributes;  // the battle attributes of this enemy
    String name;            // the enemy name
    Texture enemySheet;   // the sprite sheet of enemies
    EnemyType type;         // the type of enemy
    Vector2 sprScale;         // the scale to apply to enemy sprite
    EnemyAnimator eAnimator; // enemy animator machine
    Vector2 battlePos; // enemy battle position
    Color currentColor; // enemy current color tint
    Reward reward;  // enemy reward
    private float fixedXWidth; // enemy fixed X width for damage text

    // enemy types
    public enum EnemyType {Skeleton1, Ogre1, Ogre2, Minotaur1, Minotaur2, Minotaur3, Goblin1, Goblin2}

    /**
     * Enemy constructor
     * @param attributes    the battle attributes of this enemy
     * @param name          the enemy name
     * @param reward        the enemy reward
     * @param enemySheet    the sprite sheet of enemies
     * @param eType         the type of enemy spawned
     * @param sprScale      the scale to apply to enemy sprite
     */
    public Enemy(Attributes attributes, String name, Reward reward,
                 Texture enemySheet, EnemyType eType, Vector2 sprScale) {
        this.attributes = attributes;
        this.name = name;
        this.reward = reward;
        this.enemySheet = enemySheet;
        this.type = eType;
        this.sprScale = sprScale;
        create(); // loads enemy resources
    }

    @Override
    public void create() {
        // initially color is white so no tint is applied
        currentColor = Color.WHITE;
        // creates player animator (wraps animator processes)
        eAnimator = new EnemyAnimator(enemySheet, type, sprScale);
        // starts animator on idle state
        eAnimator.startAnimator(EnemyAnimator.animStates.idle);
        // calculate enemy battle position
        battlePos = new Vector2(Config.baseWidth*0.9f, Config.baseHeight*0.32f);
        // adjusts enemy positioning
        fixedXWidth = eAnimator.getScaledSprWidth();
        battlePos.x -= fixedXWidth / 2;
        battlePos.y += eAnimator.getScaledSprHeight() / 2;
    }

    @Override
    public void update() {
        // always try to interpolate to white, in case of damaged tint color is applied
        currentColor = Common.lerpColor(currentColor, Color.WHITE, Config.dmgColorSpeed);

        // updates animator
        eAnimator.update();
    }

    @Override
    public void render(){}

    /**
     * Renders enemies in battle
     * @param battleBatch the sprite batch to draw enemy into
     * @param scale the scale to draw enemy with
     */
    public void renderBattle(SpriteBatch battleBatch, float scale) {
        // current sprite depending on current animation state and frame
        TextureRegion currSprite = eAnimator.getCurrentSprite();

        Sprite enemySpr = new Sprite(currSprite);
        enemySpr.setSize(eAnimator.getScaledSprWidth()*scale, eAnimator.getScaledSprHeight()*scale);
        enemySpr.setPosition(battlePos.x - (enemySpr.getWidth() / 2), battlePos.y);
        enemySpr.setColor(currentColor);
        enemySpr.draw(battleBatch);
    }

    /**
     * Do enemy hurt related operations
     */
    public void hurt() {
        // applies tint to enemy sprite
        currentColor = Config.enemyDamagedColor.cpy();
    }

    @Override
    public void dispose() {
        eAnimator.dispose();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Getters and setters
     */

    public Vector2 getBattlePos() {return battlePos;}

    public Attributes getAttributes() {
        return attributes;
    }

    public float getWidth() {return eAnimator.getSprWidth();}

    public float getHeight() {return eAnimator.getSprHeight();}

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnemyAnimator getAnimator() {
        return eAnimator;
    }

    public Reward getReward() {return reward; }

    public float getFixedXWidth() {return fixedXWidth;}

}
