package com.mygdx.game.states.game.standard.map;

/**
 * Class that represents a layer in the context of tile-based maps,
 * in which a layer implementation gives a new dimension to the 2D world,
 * allowing 2D objects to be positioned and drawn on top of another
 * in an organized and controlled way, without much complication.
 *
 * @author	Pedro Sampaio
 * @since	0.3
 *
 */
public class Layer {

    private Tile[][] tiles;					//  matrix that contains all the tiles that composes the layer (including non created tiles)
    private float zIndex;					//	the z-index (depth) of the layer: bigger indexes on top of smaller indexes
    private float opacity;					//	the current opacity of the layer (for visualization tweaks in the viewport)

    /**
     * Constructors
     */

    /**
     * Default constructor
     */
    public Layer () {
        this.zIndex = 0;
        this.opacity = 0;
        tiles = null;
    }

    /**
     * Constructor with layer parameters
     *
     * @param zIndex		the z-index (depth) of the layer: bigger indexes on top of smaller indexes
     * @param opacity		the current opacity of the layer (for visualization tweaks in the viewport)
     */
    public Layer (float zIndex, float opacity) {
        this.zIndex = zIndex;
        this.opacity = opacity;
        // initializes tiles with current map size configurated
        tiles = new Tile[MapConfig.getInstance().getCurrentMap().getMapSizeY()]
                        [MapConfig.getInstance().getCurrentMap().getMapSizeX()];
    }

    /**
     * Constructor with layer parameters
     *
     * @param tiles			the tiles data for the layer
     * @param zIndex		the z-index (depth) of the layer: bigger indexes on top of smaller indexes
     * @param opacity		the current opacity of the layer (for visualization tweaks in the viewport)
     */
    public Layer(Tile[][] tiles, float zIndex, float opacity) {
        this.tiles = tiles;
        this.zIndex = zIndex;
        this.opacity = opacity;
    }

    /**
     * Getters and setters (generated)
     */

    /**
     * @return the zIndex (depth) of the layer: bigger indexes on top of smaller indexes
     */
    public float getzIndex() {
        return zIndex;
    }
    /**
     * @param zIndex the zIndex to set
     */
    public void setzIndex(float zIndex) {
        this.zIndex = zIndex;
    }
    /**
     * @return the current opacity of the layer (for visualization tweaks in the viewport)
     */
    public float getOpacity() {
        return opacity;
    }
    /**
     * @param opacity the opacity to set
     */
    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    /**
     * @return the matrix that contains all the tiles that composes the layer
     */
    public Tile[][] getTiles() {
        return tiles;
    }

    /**
     * @param tiles the tiles to set
     */
    public void setTiles(Tile[][] tiles) {
        this.tiles = tiles;
    }

    /**
     * creates a copy of the tiles structure
     * @return	the copy of the tiles structure
     */
    public Tile[][] createCopy() {
        int mapSizeY = MapConfig.getInstance().getCurrentMap().getMapSizeY();
        int mapSizeX = MapConfig.getInstance().getCurrentMap().getMapSizeX();
        final Tile[][] result = new Tile[mapSizeY][mapSizeX];
        for (int i = 0; i < mapSizeY; i++) {
            for (int j = 0; j < mapSizeX; j++) {
                if(tiles[i][j] != null)
                    result[i][j] = new Tile(tiles[i][j].getIndexI(), tiles[i][j].getIndexJ(), tiles[i][j].getTileset(),
                            tiles[i][j].getId(),tiles[i][j].getDrawI(), tiles[i][j].getDrawJ(), tiles[i][j].isComplete());
            }
        }
        return result;
    }

    /**
     * @return if the layer is empty (no tiles in it)
     */
    public boolean isEmpty() {

        for(int i = 0; i < tiles.length; i++) {
            for(int j = 0; j < tiles[i].length; j++) {
                if(tiles[i][j] != null)
                    return false;
            }
        }
        return true;
    }

}
