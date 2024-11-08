package com.mygdx.game.states.game.standard.pervasive;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.Main;
import com.mygdx.game.messages.MessageContent;
import com.mygdx.game.messages.MessageFlag;
import com.mygdx.game.messages.MessageMarket;
import com.mygdx.game.messages.MessageMissionData;
import com.mygdx.game.messages.ServerMessages;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.architecture.ServerListener;
import com.mygdx.game.states.game.standard.inventory.Inventory;
import com.mygdx.game.ui.ColliderButton;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;
import com.sddl.client.PucmonClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class responsible for displaying
 * pervasive map and its locations
 * as well as providing interaction
 * for the player with the pervasive world
 *
 * @author Pedro Sampaio
 * @since 1.9
 */
public class PervasiveMap implements ServerListener {

    private SpriteBatch batch; // the batch to draw pervasive map
    private SpriteBatch uiBatch; // the batch to draw static objects
    private OrthographicCamera camera; // camera to aid in map zooming
    private OrthographicCamera uiCamera; // camera to aid in ui drawing
    private Viewport viewport; // viewport to be able to adapt to different resolutions
    private Viewport uiViewport; // viewport to adapt static elements to different resolutions
    private Texture mapTex; // texture of pervasive map
    private Rectangle scissors;   // scissors for clipping
    private Rectangle clipBounds; // clipping bounds rect
    private float camX; // position of the pervasive map cam in the X axis
    private float camY; // position of the pervasive map cam in the Y axis
    private float width; // current width of the pervasive map
    private float height; // current height of the pervasive map
    private float zoom; // current zoom of the pervasive map
    private float minX; // minimum X camera boundary
    private float minY; // minimum Y camera boundary
    private float maxX; // maximum X camera boundary
    private float maxY; // maximum Y camera boundary
    private float mapPercent = 0.9f; // the amount in % that map will occupy background
    private HashMap<Integer, Location> locations; // hashmap of pervasive map locations
    private ArrayList<ColliderButton> locationBtns; // array list of location buttons
    private static int selectedLocation = -1; // selected location (-1 for no locations selected)
    private String infoStr; // string containing information to display to player
    private float infoTimer = 0f; // information message timer

    /**
     * UI textures
     */
    TextureRegion texBtnPressed; // button pressed region texture
    TextureRegion texBtnUnpressed; // button unpressed texture region
    TextureRegion texLocationBG; // background of location detailing texture region
    TextureRegion texContentBox; // content box region texture
    private String character;   // player`s character name
    private boolean isRetrieving; // if is retrieving server data

    public PervasiveMap () {
        // subscribe to server to be able to communicate with it
        subscribeToServer();

        // initializes sprite batch
        batch = new SpriteBatch();

        // initializes ui sprite batch
        uiBatch = new SpriteBatch();

        // sets libgdx camera for game scaling on different screens
        camera = new OrthographicCamera(Config.baseWidth / Config.pervasiveMapDefaultZoom,
                                        Config.baseHeight / Config.pervasiveMapDefaultZoom);
        camera.setToOrtho(false, Config.baseWidth / Config.pervasiveMapDefaultZoom,
                                Config.baseHeight / Config.pervasiveMapDefaultZoom);

        // sets viewport behaviour on scaling for different screens
        viewport = new StretchViewport(Config.baseWidth / Config.pervasiveMapDefaultZoom,
                                        Config.baseHeight / Config.pervasiveMapDefaultZoom, camera);
        viewport.apply();

        // sets libgdx camera for  UI scaling on different screens
        uiCamera = new OrthographicCamera(Config.baseWidth, Config.baseHeight);
        uiCamera.setToOrtho(false, Config.baseWidth, Config.baseHeight);

        // sets UI viewport behaviour on scaling for different screens
        uiViewport = new StretchViewport(Config.baseWidth, Config.baseHeight, uiCamera);
        uiViewport.apply();

        // gets reference pervasive map texture from resource
        mapTex = Resource.pucMap;

        // create clipping scissors
        scissors = new Rectangle();
        clipBounds = new Rectangle(0, 0, camera.viewportWidth, camera.viewportHeight - 100);

        // set initial dimension and position values
        zoom = Config.pervasiveMapDefaultZoom;
        width = mapTex.getWidth();
        height = mapTex.getHeight() ;
        camX = -(viewport.getWorldWidth() / 2f - width / 2f);
        camY = -(viewport.getWorldHeight() / 2f - height / 2f);
        updateCamBounds();

        // gets UI region textures
        texBtnPressed = new TextureRegion(Resource.pMapUI, 140, 0, 137, 100);
        texBtnUnpressed = new TextureRegion(Resource.pMapUI, 1, 1, 137, 100);
        texLocationBG = new TextureRegion(Resource.pMapUI, 0, 123, 419, 317);
        texContentBox = new TextureRegion(Resource.pMapUI, 282, 1, 134, 119);

        // load locations of the pervasive map
        loadLocations();

        // load missions of the pervasive map
        loadMissions();
    }

