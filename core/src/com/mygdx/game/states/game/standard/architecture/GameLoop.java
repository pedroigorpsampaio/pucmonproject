package com.mygdx.game.states.game.standard.architecture;

/**
 * An interface that represents a game loop
 * in the project with essential callbacks.
 * It differs from GameState interface because
 * it does not need to represent a rendering state
 * with all needed callbacks, as it can be implemented
 * by any single entity of a rendering state
 *
 * @author Pedro Sampaio
 * @since 0.4
 */
public interface GameLoop {
    /**
     *  Called for initialization purposes
     */
    void create();

    /**
     *  Called for update purposes
     */
    void update();

    /**
     * Called for rendering purposes
     */
    void render();

    /**
     * Called for disposing purposes
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
