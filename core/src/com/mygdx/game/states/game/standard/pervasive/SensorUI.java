package com.mygdx.game.states.game.standard.pervasive;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.Main;
import com.mygdx.game.messages.MessageContent;
import com.mygdx.game.messages.MessageFlag;
import com.mygdx.game.messages.MessageMissionData;
import com.mygdx.game.messages.MessageSensor;
import com.mygdx.game.messages.ServerMessages;
import com.mygdx.game.sensors.SensorCompact;
import com.mygdx.game.sensors.SensorInfo;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.architecture.ServerListener;
import com.mygdx.game.ui.ColliderButton;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;
import com.sddl.client.PucmonClient;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class responsible for displaying an UI that guides
 * user interactions to the found nearby sensors
 *
 * @author  Pedro Sampaio
 * @since   2.0
 */
public class SensorUI implements ServerListener {

    private final float titleW; // title text width
    private final GlyphLayout defaultGL; // default glyph layout
    private float uiX;  // sensor ui x position on screen
    private float uiY;  // sensor ui y position on screen
    private float uiW;  // sensor ui width on screen
    private float uiH;  // sensor ui height on screen
    private Rectangle scissors;   // sensor ui scissors for clipping
    private Rectangle clipBounds; // clipping bounds rect
    private boolean isRetrieving; // is data being retrieved?
    private String infoStr; // string containing information to display to player
    private float entryX;  // sensor ui entry x position on screen
    private float entryW;  // sensor ui entry width on screen
    private float entryH;  // sensor ui entry height on screen
    private float gapY; // sensor ui entry gaps on Y axis
    private float titleY; // sensor ui title Y position
    private float deltaY; // delta Y on sensor ui
    private float infoTimer = 0f; // information message timer
    private float normalFontHeight; // normal font size height
    private float reducedFontHeight; // reduced font size height
    private Stage uiStage; // sensor ui stage
    private boolean active; // is sensor UI active ?
    private TextField lastInputField; // last input field
    private String character; // player character name

    /**
     * Sensors list
     */
    ArrayList<SensorInfo> sensors; // list of nearby sensors
    ArrayList<SensorCompact> serverSensors; // list of registered server sensors
    private boolean sensorsRetrieved = false; // have sensors been retrieved from server yet?
    private boolean missionsRetrieved = false; // have player completed missions been retrieved from server yet?

    /**
     * Selected sensor in sensors list
     */
    private int selectedSensor;

    /**
     * Sensor UI Texture vars
     */
    private TextureRegion backgroundTex;    // sensor ui background texture
    private TextureRegion pressedButtonTex; // sensor ui pressed button texture
    private TextureRegion buttonTex;        // sensor ui button texture
    private TextureRegion sensorSlotTex;      // sensor ui item slot texture
    private TextureRegion sensorHolderTex;    // sensor ui item holder texture

    /**
     * Sensor UI Buttons
     */
    private ArrayList<ColliderButton> entryButtons; // list of sensor entry buttons

