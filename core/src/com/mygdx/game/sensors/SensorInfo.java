package com.mygdx.game.sensors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.item.factory.Factory;
import com.mygdx.game.states.game.standard.pervasive.Mission;
import com.mygdx.game.states.game.standard.pervasive.SensorUI;
import com.mygdx.game.ui.ColliderButton;
import com.mygdx.game.ui.InputBox;
import com.mygdx.game.ui.UppersTextField;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;
import com.sddl.client.PucmonClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Contains the sensor data received from the
 * Mobile Objects
 */
public class SensorInfo implements Serializable {
    /** DEBUG */
    private static final String TAG = SensorInfo.class.getSimpleName();

    /** Message Data */
    private String   mUuid;
	private String   mouuid;
    private Double   signal;
    private String   action;
	private String   sensorName;
	private Double[] sensorValue;
    private long     timestamp;
    /** Message location */
    private Double mLatitude;
    private Double mLongitude;

    /** M-OBJ Actions */
	public static final String FOUND        = "found";
	public static final String CONNECTED    = "connected";
	public static final String DISCONNECTED = "disconnected";
	public static final String READ         = "read";
	//public static final String WRITE        = "write";

    /** JSON Keys */
    // General
    protected static final String UUID      = "uuid";
    protected static final String FUNCTION  = "tag";
    protected static final String LATITUDE  = "latitude";
    protected static final String LONGITUDE = "longitude";
    protected static final String TIMESTAMP = "timestamp";
    // LocationData
    protected static final String DATE       = "date";
    protected static final String ACCURACY   = "accuracy";
    protected static final String PROVIDER   = "provider";
    protected static final String SPEED      = "speed";
    protected static final String BEARING    = "bearing";
    protected static final String ALTITUDE   = "altitude";
    protected static final String CONNECTION = "connection";
    protected static final String BATTERY    = "battery";
    protected static final String CHARGING   = "charging";
    // SensorData
    protected static final String SOURCE  = "source";
    protected static final String SIGNAL  = "signal";
    protected static final String ACTION  = "action";
    protected static final String SERVICE = "sensor_name";
    protected static final String VALUE   = "sensor_value";
    // EventData
    protected static final String LABEL = "label";
    protected static final String DATA  = "data";
    // ErrorData
    protected static final String COMPONENT = "component";
    protected static final String MESSAGE   = "message";

    /** ID Separator */
    public static final String SEPARATOR = "-";

    /**
     * UI Vars
     */
    private ColliderButton back; // back to the sensors UI button
    private ColliderButton link; // link sensor button
    private UppersTextField code; // code text field (Upper letters only) for locked sensors
    public ArrayList<TextField> inputFields; // input text fields for sensors that gather user inputs
    public ArrayList<ButtonGroup> buttonGroups; // input button groups for sensors that gather user inputs via buttons
    private float infoTimer = 0f; // information message timer
    public String infoStr = ""; // information message timer
    private TextureRegion box; // the content box for photo, description and mission list of this dialog
    private Rectangle descScissors;   // description scissors for clipping
    private Rectangle descClip; // sensor mission description clipping rect
    private GlyphLayout descLayout; // description glyph layout
    float descBoxX; // description box x coord
    float descBoxY; // description box y coord
    float descBoxW; // description box width
    float descBoxH; // description box height
    float descDeltaY; // description current delta in Y axis
    float descMaxDeltaY; // description max delta in Y axis
    float txtGapY = 0.033f; // gap between text and input and code fields
    float initialGapY = 0.05f; // initial gap between text and input and code fields

    /**
     * Sensor data retrieved from game server
     */

    /** Sensor Services map (service name, values) to be stored */
    private HashMap<String, Double[]> services;
    /** Sensor Inputs map (input name, values) to be stored */
    public HashMap<String, String> inputs;

    private String sensor_type; // type of this sensor in game
    private int mission_id;     // mission id related to this sensor, in case of mission type sensor
    private int thumbnail_id;   // thumbnail id of this sensor
    private String sensor_code; // sensor code to unlock linking
    public int n_inputs; // sensor number of required inputs
    String input_type; // type of input that this sensor requires ("integer", "string"...)
    String description = ""; // the description of this sensor
    public String linkText = ""; // the text to display when link is successfully made
    public String displayText = ""; // text that is being displayed at the moment
    private boolean completedData = false; // is data from this sensor completed?
    public boolean linked = false; // has this sensor been linked already?
    public boolean waitingMission = false; // mission finishing was sent to server and responded?
    private String character = ""; // name of player`s character
    private GlyphLayout sName; // sensor mission name glyph
    private GlyphLayout codeName; // code string glyph
    private ArrayList<GlyphLayout> inputsName; // inputs glyphs


