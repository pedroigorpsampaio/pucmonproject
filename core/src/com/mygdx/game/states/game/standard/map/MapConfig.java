package com.mygdx.game.states.game.standard.map;

import com.mygdx.game.util.Config;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Class that will hold map configurations and information.
 *
 * @author 	Pedro Sampaio
 * @since	0.3
 *
 */
public class MapConfig extends Observable {

    public static float zoom;            // map visualization zoom
    private float zoomSpeed = 0.1f;        // map's zoom speed (percentage)
    private int moveY = 0;                // map's current speed on x axis
    private int moveX = 0;                // map's current speed on y axis
    private int speed = 5;                // map's camera speed
    private static float minZoom;    // minimum zoom (maintain min view relative to the tilesize)
    private static float maxZoom;    // maximum zoom (maintain max view relative to the tilesize)

    private Map currentMap; // the current game map being rendered
    private ArrayList<Map> maps; // list of game maps

    /**
     * Gets the list of game maps
     * @return the list of game maps
     */
    public ArrayList<Map> getMaps() {
        return maps;
    }

    /**
     * Sets the list of game maps
     * @param maps the list of game maps
     */
    public void setMaps(ArrayList<Map> maps) {
        this.maps = maps;
    }

    /**
     * Gets current map being rendered on screen
     * @return the current map of the game
     */
    public Map getCurrentMap() {
        return currentMap;
    }

    /**
     * Sets the current map being rendered on screen
     * @param currentMap the current map of the game
     */
    public void setCurrentMap(Map currentMap) {
        this.currentMap = currentMap;
    }


    // mantains only one instance of map config (singleton pattern)
    private static MapConfig instance = null;

    protected MapConfig() {
        // Exists only to defeat instantiation.
        setDefault();
        // initializes list of game maps
        maps = new ArrayList<Map>();
    }

    /**
     * @return returns map config instance
     * creates the instance if does not exist yet
     */
    public static MapConfig getInstance() {
        if (instance == null)
            instance = new MapConfig();

        return instance;
    }

    /**
     * Sets a default configuration for the map
     *
     */
    public static void setDefault() {
        minZoom = 1;    // minimum zoom (maintain min view relative to the tilesize)
        maxZoom = 5;// maximum zoom (maintain max view relative to the tilesize)
        zoom = minZoom;
    }

    /**
     * Updates default configuration with
     * new configuration that is specific to a map
     *
     * @param tilesize     the size of a tile in the map
     * @param mapSizeXAxis the number of tiles in the X-axis of map
     * @param mapSizeYAxis the number of tiles in the Y-axis of map
     */
    public static void updateConfig(int tilesize, int mapSizeXAxis, int mapSizeYAxis) {
        minZoom = 2;    // minimum zoom (maintain min view relative to the tilesize)
        maxZoom = 5;// maximum zoom (maintain max view relative to the tilesize)
        zoom = minZoom;
    }

    /**
     * Calculates the size of the tile in map
     * considering what is current zoom value
     *
     * @author Pedro Sampaio
     * @return the size of the tile multiplied by current map zoom
     * @since 0.5
     */
    public static int getTileZoomed() {
        return (int) Math.floor(instance.getCurrentMap().getTileSize() * MapConfig.zoom);
    }

