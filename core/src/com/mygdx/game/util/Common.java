package com.mygdx.game.util;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.item.Equipment;
import com.mygdx.game.states.game.standard.item.Item;
import com.mygdx.game.states.game.standard.item.factory.Factory;
import com.mygdx.game.states.game.standard.pervasive.Mission;
import com.mygdx.game.states.game.standard.player.Player;
import com.mygdx.game.ui.TranslatableImageButton;
import com.mygdx.game.states.game.standard.camera.GameCamera;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Class that holds common methods
 * for different modules
 * Pattern:
 * Singleton
 *
 * @author Pedro Sampaio
 * @since   0.2
 */
public class Common {

    // random object for randInt method
    private Random _rnd;

    // hashmap of missions to optimize mission iterations
    private HashMap<Integer, Mission> missions;
    // hashmap of player's completed missions
    private HashMap<Integer, Timestamp> missionsDone;

    // singleton reference
    private static Common instance = null;
    private Player player; // player reference

    // defeats external instantiation
    private Common () {
        _rnd = new Random();
        missions = new HashMap<Integer, Mission>();
        missionsDone = new HashMap<Integer, Timestamp>();
    }

    // initializes Common or use a previous instantiation if it exists
    public static Common getInstance() {
        if (instance == null)
            instance = new Common();

        return instance;
    }

    /**
     * Sets a touchable policy for all actors in a stage
     *
     * @param stage     the stage to set new touchable policy
     * @param touchable the touchable policy
     */
    public static void setTouchableStage(Stage stage, Touchable touchable) {
        // set touchable in all stage actors
        Array<Actor> actors = stage.getActors();
        for(int i = 0; i < actors.size; i++)
            actors.get(i).setTouchable(touchable);
    }

    /**
     * Updates all texts for translatable buttons
     * on account of language changes
     */
    public static void updateAllTButtonTexts() {
        ArrayList<TranslatableImageButton> tButtons = Main.getInstance().getTButtons();
        for(int i = 0; i < tButtons.size(); i++)
            tButtons.get(i).updateLanguage();
    }

    public static Sprite createScaledSprite(Texture texture) {
        Sprite sprite = new Sprite(texture);
        float SCALE_RATIO = texture.getWidth() / Gdx.graphics.getWidth();
        sprite.getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
        sprite.setSize(sprite.getWidth() / SCALE_RATIO,
                sprite.getHeight() / SCALE_RATIO);
        return sprite;
    }

    /**
     * Converts coordinates to match zoomed viewport of game camera
     *
     * @param coords    the coordinates to be converted into zoomed viewport
     * @param gameCam   the camera containing the zoom information
     * @return  the converted coordinates in a Vector2
     */
    public static Vector2 convertToZoomedViewport(Vector2 coords, GameCamera gameCam) {
        // converts input coords to match game camera viewport and zoom
        float ratioWidth = Gdx.graphics.getWidth() / Config.baseWidth;
        float ratioHeight = Gdx.graphics.getHeight() / Config.baseHeight;
        Vector2 zoomViewCoords = new Vector2(coords.x/(ratioWidth * gameCam.getZoom()),
                                            coords.y/(ratioHeight * gameCam.getZoom()));
        return zoomViewCoords;
    }

    /**
     * Converts coordinates to match zoomed viewport
     *
     * @param coords    the coordinates to be converted into zoomed viewport
     * @param zoom      the zoom information
     * @return  the converted coordinates in a Vector2
     */
    public static Vector2 convertToZoomedViewport(Vector2 coords, float zoom) {
        // converts input coords to match game camera viewport and zoom
        float ratioWidth = Gdx.graphics.getWidth() / Config.baseWidth;
        float ratioHeight = Gdx.graphics.getHeight() / Config.baseHeight;
        Vector2 zoomViewCoords = new Vector2(coords.x/(ratioWidth * zoom),
                coords.y/(ratioHeight * zoom));
        return zoomViewCoords;
    }

    /**
     * Converts coordinates to match viewport of camera
     * with default width and height and y-up libgdx coordinate system
     *
     * @param coords    the coordinates to be converted into viewport
     * @return  the converted coordinates in a Vector2
     */
    public static Vector2 convertToDefaultViewport(Vector2 coords) {
        // converts input coords to match game camera viewport and zoom
        float ratioWidth = Gdx.graphics.getWidth() / Config.baseWidth;
        float ratioHeight = Gdx.graphics.getHeight() / Config.baseHeight;
        Vector2 zoomViewCoords = new Vector2(coords.x/(ratioWidth),
                Config.baseHeight - coords.y/(ratioHeight));
        return zoomViewCoords;
    }