    /**
     * Sensor info constructor
     */
    public SensorInfo(){
        // initialize hashmap of sensor services
        services = new HashMap<String, Double[]>();
        // initialize hashmap of sensor inputs
        inputs = new HashMap<String, String>();
        // gets content box texture region
        box = new TextureRegion(Resource.pMapUI, 282, 1, 134, 119);
        // initialize array of inputs glyphs
        inputsName = new ArrayList<GlyphLayout>();
    }

    /**
     * Completes this sensor data with info retrieved from server
     * @param serverSensors server sensor data retrieved containing remaining needed data
     */
    public void completeData(ArrayList<SensorCompact> serverSensors) {
        // checks if data of this sensor is complete
        if(!completedData) { // if not
            // gets remaining needed info
            for (int i = 0; i < serverSensors.size(); i++) {
                // found this sensor, gets remaining data
                if (serverSensors.get(i).getSensor_id().equals(this.getMouuid())) {
                    this.mission_id = serverSensors.get(i).getMission_id();
                    this.sensor_type = serverSensors.get(i).getSensor_type();
                    this.thumbnail_id = serverSensors.get(i).getThumbnail_id();
                    this.sensor_code = serverSensors.get(i).getSensor_code();
                    this.n_inputs = serverSensors.get(i).getN_inputs();
                    this.input_type = serverSensors.get(i).getInput_type();
                    this.completedData = true;
                    break;
                }
            }
            // gets dinamically lang strings
            this.description = Main.getInstance().getLang().get(getMouuid() + "_Desc");
            this.linkText = Main.getInstance().getLang().get(getMouuid() + "_Link");
            // initially the displayed text is the sensor description
            this.displayText = this.description;
        }
    }

