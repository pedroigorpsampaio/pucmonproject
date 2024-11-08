package com.mygdx.game.states.game.standard.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.camera.GameCamera;
import com.mygdx.game.states.game.standard.physics.Collider;
import com.mygdx.game.util.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Class that represents a map in the game.
 * A map is a collection of tiles arranged in a certain way
 * that composes the 2D world in a tile-based game.
 * Considering that we have different layers for the map,
 * the map is the collection of all layers with all of its tiles
 *
 * @author	Pedro Sampaio
 * @since	0.3
 *
 */
public class Map {

    private TextureRegion tileTex; // texture of tile that will be dinamically changed
    private ArrayList<Layer> layers;	// main data of the program: List that contains all the layers that composes the map
    private String name; // the name of the map file
    private Collider[][] colliders; // the collider mask of map
    private Field[][] fields; // the fields mask of map
    private ArrayList<Field> mapTeleports; // auxiliary list of map teleports to aid in binding
    private ShapeRenderer shapeRenderer; // shape renderer for collider debug
    private Rectangle viewportGrid; // the visible grid coordinates and dimension
    private Vector2 startPoint; // the start point of this map

    private int mapSizeX; // map size on X axis
    private int mapSizeY; // map size on Y axis
    private int tileSize; // tilesize of map

    private boolean randomBattleEnabled; // boolean that represents if random battle should occur in this map

    // existent world maps
    public enum MapID {example_map, testmap, mapdemo;}

    /**
     * Constructor for this class
     * Creates the first layer of the map
     *
     */
    public Map() {
        // initializes layers
        layers = new ArrayList<Layer>();
        // creates and adds first layer (idx 0) to list of layers (map) with full opacity (1f)
        layers.add(new Layer(0, 1f));
        // initializes shape render
        shapeRenderer = new ShapeRenderer();
        // random battle encounters are enabled by default
        randomBattleEnabled = true;
    }

    /**
     * Constructor for this class
     * For copy, receive all properties via parameter
     *
     * @param layers 	receives the list of layers that contain all info of the tiles in layers for the map
     * @param name the name of the map file
     * @param mapSizeX number of tiles in map on x-axis (columns)
     * @param mapSizeY number of tiles in map on y-axis (lines)
     * @param tileSize the size of the map's tile
     */
    public Map(ArrayList<Layer> layers, String name, int mapSizeX, int mapSizeY, int tileSize) {
        this.layers = layers;
        this.name = name;
        this.mapSizeX = mapSizeX;
        this.mapSizeY = mapSizeY;
        this.tileSize = tileSize;
        viewportGrid = new Rectangle();
        // initializes shape render
        shapeRenderer = new ShapeRenderer();
        // initialize collider mask
        colliders = new Collider[mapSizeY][mapSizeX];
        // initialize fields mask
        fields = new Field[mapSizeY][mapSizeX];
        // initializes start point;
        startPoint = new Vector2();
        // initializes helper list of map teleports
        mapTeleports = new ArrayList<Field>();
        // reads colliders from collider mask file
        loadColliders();
        // reads fields information from map config file
        loadFields();
        // random battle encounters are enabled by default
        randomBattleEnabled = true;
        // gets tileset region that matches the image of current tile
        tileTex = new TextureRegion();
    }

    /**
     * Loads colliders from map collider mask file
     */
    private void loadColliders() {
        // the file containing the information to be loaded
        InputStream colFile = Gdx.files.internal("maps/" + name + ".col").read();
        // buffered reader to read collider file
        BufferedReader colReader = new BufferedReader(new InputStreamReader(colFile));
        // iterates through lines and columns to get collider mask
        String line;
        int i = 0;
        try {
            while ((line = colReader.readLine()) != null) {
                String[] cols = line.split(",");
                for (int j = 0; j < cols.length; j++) {
                    if(cols[j].equals("1"))  // creates collider if 1 is the info
                        colliders[i][j] = new Collider(j*tileSize, i*tileSize,
                                            tileSize, tileSize, true, new Vector2(i, j));
                    else if(cols[j].equals("2")) // trigger collider
                        colliders[i][j] = new Collider(j*tileSize, i*tileSize,
                                tileSize, tileSize, false, new Vector2(i, j));
                }
                i++;
            }
        } catch (IOException e) {
            System.err.println("Could not read file: maps/" + name + ".col");
            e.printStackTrace();
        }
    }