    /**
     * Gets existing locations and missions from pervasive map
     * @return hashmap containing pervasive map locations
     */
    public HashMap<Integer, Location> getLocations() {
        return locations;
    }

    /**
     * Refreshes pervasive map texts
     */
    public void refreshLanguage() {

        // refreshes location texts
        Iterator it = locations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Location location = (Location) pair.getValue();
            location.setDescription(Main.getInstance().getLang().get(location.getSource() + "_Desc"));
            location.setName(Main.getInstance().getLang().get(location.getSource() + "_Name"));
            location.prepareContent();
        }

    }

    /**
     * loads locations of the pervasive map
     */
    private void loadLocations() {
        // initialize array lists
        locations = new HashMap<Integer, Location>();
        locationBtns = new ArrayList<ColliderButton>();

        // gets locations data file
        InputStream eDataFile = Gdx.files.internal("data/locations.data").read();
        // buffered reader to read locations data file
        BufferedReader eDataReader = new BufferedReader(new InputStreamReader(eDataFile));
        // iterates through lines and columns to get locations data
        String line;
        try {
            while ((line = eDataReader.readLine()) != null) {
                if (line.contains("--")) // ignores any line that has "--" in any position (commentary)
                    continue;

                // end of a locations data
                if (line.contains("}"))
                    continue;

                // start of a new location data
                if (line.contains("{")) {
                    if (Config.debug)
                        System.out.println("####### NEW LOCATION #######");

                    // the name of the location in data file
                    String locationName = line.substring(0, line.indexOf('{')).trim();
                    if (Config.debug)
                        System.out.println("New Location Name: " + locationName);
                    // gets location data
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    final int locationId = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets location ID
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    int photoX = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets loc photo X in photo file
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    int photoY = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets loc photo Y in photo file
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    int photoW = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets loc photo width in photo file
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    int photoH = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets loc photo height in photo file
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    int locationX = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets location X in pervasive map
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    int locationY = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets location Y in pervasive map

                    // gets location texture region photo
                    TextureRegion locationPhoto = new TextureRegion(Resource.locPhotos, photoX, photoY, photoW, photoH);

                    // gets location description and name in language bundle file
                    String locDesc = Main.getInstance().getLang().get(locationName + "_Desc");
                    String locName = Main.getInstance().getLang().get(locationName + "_Name");

                    // creates location with all data retrieved
                    Location location = new Location(locationName, locName, locDesc, locationPhoto, texLocationBG, texContentBox);

                    // prepares location pervasive map button
                    ColliderButton locationBtn = new ColliderButton(locationX, locationY, 20, 20, texBtnUnpressed, texBtnPressed) {
                        @Override
                        public void onPress() {

                        }

                        @Override
                        public void onRelease() {
                            selectedLocation = locationId;
                        }
                    };

                    // puts location pervasive map button in the list of location buttons
                    locationBtns.add(locationBtn);

                    // puts location in the locations hash map
                    locations.put(locationId, location);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read file: data/locations.data");
            e.printStackTrace();
        }

    }

    /**
     * Loads pervasive missions into locations of the pervasive map
     */
    private void loadMissions() {
        // gets missions data file
        InputStream eDataFile = Gdx.files.internal("data/missions.data").read();
        // buffered reader to read missions data file
        BufferedReader eDataReader = new BufferedReader(new InputStreamReader(eDataFile));
        // iterates through lines and columns to get missions data
        String line;
        try {
            while ((line = eDataReader.readLine()) != null) {
                if (line.contains("--")) // ignores any line that has "--" in any position (commentary)
                    continue;

                // end of a missions data
                if (line.contains("}"))
                    continue;

                // start of a new missions data
                if (line.contains("{")) {
                    if (Config.debug)
                        System.out.println("####### NEW MISSION #######");

                    // the name of the missions in data file
                    String missionName = line.substring(0, line.indexOf('{')).trim();
                    if (Config.debug)
                        System.out.println("New Mission Name: " + missionName);
                    // gets missions data
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    final int missionId = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets mission ID
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    final int locationId = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim()); // gets mission location ID
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    final boolean isRepeatable = Boolean.parseBoolean(line.substring(line.indexOf(":") + 1).trim()); // gets if mission is repeatable
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    final long mInterval = Long.parseLong(line.substring(line.indexOf(":") + 1).trim()); // gets mission interval for repeatable ones
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    long expReward = Long.parseLong(line.substring(line.indexOf(":") + 1).trim()); // gets mission exp reward
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    long goldReward = Long.parseLong(line.substring(line.indexOf(":") + 1).trim()); // gets mission gold reward
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    ArrayList<Integer> drops = Common.stringToArrayList(line.substring(line.indexOf(":")+1).trim()); // gets mission drop rewards
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    long equipLevel = Long.parseLong(line.substring(line.indexOf(":") + 1).trim()); // gets mission gold reward
                    while ((line = eDataReader.readLine()).contains("--")) ; // ignore all comments until next data
                    ArrayList<Integer> preReqs = Common.stringToArrayList(line.substring(line.indexOf(":")+1).trim()); // gets mission prerequisites
                    if (Config.debug) {
                        for(int i = 0; i < preReqs.size(); i++)
                            System.out.println("preReq ID: " + preReqs.get(i));
                    }

                    // resets drop list to null if there are no drops in order to represent no drops
                    if(drops.size() <= 0)
                        drops = null;

                    // gets mission description and name in language bundle file
                    String mDesc = Main.getInstance().getLang().get(missionName + "_Desc");
                    String mName = Main.getInstance().getLang().get(missionName + "_Name");

                    // creates mission with retrieved data
                    Mission mission = new Mission(missionName, missionId, mName, mDesc, isRepeatable,
                                                    mInterval, expReward, goldReward, drops, equipLevel, preReqs);
                    // sets mission location id
                    mission.setLocationID(locationId);
                    // adds mission to its respective location
                    locations.get(locationId).addMission(mission);
                    // adds to auxiliar data map
                    Common.getInstance().getMissions().put(missionId, mission);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read file: data/missions.data");
            e.printStackTrace();
        }
    }

    /**
     * Retrieves updated mission data
     * by sending a market message to the server
     * @param character the player's character name
     */
    public void retrieveData(String character) {
        this.character = character; // stores character name

        // if not connected do not try to send message to server
        if(!PucmonClient.getInstance().isConnected()) {
            return;
        }

        // refresh mission data
        refreshMissionData();
    }

    /**
     * Refreshes player missions based on completed missions
     * by sending a message to server to receive completed missions data
     */
    public void refreshMissionData() {
        isRetrieving = true; // sets retrieving data flag
        // sends message to server to retrieve mission data
        // mission message
        MessageMissionData missionMsg = new MessageMissionData(character, MessageMissionData.Action.RETRIEVE_MISSIONS);
        // wraps mission message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), missionMsg, MessageContent.Type.MISSION_DATA);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
    }

    /**
     * Resize callback to adjust viewport
     * @param width     new width
     * @param height    new height
     */
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiViewport.update(width, height);

        if(selectedLocation >= 0)
            locations.get(selectedLocation).resize(width, height);
    }

    /**
     * Renders pervasive map visualization
     */
    public void render() {
        // renders background in ui batch without clipping and camera
        uiBatch.setProjectionMatrix(uiCamera.combined);
        uiBatch.begin();

        // draws background
        uiBatch.draw(Resource.pMapBG, 0, Config.pervasiveMapGapY, uiCamera.viewportWidth,
                uiCamera.viewportHeight-(Config.pervasiveMapGapY));

        // ends ui batch
        uiBatch.end();

        // updates camera
        camera.update();

        // updates boundaries
        updateCamBounds();

        clipBounds = new Rectangle(0, (Config.pervasiveMapGapY / (zoom * mapPercent)),
                camera.viewportWidth, camera.viewportHeight);

        // set projection matrix
        batch.setProjectionMatrix(camera.combined);

        // begin batch
        batch.begin();

        // stacks scissors to draw each top player only in clipping bounds
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
        ScissorStack.pushScissors(scissors);

        // draw puc rio pervasive map at current position
        batch.draw(mapTex, 0 - camX, 0 - camY);

        // draw location buttons
        for(int i = 0; i < locationBtns.size(); i++) {
            // renders button on updated position
            float locBtnX = locationBtns.get(i).getBasePosition().x - camX;
            float locBtnY = locationBtns.get(i).getBasePosition().y - camY;
            locationBtns.get(i).setPosition(locBtnX, locBtnY);
            locationBtns.get(i).render(batch);
        }

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();

        // ends batch
        batch.end();

        // renders PUC Rio text in ui batch without clipping and camera
        uiBatch.setProjectionMatrix(uiCamera.combined);
        uiBatch.begin();

        // draws puc rio text
        Resource.rankingTitleFont.draw(uiBatch, "PUC-Rio", uiCamera.viewportWidth / 2 - 52, uiCamera.viewportHeight - 20);

        // ends ui batch
        uiBatch.end();

        // draws location dialog if any is open
        if(selectedLocation != -1)
            locations.get(selectedLocation).render();

//        // if not connected, draw info string informing it and return
//        if (!PucmonClient.getInstance().isConnected()) {
//            infoStr = Main.getInstance().getLang().get("notConnected");
//            renderMapInfo(batch);
//            return;
//        }
//
//        // if retrieving data, wait to continue
//        if(isRetrieving) {
//            // draws waiting server response text
//            infoStr = Main.getInstance().getLang().get("waitingServerResponse");
//            renderMapInfo(batch);
//            return;
//        }
    }

    /**
     * dispose pervasive map remaining resources
     */
    public void dispose() {
        batch.dispose();
    }

    /**
     * Touch down input handler
     * @param screenY   the x position of touch on screen
     * @param screenX   the y position of touch on screen
     */
    public void touchDown(int screenX, int screenY) {
        // if a location is opened, send touch down data to it
        if(selectedLocation >= 0) {
            locations.get(selectedLocation).touchDown(screenX, screenY);
            // and return
            return;
        }

        // send touch to buttons to see if any is triggered
        for(int i = 0; i < locationBtns.size(); i++) {
            locationBtns.get(i).touchDown(screenX/zoom, screenY/zoom);
        }
    }

    /**
     * Touch up input handler
     * @param screenY   the x position of touch on screen
     * @param screenX   the y position of touch on screen
     */
    public void touchUp(int screenX, int screenY) {
        // if a location is opened, send touch up data to it
        if(selectedLocation >= 0) {
            locations.get(selectedLocation).touchUp(screenX, screenY);
            // and return
            return;
        }

        // send touch to buttons to see if any is triggered
        for(int i = 0; i < locationBtns.size(); i++)
            locationBtns.get(i).touchUp(screenX/zoom, screenY/zoom);
    }

    /**
     * Applies zoom to the pervasive map
     * Called from zoom gesture listener callback
     * @param initialDistance distance between fingers when the gesture started
     * @param distance  current distance between fingers
     */
    public boolean zoom(float initialDistance, float distance) {
        // calculates zoom alteration based on zoomSpeed
        float deltaZoom = (distance / initialDistance);
        // applies alteration to zoom
        float newZoom = deltaZoom * zoom;
        // gets zoom alteration and applies zoom speed
        float zoomDiff = (newZoom - zoom) * Config.pervasiveMapZoomSpeed;
        // sets new zoom
        zoom = MathUtils.clamp(zoom+zoomDiff, Config.pervasiveMapMinZoom, Config.pervasiveMapMaxZoom);
        // saves old world dimensions
        float oldWidth = camera.viewportWidth;
        float oldHeight = camera.viewportHeight;
        // recreates camera to apply new zoom
        camera = new OrthographicCamera(Config.baseWidth / zoom, Config.baseHeight / zoom);
        camera.setToOrtho(false, Config.baseWidth / zoom, Config.baseHeight / zoom);
        // keeps map centered on zooming
        camX -= (camera.viewportWidth - oldWidth)/2;
        camY -= (camera.viewportHeight - oldHeight)/2;

        return true;
    }

    /**
     * Called when the user drags a finger over the screen and pervasive map is opened.
     * @param deltaX the difference in pixels to the last drag event on x
     * @param deltaY the difference in pixels to the last drag event on y
     */
    public void pan(float deltaX, float deltaY) {
        // if a location is opened, send pan data to it
        if(selectedLocation >= 0) {
            locations.get(selectedLocation).pan(deltaX, deltaY);
            // and return without panning pervasive map
            return;
        }

        // gets finger movement on both axis
        Vector2 moveNor = new Vector2(deltaX, deltaY);
        // gets magnitude of movement
        float len = moveNor.len();
        // normalizes vector for uniform direction speed movement
        moveNor.nor();
        // applies movement to pervasive map
        camX -= moveNor.x * len * Config.pervasiveMapSensitivityX * Gdx.graphics.getDeltaTime();
        camY += moveNor.y * len * Config.pervasiveMapSensitivityY * Gdx.graphics.getDeltaTime();
    }

    /**
     * updates camera boundaries
     */
    private void updateCamBounds() {
        minX = (width) < camera.viewportWidth ?
                -(camera.viewportWidth - (width)) : 0;
        minY = (height) < camera.viewportHeight -(Config.pervasiveMapGapY/mapPercent)/zoom ?
                -(camera.viewportHeight - (height)) : - ((Config.pervasiveMapGapY/mapPercent) / zoom);
        maxX = (width) < camera.viewportWidth ?
                0 : -(camera.viewportWidth - (width));
        maxY = (height) < camera.viewportHeight -(Config.pervasiveMapGapY/mapPercent)/zoom ?
                -(Config.pervasiveMapGapY/mapPercent)/zoom : -(camera.viewportHeight - (height)) ;

        // clamp to respect boundaries
        if(camX < minX)
            camX = minX;
        if(camX > maxX)
            camX = maxX;

        if(camY < minY)
            camY = minY;
        if(camY > maxY)
            camY = maxY;
    }

    /**
     * Renders market information string on market interface
     * @param batch the sprite batch to draw
     */
    private void renderMapInfo(SpriteBatch batch) {
        if(infoStr == "" || infoStr == null) // ignore if there is no info message
            return;

        // increases infoTimer
        infoTimer += Gdx.graphics.getDeltaTime();

        float x = 0;
        float y = Config.pervasiveMapGapY;
        float w = uiCamera.viewportWidth;
        float h = uiCamera.viewportHeight-(Config.pervasiveMapGapY);

        // sets color of info message
        Resource.marketFont.setColor(Config.marketInfoColor);
        // sets coordinates of info message
        float infoX = x + w/2f - new GlyphLayout(Resource.marketFont, infoStr).width/2f;
        float infoY = y + h * 0.14f;
        Resource.marketFont.draw(batch, infoStr, infoX, infoY);

        // resets color
        Resource.marketFont.setColor(Config.marketTextColor);

        // if timer surpasses limit, erases info message
        if(infoTimer > Config.marketInfoStrTime) {
            infoTimer = 0f;
            infoStr = "";
        }
    }

    /**
     * Resets selected location
     */
    public static void resetLocation() {
        selectedLocation = -1;
    }

    /**
     * Server communications
     */
    @Override
    public void subscribeToServer() {
        ServerMessages.getInstance().subscribe(this); // subscribes to server messages to be able to listen to them
    }

    @Override
    public void handleServerMessage(MessageContent msg) {
        // stores received message data
        MessageFlag svResponseFlag = msg.getFlag();
        Object svResponseObject = msg.getContent();
        MessageContent.Type svResponseType = msg.getType();

        // switches types of messages received
        switch(svResponseType) {
            case MISSION_DATA:
                // cast object to message market
                MessageMissionData msgMission = ((MessageMissionData) svResponseObject);
                isRetrieving = false; // retrieve complete
                // server response was ok?
                if(svResponseFlag == MessageFlag.OKIDOKI) {
                    // switch between types of actions to correctly handle message
                    switch(msgMission.getAction()) {
                        case RETRIEVE_MISSIONS:
                            handleMissionsRetrieving(msgMission);
                            break;
                        default:
                            System.err.println("Unknown type of mission action performed: " + msgMission.getAction());
                            break;
                    }
                } else { // if not server transaction was not successfully complete, show info
                    switch(msgMission.getAction()) {
                        case RETRIEVE_MISSIONS:
                            System.err.println(Main.getInstance().getLang().get("generalError"));
                            break;
                        default:
                            System.err.println(Main.getInstance().getLang().get("generalError"));
                            break;
                    }
                }
                break;
            default:
                System.err.println("Unknown type of message received: " + svResponseType);
                break;
        }
    }

    /**
     * Performs operations to update player current missions available
     * based on missions already completed and repeatable missions interval
     *
     * @param msgMission    the mission data message retrieved from server containing
     *                      player completed mission data
     */
    private void handleMissionsRetrieving(MessageMissionData msgMission) {
        // gets player missions completed
        HashMap<Integer, Timestamp> missionsDone = msgMission.getMissions();
        Common.getInstance().setMissionsDone(missionsDone);

        // updates with the newly obtained info
        for(int i = 0; i < locations.size(); i++) {
//            for(int j = 0; j < locations.get(i).getMissions().size(); j++) {
//                locations.get(i).getMissions().get(j).setMissionsDone(missionsDone);
//                // updates auxiliary data map
//                Common.getInstance().getMissions().put(locations.get(i).getMissions().get(j).getId(),
//                                                        locations.get(i).getMissions().get(j));
//            }
            locations.get(i).refreshMissions(); // updates location missions texts
        }
    }
}
