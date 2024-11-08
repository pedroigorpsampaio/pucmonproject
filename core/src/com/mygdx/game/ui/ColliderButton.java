package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.physics.Collider;

/**
 * Represents a button with its visualization.
 * It also stores a collider that wraps the button.
 *
 * @author Pedro Sampaio
 * @since 1.8
 */
public abstract class ColliderButton {
    private Vector2 position; // position of button
    private float width; // the width of button
    private float height; // the height of button
    private TextureRegion upButtonTex; // up texture of button
    private TextureRegion downButtonTex; // down texture of button
    private BitmapFont font; // font to draw text of button with
    private String buttonTextKey; // text key of button
    private Collider collider; // collider of button
    private boolean pressed; // is the button pressed atm
    private float pressedMoveY = 1f; // the amount of movement applied on Y axis on button press
    private Vector2 basePosition; // base position of button (should not be changed, only on initialization)
    private float baseWidth; // base width of button (should not be changed, only on initialization)
    private float baseHeight; // base height of button (should not be changed, only on initialization)
    private boolean hasText; // does this collider button has text?

    /**
     * ColliderButton constructor
     * stores references and create collider in initial state
     * @param x the x position of button
     * @param y the y position of button
     * @param width the width of button
     * @param height the height of button
     * @param upButtonTex   up button (unpressed) texture
     * @param downButtonTex down button (pressed) texture
     * @param font  the font for the text of button
     * @param buttonTextKey    text key of button
     */
    public ColliderButton(float x, float y, float width, float height,
                          TextureRegion upButtonTex, TextureRegion downButtonTex,
                          BitmapFont font, String buttonTextKey) {
        // stores parameters data received
        this.position = new Vector2(x, y);
        this.basePosition = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.baseWidth = width;
        this.baseHeight = height;
        this.upButtonTex = upButtonTex;
        this.downButtonTex = downButtonTex;
        this.font = font;
        this.buttonTextKey = buttonTextKey;
        // creates collider
        this.collider = new Collider(position.x, position.y, width, height, false, this);
        // initially is pressed is false
        pressed = false;
        // has text
        hasText = true;
    }

    public ColliderButton(float x, float y, float width, float height,
                          TextureRegion upButtonTex, TextureRegion downButtonTex) {
        // stores parameters data received
        this.position = new Vector2(x, y);
        this.basePosition = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.baseWidth = width;
        this.baseHeight = height;
        this.upButtonTex = upButtonTex;
        this.downButtonTex = downButtonTex;
        // creates collider
        this.collider = new Collider(position.x, position.y, width, height, false, this);
        // initially is pressed is false
        pressed = false;
        // does not has text
        hasText = false;
    }

    /**
     * Sets button position
     * updating collider
     * @param x the x position of the button
     * @param y the y position of the button
     */
    public void setPosition(float x, float y) {
        this.position = new Vector2(x, y); // updates position
        this.collider.setPosition(x, y); // updates collider
    }

    /**
     * Sets dimension of button
     * updating collider
     * @param width     the width of the button
     * @param height    the height of the button
     */
    public void setDimension(float width, float height) {
        // updates dimension of button
        this.width = width;
        this.height = height;
        // updates collider dimension
        this.collider.setWidth(width);
        this.collider.setHeight(height);
    }

    /**
     * Returns if touch received via parameter
     * presses this button
     * @param touchX the x position of touch to check collision
     * @param touchY the y position of touch to check collision
     * @return true if button is pressed by parameter touch, false otherwise
     */
    public boolean isPressed(float touchX, float touchY) {
        pressed = collider.checkTouchCollision(new Vector2(touchX, touchY));
        return pressed;
    }

    /**
     * Must be called when there is a touch on screen
     * @param touchX   the x position of touch
     * @param touchY   the y position of touch
     */
    public void touchDown(float touchX, float touchY) {
        pressed =  collider.checkTouchCollision(new Vector2(touchX, touchY));
        onPress();
    }

    /**
     * Must be called when there is a touch lift on screen
     * @param touchX   the x position of touch lift
     * @param touchY   the y position of touch lift
     */
    public void touchUp(float touchX, float touchY) {
        if(!pressed)
            return;

        pressed = false;
        if(collider.checkTouchCollision(new Vector2(touchX, touchY)))
            onRelease();
    }

    /**
     * Abstract implementation to deal with button
     * operations on press. Called when there is
     * a touch that intersects button collider
     */
    public abstract void onPress();

    /**
     * Abstract implementation to deal with button
     * operations on release. Called when there is
     * a touch lift that intersects button collider
     */
    public abstract void onRelease();


    /**
     * Renders button on batch received in parameter
     * @param batch the batch to render button
     */
    public void render(SpriteBatch batch) {
        if(pressed) // if pressed atm, renders pressed texture
            batch.draw(downButtonTex, position.x, position.y-pressedMoveY, width, height);
        else // if not pressed, renders unpressed texture
            batch.draw(upButtonTex, position.x, position.y, width, height);
        // renders text in the middle of button (if text is enabled)
        if(hasText) {
            final GlyphLayout textLayout = new GlyphLayout(font, Main.getInstance().getLang().get(buttonTextKey));
            float textX = position.x + (width / 2) - (textLayout.width / 2);
            float textY = position.y + (height / 2) + (textLayout.height / 2);
            font.draw(batch, Main.getInstance().getLang().get(buttonTextKey), textX, textY - (pressed ? pressedMoveY : 0));
        }
    }


    /**
     * Renders button on batch received in parameter
     * in camera relative position based on camera coordinates
     * received in parameters
     *
     * @param batch the batch to render button
     * @param camX  the x position of camera
     * @param camY  the y position of camera
     */
    public void render(SpriteBatch batch, float camX, float camY) {

    }

    /**
     * Getters and setters
     */

    public float getPressedMoveY() {
        return pressedMoveY;
    }

    public void setPressedMoveY(float pressedMoveY) {
        this.pressedMoveY = pressedMoveY;
    }

    public Vector2 getPosition() {return position;}

    public Vector2 getBasePosition() {return basePosition;}

    public float getBaseWidth() {return baseWidth;}

    public float getBaseHeight() {return baseHeight;}

}