    /**
     * Loads special fields into the mask of fields
     */
    private void loadFields() {
        // the file containing the information to be loaded
        InputStream configFile = Gdx.files.internal("maps/" + name + ".config").read();
        // buffered reader to read map config file
        BufferedReader configReader = new BufferedReader(new InputStreamReader(configFile));
        // iterates through lines and columns to get fields mask
        String line, type, id, complement;
        Vector2 index;
        try {
            while ((line = configReader.readLine()) != null) {
                if(line.contains("--")) // ignores any line that has "--" in any position
                    continue;

                // contains type and ID
                if(line.contains("_")) {
                    type = line.substring(0, line.indexOf('_')).trim();
                    id = line.substring(line.indexOf('_')+1, line.indexOf("=")).trim();
                } else { // contains only type
                    type = line.substring(0, line.indexOf('=')).trim();
                    id = "unused";
                }
                // gets indexes
                index = Common.stringToVector2(line);

                // gets complement information (if there is any)
                if(line.contains(":")) {
                    complement = line.substring(line.indexOf(':')+1).trim();
                } else {
                    complement = "unused";
                }

                // translates type to enum of field types
                Field.FieldType fieldType = Field.FieldType.valueOf(type);

                // creates the field in the correspondent position
                int i = (int)index.x; int j = (int)index.y;
                Field field = new Field(index, fieldType, id, complement);
                fields[i][j] = field;

                // if type is start, save start point
                if(fieldType == Field.FieldType.START) {
                    startPoint = index.cpy();
                }
                // if type is map teleport, save to helper list to aid in binding
                else if (fieldType == Field.FieldType.MAPTELEPORT) {
                    mapTeleports.add(new Field(index.cpy(), fieldType, id, complement));
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read file: maps/" + name + ".config");
            e.printStackTrace();
        }
    }

    /**
     * Gets the binding location of this map
     * with the map passed in the parameter
     * @param otherMap the other map to get binding location with
     * @return the i and j indexes of the binding location between maps
     */
    public Vector2 getTeleportBind(Map otherMap) {
        // other maps name (will help finding bind)
        String otherName = otherMap.getName();

        // search list of map teleports to find specific location of binding
        for(int i = 0; i < mapTeleports.size(); i++) {
            // gets field iterator
            Field field = mapTeleports.get(i);

            // if field teleports to other map, binding location was found
            if(field.getID().equals(otherName)) {
                return field.getIndex();
            }
        }

        // if binding location is not found, return default
        System.err.println("Binding location not found for teleporting: " + otherMap.getName()
                            + " and " + getName());
        return null;
    }

    /**
     * @return the existing layers of the map
     */
    public ArrayList<Layer> getLayers() {
        return layers;
    }

    /**
     * Creates a complete copy of the map
     * @return	the copy of the map
     */
    public Map createCopy() {
        ArrayList<Layer> copyLayers = new ArrayList<Layer>();
        for(int i = 0; i < layers.size(); i++) {
            copyLayers.add(new Layer(layers.get(i).createCopy(), layers.get(i).getzIndex(), layers.get(i).getOpacity()));
        }

        return new Map(copyLayers, name, mapSizeX, mapSizeY, tileSize);
    }

    /**
     * disposes all textures used in the map
     */
    public void dispose() {
        // disposes tilesets textures
        ArrayList<Tileset> tilesets = TilesetConfig.getInstance().getTilesets();
        // iterates through each tileset to dispose its texture
        for(int i = 0; i < tilesets.size(); i++)
            tilesets.get(i).getImage().dispose();
        // disposes shape renderer
        shapeRenderer.dispose();
    }

    /**
     * getter for the collider mask of the map
     * @return the collider mask of the map
     */
    public Collider[][] getColliders() {return colliders;}

    /**
     * getter for the field mask of the map
     * @return the field mask of the map
     */
    public Field[][] getFields() {return fields;}


    public Vector2 getStartPoint() {return startPoint;}

    /**
     * Renders tiled map created with 2d map builder
     * with all its tiles and layers
     *
     * @param gameCam the game camera object of game
     * @param camera  the libgdx camera for the game
     * @param batch   the sprite batch to render map
     */
    public void render(GameCamera gameCam, Camera camera, SpriteBatch batch) {
        // get map layers
        ArrayList<Layer> layers = this.getLayers();

        // gets frontiers of displayed tiles map matrix to minimize cost
        int first_tile_x = (int) Math.floor(gameCam.getPosition().x / tileSize);
        int first_tile_y = (int) Math.floor(gameCam.getPosition().y / tileSize);
        int offset_x = (int) Math.floor(gameCam.getPosition().x % tileSize);
        int offset_y = (int) Math.floor(gameCam.getPosition().y % tileSize);
        int last_tile_x = (int) Math.ceil(camera.viewportWidth / tileSize) + 2;
        //  + 2 makes sure that we have enough tiles for smooth transition
        int last_tile_y = (int) Math.ceil(camera.viewportHeight  / tileSize) + 2;
        // + 2 makes sure that we have enough tiles for smooth transition

        // make sure not to go out of bounds
        if (last_tile_x > mapSizeX)
            last_tile_x = mapSizeX;
        if (last_tile_y > mapSizeY)
            last_tile_y = mapSizeY;

        // updates viewport grid info
        viewportGrid.x = first_tile_x;
        viewportGrid.y = first_tile_y;
        viewportGrid.width = last_tile_x - first_tile_x;
        viewportGrid.height = last_tile_y - first_tile_y;

        // iterates through layers drawing tiles one by one
        for (int l = 0; l < layers.size(); l++) {
            // gets tiles in current layer
            Tile[][] lTiles = layers.get(l).getTiles();

            // draws each tile
            for (int i = 0; i < last_tile_y; i++) {
                for (int j = 0; j < last_tile_x; j++) {

                    // gets current tile i and j
                    int dataI = first_tile_y + i;
                    int dataJ = first_tile_x + j;

                    // make sure not to draw out of bounds
                    if(dataI < 0 || dataJ < 0 || dataI >= mapSizeY || dataJ >= mapSizeX) {
                        continue;
                    }

                    // checks if null before trying to draw
                    if(lTiles[dataI][dataJ] != null) {
                        // gets tilesize for cutting the tile in tileset
                        int tSize = lTiles[dataI][dataJ].getTileSize();
                        // gets indexes of tile in tileset
                        int tIdxI = lTiles[dataI][dataJ].getIndexI();
                        int tIdxJ = lTiles[dataI][dataJ].getIndexJ();
                        // gets anchors for cutting tile in tileset
                        int sImgX = tIdxJ * tSize;
                        int sImgY = tIdxI * tSize;
                        int tileSizeX = tSize; int tileSizeY = tSize;
                        // clamps for image source bounds
                        if(tileSizeX > lTiles[dataI][dataJ].getTileset().getImage().getWidth())
                            tileSizeX = lTiles[dataI][dataJ].getTileset().getImage().getWidth();
                        if(tileSizeY > lTiles[dataI][dataJ].getTileset().getImage().getHeight())
                            tileSizeY = lTiles[dataI][dataJ].getTileset().getImage().getHeight();
                        if(sImgX + tSize > lTiles[dataI][dataJ].getTileset().getImage().getWidth()) // raster limit on X
                            sImgX = (lTiles[dataI][dataJ].getTileset().getImage().getWidth() - tileSizeX);
                        if(sImgY + tSize > lTiles[dataI][dataJ].getTileset().getImage().getHeight()) // raster limit on Y
                            sImgY = (lTiles[dataI][dataJ].getTileset().getImage().getHeight() - tileSizeY);
                        if(sImgX < 0) sImgX = 0;
                        if(sImgY < 0) sImgY = 0;

                        // gets tileset region that matches the image of current tile
                        //TextureRegion tileTex = new TextureRegion(lTiles[dataI][dataJ].getTileset().getImage(),
                              //  sImgX, sImgY, tileSizeX, tileSizeY);
                        tileTex.setTexture(lTiles[dataI][dataJ].getTileset().getImage());
                        tileTex.setRegion(sImgX, sImgY, tileSizeX, tileSizeY);
                        tileTex.flip(false, true);
                        // finds correct position to draw current tile
                        int x = (int) ((j * tSize) - offset_x);
                        int y = (int) ((i * tSize) - offset_y);

                        // draws current tile
                        batch.draw(tileTex, x, y);
                    }
                }
            }
        }
    }

    /**
     * Gets the viewport dimensions and coordinates for the map grid
     */
    public Rectangle getViewportGrid() {
        return viewportGrid;
    }

    /**
     * renders player collider for debug
     */
    public void renderCollider(GameCamera gameCam, Camera camera) {
        // sets libgdx camera for correct projection
        shapeRenderer.setProjectionMatrix(camera.combined);
        // begins shape render with line mode
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // gets boundaries for visible colliders drawing
        int first_j = (int) viewportGrid.x;
        int first_i = (int) viewportGrid.y;
        int last_j = (int)viewportGrid.width + first_j;
        int last_i = (int)viewportGrid.height + first_i;

        // iterates through visible colliders
        for(int i = 0; i < last_i; i++) {
            for(int j = 0; j < last_j; j++) {
                // gets current tile i and j
                int dataI = first_i + i;
                int dataJ = first_j + j;

                // make sure not to go out of bounds
                if(dataI < 0)
                    dataI = 0;
                if(dataJ < 0)
                    dataJ = 0;
                if(dataI >= mapSizeY)
                    dataI = mapSizeY - 1;
                if(dataJ >= mapSizeX)
                    dataJ = mapSizeX - 1;

                if (colliders[dataI][dataJ] != null) { // only draws if exists a collider in mask pos
                    // gets iteration collider
                    Collider collider = colliders[dataI][dataJ];
                    // transforms world coords to screen coords for correct rendering
                    float collPosX = gameCam.worldToScreenPositionX(collider.x);
                    float collPosY = gameCam.worldToScreenPositionY(collider.y);
                    // renders collider for debug
                    if(!collider.isPhysical())
                        shapeRenderer.setColor(Color.RED);
                    else
                        shapeRenderer.setColor(Color.WHITE);
                    shapeRenderer.rect(collPosX, collPosY, collider.getWidth(), collider.height);
                }
            }
        }
        // ends shaperenderer
        shapeRenderer.end();
    }

    /**
     * @return returns name of the map (unique identifier)
     */
    public String getName(){return name;}


    public int getMapSizeX() {
        return mapSizeX;
    }

    public void setMapSizeX(int mapSizeX) {
        this.mapSizeX = mapSizeX;
    }

    public int getMapSizeY() {
        return mapSizeY;
    }

    public void setMapSizeY(int mapSizeY) {
        this.mapSizeY = mapSizeY;
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public boolean isRandomBattleEnabled() {
        return randomBattleEnabled;
    }

    public void setRandomBattleEnabled(boolean randomBattleEnabled) {
        this.randomBattleEnabled = randomBattleEnabled;
    }
}
