package com.mygdx.game.states.game.standard.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.architecture.Collision;
import com.mygdx.game.states.game.standard.map.Map;
import com.mygdx.game.states.game.standard.map.MapConfig;

import java.util.ArrayList;

/**
 * Class that represents physical attributes of an entity
 * in the game world, like position, scale and rotation.
 * It should be a component of every entity present in
 * the game world, facilitating operations such as translating,
 * scaling, and rotating and collision check.
 *
 * @author Pedro Sampaio
 * @since   0.4
 */
public class Transform {
    private Collider collider;   // the parent collider of this transform
    private Vector2 position; // the physical position in axis x and y of the world
    private Vector2 scale;    // the scale factor of both axis x and y of the world
    private float   width;    // the physical width of the object
    private float   height;   // the physical height of the object
    private float   rotation; // the rotation angle of the entity (2D world rotation)
    private float   layer;    // layer of the object in the world (affects order of drawing)
    private ArrayList<Collider> enteredTriggers; // helps controlling trigger callbacks

    /**
     * Transform constructor
     * @param position      the physical position in axis x and y of the world
     * @param scale         the scale factor of both axis x and y of the world
     * @param rotation      the rotation angle of the entity (2D world rotation)
     * @param layer         layer of the object in the world (affects order of drawing)
     */
    public Transform(Vector2 position, float width, float height, Vector2 scale, float rotation, int layer) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.rotation = rotation;
        this.layer = layer;
        enteredTriggers = new ArrayList<Collider>();
    }

    /**
     * Applies movement to a transform physical
     * position if physical collision does not happen
     * or object does not have a collider
     *
     * @param move
     */
    public void move(Vector2 move) {
        // object does note have collider
        if(collider == null) {
            // apply movement
            this.getPosition().x += move.x;
            this.getPosition().y += move.y;
        } else { // there is a collider
            // applies movement to the position avoiding
            // getting out of bounds and physical collisions
            if(!checkCollision(move)) {
                getPosition().x += move.x;
                getPosition().y += move.y;
            } else { // collision detected
                // tries to move only on x axis
                if(!checkCollision(new Vector2(move.x, 0)))
                    getPosition().x += move.x;
                else if (!checkCollision(new Vector2(0, move.y)))// tries to move only on y axis
                    getPosition().y += move.y;
            }

            // updates collider
            collider.update(getPosition().x , getPosition().y);
        }
    }

    /**
     * Teleports transform to a new position
     * @param position the new position to teleport transform
     */
    public void teleport(Vector2 position) {
        this.position = position;
    }

    /**
     * Teleports transform to a new position
     * @param x the new x position to teleport transform
     * @param y the new y position to teleport transform
     */
    public void teleport(float x, float y) {
        position.x = x;
        position.y = y;
    }

    /**
     * Detects collision with physical entities of world
     *
     * @param move  the movement that might be applied to trasform current position
     *              in case of no collisions detected
     * @return  if collision was detected
     */
    public boolean checkCollision(Vector2 move) {
        // updates player world boundaries
        int mapSizeY = MapConfig.getInstance().getCurrentMap().getMapSizeY();
        int mapSizeX = MapConfig.getInstance().getCurrentMap().getMapSizeX();
        int tileSize = MapConfig.getInstance().getCurrentMap().getTileSize();
        int max_x = (int)((mapSizeX * tileSize) - collider.getWidth());
        int max_y = (int)((mapSizeY * tileSize) - collider.getHeight());

        // check collision with world boundaries in x axis
        if(collider.x + move.x >= max_x ||
                collider.x + move.x <= 0)
            return true;

        // check collision with world boundaries in y axis
        if(collider.y + move.y >= max_y ||
                collider.y + move.y <= 0)
            return true;

        // gets the future collider with movement applied
        Collider futureCollider = new Collider(collider.x + move.x, collider.y + move.y,
                                                 collider.width, collider.height, true, this);

        // gets current map
        Map currentMap = MapConfig.getInstance().getCurrentMap();

        // gets current map active colliders
        Collider[][] mapColliders = currentMap.getColliders();
        // gets current visible map viewport info
        Rectangle vp = currentMap.getViewportGrid();
        int first_j = (int) vp.x;
        int first_i = (int) vp.y;
        int last_j = (int)vp.width + first_j;
        int last_i = (int)vp.height + first_i;

        // iterates through visible colliders
        for(int i = 0; i < last_i; i++) {
            for(int j = 0; j < last_j; j++) {
                // gets current tile i and j
                int dataI = first_i + i;
                int dataJ = first_j + j;

                // make sure not to go out of bounds
                if(dataI < 0)
                    dataI = 0;
                if(dataJ < 0)
                    dataJ = 0;
                if(dataI >= mapSizeY)
                    dataI = mapSizeY - 1;
                if(dataJ >= mapSizeX)
                    dataJ = mapSizeX - 1;

                Collider coll = mapColliders[dataI][dataJ]; // other collider

                if(coll != null) { // checks if a collider exists in iteration pos
                    // only checks physical collision if collider is physical
                    if(coll.isPhysical()) {
                        // check if there is a collision
                        if (futureCollider.overlaps(coll)) {
                            // trigger physical collision callback
                            if (collider.getParent() instanceof Collision) {
                                Collision collision = (Collision) collider.getParent();
                                collision.onColliderEnter(coll);
                                return true; // if collides, return that collision happened
                            }
                        }
                    }
                    else { // checks trigger collisions
                        if (futureCollider.overlaps(mapColliders[dataI][dataJ])) {
                            // trigger trigger collision callback
                            if (collider.getParent() instanceof Collision) {
                                Collision collision = (Collision) collider.getParent();
                                if(!coll.isEntered()) {
                                    collision.onTriggerEnter(coll);
                                    enteredTriggers.add(coll);
                                    coll.setEntered(true);
                                }
                            }
                        }
                    }
                }
            }
        }

        // no collision happened
        return false;
    }

    /**
     * Updates collision callbacks
     */
    public void update() {
        if (collider.getParent() instanceof Collision) {
            Collision collision = (Collision) collider.getParent();
            // calls on trigger stay for all current entered triggers
            for(int i = 0; i < enteredTriggers.size(); i++) {
                collision.onTriggerStay(enteredTriggers.get(i));
            }

            // checks if has exited trigger collisions
            for (int i = 0; i < enteredTriggers.size(); i++) {
                if (!collider.overlaps(enteredTriggers.get(i))) {
                    collision.onTriggerExit(enteredTriggers.get(i));
                    enteredTriggers.get(i).setEntered(false);
                    enteredTriggers.remove(i);
                }
            }
        }
    }

    /**
     * Getters and Setters
     */

    /**
     * Getter for transform's position
     * @return the position for this transform in the game world
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Setter for transform's position
     * @param position the position for this transform in the game world
     */
    public void setPosition(Vector2 position) {
        this.position = position;
    }

    /**
     * Getter for transform's scale
     * @return the scale for this transform in the game world
     */
    public Vector2 getScale() {
        return scale;
    }

    /**
     * Setter for transform's scale
     * @param scale the scale for this transform in the game world
     */
    public void setScale(Vector2 scale) {
        this.scale = scale;
    }

    /**
     * Getter for transform's rotation
     * @return the rotation for this transform in the game world
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Setter for transform's rotation
     * @param rotation the rotation for this transform in the game world
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     * Getter for transform's layer
     * @return the layer for this transform in the game world
     */
    public float getLayer() {
        return layer;
    }

    /**
     * Setter for transform's layer
     * @param layer the layer for this transform in the game world
     */
    public void setLayer(float layer) {
        this.layer = layer;
    }


    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }


    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }


    public void setCollider(Collider collider) {
        this.collider = collider;
    }

}