    /**
     * Sensor UI constructor
     * Subscribes to server to be able to communicate with it
     * and initializes some properties like clipping values
     */
    public SensorUI() {
        // subscribe to server to be able to communicate with it
        subscribeToServer();
        // initially delta Y is 0
        deltaY = 0;
        // cuts skin to obtain sensorUI texture regions
        backgroundTex = new TextureRegion(Resource.marketSkin, 0, 252, 462, 678);
        buttonTex = new TextureRegion(Resource.marketSkin, 123, 47, 129, 41);
        pressedButtonTex = new TextureRegion(Resource.marketSkin, 123, 2, 129, 41);
        sensorSlotTex = new TextureRegion(Resource.marketSkin, 252, 0, 87, 87);
        sensorHolderTex = new TextureRegion(Resource.marketSkin, 0, 90, 445, 160);
        // calculates ui coords
        uiX = (Config.baseWidth / 2) - (backgroundTex.getRegionWidth() * Config.sensorUIScale / 2);
        uiY = (Config.baseHeight / 2) - (backgroundTex.getRegionHeight()  * Config.sensorUIScale / 2);
        uiW = backgroundTex.getRegionWidth() * Config.sensorUIScale;
        uiH = backgroundTex.getRegionHeight() * Config.sensorUIScale;
        // sensor UI title dimension
        titleW = new GlyphLayout(Resource.marketFont, Main.getInstance().getLang().get("sensorsGame")).width;
        // define dimensions and base coords for sensor UI entries
        entryW = uiW * 0.93f;
        entryH = uiH * 0.25f;
        entryX = uiX + (uiW * 0.5f) - (entryW * 0.5f);
        gapY = entryH + Config.SensorUIGapBetweenEntriesY;
        // create clipping scissors
        scissors = new Rectangle();
        float clipY = uiY+(uiH * 0.15f);
        float clipH = uiH * 0.77f;
        titleY = clipY + clipH;
        clipBounds = new Rectangle(uiX,clipY,uiW,clipH);
        // create market buttons
        createButtons();
        // font heights
        normalFontHeight = new GlyphLayout(Resource.marketFont, "height").height;
        reducedFontHeight = normalFontHeight * 0.81f;
        // initializes market stage
        uiStage = new Stage(new StretchViewport(Config.baseWidth, Config.baseHeight));
        // adds ui stage as an input processor
        Main.getInstance().addInputProcessor(uiStage);
        // initially sensor ui is not active
        active = false;
        // initially there are no selected sensors
        selectedSensor = -1;
        // get server registered sensors
        retrieveServerSensors();
        // refreshes sensors
        refreshSensors();
        defaultGL = new GlyphLayout(Resource.marketFont, "defaultLayout");
    }

    /**
     * for latency tests
     */
    private long svSensorsTS; // timestamp of sent sensors message to help calculate latency
    private long svMissionsTS; // timestamp of sent missions message to help calculate latency

    /**
     * Retrieves server registered sensors
     * by sending a message asking data from server
     */
    private void retrieveServerSensors() {
        isRetrieving = true;
        // sends message to server to retrieve sensors data
        // sensor message
        MessageSensor sensorMsg = new MessageSensor();
        // wraps sensor message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), sensorMsg, MessageContent.Type.SENSOR);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
        // latency test
        svSensorsTS = System.currentTimeMillis();
    }

    /**
     * Retrieves player completed missions
     * by sending a message asking data from server
     */
    public void retrieveMissions() {
        isRetrieving = true; // sets retrieving data flag
        // sends message to server to retrieve mission data
        // mission message
        MessageMissionData missionMsg = new MessageMissionData(character, MessageMissionData.Action.RETRIEVE_MISSIONS);
        // wraps mission message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), missionMsg, MessageContent.Type.MISSION_DATA);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
        // latency test
        svMissionsTS = System.currentTimeMillis();
    }

    /**
     * Refreshes sensors list based on signal strength and communication-less time
     * updating sensors arraylist and entry buttons
     */
    public void refreshSensors() {
        // only refreshes when there are no selected sensors
        if(selectedSensor >= 0)
            return;

        // refreshes sensors on account of communication-less time limit, signal strength, and mission availability
        Main.getSensor().refreshSensors(Config.sensorTimeLimit, Config.sensorSignalLimit);
        // updates sensor list (if no sensors are selected to avoid index out of bounds when selected sensor is being rendered)
        sensors = Main.getSensor().getSensors(serverSensors);
        // remove sensors that did not met mission prerequisites
        for(int i = sensors.size() - 1; i >= 0; i--) {
            if(!Common.getInstance().getMissions().get(sensors.get(i).getMission_id()).preReqMet())
                sensors.remove(i);
        }
        // refreshes button list
        refreshEntryButtons();
    }

    /**
     * Refreshes ui stage
     */
    public void refreshStage() {
        if(sensors.size() < uiStage.getActors().size)
            uiStage.getActors().removeRange(sensors.size(), uiStage.getActors().size-1);
    }

    /**
     * Creates sensor ui buttons
     */
    private void createButtons() {
        // initializes lists
        entryButtons= new ArrayList<ColliderButton>();
    }

