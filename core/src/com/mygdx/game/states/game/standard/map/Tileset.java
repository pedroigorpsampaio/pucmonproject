package com.mygdx.game.states.game.standard.map;

import com.badlogic.gdx.graphics.Texture;

/**
 * Class that represents a tileset to be used in map creation.
 * Contains all necessary information to store, visualize
 * and use a tileset in order to design a 2D map
 *
 * @author	Pedro Sampaio
 * @since	0.3
 *
 */
public class Tileset {

    private int id;				// the id of the tileset in the map project context (based on order of import)
    private int firstID;		// the id of the first tile of this tileset in the map project context
    private int tileCount;		// the number of tiles contained in this tileset
    private int tileSizeX;		// number of tiles in tileset on x-axis (columns)
    private int tileSizeY;		// number of tiles in tileset on y-axis (lines)
    private String name;		// the name of the tileset
    private int tileSize;		// the size of tiles in the tileset (square tiles)
    private Texture image;		// the source image of the tileset
    private String imagePath;	// the image path for the tileset

    /**
     * Class constructor
     *
     * @param name 			desired name for the tileset
     * @param tileSize 		desired tile size for the tileset
     * @param image 		desired image for the tileset
     * @param imagePath 	the path of the image
     * @param firstID		the id(global - for all tilesets) of the first tile in the tileset
     */
    public Tileset(String name, int tileSize, Texture image, String imagePath, int firstID) {
        this.setName(name);
        this.setTileSize(tileSize);
        this.setImage(image);
        this.setImagePath(imagePath);
        this.setFirstID(firstID);

        // image cannot be null at this point
        assert(image != null);

        // calculates tile with image's width and height info, and the size of a tile
        this.tileSizeX = (int) Math.ceil(image.getWidth()/(float)(tileSize));
        this.tileSizeY = (int) Math.ceil(image.getHeight()/(float)(tileSize));

        this.tileCount = this.tileSizeX * this.tileSizeY;
    }

    /**
     * Class constructor
     * Receives all informations for cases of tileset loaded from save file
     *
     * @param name 			desired name for the tileset
     * @param tileSize 		desired tile size for the tileset
     * @param image 		desired image for the tileset
     * @param imagePath 	the path of the image
     * @param firstID		the id(global - for all tilesets) of the first tile in the tileset
     * @param tilecount		the number of tiles that exists in the tileset
     */
    public Tileset(String name, int tileSize, Texture image, String imagePath, int firstID, int tilecount) {
        this.setName(name);
        this.setTileSize(tileSize);
        this.setImage(image);
        this.setImagePath(imagePath);
        this.setFirstID(firstID);
        this.tileCount = tilecount;

        // image cannot be null at this point
        assert(image != null);

        // calculates tile with image's width and height info, and the size of a tile
        this.tileSizeX = (int) Math.ceil(image.getWidth()/(float)(tileSize));
        this.tileSizeY = (int) Math.ceil(image.getHeight()/(float)(tileSize));
    }

    /**
     * Getters and Setters of object properties
     */

    /**
     * @return the id
     */
    protected int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    protected void setId(int id) {
        this.id = id;
    }

    /**
     * @return the id of the first tile of this tileset in the map project context
     */
    public int getFirstID() {
        return firstID;
    }

    /**
     * @param firstID the firstID to set
     */
    public void setFirstID(int firstID) {
        this.firstID = firstID;
    }

    /**
     * @return the number of tiles contained in this tileset
     */
    public int getTileCount() {
        return tileCount;
    }

    /**
     * @return the name of the tileset
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the size of tiles in the tileset (square tiles)
     */
    public int getTileSize() {
        return tileSize;
    }

    /**
     * @param tileSize the tileSize to set
     */
    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    /**
     * @return the source image of the tileset
     */
    public Texture getImage() {
        return  image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(Texture image) {
        this.image = image;
    }

    /**
     * @return the number of tiles in tileset on x-axis (columns)
     */
    public int getTileSizeX() {
        return tileSizeX;
    }

    /**
     * @return the number of tiles in tileset on y-axis (lines)
     */
    public int getTileSizeY() {
        return tileSizeY;
    }

    /**
     * @return the image path of the tileset
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * @param imagePath the image path for the tileset
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

}