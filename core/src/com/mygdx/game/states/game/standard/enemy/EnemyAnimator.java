package com.mygdx.game.states.game.standard.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.animation.Animation;
import com.mygdx.game.states.game.standard.animation.Animator;
import com.mygdx.game.states.game.standard.physics.Collider;
import com.mygdx.game.states.game.standard.player.PlayerAnimator;
import com.mygdx.game.util.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Enemy states class for animator
 * state machine that controls
 * sprites animations
 *
 * @author Pedro Sampaio
 * @since 1.0
 */
public class EnemyAnimator {
    private Enemy.EnemyType enemyType;

    /**
     * States of the animator state machine
     */
    public enum animStates {
        idle,
        dead
    }

    /**
     * Commands for transitions in animator state machine
     */
    public enum animCommands {
        enterIdle,
        die,
        exit
    }

    // The animator state machine to use for animation
    private Animator animator;
    // state transitions
    private java.util.Map<Animator.StateTransition, Enum<?>> transitions;
    // enemy animations
    private java.util.Map<Enum<?>, Animation> animations;
    // enemy sprite sheet
    private Texture spriteSheet;
    // enemy sprite width
    private int sprWidth;
    // enemy sprite height
    private int sprHeight;
    // enemy sprite scale
    private Vector2 sprScale;

    // animations time
    private float baseDelta = 1/10f;
    private float idleDeltaTime = baseDelta;

    /**
     * Constructor
     * Creates the transitions and animations of enemy animation
     *
     * @param sprScale scale factor for the enemy sprite rendering
     */
    public EnemyAnimator(Texture spriteSheet, Enemy.EnemyType enemyType, Vector2 sprScale) {
        this.sprWidth = sprWidth;
        this.sprHeight = sprHeight;
        this.sprScale = sprScale;
        this.spriteSheet = spriteSheet;
        this.enemyType = enemyType;
        // initializes transitions map
        transitions = new HashMap<Animator.StateTransition, Enum<?>>();
        // initializes animations map
        animations = new HashMap<Enum<?>, Animation>();

        // creates possible transitions (state + command -> new state)
        createTransitions();

        // create animations
        createAnimations();
    }

    /**
     * create state transitions
     */
    private void createTransitions() {
    }
    /**
     * create animations
     */
    private void createAnimations() {
        // animation for idle state
        ArrayList<TextureRegion> idleFrames = new ArrayList<TextureRegion>();

        //access enemies meta data file
        // the file containing the information to be loaded
        InputStream metaFile = Gdx.files.internal("imgs/enemies/enemies.txt").read();
        // buffered reader to read meta data file
        BufferedReader metaReader = new BufferedReader(new InputStreamReader(metaFile));
        // iterates through lines to get spritesheet metadata
        String line;
        try {
            while ((line = metaReader.readLine()) != null) {
                if(enemyType.toString().equals(line)) { // found an enemy frame
                    metaReader.readLine(); // discards rotation
                    // get position (x,y)
                    String linePos = metaReader.readLine();
                    // get size (width,height)
                    String lineSize = metaReader.readLine();
                    // parse position
                    int x = Integer.parseInt(linePos.substring(linePos.indexOf(':')+1, linePos.indexOf(',')).trim());
                    int y = Integer.parseInt(linePos.substring(linePos.indexOf(',')+1).trim());
                    // parse size
                    sprWidth = Integer.parseInt(lineSize.substring(lineSize.indexOf(':')+1, lineSize.indexOf(',')).trim());
                    sprHeight = Integer.parseInt(lineSize.substring(lineSize.indexOf(',')+1).trim());
                    // adds new frame to animation
                    idleFrames.add(new TextureRegion(spriteSheet, x, y, sprWidth, sprHeight));
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read file: imgs/enemies/enemies.txt");
            e.printStackTrace();
        }

        // creates battle critical attack animation
        Animation idleAnim = new Animation(idleFrames, idleDeltaTime, 0, true);

        // attaches idle animation to idle state
        animations.put(EnemyAnimator.animStates.idle, idleAnim);
    }

    /**
     * Starts animator state machine
     * @param initialState  initial state of animator state machine
     */
    public void startAnimator (EnemyAnimator.animStates initialState) {
        // creates animator with initial state and possible transitions
        // passing necessary information about states and commands
        animator = new Animator(EnemyAnimator.animStates.class, EnemyAnimator.animCommands.class, initialState, transitions, animations);
    }

    /**
     * updates animator in order to
     * update current state animation
     */
    public void update() {
        animator.update();
    }

    /**
     * Trigger a command in the animator state machine
     * @param command the command to be triggered in animator state machine
     */
    public void triggerCommand(PlayerAnimator.animCommands command) {
        // sends command to animator state machine
        animator.getNext(command);
    }

    /**
     * Gets the current sprite to be rendered
     * on screen accordingly to current state and animation frame
     * @return the current texture region(sprite) to draw on screen
     */
    public TextureRegion getCurrentSprite() {
        return animator.getCurrentAnimation().getAnimSprite();
    }

    public void dispose() {}

    /**
     * getters and setters
     */

    /**
     * gets current animation
     * @return current animation of animator
     */
    public Animation getCurrentAnimation() {return animator.getCurrentAnimation();}

    /**
     * Returns the current state of animator
     * @return the current state of animator
     */
    public Enum<?> getCurrentState() {return animator.getCurrentState();}
    public float getScaledSprWidth() {
        return  animator.getCurrentAnimation().getAnimSprite().getRegionWidth() * sprScale.x;}
    public float getScaledSprHeight() {
        return animator.getCurrentAnimation().getAnimSprite().getRegionHeight() * sprScale.y;}
    public float getSprWidth() {return sprWidth;}
    public float getSprHeight() {return sprHeight;}
    public Vector2 getSprScale() {return sprScale;}
}