    /**
     * Keeps sensor ui entry buttons updated
     */
    public void refreshEntryButtons() {
        //deltaY = 0; // resets delta to avoid mess with clipping when removing entries
        entryButtons.clear(); // clears list to rebuild it
        refreshStage(); // refreshes stage
        // for each entry, adds entry button
        for(int i = 0; i < sensors.size(); i++) {
            final int finalI = i;
            entryButtons.add(
                    new ColliderButton(0, 0, 0, 0,
                            buttonTex, pressedButtonTex, Resource.marketFont,
                            "sensorAccess") {
                        @Override
                        public void onPress() {
                        }

                        @Override
                        public void onRelease() {
                            // selects sensor to be accessed
                            selectedSensor = finalI;
                            // checks if sensor is available on account of its mission availability
                            boolean sAvailable = Common.getInstance().getMissions().get
                                                    (sensors.get(selectedSensor).getMission_id()).isAvailable();
                            // sets linked to true if it is not available, false otherwise
                            // blocking linking operations if needed
                            if(!sAvailable)
                                sensors.get(selectedSensor).setLinked(true);
                            else
                                sensors.get(selectedSensor).setLinked(false);
                            // prepares sensor visualization
                            sensors.get(selectedSensor).prepareUI(character, buttonTex, pressedButtonTex, SensorUI.this,
                                                                    serverSensors, uiX, uiY, uiW, uiH);
                        }
                    });
        }
    }

    /**
     * Draws sensor UI visualization on screen
     * @param batch     the batch to render UI
     * @param camera    camera to aid in clipping
     */
    public void render(SpriteBatch batch, Camera camera) {
        // if sensor ui is rendering, sensor ui is active
        active = true;

        // renders sensor ui background
        batch.draw(backgroundTex, uiX, uiY, uiW, uiH);

        // if not connected, draw info string informing it and return
        if (!PucmonClient.getInstance().isConnected()) {
            infoStr = Main.getInstance().getLang().get("notConnected");
            renderInfoText(batch, uiX, uiY, uiW, uiH);
            return;
        }

        // if somehow sensor data was not retrieved yet, retrieve it
        if(!sensorsRetrieved) {
            retrieveServerSensors();
            infoStr = Main.getInstance().getLang().get("waitingServerResponse");
            renderInfoText(batch, uiX, uiY, uiW, uiH);
            return;
        }

        // if mission data is not retrieved yet, wait
        if(!missionsRetrieved) {
            retrieveMissions();
            infoStr = Main.getInstance().getLang().get("waitingServerResponse");
            renderInfoText(batch, uiX, uiY, uiW, uiH);
            return;
        }

        // if is retrieving data, draw info text to inform retrieving status
//        if(isRetrieving) {
//            // draws waiting server response text
//            infoStr = Main.getInstance().getLang().get("waitingServerResponse");
//            renderInfoText(batch, uiX, uiY, uiW, uiH);
//        } else {
//            renderSensorUI(batch, camera);
//        }
        if(sensors.size() <= 0) {
            deselectSensor(); // deselect sensor in case any is selected
            infoStr = Main.getInstance().getLang().get("noSensorsNearby");
            renderInfoText(batch, uiX, uiY, uiW, uiH);
        } else {
            infoStr = "";
            // renders sensors UI if no sensor is selected
            if(selectedSensor < 0) {
                renderSensorUI(batch, camera);
            }
            else { //renders selected sensor information and interaction options
                sensors.get(selectedSensor).render(batch, camera, uiX, uiY, uiW, uiH);
            }
        }
    }

    /**
     * Deselects any selected sensor in order
     * to render sensors UI instead of sensor information and interaction
     */
    public void deselectSensor() {
        selectedSensor = -1;
    }

    /**
     * Renders sensor UI with all its components
     * @param batch     the sprite to render sensor UI
     * @param camera    camera to aid in clipping
     */
    private void renderSensorUI(SpriteBatch batch, Camera camera) {
        // draws sensor UI title
        float tX = uiX + (uiW / 2f) - (titleW / 2f);
        Resource.marketFont.draw(batch, Main.getInstance().getLang().get("sensorsGame"), tX, uiY + (uiH * 0.96f));
        // renders clipping frame
        renderClippingFrame(batch);
        // ends batch to avoid clipping
        batch.flush();
        batch.end(); // ends batch
        // restart batch
        batch.begin();

        // stacks scissors to draw each market item entry only in clipping bounds
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
        ScissorStack.pushScissors(scissors);

        // calculates number of entries show
        int nEntriesShown = MathUtils.ceil(clipBounds.height / gapY) + 1;
        // calculates first entry shown
        int firstEntry = MathUtils.floor(deltaY / gapY);
        // render entries
        for(int i = 0; i < nEntriesShown; i++) {
            if(sensors.size() <= 0) break;
            // clamp for safety
            int idx = MathUtils.clamp(i + firstEntry, 0, sensors.size() - 1);
            renderEntry(batch, sensors.get(idx), idx, entryX,
                    (titleY - entryH) - (gapY * (idx)) + deltaY,
                    entryW * Config.sensorUIEntryHolderScale, entryH * Config.sensorUIEntryHolderScale);
        }

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();

        // renders sensor UI info string
        renderSensorUIInfo(batch);
    }