    /**
     * Gets the overall zoom applied into the game viewport
     * in the X axis (width)
     *
     * @param gameCam   the camera containing the zoom information
     * @return  the overall zoom applied into the game viewport width
     */
    public static float getOverallZoomX(GameCamera gameCam) {
        return (Gdx.graphics.getWidth() / Config.baseWidth) * gameCam.getZoom();
    }

    /**
     * Gets the overall zoom applied into the game viewport
     * in the Y axis (height)
     *
     * @param gameCam   the camera containing the zoom information
     * @return  the overall zoom applied into the game viewport height
     */
    public static float getOverallZoomY(GameCamera gameCam) {
        return (Gdx.graphics.getHeight() / Config.baseHeight) * gameCam.getZoom();
    }

    private static ShapeRenderer debugRenderer = new ShapeRenderer();

    public static void drawDebugLine(Vector2 start, Vector2 end)
    {
        Gdx.gl.glLineWidth(2);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.WHITE);
        debugRenderer.line(start, end);
        debugRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public static float smootherStep(float edge0, float edge1, float x)
    {
        // Scale, and clamp x to 0..1 range
        x = clamp((x - edge0)/(edge1 - edge0), 0.0f, 1.0f);
        // Evaluate polynomial
        return x*x*x*(x*(x*6 - 15) + 10);
    }

    public static float clamp(float x, float lowerLimit, float upperLimit)
    {
        if (x < lowerLimit) x = lowerLimit;
        if (x > upperLimit) x = upperLimit;
        return x;
    }