    /**
     * Prepares sensor info visualization
     * @param character         the name of player`s character
     * @param buttonTex         the TextureRegion for unpressed buttons
     * @param pressedButtonTex  the TextureRegion for pressed buttons
     * @param ui                the ui reference to be able to go back to all sensors visualization
     * @param serverSensors     server sensor data retrieved containing remaining needed data
     * @param uiX               the x coordinate of UI
     * @param uiY               the y coordinate of UI
     * @param uiW               the width of UI
     * @param uiX               the height of UI
     */
    public void prepareUI(String character, TextureRegion buttonTex, TextureRegion pressedButtonTex,
                          final SensorUI ui, ArrayList<SensorCompact> serverSensors,
                          float uiX, float uiY, float uiW, float uiH) {
        this.character = character;
        completeData(serverSensors);

        // if linked but linked text is not loaded, load it
        if(linked && displayText.equals(description)) {
            buildLinkText();
            displayText = linkText;
        }

        // calculate coords and dimension for description box
        descBoxW = uiW * 0.9f;
        descBoxH = uiH * 0.7f;
        descBoxX = uiX + (uiW / 2f) - (descBoxW / 2f);
        descBoxY = uiY + (uiH / 2f) - (descBoxH / 2f);

        // creates glyph layout for description line wrapping and truncation
        descLayout = new GlyphLayout(Resource.defaultFont, displayText);
        descLayout.setText(Resource.defaultFont, displayText, Config.locationFontColor,
                descBoxW * 0.9f, Align.left, true);

        // create description clipping scissors
        descScissors = new Rectangle();
        descClip = new Rectangle(descBoxX,descBoxY,descBoxW,descBoxH);

        // sensor mission glyph
        sName = new GlyphLayout(Resource.marketFont, Common.getInstance().getMissions().get(getMission_id()).getName());

        // code string glyph
        codeName = new GlyphLayout(Resource.marketFont, Main.getInstance().getLang().get("sensorCode") + ": ");

        // inputs glyphs
        for(int i = 0; i < n_inputs; i++) {
            inputsName.add(new GlyphLayout(Resource.marketFont, Main.getInstance().getLang().get(getMouuid() + "_Input" + (i + 1)) + ": "));
        }

        // calculate description max delta Y based
        // on description layout height and clipping height
        // as well as input and code text fields
        float txtFieldsDelta = 0f;
        if(!linked && PucmonClient.getInstance().isConnected()) {
            if (sensor_code != null || n_inputs > 0)
                txtFieldsDelta += (uiH*initialGapY);

            if (sensor_code != null) {
                txtFieldsDelta += uiH * 0.06f + (txtGapY*uiH);
            }
            txtFieldsDelta += (n_inputs) * (uiH * 0.06f + (txtGapY*uiH));
            if(n_inputs > 0)
                txtFieldsDelta += (n_inputs) * inputsName.get(0).height;
        }
        descMaxDeltaY = descLayout.height - (descClip.height * 0.88f) + txtFieldsDelta;
        if(descMaxDeltaY < 0) descMaxDeltaY = 0; // clamp to 0 for safety

        // initially deltas are 0
        descDeltaY = 0;

        // create description clipping scissors
        descScissors = new Rectangle();
        descClip = new Rectangle(descBoxX,descBoxY*1.075f,descBoxW,descBoxH*0.94f);

        back = new ColliderButton(0, 0, 0, 0,
                buttonTex, pressedButtonTex, Resource.marketFont,
                "returnStr") {
            @Override
            public void onPress() {
            }

            @Override
            public void onRelease() {
                // removes existing text fields from UI stage
                cleanStage();

                // resets info str
                infoStr = "";

                // deselects sensor to return to sensors UI visualization
                ui.deselectSensor();
            }
        };

        link = new ColliderButton(0, 0, 0, 0,
                buttonTex, pressedButtonTex, Resource.marketFont,
                "sensorLink") {
            @Override
            public void onPress() {
            }

            @Override
            public void onRelease() {
                // is code Ok (in case sensor needs code)
                boolean codeOk = (sensor_code == null) ? true : (sensor_code.equals(code.getText())) ? true : false;
                // are all inputs fulfilled?
                boolean inputsFulfilled = true;
                // if text field input, default is null, so a check must be made
                for (int i = 0; i < n_inputs; i++) {
                    if (input_type.equals("integer")) {
                        if (inputFields.get(i).getText() == "") {
                            inputsFulfilled = false;
                            break;
                        }
                    }
                }
                // is input ok (in case sensor needs input)
                boolean inputOk = (n_inputs <= 0) ? true : inputsFulfilled ? true : false;

                // is player inventor has no capacity for quest item reward?
                boolean fullInv = Common.getInstance().getMissions().get(mission_id).getItemReward().size() >
                                    Common.getPlayer().getInventory().nEmptySlots() ? true : false;

                // if everything is ok and not linked yet
                if(codeOk && inputOk && !fullInv) {
                    // then complete mission
                    ui.completeMission();
                } else if(!codeOk) {
                    timedInfo(Main.getInstance().getLang().get("sensorWrongCode"));
                } else if(!inputOk) {
                    timedInfo(Main.getInstance().getLang().get("sensorInputMissing"));
                } else {
                    timedInfo(Main.getInstance().getLang().get("marketFullInv"));
                }
            }
        };

        /**
         * if this sensor is locked through a code
         */
        if(sensor_code != null) {
            // creates textfield to get code
            code = new UppersTextField("", Resource.gameSkin);
            code.setMaxLength(Config.sensorCodeMaxLength);
            code.setMessageText(Main.getInstance().getLang().get("sensorCode"));
            code.setAlignment(Align.center);
            code.setOnscreenKeyboard(new InputBox(code, Main.getInstance().getLang().get("sensorUnlock"),
                                        "", Main.getInstance().getLang().get("sensorUnlock")));
            ui.getStage().addActor(code);
        }

        /**
         * If this sensor needs inputs, create text fields for it
         */
        inputFields = new ArrayList<TextField>();
        buttonGroups = new ArrayList<ButtonGroup>();


        for (int i = 0; i < n_inputs; i++) {
            if (input_type.equals("integer")) { // create text fields for text integer inputs
                TextField input = new TextField("", Resource.gameSkin);
                String txt = Main.getInstance().getLang().get(getMouuid() + "_Input" + (i + 1));
                input.setMaxLength(Config.sensorInputMaxLength);
                input.setMessageText(txt);
                input.setAlignment(Align.center);
                input.setOnscreenKeyboard(new InputBox(input, txt, "", txt));
                input.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
                ui.getStage().addActor(input);
                inputFields.add(input);
            } else { // create button group for button inputs
                Skin skin = new Skin(Gdx.files.internal("skin/holo/Holo-dark-hdpi.json"));
                ButtonGroup btnGroup = new ButtonGroup();
                //next set the max and min amount to be checked
                btnGroup.setMaxCheckCount(1);
                btnGroup.setMinCheckCount(1);
                // If true, when the maximum number of buttons are checked and an additional
                // button is checked, the last button to be checked is unchecked
                // so that the maximum is not exceeded.
                btnGroup.setUncheckLast(true);
                // adds buttons for the created button group
                for (int j = 0; j < 5; j++) {
                    TextButton btn = new TextButton(Integer.toString(j+1), skin, "toggle");
                    if (j == 2)
                        btn.getStyle().checked = btn.getStyle().down;
                    ui.getStage().addActor(btn);
                    btnGroup.add(btn);
                }
                // adds created button group to the list of button groups
                buttonGroups.add(btnGroup);
            }
        }

//            TextButton btn1 = new TextButton("1", skin);
//            btn1.getStyle().checked = btn1.getStyle().down;
//            ui.getStage().addActor(btn1);
//            ButtonGroup btnGroup1 = new ButtonGroup(btn1);
//            //next set the max and min amount to be checked
//            btnGroup1.setMaxCheckCount(1);
//            btnGroup1.setMinCheckCount(1);
//            // If true, when the maximum number of buttons are checked and an additional
//            // button is checked, the last button to be checked is unchecked
//            // so that the maximum is not exceeded.
//            btnGroup1.setUncheckLast(true);
//            buttonGroups.add(btnGroup1);

    }