    /**
     * Renders entry visualization on screen
     * in given coordinates and dimensions received in parameter
     *
     * @param batch the sprite batch to render
     * @param sensor the sensor entry to be rendered
     * @param sensorIdx the index of sensor in the list of sensors
     * @param x the x coordinate on screen
     * @param y the y coordinate on screen
     * @param w the width of entry visualization
     * @param h the height of entry visualization
     */
    private void renderEntry(SpriteBatch batch, SensorInfo sensor, int sensorIdx, float x,
                                   float y, float w, float h) {

        // if prerequisites of this sensor mission is not met, ignore it
        if(!Common.getInstance().getMissions().get(sensor.getMission_id()).preReqMet())
            return;

        // is this sensor mission available (mission not completed yet or repeatable interval has been surpassed)?
        boolean sAvailable = Common.getInstance().getMissions().get(sensor.getMission_id()).isAvailable();
        // changes color depending if sensor is available or not
        if(!sAvailable)
            batch.setColor(Config.sensorUnavailableColor);
        else
            batch.setColor(Color.WHITE);
        // draws sensor entry holder (background texture of sensor entries)
        batch.draw(sensorHolderTex, x, y, w, h);

        // draws sensor slot on sensor holder for visualization of sensor sprite
        float slotW = w * 0.25f;
        float slotH = w * 0.25f;
        float slotX = x + (w * 0.03f);
        float slotY = y + (h/2) - (slotH/2);
        batch.draw(sensorSlotTex, slotX, slotY, slotW, slotH);

        // makes sure that sensor data is completed
        sensor.completeData(serverSensors);

        // draws sensor thumbnail on sensor slot for visualization
        int tID = sensor.getThumbnail_id();
        TextureRegion sThumbTex = new TextureRegion(Resource.sensorThumbs, 200 * tID, 0, 200, 200);
        float sprW = slotW * 0.8f;
        float sprH = slotH * 0.8f;
        float sprX = slotX + (slotW/2) - (sprW/2);
        float sprY = slotY + (slotH/2) - (sprH/2);
        batch.draw(sThumbTex, sprX, sprY, sprW, sprH);

        // gets action button based on current tab
        ColliderButton actionButton = null;

        // for safety, do nothing if buttons are not correctly refreshed
        if(entryButtons.size() <= 0 || sensorIdx >= entryButtons.size())
            return;
        actionButton = entryButtons.get(sensorIdx);

        // draws action button
        float btnW = w * 0.30f;
        float btnH = h * 0.20f;
        float btnX = (x + w) - btnW - (w * 0.05f);
        float btnY = y + h * 0.10f;
        actionButton.setPosition(btnX, btnY);
        actionButton.setDimension(btnW, btnH);
        actionButton.render(batch);

        // draws sensor mission title
        String nameStr = Common.getInstance().getMissions().get(sensor.getMission_id()).getName();
        float nameX = slotX + slotW + (w * 0.066f);
        float nameY = (y + h) - normalFontHeight  - (h * 0.01f);
        defaultGL.setText(Resource.marketFont, nameStr, Color.BLACK, w * 0.5f, Align.left, true);
        Resource.marketFont.draw(batch, defaultGL, nameX, nameY);

        // draws sensor signal
        renderSensorSignal(batch, sensor.getSignal(), sAvailable, slotX, slotY, slotW, slotH);

        batch.setColor(Color.WHITE);

        // scales font for other data
        Resource.marketFont.getData().setScale(Config.sensorUITextScale * 0.81f);

        // resets font
        Resource.marketFont.setColor(Config.sensorUITextColor);
        Resource.marketFont.getData().setScale(Config.sensorUITextScale);
    }

