package com.mygdx.game.states.game.standard.pervasive;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.ui.ColliderButton;
import com.mygdx.game.util.Config;

import java.util.ArrayList;

/**
 * Class that represents a pervasive
 * location in the pervasive map
 * with all information related to it
 *
 * @author Pedro Sampaio
 * @since 1.9
 */
public class Location {

    /**
     * basic resource
     */
    private TextureRegion bg; // the background of the dialog
    private TextureRegion box; // the content box for photo, description and mission list of this dialog
    private String source; // the name representing the source of this location in locations data file
    private String name; // name of this location
    private String description; // description of this location
    private ArrayList<Mission> missions; // list of missions of this location
    private ArrayList<Mission> availableMissions; // list of available missions of this location (for current player)
    private int selectedMission; // current selected mission
    private TextureRegion photo; // photo of this location
    private OrthographicCamera camera; // camera
    private Viewport viewport; // viewport to be able to adapt to different resolutions
    private float defaultCharW; // default font char width in default size
    private float defaultCharH; // default font char height in default size
    private GlyphLayout descLayout; // description glyph layout
    private GlyphLayout mLayout; // missions glyph layout
    private GlyphLayout mDescLayout; // mission description glyph layout
    private String missionsText; // mission list formatted string
    private int pMapX; // x coordinate of location in the pervasive map
    private int pMapY; // y coordinate of location in the pervasive map
    /**
     * spritebatches
     */
    private SpriteBatch batchUnclipped; // the batch to render unclipped content
    private SpriteBatch batchDescription; // the batch to render description
    private SpriteBatch batchMission; // the batch to render mission
    private SpriteBatch batchMissionDesc; // the batch to render missions description
    /**
     * coordinates and dimension
     */
    float locX; // x position of location dialog
    float locY; // y position of location dialog
    float locW; // width of location dialog
    float locH; // height of location dialog
    float picBoxX; // picture box x coord (serves as layout anchor)
    float picBoxY; // picture box y coord (serves as layout anchor)
    float picBoxW; // picture box width (serves as layout anchor)
    float picBoxH; // picture box height (serves as layout anchor)
    float descBoxX; // description box x coord
    float descBoxY; // description box y coord
    float descBoxW; // description box width
    float descBoxH; // description box height
    float mBoxX; // mission box x coord
    float mBoxY; // mission box y coord
    float mBoxW; // mission box width
    float mBoxH; // mission box height
    float mDescBoxX; // mission description box x coord
    float mDescBoxY; // mission description box y coord
    float mDescBoxW; // mission description box width
    float mDescBoxH; // mission description box height
    float descDeltaY; // description current delta in Y axis
    float descMaxDeltaY; // description max delta in Y axis
    float mDeltaY; // missions current delta in Y axis
    float mMaxDeltaY; // missions max delta in Y axis
    float mDeltaX; // missions list current delta in X axis
    float mMaxDeltaX; // missions list current delta in Y axis
    float mDescDeltaY; // missions description current delta in Y axis
    float mDescMaxDeltaY; // missions description max delta in Y axis
    float lastTouchY; // last coord Y touched down
    float lastTouchX; // last coord X touched down
    /**
     * Clipping and Scissors
     */
    private Rectangle descScissors;   // description scissors for clipping
    private Rectangle descClip; // description clipping rect
    private Rectangle mScissors;   // missions scissors for clipping
    private Rectangle mClip; // missions clipping rect
    private Rectangle mDescScissors;   // missions description scissors for clipping
    private Rectangle mDescClip; // missions description clipping rect
    private boolean touchFromMList = false; // if touch down was in mission list bounds
    /**
     * Texture regions
     */
    private TextureRegion selectionBG; // texture region of selection for mission selected
    private TextureRegion closeIcon; // texture region for closeIcon
    /**
     * Collider buttons
     */
    private ColliderButton closeBtn; // button that closes location

