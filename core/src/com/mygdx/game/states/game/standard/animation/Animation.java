package com.mygdx.game.states.game.standard.animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

/**
 * Animation class that holds animation frames
 * and information of a animated game sprite
 *
 * @author Pedro Sampaio
 * @since 0.6
 */
public class Animation {

    private ArrayList<TextureRegion> frames;    // frames (sprites) of the animation
    private float timeBetweenFrames;            // time between each frame of the animation
    private float animTimer;                    // timer to control frame altering
    private int initialFrame;                   // initial frame of the animation
    private int currentFrame;                   // current frame of the animation
    private boolean loop;                       // if this animation is to be looped
    private boolean finished;                   // represents if animation has finished (for not loopable anim)

    /**
     * Constructor receiving all needed parameters except loop parameter
     * (set to true by default)
     *
     * @param frames            // frames (sprites) of the animation
     * @param timeBetweenFrames // time between each frame of the animation
     * @param initialFrame      // initial frame of the animation
     */
    public Animation (ArrayList<TextureRegion> frames, float timeBetweenFrames, int initialFrame) {
        this.frames = frames;
        this.timeBetweenFrames = timeBetweenFrames;
        this.initialFrame = initialFrame;
        this.animTimer = 0f;
        this.loop = true;
        this.finished = false;
    }

    /**
     * Constructor receiving all needed parameters
     *
     * @param frames            // frames (sprites) of the animation
     * @param timeBetweenFrames // time between each frame of the animation
     * @param initialFrame      // initial frame of the animation
     * @param loop              // param that indicates if this animation is to be looped
     */
    public Animation (ArrayList<TextureRegion> frames, float timeBetweenFrames, int initialFrame, boolean loop) {
        this.frames = frames;
        this.timeBetweenFrames = timeBetweenFrames;
        this.initialFrame = initialFrame;
        this.animTimer = 0f;
        this.loop = loop;
        this.finished = false;
    }

    /**
     * Updates animation frames
     */
    public void update() {
        if(!finished) { // in case of not loopable animation
            // updates timer
            animTimer += Gdx.graphics.getDeltaTime();
            // changes sprite if time between frame has been surpassed
            if (animTimer >= timeBetweenFrames) {
                animTimer = 0f;  // resets timer
                currentFrame++;  // increases animation frame
                // checks if has surpassed last frame of anim
                if (currentFrame >= frames.size()) {
                    if(loop)
                        currentFrame = 0;   // loop animation
                    else {
                        currentFrame--; // avoid getting out of bounds
                        finished = true; // finish animation
                    }
                }
            }
        }
    }

    /**
     * Flips texture regions vertically
     */
    public void flipVertical() {
        for(int i = 0; i < frames.size(); i++)
            frames.get(i).flip(false, true);
    }

    /**
     * Flips texture regions horizontally
     */
    public void flipHorizontal() {
        for(int i = 0; i < frames.size(); i++)
            frames.get(i).flip(true, false);
    }

    /**
     * Flips texture regions both vertically and horizontally
     */
    public void flipVerticalHorizontal() {
        for(int i = 0; i < frames.size(); i++)
            frames.get(i).flip(true, true);
    }

    /**
     * Returns the current sprite of animation
     * @return the current texture region of the animation
     */
    public TextureRegion getAnimSprite() {
        return frames.get(currentFrame);
    }

    /**
     * Resets animation to its initial frame
     * and sets finished to false to be able to animate
     */
    public void resetAnimation() {
        currentFrame = initialFrame;
        finished = false;
    }

    /**
     * Gets current time between frames of animation
     * @return  a float value that represents time between each frame in seconds
     */
    public float getTimeBetweenFrames() {
        return timeBetweenFrames;
    }

    /**
     * Sets a new time between frames of animation
     * @param timeBetweenFrames a float value that will be the time between each frame in seconds
     */
    public void setTimeBetweenFrames(float timeBetweenFrames) {
        this.timeBetweenFrames = timeBetweenFrames;
    }

    /**
     * Returns if animation is finished (in case of non loopable animations)
     * @return true if finished, false otherwise
     */
    public boolean isFinished() {
        return finished;
    }
}
