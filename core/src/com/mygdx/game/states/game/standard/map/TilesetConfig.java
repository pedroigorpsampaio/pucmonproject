package com.mygdx.game.states.game.standard.map;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

/**
 * Class that contains information about
 * the tilesets loaded in the game
 *
 * @author Pedro Sampaio
 * @since  0.3
 *
 */
public class TilesetConfig {

    /**
     * Patterns:
     * Singleton
     */

    private int currentTilesetIdx;		// the index of the current loaded tileset on the tilesets list
    private ArrayList<Tileset> tilesets; // the list of the loaded tilesets on the program

    // mantains only one instance of map config (singleton pattern)
    private static TilesetConfig instance = null;

    protected TilesetConfig() {
        // defeats instantiation.
        tilesets = new ArrayList<Tileset> (); // initializes array list of tilesets
    }

    /**
     * @return returns map config instance
     * creates the instance if does not exist yet
     */
    public static TilesetConfig getInstance() {
        if(instance == null)
            instance = new TilesetConfig();

        return instance;
    }

    /**
     * Creates a Tileset object that represents a tileset to be used on
     * 2D map creation. A tileset is a set of tiles grouped in a single
     * image later to be divided by the size of the tile.
     * Receives all information for cases of creating a loaded tileset.
     *
     * @param	name			The desired name for the Tileset object to be created
     * @param 	tileSize		The desired tile size for the Tileset object to be created
     * @param 	imagePath		The path to the desired image for the Tileset object to be created
     * @param	firstID			the id(global - for all tilesets) of the first tile in the tileset
     * @param	tilecount		the number of tiles that exists in the tileset
     * @return					Returns the created Tileset object with all information stored
     * 							or null if image could not be loaded
     * @since	0.2
     */
    public Tileset createTileset(String name, int tileSize, String imagePath, int firstID, int tilecount) {
        Texture image; // source image for the tileset

        // load image
        image = new Texture(imagePath);

        if(!image.equals(null)) {
            return new Tileset(name, tileSize, image, imagePath, firstID, tilecount);
        }
        else
            return null;
    }

    /**
     * Creates a Tileset object that represents a tileset to be used on
     * 2D map creation. A tileset is a set of tiles grouped in a single
     * image later to be divided by the size of the tile
     *
     * @param	name			The desired name for the Tileset object to be created
     * @param 	tileSize		The desired tile size for the Tileset object to be created
     * @param 	imagePath		The path to the desired image for the Tileset object to be created
     * @return					Returns the created Tileset object with all information stored
     * 							or null if image could not be loaded
     * @since	0.2
     */
    public Tileset createTileset(String name, int tileSize, String imagePath) {

        Texture image; // source image for the tileset

        // load image
        image = new Texture(imagePath);

        if(!image.equals(null)) {
            return new Tileset(name, tileSize, image, imagePath, calculateFirstID());
        }
        else
            return null;
    }

    /**
     * Calculates the first ID for the next tileset
     * to be added to the list of tilesets
     * @author Pedro Sampaio
     * @since 1.4
     * @return  the firstID for the new tileset to be added
     */
    public int calculateFirstID() {
        // calculates first id of tileset
        int firstID = 1;
        // if its not first, tileset first id must be
        // one more than the last tile of the latest tileset
        if(tilesets.size() != 0) {
            Tileset lastTS = tilesets.get(tilesets.size()-1);
            firstID = lastTS.getFirstID()+lastTS.getTileCount();
        }
        // returns first ID for the next tileset
        return firstID;
    }

    /**
     * @return the current loaded tileset on the tilesets list
     */
    public Tileset getCurrentTileset() {
        return tilesets.get(currentTilesetIdx);
    }

    /**
     * @return the index of the current loaded tileset on the tilesets list
     */
    public int getCurrentTilesetIdx() {
        return currentTilesetIdx;
    }

    /**
     * @param currentTilesetIdx the index of the current loaded tileset on the tilesets list
     */
    public void setCurrentTilesetIdx(int currentTilesetIdx) {
        this.currentTilesetIdx = currentTilesetIdx;
    }

    /**
     * @return the list of the loaded tilesets on the program
     */
    public ArrayList<Tileset> getTilesets() {
        return tilesets;
    }

    /**
     * @param tilesets the list of the loaded tilesets on the program
     */
    public void setTilesets(ArrayList<Tileset> tilesets) {
        this.tilesets = tilesets;
    }

}