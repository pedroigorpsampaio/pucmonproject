package sddl.server;

/**
 * Class that represents an online player
 * in the server storing necessary data
 * to determine a character online status
 *
 * @author Pedro Sampaio
 * @since   1.6
 */
public class PlayerState {

    private String name; // the name of the character

    private long lastTick; // the last tick received by character to determine online status

    /**
     * player state constructor
     *
     * @param name       the player's character name
     */
    public PlayerState(String name) {
        this.name = name;
    }

    /**
     * Getters and setters
     */

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public long getLastTick() {return lastTick;}

    public void setLastTick(long lastTick) {this.lastTick = lastTick;}
}
