package com.mygdx.game.states.game.standard.architecture;

import com.mygdx.game.states.game.standard.physics.Collider;

/**
 * Interface for objects that have
 * colliders (physical and/or trigger)
 * and want an automatic collision check
 * with automatic callback calls
 *
 * @author Pedro Sampaio
 * @since 0.8
 */
public interface Collision {

    /**
     * Called when physical collision starts to happen
     * with another collider
     * @param other the other collider of the physical collision
     */
    void onColliderEnter(Collider other);

    /**
     * Called when trigger collision starts to happen
     * with a trigger collider.
     * Physical collider can collide both with
     * physical and trigger colliders. The same
     * goes for the other way.
     * @param other the other trigger collider of the collision
     */
    void onTriggerEnter(Collider other);

    /**
     * Called when trigger collision is happening
     * with a trigger collider
     * @param other the other trigger collider of the physical collision
     */
    void onTriggerStay(Collider other);

    /**
     * Called when trigger collision stops happening
     * with a trigger collider
     * @param other the other trigger collider of the physical collision
     */
    void onTriggerExit(Collider other);

}