    /**
     * Constructor sets base data of this location
     *
     * @param source        the name representing the source of this location in locations data file
     * @param name          name of this location
     * @param description   description of this location
     * @param photo         photo of this location
     * @param bg            the background of the dialog
     * @param box           the content box for photo, description and mission list of this dialog
     */
    public Location(String source, String name, String description, TextureRegion photo, TextureRegion bg, TextureRegion box) {
        // sets received params
        this.source = source;
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.bg = bg;
        this.box = box;
        // initialize vars
        missions = new ArrayList<Mission>();
        availableMissions = new ArrayList<Mission>();
        batchUnclipped = new SpriteBatch();
        batchDescription = new SpriteBatch();
        batchMission = new SpriteBatch();
        batchMissionDesc = new SpriteBatch();

        // sets libgdx camera for game scaling on different screens
        camera = new OrthographicCamera(Config.baseWidth, Config.baseHeight);
        camera.setToOrtho(false, Config.baseWidth, Config.baseHeight);

        // sets viewport behaviour on scaling for different screens
        viewport = new StretchViewport(Config.baseWidth, Config.baseHeight, camera);
        viewport.apply();

        // calculates ranking coords
        locX = (Config.baseWidth / 2) - (bg.getRegionWidth() * Config.locationDialogScaleX / 2);
        locY = (Config.baseHeight / 2) - (bg.getRegionHeight() * Config.locationDialogScaleY / 2) + Config.pervasiveMapGapY/2f;
        locW = bg.getRegionWidth() * Config.locationDialogScaleX;
        locH = bg.getRegionHeight() * Config.locationDialogScaleY;

        // calculates default char width and height in default font in default font size
        defaultCharW = new GlyphLayout(Resource.defaultFont, "a").width;
        defaultCharH = new GlyphLayout(Resource.defaultFont, "a").height;

        // prepare content (boxes, texts, scissors and clippings...)
        prepareContent();
    }

    /**
     * Prepares location content (including content
     * boxes, text contents and clipping data)
     */
    public void prepareContent() {
        // calculate coords and dimension for picture box
        picBoxW = photo.getRegionWidth() * 1.1f * (Config.locationDialogScaleX) * Config.locationPictureScale;
        picBoxH = picBoxW;
        picBoxX = locX + (0.05f*locW);
        picBoxY = (locY + locH) - (0.15f*locW) - picBoxW;

        // calculate coords and dimension for description box
        descBoxX = (picBoxX + picBoxW) + (Config.locationPicDescGapX * Config.locationDialogScaleX);
        descBoxW = (locX + locW - descBoxX) * 0.92f;
        descBoxH = picBoxH;
        descBoxY = picBoxY;

        // calculate coords and dimension for missions box
        mBoxW = locW * 0.9f;
        float invY = picBoxY - (Config.locationPicMGapY * Config.locationDialogScaleY);
        mBoxX = picBoxX;
        mBoxH = ((invY - locY) * 1f / 2f) - (Config.locationPicMGapY * Config.locationDialogScaleY);
        mBoxY = invY - mBoxH;

        // calculate coords and dimension for missions box
        mDescBoxW = mBoxW;
        mDescBoxX = mBoxX;
        mDescBoxH = mBoxH;
        mDescBoxY = (mBoxY - mDescBoxH) - (Config.locationPicMGapY * Config.locationDialogScaleY);

        // creates glyph layout for description line wrapping and truncation
        descLayout = new GlyphLayout(Resource.defaultFont, description);
        descLayout.setText(Resource.defaultFont, description, Config.locationFontColor,
                            descBoxW * 0.9f, Align.left, true);

        // create description clipping scissors
        descScissors = new Rectangle();
        descClip = new Rectangle(descBoxX,descBoxY,descBoxW,descBoxH);

        // calculate description max delta Y based
        // on description layout height and clipping height
        descMaxDeltaY = descLayout.height - (descClip.height * 0.88f);
        if(descMaxDeltaY < 0) descMaxDeltaY = 0; // clamp to 0 for safety

        // create mission description clipping scissors
        mScissors = new Rectangle();
        float gapX = locW*0.02f;
        mClip = new Rectangle(mBoxX+gapX,mBoxY,mBoxW-(gapX*2.38f),mBoxH);

        // updates missions list
        refreshMissions();

        // create mission description clipping scissors
        mDescScissors = new Rectangle();
        mDescClip = new Rectangle(mDescBoxX,mDescBoxY,mDescBoxW,mDescBoxH);

        // initially deltas are 0
        descDeltaY = 0; mDeltaY = 0; mDeltaX = 0; mDescDeltaY = 0;

        // initially no missions are selected
        selectedMission = -1; // -1 simbolizes no selected mission

        // creates selection texture region
        selectionBG = new TextureRegion(Resource.pMapUI, 0, 442, 419, 28);

        // creates close icon region texture
        closeIcon = new TextureRegion(Resource.pMapUI, 0, 475, 45, 45);

        // creates close button
        float cBtnW = closeIcon.getRegionWidth() * Config.locationDialogScaleX * Config.locationCloseBtnScale;
        float cBtnH = cBtnW;
        float cBtnX = (locX + locW) - cBtnW * Config.locationCloseBtnGapScale;
        float cBtnY = (locY + locH) - cBtnH * Config.locationCloseBtnGapScale;
        closeBtn = new ColliderButton(cBtnX, cBtnY, cBtnW, cBtnH, closeIcon, closeIcon) {
            @Override
            public void onPress() {

            }

            @Override
            public void onRelease() {
                PervasiveMap.resetLocation();
                // reset data
                mDeltaY = 0; descDeltaY = 0; mDescDeltaY = 0; mDeltaX = 0;
                selectedMission = -1;
            }
        };
    }

