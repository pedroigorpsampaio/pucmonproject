package com.mygdx.game.states.game.standard.map;

/**
 * Class that represents a tile.
 * A tile is the quad that represents a unit in a tile-based 2D map.
 * Every tileset contains a number of tiles that composes the tileset,
 * designed to be used in the map creation and in the 2D game itself
 *
 * @author 	Pedro Sampaio
 * @since	0.3
 *
 */
public class Tile {

    private int id;				// 	the id of the tile in the game map context
    private int indexI;			//	the i-index (line) of the tile in matrix
    private int indexJ;			//	the j-index (column) of the tile in matrix
    private int drawI;			//	the i-index (line) of the tile in map's viewport for preview
    private int drawJ;			//	the j-index (column) of the tile in map's viewport for preview
    private int matrixI;		//	the i-index (line) of the tile in data matrix (for paste operation anchoring)
    private int matrixJ;		//	the j-index (column) of the tile in data matrix (for paste operation anchoring)
    private Tileset tileset;	//	the tileset source of the tile
    private boolean isComplete;	// 	bool that represents if tile has all informations stored

    /**
     * Constructors
     */

    /**
     * Default constructor
     */
    public Tile () {
        this.indexI = 0;
        this.indexJ = 0;
        this.tileset = null;
        this.id = -1;
        setDrawI(0);
        setDrawJ(0);
        isComplete = false;
    }

    /**
     * Constructor with tile parameters
     *
     * @param indexI	the i-index (line) of the tile in tileset
     * @param indexJ	the j-index (column) of the tile in tileset
     * @param tileset	the tileset source of the tile
     */
    public Tile (int indexI, int indexJ, Tileset tileset) {
        this.indexI = indexI;
        this.indexJ = indexJ;
        this.tileset = tileset;
        this.id	= generateID();
        setDrawI(0);
        setDrawJ(0);
        isComplete = false;
    }


    /**
     * Constructor with tile parameters
     *
     * @param indexI	the i-index (line) of the tile in tileset
     * @param indexJ	the j-index (column) of the tile in tileset
     * @param tileset	the tileset source of the tile
     * @param id		the global id of the tileset in the project
     * @param matrixI	the i-index of tile in the map matrix
     * @param matrixJ	the j-index of tile in the map matrix
     */
    public Tile (int indexI, int indexJ, Tileset tileset, int id, int matrixI, int matrixJ) {
        this.indexI = indexI;
        this.indexJ = indexJ;
        this.tileset = tileset;
        this.id	= id;
        this.matrixI = matrixI;
        this.matrixJ = matrixJ;
        setDrawI(0);
        setDrawJ(0);
        isComplete = false;
    }


    /**
     * Constructor with all tile parameters
     *
     * @param indexI	the i-index (line) of the tile in tileset
     * @param indexJ	the j-index (column) of the tile in tileset
     * @param tileset	the tileset source of the tile
     * @param id 		the id of the tile in the map project context
     * @param drawI 	the i-index (line) of the tile in map's viewport
     * @param drawJ 	the j-index (column) of the tile in map's viewport
     * @param isComplete bool that represents if tile has all informations stored
     */
    public Tile (int indexI, int indexJ, Tileset tileset, int id, int drawI, int drawJ, boolean isComplete) {
        this.indexI = indexI;
        this.indexJ = indexJ;
        this.tileset = tileset;
        this.id	= generateID();
        this.drawI = drawI;
        this.drawJ = drawJ;
        this.isComplete = isComplete;
    }


    /**
     * Calculates the ID of the tile based on its position on the tileset
     * and on the tileset position(order) in the context of the project.
     * The generated ID will be the info that represents this tile on
     * the map file generated, and will be sufficient to retrieve
     * which tileset the tile came from as well as what is its position
     * in the tileset.
     *
     * @since  0.3
     * @return the global id of the tile considering all tilesets in project
     */
    protected int generateID() {
        // the id of the tile in its tileset (based on i and j position)
        int tsTileID = (indexI * tileset.getTileSizeX()) + indexJ;
        // the global id considering all tilesets (sum with tileset's firstid)
        return tileset.getFirstID() + tsTileID;
    }

    /**
     * Getters and setters
     */

    /**
     * @return the indexI (line) of the tile in tileset
     */
    public int getIndexI() {
        return indexI;
    }
    /**
     * @param indexI the indexI to set
     */
    public void setIndexI(int indexI) {
        this.indexI = indexI;
    }
    /**
     * @return the indexJ (column) of the tile in tileset
     */
    public int getIndexJ() {
        return indexJ;
    }
    /**
     * @param indexJ the indexJ to set
     */
    public void setIndexJ(int indexJ) {
        this.indexJ = indexJ;
    }
    /**
     * @return the tileset source of the tile
     */
    public Tileset getTileset() {
        return tileset;
    }
    /**
     * @param tileset the tileset to set
     */
    public void setTileset(Tileset tileset) {
        this.tileset = tileset;
    }

    /**
     * @return the id of the tile in the map project context
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    protected void setId(int id) {
        this.id = id;
    }

    /**
     * @return the tile size of the tileset that contains the tile
     */
    public int getTileSize() {
        return tileset.getTileSize();
    }

    /**
     * @return the map's index I
     */
    public int getDrawI() {
        return drawI;
    }

    /**
     * @param drawI sets the map's index I
     */
    public void setDrawI(int drawI) {
        this.drawI = drawI;
    }

    /**
     * @return the map's index J
     */
    public int getDrawJ() {
        return drawJ;
    }

    /**
     * @param drawJ sets the map's index J
     */
    public void setDrawJ(int drawJ) {
        this.drawJ = drawJ;
    }

    /**
     * @return the isComplete
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * @param isComplete the isComplete to set
     */
    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    /**
     * @return the matrixI
     */
    public int getMatrixI() {
        return matrixI;
    }

    /**
     * @param matrixI the matrixI to set
     */
    public void setMatrixI(int matrixI) {
        this.matrixI = matrixI;
    }

    /**
     * @return the matrixJ
     */
    public int getMatrixJ() {
        return matrixJ;
    }

    /**
     * @param matrixJ the matrixJ to set
     */
    public void setMatrixJ(int matrixJ) {
        this.matrixJ = matrixJ;
    }

}
