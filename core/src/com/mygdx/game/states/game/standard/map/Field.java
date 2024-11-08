package com.mygdx.game.states.game.standard.map;

import com.badlogic.gdx.math.Vector2;

/**
 * Class that represent special type of tiles
 * hereafter Field. Fields have additional
 * functions, ranging from being map start
 * location to having triggers attached to
 * perform specific actions when entered
 *
 * @author Pedro Sampaio
 * @since 0.8
 */
public class Field {

    /**
     * Types of special fields
     */
    public enum FieldType {START, MAPTELEPORT, FIRE}

    private Vector2 index;  // the index of this field in the map
    private String id;    // the identifier for this field
    private String complement; // additional information when needed
    private FieldType type; // the type of the special field

    /**
     * Constructor for field class
     *
     * @param index the i and j index of field in map stored in a Vector2
     * @param type  the type of the special field
     * @param id the identifier for the field functionality
     * @param complement additional information when needed
     */
    public Field(Vector2 index, FieldType type, String id, String complement) {
        this.index = index;
        this.type = type;
        this.id = id;
        this.complement = complement;
    }

    /**
     * Getters and setters
     */

    public Vector2 getIndex() {
        return index;
    }

    public void setIndex(Vector2 index) {
        this.index = index;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public String getID() {
        return id;
    }

    public void setID(String type) {
        this.id = type;
    }

    public String getComplement() {return complement;}
}