    /**
     * Converts string to vector2
     * @param str the string to be converted
     * @return the vector2 converted from string
     */
    public static Vector2 stringToVector2(String str) {
        Vector2 vec2 = new Vector2();
        vec2.x = Integer.parseInt(str.substring(str.indexOf('{')+1, str.indexOf(',')).trim());
        vec2.y = Integer.parseInt(str.substring(str.indexOf(',')+1, str.indexOf('}')).trim());
        return vec2;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = getInstance()._rnd.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Float between min and max, inclusive.
     * @see Random#nextFloat()
     */
    public static float randFloat(float min, float max) {
        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        float randomNum = getInstance()._rnd.nextFloat() * (max - min) + min;

        return randomNum;
    }

    /**
     * Creates a sequence of frames that represents an animation and stores
     * in a TextureRegion array. The sequence is based on sprite sheets that
     * have animations from left to right and fixed y for each sequence of animation.
     *
     * @param spriteSheet   the spritesheet that contains the sprites of animation sequence
     * @param size          the number of frames in the animation sequence
     * @param x_ini         the initial x for the cut of frames in animation sequence
     * @param delta_x       the delta x for each frame of the animation sequence
     * @param y             the fixed y for the animation sequence
     * @param x_offset      an offset that will be applied to the x initial value
     * @param y_offset      an offset that will be applied to the y fixed value
     * @param sprWidth      the width of the animation sprite to be cut
     * @param sprHeight     the height of the animation sprite to be cut
     *
     * @return the created animation sequence
     */
    public static ArrayList<TextureRegion> getAnimationSequence(Texture spriteSheet, int size, int x_ini,
                                                                 int delta_x, int y, int x_offset, int y_offset,
                                                                 int sprWidth, int sprHeight) {
        // creates array that will contain the anim sequence
        ArrayList<TextureRegion> sequence = new ArrayList<TextureRegion>();

        // initially x is the initial x value plus offset
        // after each iteration the delta is applied
        int x_acc = x_ini + x_offset;
        for(int i = 0; i < size ; i++) {
            // adds each frame of the sequence defined in
            // a fixed Y and a variable x to sequence array
            sequence.add(new TextureRegion(spriteSheet, x_acc+(delta_x*i), y+y_offset, sprWidth, sprHeight));
        }

        // return the newly created animation sequence
        return sequence;
    }

    /**
     * Rolls a dice that has 100 sides.
     * Helper method to help probability calculations.
     * @param chance  the probability desired
     * @return true if the dice has achieved the probability desired, false otherwise
     */
    public static boolean rollDice(int chance) {
        int dice = randInt(0,100);
        if(dice <= chance)
            return true;
        else
            return false;
    }

    /**
     * Rolls a dice that has 100 real sides (float).
     * Helper method to help probability calculations.
     * @param chance  the probability desired
     * @return true if the dice has achieved the probability desired, false otherwise
     */
    public static boolean rollDice(float chance) {
        float dice = randFloat(0,100);
        if(dice <= chance)
            return true;
        else
            return false;
    }


    /**
     * Returns a random enum value from
     * a generic enum
     * @param enumClass the enum class to pick a random value
     * @param <T>   the parametrized enum type
     * @return  the enum value randomly chosen
     */
    public static <T extends Enum<?>> T randomEnum(Class<T> enumClass){
        int x = getInstance()._rnd.nextInt(enumClass.getEnumConstants().length);
        return enumClass.getEnumConstants()[x];
    }

    /**
     * Linear interpolation between two colors
     * @param currentColor  the color that will be interpolated into target color
     * @param targetColor   the target color of interpolation
     * @param speed         the speed of the interpolation
     */
    public static Color lerpColor(Color currentColor, Color targetColor, float speed) {
        // updates color a little to white
        if(currentColor.r < targetColor.r)
            currentColor.r += Gdx.graphics.getDeltaTime() * speed;
        else if(currentColor.r > targetColor.r)
            currentColor.r -= Gdx.graphics.getDeltaTime() * speed;
        if(currentColor.g < targetColor.g)
            currentColor.g += Gdx.graphics.getDeltaTime() * speed;
        else if(currentColor.g > targetColor.g)
            currentColor.g -= Gdx.graphics.getDeltaTime() * speed;
        if(currentColor.b < targetColor.b)
            currentColor.b += Gdx.graphics.getDeltaTime() * speed;
        else if(currentColor.b > targetColor.b)
            currentColor.b -= Gdx.graphics.getDeltaTime() * speed;

        // clamp values for security reasons
        currentColor.r = MathUtils.clamp(currentColor.r, 0f, targetColor.r);
        currentColor.g = MathUtils.clamp(currentColor.g, 0f, targetColor.g);
        currentColor.b = MathUtils.clamp(currentColor.b, 0f, targetColor.b);

        // returns the result of this interpolation
        return currentColor;
    }

    /**
     * Check if preference received via parameter exists
     * @param prefName the preference name to check existence
     * @return true if preference exists, false otherwise
     */
    public static boolean doPrefExist ( String prefName )
    {
        Preferences tempPref = Gdx.app.getPreferences ( prefName );

        if (tempPref.getString("created") == null )
            return false;
        else
            return true;
    }

    /**
     * Receives any kind of string and
     * makes first letter uppercase and the rest lowercase
     * @param str the string to format
     * @return the string formatted with first letter capitalized
     */
    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Serializes player inventory items in a string
     * @param invPages  player inventory pagess\
     * @return the string containing the inventory items data
     */
    public static String serializeInventory(ArrayList<Item[][]> invPages) {
        // string builder that will contain all player inventory items information
        StringBuilder invInfo = new StringBuilder("");
        for(int p = 0; p < invPages.size(); p++) {
            Item[][] page = invPages.get(p); // the current page
            // iterates through page grid of items
            for(int i = 0; i < page.length; i++) {
                for (int j = 0; j < page[i].length; j++) {
                    // stores item information if exists in grid position
                    if(page[i][j] != null) {
                        invInfo.append(".id_"); // item separator token
                        if(page[i][j].isEquipable()) { // if it is equipment
                            invInfo.append(((Equipment) page[i][j]).getUniqueID()).append(";"); // stores equipment uID
                            invInfo.append("lv_").append(((Equipment) page[i][j]).getLevel()).append(";"); // stores equipment level
                        }
                        else { // if it is not equipment
                            invInfo.append(page[i][j].getId()).append(";"); // stores item id that is unique already
                            invInfo.append("lv_-1;") ; // stores -1 as level since item does not have level
                        }
                        invInfo.append("page_").append(p).append(";"); // stores equipment inventory page index
                        invInfo.append("i_").append(i).append(";"); // stores equipment inventory i - index
                        invInfo.append("j_").append(j); // stores equipment inventory j - index - last info
                    }
                }
            }
        }

        return invInfo.toString();
    }

    /**
     * Deserialize inventory data from string to inventory data structure
     * @param invInfo the string to deserialize
     * @return the pages of inventory containing item data obtained from string received
     */
    public static ArrayList<Item[][]> deserializeInventory(String invInfo) {
        // initialize pages
        ArrayList<Item[][]> pages = new ArrayList<Item[][]>();
        // adds n config pages
        for(int i = 0 ; i < Config.inventoryNPages; i++)
            pages.add(new Item[Config.inventoryMaxItemsY][Config.inventoryMaxItemsX]);
        // split strings for each item existent
        String[] items = invInfo.split("\\.");
        // iterates through player items strings
        for(int i = 1; i < items.length; i++) { // ignores first data that is trash
            // split string again, to separate item data
            String[] data = items[i].split(";");
            // gets item uID
            int uID = Integer.parseInt(data[0].substring(3));
            // gets item level
            int level = Integer.parseInt(data[1].substring(3));
            // gets item inventory page
            int page = Integer.parseInt(data[2].substring(5));
            // gets item i index in inventory
            int i_idx = Integer.parseInt(data[3].substring(2));
            // gets item ji index in inventory
            int j_idx = Integer.parseInt(data[4].substring(2));
            // creates item and adds item to its corresponded place in inventory
            pages.get(page)[i_idx][j_idx] = Factory.createItem(uID, level, false);

            if(Config.debug)
                System.out.println("Loaded Inventory Item: uID: " + uID + " ; level: " + level + " ; page: "
                        + page + " ; i-index: " + i_idx + " ; j-index: " + j_idx);
        }

        return pages;
    }

    /**
     * Serializes player worn equipments into a string
     * @param wornEquips player worn equipments to serialize
     * @return worn equipments data stored in a string
     */
    public static String serializeWornEquipment(Equipment[] wornEquips) {
        StringBuilder wornData = new StringBuilder(""); // will contain the equipment data
        for(int i = 0 ; i < wornEquips.length; i++) {
            // if there is an equipment in slot, save it
            if(wornEquips[i] != null) {
                wornData.append(".id_"); // equipment separator token
                wornData.append(wornEquips[i].getUniqueID()).append(";"); // stores equipment uID
                wornData.append("lv_").append(wornEquips[i].getLevel()).append(";"); // stores equipment level
                wornData.append("slot_").append(i); // stores equipment slot
            }
        }
        return wornData.toString();
    }

    /**
     * Deserialize worn equipment data from string to worn equipment array
     * @param wornInfo  the string containing worn equipment data to deserialize
     * @return  the array of worn equipments containing data from the received string
     */
    public static Equipment[] deserializeWornEquipment(String wornInfo) {
        // split strings for each worn equipment existent
        String[] wornEquips = wornInfo.split("\\.");
        // array of worn equipments to be loaded
        Equipment[] wornEquipment = new Equipment[Equipment.Slot.values().length];
        // iterates through player wornEquips strings
        for(int i = 1; i < wornEquips.length; i++) { // ignores first data that is trash
            // split string again, to separate item data
            String[] data = wornEquips[i].split(";");
            // gets equipment uID
            int uID = Integer.parseInt(data[0].substring(3));
            // gets equipment level
            int level = Integer.parseInt(data[1].substring(3));
            // gets equipment slot
            int slot = Integer.parseInt(data[2].substring(5));
            // creates item and adds item to its corresponded place in inventory
            wornEquipment[slot] = (Equipment)Factory.createItem(uID, level, false);

            if(Config.debug)
                System.out.println("Loaded Worn Equipments: uID: "+uID+" ; level: "+level+" ; slot: "+slot);
        }
        return wornEquipment;
    }

    /**
     * Inserts a break line character in the last
     * space character found in string received.
     * If no spaces are found, adds break line before the last character
     * @param str the string to insert the break line character
     * @return  the formatted string with the break line character
     */
    public static String insertBreakLine(String str) {
        int spaceIdx = -1; // index of last space found (-1 if not found)

        // iterates through string characters
        for(int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == ' ')
                spaceIdx = i; // updates last space index found
        }

        // if no space was found
        if(spaceIdx == -1) {
            str = new StringBuilder(str).insert(str.length()-2, "\n").toString();
        } else { // space was found, insert in last space index
            StringBuilder strBuilder = new StringBuilder(str);
            strBuilder.setCharAt(spaceIdx, '\n');
            str = strBuilder.toString();
        }

        // return formatted string
        return str;
    }

