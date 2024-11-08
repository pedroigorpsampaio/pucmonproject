package com.mygdx.game.states.game.standard.camera;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.map.Map;
import com.mygdx.game.states.game.standard.physics.Transform;

/**
 * Class that represents the game camera
 * that will aid in game rendering
 *
 * @author Pedro Sampaio
 * @since 0.3
 */
public class GameCamera {

    private float zoom;      // camera zoom
    private Transform target;       // camera target
    private Vector2 position;       // camera position
    private float height;             // camera viewport height
    private float width;              // camera viewport width
    private int mapSizeX;           // world size in the x axis (columns)
    private int mapSizeY;           // world size in the y axis (lines)
    private int tileSize;           // the size of a tile in the game world

    // camera limits
    // The lower limit will nearly always be (0,0), and in this case the upper limit
    // is equal to the size of the world minus the size of the camera's viewport.
    private float max_x;
    private float max_y;

    /**
     * Game Camera constructor
     * @param target        the target that the camera will follow
     * @param width         the width of camera's viewport
     * @param height        the height of camera's viewport
     * @param tileSize      the size of a tile in the game world
     * @param zoom          the zoom of the camera
     * @param mapSizeX      world size in the x axis (columns)
     * @param mapSizeY      world size in the y axis (lines)
     */
    public GameCamera(Transform target, float width, float height, int tileSize, float zoom,
                      int mapSizeX, int mapSizeY) {
        this.target = target;
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.zoom = zoom;
        this.mapSizeX = mapSizeX;
        this.mapSizeY = mapSizeY;
        // initializes camera position focusing on target
        position = new Vector2(target.getPosition().x - width/2, target.getPosition().y - height/2);
        // initial clamp
        position.x = MathUtils.clamp(position.x, 0, getMax_x());
        position.y = MathUtils.clamp(position.y, 0, getMax_y());
    }

    /**
     * Updates camera properties
     */
    public void update() {
        // updates max camera x and y pos on account of zoom changes
        max_x = (mapSizeX * tileSize) - getViewportSize().x;
        max_y = (mapSizeY * tileSize) - getViewportSize().y;

        position = new Vector2(target.getPosition().x -  getViewportSize().x/2,
                                target.getPosition().y - getViewportSize().y/2);

        // clamp values avoiding going out of world bounds
        position.x = MathUtils.clamp(position.x, 0, max_x);
        position.y = MathUtils.clamp(position.y, 0, max_y);
    }

    /**
     * Returns the viewport size considering current zoom
     * @return the width (x) and height (y) of the current viewport size
     */
    public Vector2 getViewportSize() {
        return new Vector2(width/zoom, height/zoom);
    }

    /**
     * Transforms world coordinates to screen coordinates
     * based on camera currently position
     *
     * @param worldPosition    world position of the point
     * @return  the screen position of the point
     */
    public Vector2 worldToScreenPosition(Vector2 worldPosition)
    {
        return new Vector2((worldPosition.x - position.x), (worldPosition.y - position.y));
    }

    /**
     * Transforms world coordinates X to screen coordinates
     * based on camera currently position
     *
     * @param worldPositionX    world position X
     * @return  the screen position X
     */
    public float worldToScreenPositionX(float worldPositionX)
    {
        return worldPositionX - position.x;
    }

    /**
     * Transforms world coordinates Y to screen coordinates
     * based on camera currently position
     *
     * @param worldPositionY    world position Y
     * @return  the screen position Y
     */
    public float worldToScreenPositionY(float worldPositionY)
    {
        return worldPositionY - position.y;
    }

    /**
     * Transforms screen coordinates to world coordinates
     * based on camera currently position
     *
     * @param screenPosition    screen position of the point
     * @return  the world position of the point
     */
    public Vector2 screenToWorldPosition(Vector2 screenPosition)
    {
        return new Vector2((screenPosition.x + position.x), (screenPosition.y + position.y));
    }

    /**
     * Transform screen coordinates to grid indexes
     *
     * @param screenPosition    the screen coordinates
     * @return  the grid indexes
     */
    public Vector2 screenToGridPosition(Vector2 screenPosition) {
        return new Vector2(MathUtils.floor(screenToWorldPosition(screenPosition).x/tileSize),
                MathUtils.floor(screenToWorldPosition(screenPosition).y/tileSize));
    }

    /**
     * Transform world coordinates to grid indexes
     *
     * @param worldPosition    the world coordinates
     * @return  the grid indexes
     */
    public Vector2 worldToGridPosition(Vector2 worldPosition) {
        return new Vector2(MathUtils.floor(worldPosition.x/tileSize),
                           MathUtils.floor(worldPosition.y/tileSize));
    }

    /**
     * Getters and setters
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Returns the tile size after zoom has been applied
     * @return the size of a tile after zoom has been applied
     */
    public float getTileZoomed() {
        return tileSize*zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public Transform getTarget() {
        return target;
    }

    public void setTarget(Transform target) {
        this.target = target;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getMax_x() {
        return max_x;
    }

    public float getMax_y() {
        return max_y;
    }

    /**
     * Updates camera parameters based on map changes
     * @param newMap the new map that was loaded on game
     */
    public void updateMap(Map newMap) {
        this.tileSize = newMap.getTileSize();
        this.mapSizeX = newMap.getMapSizeX();
        this.mapSizeY = newMap.getMapSizeY();
    }
}
