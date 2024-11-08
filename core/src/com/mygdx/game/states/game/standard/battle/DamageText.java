package com.mygdx.game.states.game.standard.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.enemy.Enemy;
import com.mygdx.game.states.game.standard.player.Player;
import com.mygdx.game.util.Config;

/**
 * Class that provides a visual representation
 * of damages dealt by player and enemy
 *
 * @author Pedro Sampaio
 * @since 1.0
 */
public class DamageText {

    private int damage; // the damage to be displayed;
    private Object target; // the target of the damage dealt
    public enum TargetType {Player, Enemy} // enum with possible targets type
    private TargetType type; // type of target
    private SpriteBatch batch;   // batch to draw damage text
    private BitmapFont font; // font used in text display
    private boolean isFinished; // if text has finished displaying
    private Vector2 position; // current position of damage text
    private float initialY; // initial y to help calculate movement distance
    private boolean critical; // if it is a critical damage

    /**
     * Creates damage text object
     *
     * @param batch     batch to draw damage text
     * @param target    the target of the damage dealt
     * @param type      type of target
     * @param damage    the damage to be displayed;
     * @param critical  if it is a critical damage
     */
    public DamageText(SpriteBatch batch, BitmapFont font, Object target, TargetType type, int damage, boolean critical) {
        this.batch = batch;
        this.target = target;
        this.type = type;
        this.damage = damage;
        this.critical = critical;
        this.font = font;
        this.critical = critical;

        // load vars
        create();
    }

    /**
     * loads resources of damage text
     */
    public void create() {
        position = new Vector2(); // text pos

        // calculates initial text position based on target type
        if(type == TargetType.Enemy) {
            Enemy enemy = (Enemy) target;
            position = enemy.getBattlePos().cpy();
            position.x -= enemy.getFixedXWidth() / 2;
        } else if (type == TargetType.Player) {
            Player player = (Player) target;
            position = player.getBattlePos().cpy();
        }

        initialY = position.y;
    }

    /**
     * Renders damage text
     */
    public void render() {
        // updates text y position
        position.y += Gdx.graphics.getDeltaTime() * Config.dmgTextSpeed;

        // uses glyphlayout to get text bounds
        final GlyphLayout textLayout = new GlyphLayout(font, Integer.toString(damage));

        // checks if has surpassed limit
        if(!isFinished() && (position.y - initialY) >= Config.dmgMoveDistance)
            isFinished = true;

        float targetWidth = 0;
        float targetHeight = 0;

        if(type == TargetType.Enemy) {
            Enemy enemy = (Enemy) target;
            targetWidth = enemy.getWidth();
            targetHeight = enemy.getHeight();
        } else if (type == TargetType.Player) {
            Player player = (Player) target;
            targetWidth = player.getAnimator().getSprWidth();
            targetHeight = player.getAnimator().getSprHeight();
        }

        float textX = (position.x +(targetWidth - textLayout.width) / 2);
        float textY = (position.y +(targetHeight));

        // only draws if it is not finished
        if(!isFinished) {
            if(critical) // draw in yellow if its critical
                font.draw(batch, "[#fff600]" + Integer.toString(damage), textX, textY);
            else
                font.draw(batch, Integer.toString(damage), textX, textY);
        }
        else // disposes font
            dispose();
    }

    /**
     * dispose text resources
     */
    public void dispose() {
    }

    /**
     * gets if text has finished being displayed (surpassed movement length)
     * @return
     */
    public boolean isFinished() {
        return isFinished;
    }

}