    /**
     * Resize callback to adjust viewport
     * @param width     new width
     * @param height    new height
     */
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    /**
     * Adds a mission to the list of location missions
     * @param mission the mission to be added
     */
    public void addMission(Mission mission) {
        // adds mission to list of missions
        missions.add(mission);
        // updates missions list
        refreshMissions();
    }

    /**
     * Refreshes missions list
     */
    public void refreshMissions() {
        // updates available missions list
        availableMissions = new ArrayList<Mission>();
        // updates list on account of mission availability
        for (int i = missions.size() - 1; i >= 0 ; i--) {
            // shows mission if it is repeatable even if it is not available
            // in order to show player the cooldown remaining time
            // but in other cases, only shows mission if it is not
            // completed already and prerequisite is met
            if(missions.get(i).isRepeatable() || missions.get(i).isAvailable()) {
                availableMissions.add(missions.get(i));
            }
        }

        missionsText = "";
        for(int i = 0; i < availableMissions.size(); i++) {
            missionsText += "(!) " + availableMissions.get(i).getName() + "\n";
        }
        // updates glyph layout for missions line wrapping and truncation
        mLayout = new GlyphLayout(Resource.defaultFont, missionsText);
        mLayout.setText(Resource.defaultFont, missionsText, Config.locationFontColor,
                0, Align.left, true);
        // calculate missions max delta Y based
        // on missions layout height and clipping height
        mMaxDeltaY = mLayout.height - mClip.height;
        if(mMaxDeltaY < 0) mMaxDeltaY = 0; // clamp to 0 for safety

        // calculate missions max delta X based
        // on missions layout width and clipping width
        mMaxDeltaX = mLayout.width - (mClip.width * 0.95f);
        if(mMaxDeltaX < 0) mMaxDeltaX = 0; // clamp to 0 for safety
    }

    /**
     * Removes a mission from the list of location missions
     * @param mission the mission to be removed
     */
    public void removeMission(Mission mission) {missions.remove(mission);}