    /**
     * Previews the layout of a string in case of concatenation of a new word
     *
     * @param currStr   the current string without next word concatenation
     * @param currSpIdx the index of current last word next space
     * @param dataStr   the data string that will contain the next word to concatenate
     *                  the data string is exactly the same as the current string plus
     *                  the next data that will contain next words to be concatenated
     * @param font      the font to use on glyph layout creation
     * @return  a GlyphLayout containing string width and height in case of next word concatenation
     */
    public static GlyphLayout getNextWordConcatLayout(String currStr, int currSpIdx, String dataStr, BitmapFont font) {
        // gets the next word to be concatenated
        String nextWord = "";
        char c = '.';
        for(int i = currSpIdx+1; c != ' ' && i < dataStr.length(); i++) {
            c = dataStr.charAt(i);
            nextWord += c;
        }
        // creates concatenated string
        String concatStr = currStr + nextWord;
        // returns glyph layout of concatenated string
        return new GlyphLayout(font, concatStr);
    }

    /**
     * Returns the respective item quality color
     * for font to use in item info drawing
     * @param quality the quality to retrieve respective color
     * @return the respective color of the quality received in parameter
     */
    public static Color getQualityColor(Item.Quality quality) {
        Color color; // the color of the text
        // gets color depending on item rarity
        switch(quality) {
            case NORMAL:
                color = Config.normalItemColor;
                break;
            case UNCOMMON:
                color = Config.uncommonItemColor;
                break;
            case RARE:
                color = Config.rareItemColor;
                break;
            case LEGENDARY:
                color = Config.legendaryItemColor;
                break;
            default:
                System.err.println("Unmapped quality: "+quality);
                color = Config.normalItemColor;
                break;
        }
        return color;
    }

