package sddl.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages the server current state of
 * online players. Later, it might be of use also
 * for real-time online architecture for player interactions
 *
 * @author Pedro Sampaio
 * @since   1.6
 */
public class ServerState {

    private HashMap<String, Long> onlinePlayers; // currently online players with their last tick stored
    private float offlineTick = 15000f; // how long a user without tick updates will be considered offline (in ms)
    private static ServerState instance = null; // the singleton instance

    /**
     * Constructor that initializes online players data structure
     */
    private ServerState() {
        this.onlinePlayers = new HashMap<String, Long>();
        MessageHandler.getInstance().handleResetOnlineStatus();
    }

    /**
     * singleton get instance method
     * @return the singleton instance
     */
    public static ServerState getInstance() {
        if(instance == null)
            instance = new ServerState();

        return instance;
    }

    /**
     * updates status of online players
     */
    public void update() {
        checkStatus();
    }

    /**
     * Updates a online character to the structure of online players
     * @param onlinePlayer the online player to updates in structure
     */
    public void updateOnlinePlayer(String onlinePlayer) {
        onlinePlayers.put(onlinePlayer, System.currentTimeMillis());
    }

    /**
     * Checks players ticks removing
     * offline ones and updating database
     */
    public void checkStatus() {
        Iterator it = onlinePlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            // if last tick has surpassed limit, removes from online player and updates database
            if(System.currentTimeMillis() - ((Long)pair.getValue()) > offlineTick) {
                String character = (String)pair.getKey(); // character to update status
                it.remove(); // avoids a ConcurrentModificationException
                // updates character online status to offline
                MessageHandler.getInstance().handleStatusUpdate(character, false);
            }

        }
    }

    /**
     * Updates a character last tick in the
     * list of online characters
     * @param character the name of the character to update last tick
     */
//    public void updatePlayerTick(String character) {
//        for(int i = 0; i < onlinePlayers.size(); i++) {
//            if(onlinePlayers.get(i).getName().equals(character))
//                onlinePlayers.get(i).setLastTick(System.currentTimeMillis());
//        }
//    }

//    /**
//     * Getters and setters
//     */
//
//    public ArrayList<PlayerState> getOnlinePlayers() {
//        return onlinePlayers;
//    }
//
//    public void setOnlinePlayers(ArrayList<PlayerState> onlinePlayers) {
//        this.onlinePlayers = onlinePlayers;
//    }
}