    /**
     * Removes a mission from the list of location missions
     * @param index the index of the mission to be removed
     */
    public void removeMission(int index) {missions.remove(index);}

    /**
     * Renders this location dialog with all its information
     */
    public void render() {
        // sets color for font text
        Color oldColor = Resource.defaultFont.getColor();
        Resource.defaultFont.setColor(Config.locationFontColor);

        // sets camera for batch
        batchUnclipped.setProjectionMatrix(camera.combined);

        // begins unclipped batch to draw unclipped content
        batchUnclipped.begin();

        // draws background
        batchUnclipped.draw(bg, locX, locY, locW, locH);

        // draws close button
        closeBtn.render(batchUnclipped);

        // draw location dialog names
        renderNames(batchUnclipped);

        // render content boxes
        renderContentBoxes(batchUnclipped);

        // draws picture
        renderPicture(batchUnclipped);

        // ends unclipped batch
        batchUnclipped.end();

        // draws description
        renderDescription(batchDescription);

        // draws mission list
        renderMissions(batchMission);

        // draws mission description (if any is selected)
        renderMissionDescription(batchMissionDesc);

        // resets color
        Resource.defaultFont.setColor(oldColor);
    }

    /**
     * Renders content boxes in unclipped batch
     * @param batch the batch to render unclipped content
     */
    private void renderContentBoxes(SpriteBatch batch) {
        // renders picture box
        batch.draw(box, picBoxX, picBoxY, picBoxW, picBoxH);

        // renders description box
        batch.draw(box, descBoxX, descBoxY, descBoxW, descBoxH);

        // renders mission list box
        batch.draw(box, mBoxX, mBoxY-(0.005f*locH), mBoxW, mBoxH+(0.005f*locH*2f));

        // renders mission description box
        batch.draw(box, mDescBoxX, mDescBoxY, mDescBoxW,mDescBoxH);
    }

    /**
     * Renders location dialog names
     * @param batch the batch to render
     */
    private void renderNames(SpriteBatch batch) {
        // scales font to title size
        float oldScaleX = Resource.defaultFont.getData().scaleX;
        float oldScaleY = Resource.defaultFont.getData().scaleY;
        Resource.defaultFont.getData().setScale(oldScaleX * Config.locationNameScale,
                                                oldScaleY * Config.locationNameScale);
        // calculate coords
        float nameX = locX + locW/2 - (name.length()*defaultCharW*Config.locationNameScale / 2);
        float nameY = (locY + locH) - 0.075f*locH;

        // draws name
        Resource.defaultFont.draw(batch, name, nameX, nameY);

        // reset font scale
        Resource.defaultFont.getData().setScale(oldScaleX, oldScaleY);

        // gets mission list string
        String mListStr = Main.getInstance().getLang().get("missionListStr");

        // gets mission description string
        String mDescStr = Main.getInstance().getLang().get("missionDescStr");

        // coords of mission list string
        float mListX = mBoxX;
        float mListY = mBoxY + mBoxH + (0.005f*locH) + (defaultCharH * 1.2f);

        // renders mission list label
        Resource.defaultFont.draw(batch, mListStr, mListX, mListY);

        // coords of mission description string
        float mDescX = mDescBoxX;
        float mDescY = mDescBoxY + mDescBoxH + (defaultCharH * 1.2f);

        // renders mission description label
        Resource.defaultFont.draw(batch, mDescStr, mDescX, mDescY);
    }