    /**
     * Quick method to format number to two decimals
     * @param number the number to be formatted
     * @return the number formatted in two decimals
     */
    public static double formatTwoDecimal(double number) {
        number = Math.round(number * 100);
        return number/100;
    }

    /**
     * Converts string in {#anystring,#anystring,#anystring...} format to array list of integers
     * ex. {3000,5000,7000} to array list containing integers 3000, 5000 and 7000
     * @param str the string to be converted
     * @return the array list created from string
     */
    public static ArrayList<Integer> stringToArrayList(String str) {
        // creates array list
        ArrayList<Integer> list = new ArrayList<Integer>();
        // prepares string
        str = str.replace("{", "");
        str = str.replace("}", "");
        str = str.replace(" ", "");
        // splits string
        String[] splitS = str.split(",");
        // fills array list with integer values present in string
        for (int i = 0; i < splitS.length; i++) {
            if(splitS[i] != "") // only parses if there is integer in string
                list.add(Integer.parseInt(splitS[i]));
        }

        // returns the built array list
        return list;
    }

    /**
     * Draws the current information string centralized in the sensor UI interface
     * @param batch the batch to draw
     * @param infoStr the string to be drawn
     * @param uiX the x position of sensor UI menu
     * @param uiY the y position of sensor UI menu
     * @param uiW the width of sensor UI menu
     * @param uiH the height of sensor UI menu
     */
    public static void renderInfoText(SpriteBatch batch, String infoStr, float uiX, float uiY, float uiW, float uiH) {
        final GlyphLayout svInfoLayout = new GlyphLayout(Resource.marketFont, infoStr);
        float svInfoX = uiX + (uiW/2) - (svInfoLayout.width/2);
        float svInfoY = uiY + (uiH/2) - (svInfoLayout.height/2);
        Resource.marketFont.draw(batch, svInfoLayout , svInfoX, svInfoY);
        // TODO - be careful with width bounds (resize on necessity?)
        // TODO - improve the FPS on market rendering
    }

    public static void setPlayer(Player player) {
        getInstance().player = player;
    }

    public static Player getPlayer() {return getInstance().player;}

    public HashMap<Integer, Mission> getMissions() {
        return missions;
    }

    /**
     * Gives player mission (received from param) rewards
     * @param mission_id the id of the mission which rewards will be given to player
     */
    public void giveRewards(int mission_id) {
        // gets mission to get correct rewards
        Mission mission = getMissions().get(mission_id);
        // gives player the exp reward
        player.getAttributes().addExp(mission.getExpReward());
        // gives player gold reward
        player.getInventory().addGold(mission.getGoldReward());
        // gives player items rewards (based on equipment level of mission)
        for(int i = 0; i < mission.getItemReward().size(); i++) {
            player.getInventory().addItem(Factory.createItem(mission.getItemReward().get(i),
                                            (int) mission.getEquipLevel(), false));
        }
    }

    public HashMap<Integer, Timestamp> getMissionsDone() {
        return missionsDone;
    }

    public void setMissionsDone(HashMap<Integer, Timestamp> missionsDone) {
        this.missionsDone = missionsDone;
    }

    /**
     * Prints in console the desired latency in milliseconds
     * received in parameters with a label
     * @param label     the label of this latency
     * @param ms        the latency in milliseconds
     */
    public static void printLatency(String label, long ms) {
        System.out.println(label + " latency: " + ms + "ms");
    }
}