    /**
     * Creates a map with the received layers and tilesets
     * information and returns it. The layers information contains
     * a string representing the tiles information for each layer
     * and each tileset contains the remaining information needed
     * to infer from what tileset the tile information is from
     * Sets selected layer as 0 (first layer)
     *
     * @param layers   the layers of the map to be created containing all tile information for all layers
     * @param tilesets the tilesets of the map containing the remaining data necessary for the map creation
     * @param name  the name of the map file
     * @param mapSizeX number of tiles in map on x-axis (columns)
     * @param mapSizeY number of tiles in map on y-axis (lines)
     * @param tileSize the size of the map's tile
     * @return the created map with the received information, or null if map creation could not be done correctly
     */
    public Map createMap(String[] layers, ArrayList<Tileset> tilesets, String name,
                         int mapSizeX, int mapSizeY, int tileSize) {

        // debugs broken string
        if (Config.debug) {
            System.out.print("-----------------------\n Map Loading from File \n-----------------------\n");
        }

        // the list of layers for the created map;
        ArrayList<Layer> mapLayers = new ArrayList<Layer>();
        // the matrix of tiles for each layer
        Tile[][] tiles = null;
        int i = 0;
        int j = 0;
        // for each layer string received, breaks the string to get the information needed
        for (int l = 0; l < layers.length; l++) {
            // separate the lines from the whole grid string
            String[] lines = layers[l].split("[\r\n]+");

            // for matrix initialization, lets see how many columns there is
            String[] tIDs = lines[0].split(",");

            // initialize tiles matrix for this layer
            tiles = new Tile[lines.length][tIDs.length];

            // iterates for each line existing in the layer grid
            for (i = 0; i < lines.length; i++) {
                // gets the tile IDs from the current line i
                tIDs = lines[i].split(",");
                // for each tID found, creates a new tile
                for (j = 0; j < tIDs.length; j++) {
                    // information of the tile
                    int tileID = Integer.parseInt(tIDs[j]); // the global id of the tile

                    // if tile id is 0 we dont need to create
                    // a tile, since it does not exist
                    if (tileID == 0)
                        continue;

                    int tMatrixI = i;    // the i-index of the tile in the map grid
                    int tMatrixJ = j;    // the j-index of the tile in the map grid
                    // information we still need to find
                    Tileset tileTS = null;    // the tileset for the tile
                    int tIndexI = 0;        // the i-index of the tile in the tileset
                    int tIndexJ = 0;        // the j-index of the tile in the tileset

                    // lets search through the tilesets to find the remaining info needed
                    for (int k = 0; k < tilesets.size(); k++) {
                        int tileSizeX = tilesets.get(k).getTileSizeX();    // the number of tiles of the tileset in x-axis
                        int tileCount = tilesets.get(k).getTileCount(); // the number of tiles in the tileset
                        int firstID = tilesets.get(k).getFirstID();  // the firstID of the tileset

                        // tile belongs to the current tileset if tileID
                        // is within tilesets ID limits [firstID, firstID+tileCount[
                        if (tileID >= firstID && tileID < (firstID + tileCount)) {
                            tileTS = tilesets.get(k); // stores tile tileset
                            // calculates the index I of tile in tileset
                            tIndexI = (tileID - firstID) / tileSizeX;
                            tIndexJ = (tileID - firstID) % tileSizeX;
                            // done finding needed info
                            break;
                        }

                    }

                    // could not find a tile tileset, abort map creation
                    if (tileTS == null) {
                        // debugs the problematic tile
                        if (Config.debug) {
                            System.out.println("\nCould not find tileset of tileID: " + tileID);
                        }
                        return null;
                    }

                    // now that we have all information, lets create the tile and add to the matrix of tiles
                    tiles[tMatrixI][tMatrixJ] = new Tile(tIndexI, tIndexJ, tileTS, tileID, tMatrixI, tMatrixJ);

                    // debugs broken string
                    if (Config.debug) {
                        System.out.print(tIDs[j] + ",");
                    }
                }
                // debugs broken string
                if (Config.debug) {
                    System.out.print("\n");
                }
            }

            // creates layer that will contain the tiles for the current
            // layer and adds to the list of map layers
            mapLayers.add(new Layer(tiles, l, 1));

            // debugs broken string
            if (Config.debug) {
                System.out.print("\n");
            }
        }

        // return the new map created with all information
        // (selected layer is set as the first one - 0)
        return new Map(mapLayers, name, mapSizeX, mapSizeY, tileSize);
    }

    /**
     * @return the moveY
     */
    public int getMoveY() {
        return moveY;
    }

    /**
     * @param moveY the moveY to set
     */
    public void setMoveY(int moveY) {
        this.moveY = moveY;
    }

    /**
     * @return the moveX
     */
    public int getMoveX() {
        return moveX;
    }

    /**
     * @param moveX the moveX to set
     */
    public void setMoveX(int moveX) {
        this.moveX = moveX;
    }

    /**
     * @return the speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * @return the maxZoom
     */
    public float getMaxZoom() {
        return maxZoom;
    }

    /**
     * @param maxZ the maxZoom to set
     */
    public void setMaxZoom(float maxZ) {
        maxZoom = maxZ;
    }

    /**
     * @return the minZoom
     */
    public float getMinZoom() {
        return minZoom;
    }

    /**
     * @param minZ the minZoom to set
     */
    public void setMinZoom(float minZ) {
        minZoom = minZ;
    }

    /**
     * @return the zoomInSpeed
     */
    public float getZoomInSpeed() {
        return (1 + zoomSpeed);
    }

    /**
     * @return the zoomOutSpeed
     */
    public float getZoomOutSpeed() {
        return (1 - zoomSpeed);
    }

    /**
     * Loads all maps in the list of game maps
     * to be accessed throughout the games life
     * @param mapID the map ID (From enum of maps) to be set as current map
     */
    public void loadMaps(int mapID) {
        // creates maps
        Map exampleMap = MapReaderM2D.loadFileDOM("example_map");
        Map testMap = MapReaderM2D.loadFileDOM("testmap");
        Map mapDemo = MapReaderM2D.loadFileDOM("mapdemo");
        String currentMap = Map.MapID.values()[mapID].toString();
        // sets current map
        if(currentMap.equals("example_map"))
            setCurrentMap(exampleMap);
        else if(currentMap.equals("testmap"))
            setCurrentMap(testMap);
        else if(currentMap.equals("mapdemo"))
            setCurrentMap(mapDemo);
        else {
            System.err.println("Unknown map saved. Loaded default map: "+mapDemo.getName());
            setCurrentMap(mapDemo);
        }
        // adds to the list of maps
        maps.add(exampleMap);
        maps.add(testMap);
        maps.add(mapDemo);
    }

    /**
     * Gets map from list of game maps
     * with the correspondent id
     * @return the map attached to the id received in parameter
     */
    public Map getMapWithID(String id) {
        // iterates through list of maps
        for(int i = 0 ; i < maps.size(); i++) {
            // current map of iteration
            Map map = maps.get(i);
            if(map.getName().equals(id)) {
                return map;
            }
        }

        System.err.println("Map "+id+" was not found.");
        return null;
    }
}