    /**
     * Cleans sensor UI stage
     */
    public void cleanStage() {
        if(sensor_code != null && code != null)
            code.remove();
        for(int i = 0; i < inputFields.size() ; i++)
            inputFields.get(i).remove();
        for(int i = 0; i < buttonGroups.size() ; i++) {
            for(int j = 0; j < 5/*buttonGroups.get(i).getButtons().size*/ ; j++) {
                TextButton btn = (TextButton) buttonGroups.get(i).getButtons().get(j);
                btn.remove();
            }
            buttonGroups.get(i).remove();
        }
    }

    /** Getters */
    public String getUuid() {
        return mUuid;
    }

	public String getMouuid() {
	    return this.mouuid;
	}

    public Double getLatitude() {
        return this.mLatitude;
    }

    public Double getLongitude() {
        return this.mLongitude;
    }

    public Double getSignal() {
        return this.signal;
    }

    public String getAction() {
        return this.action;
    }

	public String getSensorName() {
		return this.sensorName;
	}
	
	public Double[] getSensorValue() {
		return this.sensorValue;
	}

    public HashMap<String, Double[]> getServices() {return this.services;}

    public long getTimestamp() {return timestamp;}
	/** Getters */
	
	/** Setters */
    public void setUuid(String uuid) {
        this.mUuid = uuid;
    }

	public void setMouuid( String mouuid ) {
	    this.mouuid = mouuid;
	}

    public void setSignal( Double signal ) {
        this.signal = signal;
    }

    public void setLatitude(Double latitude) {
        this.mLatitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.mLongitude = longitude;
    }

    public void setAction( String action ) {
        this.action = action;
    }

	public void setSensorName( String sensorName ) {
		this.sensorName = sensorName;
	}
	
	public void setSensorValue( Double[] sensorValue ) {
		this.sensorValue = sensorValue;
	}

    public void setTimestamp(long timestamp) {this.timestamp = timestamp;}
	/** Setters */

	public String toJSON() throws Exception {
		JSONObject data = new JSONObject();

        data.put( UUID,   getUuid() );
        data.put( SOURCE, getMouuid() );
        data.put( ACTION, getAction() );

        if( signal != null )
            data.put( SIGNAL, getSignal() );

        if( sensorName != null && sensorValue != null ) {
            data.put( SERVICE, getSensorName() );

            Double[] values = getSensorValue();
            JSONArray array = new JSONArray();

            for(int i=0;i<values.length;i++){
                array.add(values[i]);
            }

            data.put( VALUE, array);
        }

        if( getLatitude() != null && getLongitude() != null ) {
            data.put( LATITUDE,  getLatitude() );
            data.put( LONGITUDE, getLongitude() );
        }
		
		return data.toString();
	}

