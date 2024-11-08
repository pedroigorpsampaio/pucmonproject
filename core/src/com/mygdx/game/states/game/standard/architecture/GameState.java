package com.mygdx.game.states.game.standard.architecture;

/**
 * An interface for the modules that will represent
 * a game state to be rendered on the screen,
 * following LibGDX's gameloop and input control
 *
 * @author Pedro Sampaio
 * @since   0.2
 */
public interface GameState {

    /**
     *  Called when the Application is first created.
     */
    void create();

    /**
     *  Called when the Application is resized.
     *  This can happen at any point during a non-paused state
     *  but will never happen before a call to create()
     *
     * @param width the new width in pixels
     * @param height the new height in pixels
     */
    void resize(int width,
                int height);

    /**
     * Called when the Application should render itself.
     */
    void render();

    /**
     * Called when the Application is paused,
     * usually when it's not active or visible on screen.
     * An Application is also paused before it is destroyed.
     */
    void pause();

    /**
     * Called when the Application is resumed from a paused state,
     * usually when it regains focus.
     */
    void resume();

    /**
     * Called when the Application is destroyed.
     * Preceded by a call to pause().
     */
    void dispose();

    /**
     * Called when the screen was touched or a mouse button was pressed.
     * The button parameter will be Input.Buttons.LEFT on iOS.
     *
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event
     * @param button the mouse button
     * @return  whether the input was processed
     */
    boolean touchDown(int screenX, int screenY, int pointer, int button);

    /**
     * Called when a finger was lifted or a mouse button was released.
     * The button parameter will be Input.Buttons.LEFT on iOS.
     *
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event
     * @param button the mouse button
     * @return whether the input was processed
     */
    boolean touchUp(int screenX, int screenY, int pointer, int button);

    /**
     * Called when a finger or the mouse was dragged.
     *
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event
     * @return whether the input was processed
     */
    boolean touchDragged(int screenX, int screenY, int pointer);
}