    /**
     * Renders sensor signal strength information
     * @param batch     the batch to render
     * @param signal    the sensor signal data
     * @param available is this sensor available?
     * @param slotX     the slot X position of sensor to draw signal below
     * @param slotY     the slot Y position of sensor to draw signal below
     * @param slotW     the slot width
     * @param slotH     the slot height
     */
    private void renderSensorSignal(SpriteBatch batch, Double signal, boolean available, float slotX, float slotY, float slotW, float slotH) {
        // built text that is going to be the display of signal strength
        StringBuilder builtTxt = new StringBuilder(Main.getInstance().getLang().get("sensorSignal"));
        builtTxt.append(": ");

        if(signal == null) {
            builtTxt.append("...");
        } else if (signal > -42) {
            Resource.marketFont.setColor(Config.sensorStrongestColor);
            builtTxt.append(Main.getInstance().getLang().get("sensorStrongest"));
        } else if (signal > - 55) {
            Resource.marketFont.setColor(Config.sensorStrongColor);
            builtTxt.append(Main.getInstance().getLang().get("sensorStrong"));
        } else if (signal > - 67) {
            Resource.marketFont.setColor(Config.sensorModerateColor);
            builtTxt.append(Main.getInstance().getLang().get("sensorMedium"));
        } else if (signal > - 78) {
            Resource.marketFont.setColor(Config.sensorWeakColor);
            builtTxt.append(Main.getInstance().getLang().get("sensorWeak"));
        } else {
            Resource.marketFont.setColor(Config.sensorWeakestColor);
            builtTxt.append(Main.getInstance().getLang().get("sensorWeakest"));
        }
        // if sensor is not available, use unavailable color
        if(!available)
            Resource.marketFont.setColor(Config.sensorUnavailableColor);

        String signalTxt = builtTxt.toString();
        float signalX = slotX;
        float signalY = slotY * 1f;
        Resource.marketFont.draw(batch, signalTxt, signalX, signalY);

        // resets font
        Resource.marketFont.setColor(Config.sensorUITextColor);
    }

    /**
     * Renders sensor UI information string on sensor UI interface
     * @param batch the sprite batch to draw
     */
    private void renderSensorUIInfo(SpriteBatch batch) {
        if(infoStr == "" || infoStr == null) // ignore if there is no info message
            return;

        // increases infoTimer
        infoTimer += Gdx.graphics.getDeltaTime();

        // sets color of info message
        Resource.marketFont.setColor(Config.sensorUIInfoColor);
        // sets coordinates of info message
        float infoX = uiX + uiW/2f - new GlyphLayout(Resource.marketFont, infoStr).width/2f;
        float infoY = uiY + uiH * 0.14f;
        Resource.marketFont.draw(batch, infoStr, infoX, infoY);

        // resets color
        Resource.marketFont.setColor(Config.sensorUITextColor);

        // if timer surpasses limit, erases info message
        if(infoTimer > Config.sensorUIInfoStrTime) {
            infoTimer = 0f;
            infoStr = "";
        }
    }

    /**
     * Renders clipping frame to wrap entries
     *
     * @param batch the sprite batch to render
     */
    private void renderClippingFrame(SpriteBatch batch) {
        float clipY = uiY+(uiH * 0.15f);
        float clipH = uiH * 0.77f;
        float expansionY = 2f;
        batch.setColor(Config.sensorUIClippingFrameColor); // tint next drawing
        batch.draw(backgroundTex, entryX, clipY-expansionY, entryW*Config.sensorUIEntryHolderScale, clipH+(2f*expansionY));
        batch.setColor(Color.WHITE); // resets tint
    }

    /**
     * Perform operations to display
     * information to user for a period defined in config
     * @param info the info to be displayed
     */
    private void timedInfo(String info) {
        infoTimer = 0f;
        infoStr = info;
    }

    /**
     * returns if sensor ui is active or not
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * disable sensor ui
     */
    public void disable () {
        Common.setTouchableStage(uiStage, Touchable.disabled);
        active = false;
        deselectSensor();
    }

    /**
     * enables sensor ui
     */
    public void enable () {
        Common.setTouchableStage(uiStage, Touchable.enabled);
        active = true;
    }