	public String toString() {
        return TAG + " [uuid=" + getUuid() + ", source=" + mouuid
                + ", signal=" + valueOf( signal ) + ", action=" + action
                + ", sensor=" + valueOf( sensorName ) + ", value=" + Arrays.toString( sensorValue )
                + "]";
	}

    /**
     * Gets an element in its String representation
     * @param obj The object to be transformed
     * @return The String representation
     */
    public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }

    /**
     * Renders this sensor information and interaction options
     * on sensor UI interface
     * @param batch     the unclipped ui batch
     * @param camera    camera to aid in clipping
     * @param uiX       x coordinate of sensor ui
     * @param uiY       y coordinate of sensor ui
     * @param uiW       width of sensor ui
     * @param uiH       height of sensor ui
     */
    public void render(SpriteBatch batch, Camera camera, float uiX, float uiY, float uiW, float uiH) {
        // if mission finishing data is being retrieved, wait
        if(waitingMission) {
            infoStr = Main.getInstance().getLang().get("waitingServerResponse");
            Common.renderInfoText(batch, infoStr, uiX, uiY, uiW, uiH);
            return;
        }

        // renders description box
        batch.draw(box, descBoxX, descBoxY, descBoxW, descBoxH);

        // draws sensor mission name
        float sNameX = uiX + (uiW / 2f) - (sName.width / 2f);
        float sNameY = (uiY + uiH) - (uiH * 0.05f);
        // scales font for title
        Resource.marketFont.getData().setScale(Config.sensorUITextScale * 1.2f);
        Resource.marketFont.draw(batch, sName, sNameX, sNameY);
        // resets font scale
        Resource.marketFont.getData().setScale(Config.sensorUITextScale);

        // draws back button
        float backW = uiW * 0.30f;
        float backH = uiH * 0.10f;
        float backX = uiX + (uiW * 0.066f);
        float backY = uiY + uiH * 0.022f;
        back.setPosition(backX, backY);
        back.setDimension(backW, backH);
        back.render(batch);

        // draws link button and other ui elements that exists only when link is not done yet
        // and user is connected to the internet
        if(!linked && PucmonClient.getInstance().isConnected()) {
            float linkX = backX + (uiW * 0.55f);
            link.setPosition(linkX, backY);
            link.setDimension(backW, backH);
            link.render(batch);
        }

        // renders information messages in interface
        renderSensorMsg(batch, uiX, uiY, uiW, uiH);

        // renders description
        renderDescription(batch, camera, uiX, uiY, uiW, uiH);
    }

    /**
     * Renders sensor description texts as well as interaction text fields
     * such as code and data inputs
     * @param batch     the description batch that will be clipped in description area
     * @param camera    the camera to aid in clipping
     * @param uiX       the x coord of sensor UI
     * @param uiY       the y coord of sensor UI
     * @param uiW       the width of sensor UI
     * @param uiH       the height of sensor UI
     */
    private void renderDescription(SpriteBatch batch, Camera camera, float uiX, float uiY, float uiW, float uiH) {
        // ends batch to avoid clipping
        batch.flush();
        batch.end(); // ends batch
        // restart batch
        batch.begin();

        // stacks scissors to draw each market item entry only in clipping bounds
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), descClip, descScissors);
        ScissorStack.pushScissors(descScissors);

        // description coords
        float descX = descBoxX + (0.03f * uiW);
        float descY = (descBoxY + descBoxH) - (0.04f * uiW);
        // draws description using description glyph layout
        Resource.defaultFont.draw(batch, descLayout, descX, descY + descDeltaY);

        if(!linked && PucmonClient.getInstance().isConnected()) {
            float codeW = uiW * 0.25f;
            float codeH = uiH * 0.06f;
            float cStrX = uiX + (uiW/2f) - ((codeName.width + codeW + (uiW * 0.01f))/2f);
            float codeX = cStrX + (codeName.width) + uiW * 0.01f;
            float codeY = (descY + descDeltaY) - (codeH) - (txtGapY*uiH) - descLayout.height - (uiH*initialGapY);
            // draws code text field (upper only) in case this sensor is locked through a code
            if (sensor_code != null) {
                Resource.defaultFont.draw(batch, codeName, cStrX, (codeH*1.24f) + codeY + codeH - codeName.height / 3f);
                code.setPosition(codeX, (codeH*1.24f) + codeY);
                code.setWidth(codeW);
                code.setHeight(codeH);
                code.draw(batch, 1);
            }

            // draws input text fields or button groups in case this sensor has inputs
            for (int i = 0; i < n_inputs; i++) {
                if (input_type.equals("integer")) { // draws text fields for text integer inputs
                    float cInpX = uiX + (uiW / 2f) - (inputsName.get(i).width / 2f);
                    float inpY = codeY - ((1.5f * codeH + (txtGapY * uiH)) * (i));
                    float inpX = uiX + (uiW / 2f) - (codeW / 2f);
                    Resource.defaultFont.draw(batch, inputsName.get(i), cInpX, inpY + codeH + inputsName.get(i).height / 2.65f);
                    TextField input = inputFields.get(i);
                    input.setPosition(inpX, inpY - inputsName.get(i).height);
                    input.setWidth(codeW);
                    input.setHeight(codeH);
                    input.draw(batch, 1);
                } else { // draws button groups for toggle inputs
                    float cInpX = uiX + (uiW / 2f) - (inputsName.get(i).width / 2f);
                    float inpY = codeY - ((1.5f * codeH + (txtGapY * uiH)) * (i));
                    float btnW = codeW/3f;
                    float inpX = uiX + (uiW / 2f) - ((btnW / 2f) * 5.4f);
                    Resource.defaultFont.draw(batch, inputsName.get(i), cInpX, inpY + codeH + inputsName.get(i).height / 2.65f);
                    for(int j = 0; j < 5/*buttonGroups.get(i).getButtons().size*/ ; j++) {
                        TextButton btn = (TextButton) buttonGroups.get(i).getButtons().get(j);
                        btn.setPosition(inpX + (btnW * 1.1f * j), inpY - inputsName.get(i).height);
                        btn.setWidth(btnW);
                        btn.setHeight(codeH);
                        btn.draw(batch, 1);
                    }
                }
            }
//            // draws input text fields in case this sensor has inputs
//            if(!getMouuid().equals("1-F45EAB2755CC")) { // render text fields for text inputs
//                for (int i = 0; i < inputFields.size(); i++) {
//                    float cInpX = uiX + (uiW / 2f) - (inputsName.get(i).width / 2f);
//                    float inpY = codeY - ((1.5f * codeH + (txtGapY * uiH)) * (i));
//                    float inpX = uiX + (uiW / 2f) - (codeW / 2f);
//                    Resource.defaultFont.draw(batch, inputsName.get(i), cInpX, inpY + codeH + inputsName.get(i).height / 2.65f);
//                    TextField input = inputFields.get(i);
//                    input.setPosition(inpX, inpY - inputsName.get(i).height);
//                    input.setWidth(codeW);
//                    input.setHeight(codeH);
//                    input.draw(batch, 1);
//                }
//            } else {  // render button groups for inputs
//                for (int i = 0; i < buttonGroups.size(); i++) {
//                    float cInpX = uiX + (uiW / 2f) - (inputsName.get(i).width / 2f);
//                    float inpY = codeY - ((1.5f * codeH + (txtGapY * uiH)) * (i));
//                    float inpX = uiX + (uiW / 2f) - (codeW / 2f);
//                    Resource.defaultFont.draw(batch, inputsName.get(i), cInpX, inpY + codeH + inputsName.get(i).height / 2.65f);
//                    for(int j = 0; j < 5/*buttonGroups.get(i).getButtons().size*/ ; j++) {
//                        TextButton btn = (TextButton) buttonGroups.get(i).getButtons().get(j);
//                        btn.setPosition(inpX + (codeW * 1.1f * j), inpY - inputsName.get(i).height);
//                        btn.setWidth(codeW);
//                        btn.setHeight(codeH);
//                        btn.draw(batch, 1);
//                    }
//                }
//            }
       }

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();
    }

    /**
     * Called when there is a touch on screen
     * and this sensor is opened.
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    public void touchDown(int screenX, int screenY) {
        // sends touch data to each  button
        back.touchDown(screenX, screenY);
        // if not linked , send touch data to link button
        if(!linked)
            link.touchDown(screenX, screenY);
    }

    /**
     * Called when there is a touch lift on screen
     * and this sensor is opened.
     * @param screenX   the x position of touch lift
     * @param screenY   the y position of touch lift
     */
    public void touchUp(int screenX, int screenY) {
        // sends touch data to each  button
        back.touchUp(screenX, screenY);
        // if not linked , send touch data to link button
        if(!linked)
            link.touchUp(screenX, screenY);
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
     * Renders market information string on market interface
     * @param batch the sprite batch to draw
     * @param x the x coord of sensor UI
     * @param y the y coord of sensor UI
     * @param w the width of sensor UI
     * @param h the height of sensor UI
     */
    private void renderSensorMsg(SpriteBatch batch, float x, float y, float w, float h) {
        if(infoStr == "" || infoStr == null) // ignore if there is no info message
            return;

        // increases infoTimer
        infoTimer += Gdx.graphics.getDeltaTime();

        // sets color of info message
        Resource.marketFont.setColor(Config.marketInfoColor);
        // sets coordinates of info message
        float infoX = x + w/2f - new GlyphLayout(Resource.marketFont, infoStr).width/2f;
        float infoY = y + h * 0.152f;
        Resource.marketFont.draw(batch, infoStr, infoX, infoY);

        // resets color
        Resource.marketFont.setColor(Config.marketTextColor);

        // if timer surpasses limit, erases info message
        if(infoTimer > Config.marketInfoStrTime) {
            infoTimer = 0f;
            infoStr = "";
        }
    }

    public String getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(String sensor_type) {
        this.sensor_type = sensor_type;
    }

    public int getMission_id() {
        return mission_id;
    }

    public void setMission_id(int mission_id) {
        this.mission_id = mission_id;
    }

    public int getThumbnail_id() {
        return thumbnail_id;
    }

    public void setThumbnail_id(int thumbnail_id) {
        this.thumbnail_id = thumbnail_id;
    }

    public String getSensor_code() {return sensor_code;}

    public void setSensor_code(String sensor_code) {this.sensor_code = sensor_code;}

    public int getN_inputs() {return n_inputs;}

    public void setN_inputs(int n_inputs) {this.n_inputs = n_inputs;}

    public String getInput_type() {return input_type;}

    public void setInput_type(String input_type) {this.input_type = input_type;}

    public boolean isLinked() {return linked;}

    public void setLinked(boolean linked) {this.linked = linked;}

    /**
     * Builds the complete link text
     */
    public void buildLinkText() {
        // gets mission
        Mission mission = Common.getInstance().getMissions().get(mission_id);
        // builds link string
        StringBuilder builtLinkStr = new StringBuilder(linkText);
        builtLinkStr.append("\n");
        builtLinkStr.append(Main.getInstance().getLang().get("sensorDefaultReward"));
        builtLinkStr.append(": ");
        builtLinkStr.append("\n");
        builtLinkStr.append(Main.getInstance().getLang().get("statsExp"));
        builtLinkStr.append(": ");
        builtLinkStr.append(mission.getExpReward());
        builtLinkStr.append("\n");
        builtLinkStr.append(Main.getInstance().getLang().get("goldStr"));
        builtLinkStr.append(": ");
        builtLinkStr.append(mission.getGoldReward());
        // builds last info with items reward
        for(int i = 0; i < mission.getItemReward().size(); i++) {
            String itemName = Factory.getItemName(mission.getItemReward().get(i));
            long itemLevel = mission.getEquipLevel();
            builtLinkStr.append("\n");
            builtLinkStr.append(itemName);
            builtLinkStr.append(" ");
            builtLinkStr.append(Main.getInstance().getLang().get("levelStr"));
            builtLinkStr.append(" ");
            builtLinkStr.append(itemLevel);
        }
        linkText = builtLinkStr.toString();
    }

    /**
     * Input handling
     */

    /**
     * Called when the user drags a finger over the screen and this sensor is opened.
     * @param deltaX the difference in pixels to the last drag event on x
     * @param deltaY the difference in pixels to the last drag event on y
     */
    public void pan(float deltaX, float deltaY) {
        // current delta Y being altered
        float currDeltaY = descDeltaY;
        // current max delta Y
        float maxDeltaY = descMaxDeltaY;

        // calculates movement on Y axis
        currDeltaY -= deltaY * Config.locationSensitivityY * Gdx.graphics.getDeltaTime();

        if(currDeltaY < 0) // clamp delta Y to 0
            currDeltaY = 0;

        if (currDeltaY > maxDeltaY) // clamps to avoid surpassing max delta Y
            currDeltaY = maxDeltaY;

        descDeltaY = currDeltaY; // sets delta Y
    }
}
