package com.mygdx.game.states.game.standard.physics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.player.Player;

/**
 * Collider class for bounding box collisions.
 * Bounding box collision includes rectangular shapes.
 * Both physical and trigger collisions are represented
 * by this class and are implemented in the collision system.
 * Physical collision is a type of collision that blocks
 * physical movements. Trigger collision does not block
 * physical movements, only serves the purposes of triggering
 * desired operations on collision detection.
 *
 * @author Pedro Sampaio
 * @since 0.7
 */
public class Collider extends Rectangle {

    /**
     * All box properties are heritage of Rectangle class
     */

    private boolean isPhysical;   // bool that represents if this collider is physical or trigger
    private Object parent;        // the parent object of this collider to identify collision objects
    private float offsetX;        // collider offset in X axis
    private float offsetY;        // collider offset in Y axis
    private boolean entered;      // bool to help control of collision callbacks

    /**
     * Constructor for collider object (only for collision tests)
     *
     * @param x             the x coordinate of the collider in the world
     * @param y             the y coordinate of the collider in the world
     * @param width         the width of the collider in the world
     * @param height        the height of the collider in the world
     * @param isPhysical    if the collider is a physical or a trigger collider
     * @param parent        the parent object that the collider is attached to
     */
    public Collider(float x, float y, float width, float height, boolean isPhysical, Object parent) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isPhysical = isPhysical;
        this.parent = parent;
        this.offsetX = 0;
        this.offsetY = 0;
        this.entered = false;
    }

    /**
     * Constructor for collider object
     *
     * @param transform     the object's transform
     * @param isPhysical    if the collider is a physical or a trigger collider
     * @param parent        the parent object that the collider is attached to
    */
    public Collider(Transform transform, boolean isPhysical, Object parent) {
        this.x = transform.getPosition().x;
        this.y = transform.getPosition().y;
        this.width = transform.getWidth() * transform.getScale().x;
        this.height = transform.getHeight() * transform.getScale().y;
        this.isPhysical = isPhysical;
        this.parent = parent;
        this.entered = false;
        transform.setCollider(this);
    }

    /**
     * sets scale for collider width
     * @param scale the factor to multiply collider width
     */
    public void setScaleWidth(float scale) {
        this.width *= scale;
    }

    /**
     * sets scale for collider height
     * @param scale the factor to multiply collider height
     */
    public void setScaleHeight(float scale) {
        this.height *= scale;
    }

    /**
     * sets offset of collider in axis X
     * @param offset the offset to apply to collider in axis X
     */
    public void setOffsetX(float offset) {
        this.x += offset;
        this.offsetX = offset;
    }

    /**
     * sets offset of collider in axis Y
     * @param offset the offset to apply to collider in axis Y
     */
    public void setOffsetY(float offset) {
        this.y += offset;
        this.offsetY = offset;
    }

    /**
     * Checks if collision happens with other collider
     * @param other the other collider to check collision with
     * @return  if collision happened or not
     */
    public boolean checkCollision(Collider other) {
        return this.overlaps(other);
    }

    /**
     * Updates collider position for moving entities
     *
     * @param x the new x coordinate of the collider in the world
     * @param y the new y coordinate of the collider in the world
     */
    public void update(float x, float y) {
        this.x = x + offsetX;
        this.y = y + offsetY;
        // updates transform to update collision callbacks

    }

    /**
     * Checks if there is collision involving this collider
     * and the touch position received in parameter
     * @param touchPos the position of touch to check collision with this collider
     * @return true if there was a collision, false otherwise
     */
    public boolean checkTouchCollision(Vector2 touchPos) {return this.contains(touchPos);}

    /**
     * Getters and setters
     */

    public boolean isPhysical() {return isPhysical;}
    public Object getParent() {return parent;}
    public String getParentName() {return parent.getClass().toString();}
    public boolean isEntered() {return entered;}
    public void setEntered(boolean entered) {this.entered = entered;}
}
