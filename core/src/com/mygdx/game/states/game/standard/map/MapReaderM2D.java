package com.mygdx.game.states.game.standard.map;

import com.badlogic.gdx.Gdx;
import com.mygdx.game.states.game.standard.map.Map;
import com.mygdx.game.states.game.standard.map.MapConfig;
import com.mygdx.game.states.game.standard.map.Tileset;
import com.mygdx.game.states.game.standard.map.TilesetConfig;
import com.mygdx.game.util.Config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Reads maps created with 2DMapBuilder map editor
 * parsing all information into known structures
 *
 * @author  Pedro Sampaio
 * @since   0.3
 */
public class MapReaderM2D {

    /**
     * Loads a map from a file of 2D Map Builder extension (.m2d)
     * which carries a XML notation. Loads all information necessary
     * for using it in the game as a tile map.
     * .m2d maps must be stored in assets/maps and tilesets
     * used in the maps must be stored in assets/imgs/tilesets
     *
     * @param map 	the .m2d file map name (maps must be in assets/maps).
     *              do not include the format in the name (.m2d)
     * @return the Map object with the loaded information from map file
     */
    public static Map loadFileDOM(String map) {

        try {
            // the file containing the information to be loaded
            InputStream fXmlFile = Gdx.files.internal("maps/"+map+".m2d").read();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            // reduction of redundancies
            doc.getDocumentElement().normalize();

            // debugs info read
            if(Config.debug) {
                System.out.println("\n----------------------------");
                System.out.println("Loading file: "+map);
                System.out.println("----------------------------");
                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            }

            /**
             * Retrieving basic information about the project's map
             */

            // width of the map
            int mapWidth = Integer.parseInt(doc.getElementsByTagName("mapwidth").item(0).getTextContent());
            // height of the map
            int mapHeight = Integer.parseInt(doc.getElementsByTagName("mapheight").item(0).getTextContent());
            // width of the map
            int tileSize = Integer.parseInt(doc.getElementsByTagName("tilesize").item(0).getTextContent());

            // debugs basic map info read
            if(Config.debug) {
                System.out.println("\nBasic Map Info:");
                System.out.println("Map Width: "+mapWidth);
                System.out.println("Map Height: "+mapHeight);
                System.out.println("Tile Size: "+tileSize);
            }

            /**
             * Retrieving tilesets information
             */
            NodeList tsList = doc.getElementsByTagName("tileset"); // list of tileset nodes
            ArrayList<Tileset> tilesets = new ArrayList<Tileset>(); // the list of tilesets to be contained in the project

            if(Config.debug) {
                System.out.println("\nTileset Info:");
            }

            // iterates through list of tilesets nodes to gather information
            for (int i = 0; i < tsList.getLength(); i++) {

                Node tsNode = tsList.item(i); // the current tileset node

                // gets information only if node is of element type
                if (tsNode.getNodeType() == Node.ELEMENT_NODE) {

                    // the current element of tileset list
                    Element tsElem = (Element) tsNode;

                    // information gathered
                    String tsName = tsElem.getElementsByTagName("name").item(0).getTextContent();
                    String tsSource = tsElem.getElementsByTagName("source").item(0).getTextContent();
                    int tsFirstID = Integer.parseInt(tsElem.getElementsByTagName("firstid").item(0).getTextContent());
                    int tsTileCount = Integer.parseInt(tsElem.getElementsByTagName("tilecount").item(0).getTextContent());
                    int tsTileSize = Integer.parseInt(tsElem.getElementsByTagName("tilesize").item(0).getTextContent());

                    // creates tileset and adds to the list of tilesets
                    tilesets.add(TilesetConfig.getInstance().createTileset(tsName, tsTileSize,
                                "imgs/tilesets/"+tsSource, tsFirstID, tsTileCount));

                    // debugs tileset info read
                    if(Config.debug) {
                        System.out.println("Name : " + tsName);
                        System.out.println("Source : " + tsSource);
                        System.out.println("First ID : " + tsFirstID);
                        System.out.println("Tilecount : " + tsTileCount);
                        System.out.println("Tilesize : " + tsTileSize+"\n");
                    }

                }
            }

            /**
             * Retrieving layers and tiles information
             */
            NodeList lList = doc.getElementsByTagName("layer"); // list of layer nodes
            String[] layers = new String[lList.getLength()];

            if(Config.debug) {
                System.out.println("\nLayers Info:");
            }

            // iterates through list of layer nodes to gather information
            for (int i = 0; i < lList.getLength(); i++) {

                Node lNode = lList.item(i); // the current layer node

                // adds to the layers string array (removing initial break line char that is an extra unecessary info)
                layers[i] =  lNode.getTextContent().substring(1, lNode.getTextContent().length());

                // debugs tileset info read
                if(Config.debug) {
                    System.out.println("Layer"+(i+1)+":\n" + lNode.getTextContent());
                }

            }

            // creates a map with the information gathered on layers and tilesets
            Map loadedMap = MapConfig.getInstance().createMap(layers, tilesets, map, mapWidth, mapHeight, tileSize);

            // createMap was not able to create the map with the information provided
            if(loadedMap == null) {
                System.err.println("\nError: could not create map with map file information");
            }

            // returns the map with loaded information
            return loadedMap;
        } catch (Exception e) { // error loading saved file
            System.err.println("Could not load map file: "+map+". Throw message: "+e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

}
