package com.mygdx.game.states.game.standard.item;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.util.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * The class that represents an item in the game.
 * Items can be weapons, armors, and other wearables (as of 1.2).
 *
 * @author  Pedro Sampaio
 * @since   1.2
 */
public class Item {

    private boolean equipable; // if this item is equipable
    private TextureRegion sprite;  // the item sprite
    private float chance;    // the chance of dropping this item
    private boolean stackable; // is this item stackable?
    private int quantity; // quantity of items (in case of stackable)
    private Quality quality; // the quality of this item
    private String name;    // the name of this item
    private String description; // description of the item
    private int id;         // the unique ID of this item

    /**
     * Enum that dictates the item quality associated with its rarity
     */
    public enum Quality {NORMAL, UNCOMMON, RARE, LEGENDARY}

    /**
     * Item constructor
     * @param id            the id of this item
     * @param name          the name of this item
     * @param description   the description of this item
     * @param sprite        the item sprite
     * @param chance        the chance of dropping this item (0 to 100)
     * @param stackable     is this item stackable?
     * @param equipable     is this item equipable?
     * @param quantity      the number of items (in case of stackable)
     * @param quality       the quality of this item
     */
    public Item(int id, String name, String description, TextureRegion sprite,
                float chance, boolean stackable, boolean equipable, int quantity, Quality quality) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sprite = sprite;
        this.chance = chance;
        this.chance = MathUtils.clamp(this.chance, 0, 100);
        this.stackable = stackable;
        this.equipable = equipable;
        this.quantity = quantity;
        this.quantity = MathUtils.clamp(this.quantity, 1, Config.maxStackOfItems);
        this.quality = quality;
    }

    /**
     * Adds an amount of items in stack (if stackable).
     * If not, ignore it.
     * @param amount the amount of items to be added to stack
     */
    public void addToStack(int amount) {
        if(stackable) {
            quantity+=amount;
            if(quantity > Config.maxStackOfItems)
                quantity = Config.maxStackOfItems;
        }
    }

    /**
     * Removes an amount of items from stack (if stackable).
     * If not, ignore it.
     * If quantity decreases to 0 or lower, removes itself from inventory
     *
     * @param amount the amount of items to be removed from stack
     */
    public void removeFromStack(int amount) {
        if(stackable) {
            quantity-=amount;
        }
    }

     /**
     * Getters and Setters
     */

    public TextureRegion getSprite() {
        return sprite;
    }

    public void setSprite(TextureRegion sprite) {
        this.sprite = sprite;
    }

    public float getChance() {
        return chance;
    }

    public void setChance(float chance) {
        this.chance = chance;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public int getQuantity() {return quantity;}

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public boolean isEquipable() {return equipable;}

    public void setEquipable(boolean equipable) {this.equipable = equipable;}

}