    /**
     * Draws the current information string centralized in the sensor UI interface
     * @param batch the batch to draw
     * @param uiX the x position of sensor UI menu
     * @param uiY the y position of sensor UI menu
     * @param uiW the width of sensor UI menu
     * @param uiH the height of sensor UI menu
     */
    private void renderInfoText(SpriteBatch batch, float uiX, float uiY, float uiW, float uiH) {
        final GlyphLayout svInfoLayout = new GlyphLayout(Resource.marketFont, infoStr);
        float svInfoX = uiX + (uiW/2) - (svInfoLayout.width/2);
        float svInfoY = uiY + (uiH/2) - (svInfoLayout.height/2);
        Resource.marketFont.draw(batch, svInfoLayout , svInfoX, svInfoY);
        // TODO - be careful with width bounds (resize on necessity?)
        // TODO - improve the FPS on market rendering
    }

    /**
     * Called when the user drags a finger over the screen and sensor UI is opened.
     * @param deltaX the difference in pixels to the last drag event on x
     * @param deltaY the difference in pixels to the last drag event on y
     */
    public void pan(float deltaX, float deltaY) {
        // send pan data to selected sensor if a sensor is selected
        if(selectedSensor >= 0) {
            sensors.get(selectedSensor).pan(deltaX, deltaY);
            return;
        }

        // calculates movement on Y axis
        this.deltaY -= deltaY * Config.sensorUISensitivityY * Gdx.graphics.getDeltaTime();
        if (this.deltaY < 0) // clamp delta Y inferior limit
            this.deltaY = 0;
        //  calculates max delta based on the current amount of sensor entries
        if (sensors != null) {
            int nEntriesShown = MathUtils.floor(clipBounds.height / gapY);
            float maxDeltaY = (sensors.size() - nEntriesShown) * gapY - (entryH);
            if(maxDeltaY < 0) maxDeltaY = 0;

            if (this.deltaY > maxDeltaY) // clamps to avoid surpassing max delta Y
                this.deltaY = maxDeltaY;
        }
    }