    /**
     * Renders location description in a content box
     * in description clipped batch
     * @param batch the description batch to render
     */
    private void renderDescription(SpriteBatch batch) {
        // sets camera for batch
        batch.setProjectionMatrix(camera.combined);

        // begins description batch
        batch.begin();

        // stacks scissors to draw in description clipping bounds
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), descClip, descScissors);
        ScissorStack.pushScissors(descScissors);

        // description coords
        float descX = descBoxX + (0.03f * locW);
        float descY = (descBoxY + descBoxH) - (0.02f * locW);
        // draws description using description glyph layout
        Resource.defaultFont.draw(batch, descLayout, descX, descY + descDeltaY);

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();

        // ends description batch
        batch.end();
    }

    /**
     * Renders missions information in respective content box
     * @param batch the batch to render mission content
     */
    private void renderMissions(SpriteBatch batch) {
        // sets camera for batch
        batch.setProjectionMatrix(camera.combined);

        // begins mission batch
        batch.begin();

        // stacks scissors to draw in mission clipping bounds
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), mClip, mScissors);
        ScissorStack.pushScissors(mScissors);

        // draws selection on selected mission, if any is selected
        if(selectedMission >= 0) {
            // calculates coords and dimension
            float rectW = mClip.width;
            float rectH = (defaultCharH*1.65f);
            float rectX = mClip.x;
            float rectY = (mClip.y+mClip.height*0.95f) - rectH -
                    (selectedMission * rectH) + mDeltaY;

            // draws selection
            batch.draw(selectionBG, rectX, rectY, rectW, rectH);
        }

        // mission list coords
        float mX = mBoxX + (0.03f * locW);
        float mY = (mBoxY + mBoxH) - (0.02f * locW);
        // draws mission list using its glyph layout
        Resource.defaultFont.draw(batch, mLayout, mX - mDeltaX, mY + mDeltaY);

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();

        // ends description batch
        batch.end();
    }

    /**
     * Renders current selected mission description in the
     * respective content batch (if any mission is selected)
     * @param batch the mission description batch to render
     */
    private void renderMissionDescription(SpriteBatch batch) {
        // returns if there is no selected mission
        if(selectedMission < 0)
            return;

        // sets camera for batch
        batch.setProjectionMatrix(camera.combined);

        // begins mission description batch
        batch.begin();

        // stacks scissors to draw in mission description clipping bounds
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), mDescClip, mDescScissors);
        ScissorStack.pushScissors(mDescScissors);

        // mission description coords
        float mDescX = mDescBoxX + (0.03f * locW);
        float mDescY = (mDescBoxY + mDescBoxH) - (0.02f * locW);
        // draws mission description using its glyph layout
        Resource.defaultFont.draw(batch, mDescLayout, mDescX, mDescY + mDescDeltaY);

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();

        // ends description batch
        batch.end();
    }

    /**
     * Renders location picture in a content box
     * @param batch the batch to render
     */
    private void renderPicture(SpriteBatch batch) {
        // calculate coords and dimension for picture
        float picW = picBoxW * 0.95f;
        float picH = picW;
        float picX = picBoxX + picBoxW/2 - picW/2;
        float picY = picBoxY + picBoxH/2 - picH/2;

        // renders picture
        batch.draw(photo, picX, picY, picW, picH);
    }

    /**
     * Input handling
     */

    /**
     * Called when the user drags a finger over the screen and this location is opened.
     * @param deltaX the difference in pixels to the last drag event on x
     * @param deltaY the difference in pixels to the last drag event on y
     */
    public void pan(float deltaX, float deltaY) {
        // was description box touched?
        boolean descBoxTouched = false;
        // was mission box touched?
        boolean mBoxTouched = false;
        // was mission description box touched?
        boolean mDescBoxTouched = false;

        // checks if initial touch is within description bounds
        if(descClip.contains(lastTouchX, lastTouchY)) {
            descBoxTouched = true;
        } else if(mClip.contains(lastTouchX, lastTouchY)) { // otherwise check if is within missions bounds
            mBoxTouched = true;
        } else if(mDescClip.contains(lastTouchX, lastTouchY)) { // otherwise check if is withing mission description bounds
            mDescBoxTouched = true;
        } else // if not in any, ignore panning
            return;

        // current delta Y being altered
        float currDeltaY = descBoxTouched ? descDeltaY : mBoxTouched ? mDeltaY : mDescDeltaY;
        // current max delta Y
        float maxDeltaY = descBoxTouched ? descMaxDeltaY : mBoxTouched ? mMaxDeltaY : mDescMaxDeltaY;

        // calculates movement on Y axis
        currDeltaY -= deltaY * Config.locationSensitivityY * Gdx.graphics.getDeltaTime();

        if(currDeltaY < 0) // clamp delta Y to 0
            currDeltaY = 0;

        if (currDeltaY > maxDeltaY) // clamps to avoid surpassing max delta Y
            currDeltaY = maxDeltaY;

        if(descBoxTouched)
            descDeltaY = currDeltaY; // sets delta Y
        else if(mBoxTouched) {
            // movement occurs on X axis only in mission list box
            // calculates movement on Y axis
            mDeltaX -= deltaX * Config.locationSensitivityX * Gdx.graphics.getDeltaTime();

            if(mDeltaX < 0) // clamp delta Y to 0
                mDeltaX = 0;

            if (mDeltaX > mMaxDeltaX) // clamps to avoid surpassing max delta Y
                mDeltaX = mMaxDeltaX;

            // sets delta Y
            mDeltaY = currDeltaY;
        } else if(mDescBoxTouched) { // else, sets delta for mission description
            mDescDeltaY = currDeltaY;
        }
    }

    /**
     * Touch down input handler
     * @param screenY   the x position of touch on screen
     * @param screenX   the y position of touch on screen
     */
    public void touchDown(int screenX, int screenY) {
        lastTouchX = screenX;
        lastTouchY = screenY;

        touchFromMList = false;
        if(mClip.contains(screenX, screenY))
            touchFromMList = true;

        // send touch data to close button
        closeBtn.touchDown(screenX, screenY);
    }

    /**
     * Touch up input handler
     * @param screenY   the x position of touch on screen
     * @param screenX   the y position of touch on screen
     */
    public void touchUp(int screenX, int screenY) {
        // checks if touched up in mission list box and touch down was from mission list
        if(mClip.contains(screenX, screenY) && touchFromMList) {
            // calculates what mission was clicked
            int mIdx = MathUtils.floor(((mClip.getY()+mClip.height*0.95f)-screenY+mDeltaY) / (defaultCharH*1.6f));
            // clamp mIdx for safety
            if(mIdx < 0) mIdx = 0; if(mIdx > availableMissions.size()-1) mIdx = availableMissions.size()-1;
            // resets mission description delta if it is a different mission
            if(mIdx != selectedMission)
                mDescDeltaY = 0;
            // sets selected missions
            selectedMission = mIdx;

            if(selectedMission < 0)
                return;

            // updates mission description vars
            // creates glyph layout for mission description line wrapping and truncation
            mDescLayout = new GlyphLayout(Resource.defaultFont, availableMissions.get(selectedMission).getDescription());
            mDescLayout.setText(Resource.defaultFont, availableMissions.get(selectedMission).getDescription(),
                                Config.locationFontColor, mDescBoxW * 0.9f, Align.left, true);

            // calculate mission description max delta Y based
            // on description layout height and clipping height
            mDescMaxDeltaY = mDescLayout.height - (mDescClip.height * 0.85f);
            if(mDescMaxDeltaY < 0) mDescMaxDeltaY = 0; // clamp to 0 for safety
        }

        // send touch data to close button
        closeBtn.touchUp(screenX, screenY);
    }

    /**
     * Getters and Setters
     */

    public int getpMapX() {
        return pMapX;
    }

    public void setpMapX(int pMapX) {
        this.pMapX = pMapX;
    }

    public int getpMapY() {
        return pMapY;
    }

    public void setpMapY(int pMapY) {
        this.pMapY = pMapY;
    }

    public String getSource() {return source;}

    public void setSource(String source) {this.source = source;}

    public void setDescription(String description) {this.description = description;}

    public void setName(String name) {this.name = name;}

    public ArrayList<Mission> getMissions() {return missions;}
}
