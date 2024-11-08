package com.mygdx.game.states.game.standard.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.animation.Animation;
import com.mygdx.game.states.game.standard.animation.Animator;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Player`s states class for animator
 * state machine that controls
 * sprites animations
 *
 * @author Pedro Sampaio
 * @since 0.6
 */
public class PlayerAnimator {

    /**
     * States of the animator state machine
     */
    public enum animStates {
        walkingLeft,
        walkingRight,
        idleRight,
        idleLeft,
        battleIdle,
        battleAttack,
        battleCriticalAttack
    }

    /**
     * Commands for transitions in animator state machine
     */
    public enum animCommands {
        walkLeft,
        walkRight,
        stopWalking,
        enterIdleBattle,
        exit,
        attack,
        criticalAttack
    }

    // The animator state machine to use for animation
    private Animator animator;
    // state transitions
    private java.util.Map<Animator.StateTransition, Enum<?>> transitions;
    // player animations
    private java.util.Map<Enum<?>, Animation> animations;
    // player sprite sheet
    private Texture spriteSheet;
    // player sprite width
    private int sprWidth;
    // player sprite height
    private int sprHeight;
    // player sprite scale
    private Vector2 sprScale;

    // animations time
    private float baseDelta = 1/10f;
    private float idleDeltaTime = baseDelta;
    private float walkDeltaTime = baseDelta/4f;
    private float attackDeltaTime = baseDelta/3f;

    /**
     * Constructor
     * Creates the transitions and animations of player animation
     *
     * @param sprScale scale factor for the player sprite rendering
     */
    public PlayerAnimator(int sprWidth, int sprHeight, Vector2 sprScale) {
        this.sprWidth = sprWidth;
        this.sprHeight = sprHeight;
        this.sprScale = sprScale;
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
        transitions.put(new Animator.StateTransition(animStates.walkingLeft, animCommands.walkRight), animStates.walkingRight);
        transitions.put(new Animator.StateTransition(animStates.walkingLeft, animCommands.stopWalking), animStates.idleLeft);
        transitions.put(new Animator.StateTransition(animStates.walkingRight, animCommands.walkLeft), animStates.walkingLeft);
        transitions.put(new Animator.StateTransition(animStates.walkingRight, animCommands.stopWalking), animStates.idleRight);
        transitions.put(new Animator.StateTransition(animStates.idleLeft, animCommands.walkLeft), animStates.walkingLeft);
        transitions.put(new Animator.StateTransition(animStates.idleLeft, animCommands.walkRight), animStates.walkingRight);
        transitions.put(new Animator.StateTransition(animStates.idleRight, animCommands.walkLeft), animStates.walkingLeft);
        transitions.put(new Animator.StateTransition(animStates.idleRight, animCommands.walkRight), animStates.walkingRight);
        // battle transitions
        transitions.put(new Animator.StateTransition(animStates.walkingLeft, animCommands.enterIdleBattle), animStates.battleIdle);
        transitions.put(new Animator.StateTransition(animStates.walkingRight, animCommands.enterIdleBattle), animStates.battleIdle);
        transitions.put(new Animator.StateTransition(animStates.idleLeft, animCommands.enterIdleBattle), animStates.battleIdle);
        transitions.put(new Animator.StateTransition(animStates.idleRight, animCommands.enterIdleBattle), animStates.battleIdle);
        transitions.put(new Animator.StateTransition(animStates.battleIdle, animCommands.attack), animStates.battleAttack);
        transitions.put(new Animator.StateTransition(animStates.battleIdle, animCommands.criticalAttack), animStates.battleCriticalAttack);
        transitions.put(new Animator.StateTransition(animStates.battleIdle, animCommands.stopWalking), animStates.idleRight);
        transitions.put(new Animator.StateTransition(animStates.battleIdle, animCommands.walkLeft), animStates.walkingLeft);
        transitions.put(new Animator.StateTransition(animStates.battleIdle, animCommands.walkRight), animStates.walkingRight);
        transitions.put(new Animator.StateTransition(animStates.battleAttack, animCommands.enterIdleBattle), animStates.battleIdle);
        transitions.put(new Animator.StateTransition(animStates.battleAttack, animCommands.stopWalking), animStates.idleRight);
        transitions.put(new Animator.StateTransition(animStates.battleAttack, animCommands.walkLeft), animStates.walkingLeft);
        transitions.put(new Animator.StateTransition(animStates.battleAttack, animCommands.walkRight), animStates.walkingRight);
        transitions.put(new Animator.StateTransition(animStates.battleAttack, animCommands.attack), animStates.battleAttack);
        transitions.put(new Animator.StateTransition(animStates.battleAttack, animCommands.exit), animStates.battleIdle);
        transitions.put(new Animator.StateTransition(animStates.battleAttack, animCommands.criticalAttack), animStates.battleCriticalAttack);

        transitions.put(new Animator.StateTransition(animStates.battleCriticalAttack, animCommands.enterIdleBattle), animStates.battleIdle);
        transitions.put(new Animator.StateTransition(animStates.battleCriticalAttack, animCommands.stopWalking), animStates.idleRight);
        transitions.put(new Animator.StateTransition(animStates.battleCriticalAttack, animCommands.walkLeft), animStates.walkingLeft);
        transitions.put(new Animator.StateTransition(animStates.battleCriticalAttack, animCommands.walkRight), animStates.walkingRight);
        transitions.put(new Animator.StateTransition(animStates.battleCriticalAttack, animCommands.attack), animStates.battleAttack);
        transitions.put(new Animator.StateTransition(animStates.battleCriticalAttack, animCommands.exit), animStates.battleIdle);
        transitions.put(new Animator.StateTransition(animStates.battleCriticalAttack, animCommands.criticalAttack), animStates.battleCriticalAttack);

    }
    /**
     * create animations
     */
    private void createAnimations() {
        // loads player sprite sheet
        spriteSheet = new Texture("imgs/characters/knightsheet.png");

        // sprite config based on sprite sheet
        int nFrames = 10;
        int x_ini = 4;
        int delta_x = 104;
        int sprOffY = 8;
        int sprOffXLW = -20;
        int sprOffXLI = -13;

        // animation for right idle state
        ArrayList<TextureRegion> rightIdleFrames;
        // retrieves animation sequence for right idle state
        rightIdleFrames = Common.getAnimationSequence(spriteSheet, nFrames, x_ini, delta_x,
                                                        136, 0, 0, sprWidth, sprHeight);

        // left idle animation
        ArrayList<TextureRegion> leftIdleFrames;
        // retrieves animation sequence for left idle state
        leftIdleFrames = Common.getAnimationSequence(spriteSheet, nFrames, x_ini, delta_x,
                                                        136, sprOffXLI, 0, sprWidth, sprHeight);

        // animation for walking right state
        ArrayList<TextureRegion> walkRightFrames;
        // retrieves animation sequence for right walking state
        walkRightFrames = Common.getAnimationSequence(spriteSheet, nFrames, x_ini, delta_x,
                                                        624, 0, sprOffY, sprWidth, sprHeight);

        // animation for walking left state
        ArrayList<TextureRegion> walkLeftFrames;
        // retrieves animation sequence for left walking state
        walkLeftFrames = Common.getAnimationSequence(spriteSheet, nFrames, x_ini, delta_x,
                                                        624, sprOffXLW, sprOffY, sprWidth, sprHeight);

        // animation for battle idle
        ArrayList<TextureRegion> battleIdleFrames;
        // retrieves animation sequence for battle idle state
        battleIdleFrames = Common.getAnimationSequence(spriteSheet, nFrames, x_ini, delta_x,
                                                        136, 0, 0, sprWidth, sprHeight);

        // animation for battle attack
        ArrayList<TextureRegion> battleAttackFrames;
        // retrieves animation sequence for battle idle state
        battleAttackFrames = Common.getAnimationSequence(spriteSheet, nFrames, x_ini, delta_x,
                                                            4, 0, sprOffY, sprWidth, sprHeight);

        // animation for battle critical attack (jump attack animation)
        ArrayList<TextureRegion> btCritAttackFrames;
        // retrieves animation sequence for battle idle state
        btCritAttackFrames = Common.getAnimationSequence(spriteSheet, nFrames, x_ini, delta_x,
                376, -5, sprOffY, sprWidth, sprHeight);

        // creates right idle animation
        Animation iRightAnim = new Animation(rightIdleFrames, idleDeltaTime, 0);
        iRightAnim.flipVertical();

        // creates left idle animation
        Animation iLeftAnim = new Animation(leftIdleFrames, idleDeltaTime, 0);
        iLeftAnim.flipVerticalHorizontal();

        // creates walk left animation
        Animation wLeftAnim = new Animation(walkLeftFrames, walkDeltaTime, 0);
        wLeftAnim.flipVerticalHorizontal();

        // creates walk right animation
        Animation wRightAnim = new Animation(walkRightFrames, walkDeltaTime, 0);
        wRightAnim.flipVertical();

        // creates battle idle animation
        Animation battleIdleAnim = new Animation(battleIdleFrames, idleDeltaTime, 0);

        // creates battle attack animation as non loopable
        Animation battleAttackAnim = new Animation(battleAttackFrames, attackDeltaTime, 0, false);

        // creates battle critical attack animation as non loopable
        Animation battleCritAttackAnim = new Animation(btCritAttackFrames, attackDeltaTime, 0, false);

        // attaches idle animation to idle state
        animations.put(animStates.idleRight, iRightAnim);
        animations.put(animStates.idleLeft, iLeftAnim);
        animations.put(animStates.walkingLeft, wLeftAnim);
        animations.put(animStates.walkingRight, wRightAnim);
        animations.put(animStates.battleIdle, battleIdleAnim);
        animations.put(animStates.battleAttack, battleAttackAnim);
        animations.put(animStates.battleCriticalAttack, battleCritAttackAnim);
    }

    /**
     * Starts animator state machine
     * @param initialState  initial state of animator state machine
     */
    public void startAnimator (animStates initialState) {
        // creates animator with initial state and possible transitions
        // passing necessary information about states and commands
        animator = new Animator(animStates.class, animCommands.class, initialState, transitions, animations);
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
    public void triggerCommand(animCommands command) {
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

    // disposes spritesheet
    public void dispose() {
        spriteSheet.dispose();
    }

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
    public float getScaledSprWidth() {return sprWidth * sprScale.x;}
    public float getScaledSprHeight() {return sprHeight * sprScale.y;}
    public float getSprWidth() {return sprWidth;}
    public float getSprHeight() {return sprHeight;}
    public Vector2 getSprScale() {return sprScale;}
    public Texture getSpriteSheet() {return spriteSheet;}
}