    /**
     * Called when there is a touch on screen
     * and sensor ui is opened.
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    public void touchDown(int screenX, int screenY) {
        // if there is a selected sensor, send touch data to selected sensor instead
        if(selectedSensor >= 0) {
            sensors.get(selectedSensor).touchDown(screenX, screenY);
            return;
        }

        // sends touch data to each entry button
        for(int i = 0; i < entryButtons.size(); i++) {
            entryButtons.get(i).touchDown(screenX, screenY);
        }
    }

    /**
     * Called when there is a touch lift on screen
     * and sensor ui is opened.
     * @param screenX   the x position of touch lift
     * @param screenY   the y position of touch lift
     */
    public void touchUp(int screenX, int screenY) {
        // if there is a selected sensor, send touch data to selected sensor instead
        if(selectedSensor >= 0) {
            sensors.get(selectedSensor).touchUp(screenX, screenY);
            return;
        }

        // sends touch data to each entry button
        for(int i = 0; i < entryButtons.size(); i++) {
            entryButtons.get(i).touchUp(screenX, screenY);
        }
    }

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
            case SENSOR:
                // cast object to message sensor
                MessageSensor msgSensor = ((MessageSensor) svResponseObject);
                isRetrieving = false; // retrieve complete
                // server response was ok?
                if (svResponseFlag == MessageFlag.OKIDOKI) {
                    // gets sensor data from server
                    this.serverSensors = msgSensor.getSensors();
                    sensorsRetrieved = true; // sensors have been retrieved
                }
                break;
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
                        case COMPLETE_MISSION:
                            handleMissionCompletion(msgMission);
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
                        case COMPLETE_MISSION:
                            System.err.println("Something went wrong while registering completed mission in database");
                            System.err.println(Main.getInstance().getLang().get("generalError"));
                        default:
                            System.err.println(Main.getInstance().getLang().get("generalError"));
                            break;
                    }
                }
                break;
            default:
                System.out.println("SensorUI: Unknown message type received from server: " + svResponseType);
                break;
        }
    }

    /**
     * Completes selected sensor mission by sending
     * to server the complete mission request with
     * with selected sensor mission id and player character name
     */
    public void completeMission() {
        // waits for server response
        sensors.get(selectedSensor).waitingMission = true;
        // if there are inputs, create map of inputs

        if(sensors.get(selectedSensor).n_inputs > 0) {
            if (sensors.get(selectedSensor).getInput_type().equals("integer")) { // get from text fields for integer inputs
                for (int i = 0; i < sensors.get(selectedSensor).n_inputs; i++) {
                    String inputName = Main.getInstance().getLang().get(sensors.get(selectedSensor).getMouuid() + "_InputName" + (i + 1));
                    sensors.get(selectedSensor).inputs.put(inputName, sensors.get(selectedSensor).inputFields.get(i).getText());
                }
            } else if (sensors.get(selectedSensor).getInput_type().equals("toggle")) { // get from button group for toggle input
                for (int i = 0; i < sensors.get(selectedSensor).n_inputs; i++) {
                    String inputName = Main.getInstance().getLang().get(sensors.get(selectedSensor).getMouuid() + "_InputName" + (i + 1));
                    int chosenIdx = sensors.get(selectedSensor).buttonGroups.get(i).getCheckedIndex() + 1;
                    sensors.get(selectedSensor).inputs.put(inputName, String.valueOf(chosenIdx));
                }
            }
        }

        // for each sensor data, send data to server
        Iterator it = sensors.get(selectedSensor).getServices().entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry pair = (java.util.Map.Entry) it.next();
            Double[] values = (Double[]) pair.getValue();
            System.out.println("Sensor Service: " + pair.getKey() + " = " + Arrays.toString(values));
        }

        // sends message to server to complete this sensor mission
        // mission message
        MessageMissionData missionMsg = new MessageMissionData(character, MessageMissionData.Action.COMPLETE_MISSION);
        missionMsg.setMission_id(sensors.get(selectedSensor).getMission_id());
        missionMsg.setSensor_id(sensors.get(selectedSensor).getMouuid());
        missionMsg.setInput_type(sensors.get(selectedSensor).getInput_type());
        // put sensor collected data
        missionMsg.setSensorData(sensors.get(selectedSensor).getServices());
        // put player sensing data
        missionMsg.setInputData(sensors.get(selectedSensor).inputs);
        // wraps mission message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), missionMsg, MessageContent.Type.MISSION_DATA);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
        // for latency tests
        svMissionsTS = System.currentTimeMillis();
        // clear selected sensor inputs
        sensors.get(selectedSensor).inputs.clear();
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

        // force sensor refresh due to new mission data received
        refreshSensors();

        missionsRetrieved = true;
    }

    /**
     * Perform operations on mission completion involving
     * linking operations as well as rewards operations
     * @param msgMission the mission data message received from server
     */
    private void handleMissionCompletion(MessageMissionData msgMission) {
        // gets mission id of completed mission in order to perform correct operations
        int missionID = msgMission.getMission_id();

        // find the correct sensor to perform operations
        int sensorIdx = -1;
        for(int i = 0; i < sensors.size(); i++) {
            if(sensors.get(i).getMission_id() == missionID) {
                sensorIdx = i;
                break;
            }
        }
        // guarantees sensorIdx is >= than 0
        assert (sensorIdx >= 0);

        // unlock interface
        sensors.get(sensorIdx).waitingMission = false;
        // removes existing actors from UI stage
        sensors.get(sensorIdx).cleanStage();
        // sensor is now linked
        sensors.get(sensorIdx).linked = true;
        // resets info string
        sensors.get(sensorIdx).infoStr = "";
        // give player rewards
        Common.getInstance().giveRewards(missionID);
        // builts final string
        sensors.get(sensorIdx).buildLinkText();
        // changes display text to link text
        sensors.get(sensorIdx).displayText = sensors.get(sensorIdx).linkText;
        // prepares UI
        sensors.get(sensorIdx).prepareUI(character, buttonTex, pressedButtonTex, SensorUI.this,
                                            serverSensors, uiX, uiY, uiW, uiH);
        // updates list of player completed missions
        Common.getInstance().getMissionsDone().put(missionID, new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Getters and Setters
     */

    public Stage getStage() {
        return uiStage;
    }

    public void setPlayerName(String playerName) {
        this.character = playerName;
    }
}
