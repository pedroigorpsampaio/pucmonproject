package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.I18NBundle;
import com.mygdx.game.messages.MessageLogin;
import com.mygdx.game.messages.MessageLogoff;
import com.mygdx.game.sensors.Sensor;
import com.mygdx.game.ui.TranslatableImageButton;
import com.mygdx.game.states.game.standard.architecture.GameState;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.battle.Battle;
import com.mygdx.game.states.game.standard.controller.Game;
import com.mygdx.game.states.game.standard.sfx.SFX;
import com.mygdx.game.states.menu.MenuInitial;
import com.mygdx.game.util.Config;
import com.mygdx.game.util.FrameRate;
import com.sddl.client.PucmonClient;

import java.util.ArrayList;
import java.util.Locale;

/**
 * The main module for the game in the
 * libgdx framework, will act as a switch
 * for all state executions in the game
 * Pattern:
 * Singleton
 *
 * @author Pedro Sampaio
 * @since 0.1
 * @version 0.2
 */
public class Main extends ApplicationAdapter implements InputProcessor {

    // the current game state being rendered on screen
    GameState currentState;
    // the initial menu game state reference
    MenuInitial initialMenu;
    // the game state reference
    Game game;
    // the input multiplexer that will gather all input processors
    InputMultiplexer inputMultiplexer;

    // enum that has all possible game states
    public enum State {MENU_INITIAL, GAME_STANDARD, BATTLE, GAME_PERVASIVE}
    // represents current state
    State gameState;
    // gets current game state
    public State getGameState() {return gameState;}

    // singleton reference
    private static Main instance = null;

    // language bundle
    private I18NBundle langBundle;
    private FileHandle langFileHandle;

    // list of all buttons used in game that are passive to translations
    private ArrayList<TranslatableImageButton> tButtons;

    // UI texture for connection status icons
    Texture UITex;

    // ok connection status icon region in UI texture
    TextureRegion connectedIcon;

    // connection not ok status icon region in UI texture
    TextureRegion disconnectedIcon;

    // current connection status
    TextureRegion currentConnStatus;

    // frame rate debugger
    private FrameRate fps;
    // main batch that runs through all game states
    private SpriteBatch mainBatch;

    // the reference to the sensor interface with android implementation
    private static Sensor sensor;

    // enables sensor interface to be accessed throughout the game modules
    public static Sensor getSensor() { return sensor; }

    // defeats external instantiation
    private Main () {}

    // initializes main or use a previous instantiation if it exists
    public static Main getInstance() {
        if (instance == null)
            instance = new Main();

        return instance;
    }

    /**
     * Sets the sensor interface reference received via android platofrm call
     * @param sensor    the sensor interface android implementation
     */
    public void setSensorInterface(Sensor sensor) {
        this.sensor = sensor;
    }

    /**
     *  Called when the Application is first created.
     */
    @Override
    public void create() {
        // initializes multiplexer for user input
        inputMultiplexer = new InputMultiplexer();

        // language bundle initial creation
        langFileHandle = Gdx.files.internal("language/language");
        // loads saved preferences
        Preferences prefs = Gdx.app.getPreferences("Preferences");
        // gets language from saved preferences
        String l_lang = prefs.getString("locale_lang", "pt");
        String l_country = prefs.getString("locale_country", "BR");
        // creates locale with saved preferences
        Locale locale = new Locale(l_lang, l_country);
        // creates language bundle with locale
        langBundle = I18NBundle.createBundle(langFileHandle, locale);
        // initializes list of translatable buttons
        tButtons = new ArrayList<TranslatableImageButton>();

        // loads resources
        Resource.getInstance().create();

        // loads uiTexture
        UITex =  new Texture("imgs/UI.png");

        // cuts icons in textures
        connectedIcon = new TextureRegion(UITex, 0,0,118,61);
        disconnectedIcon = new TextureRegion(UITex, 0,61,118,61);
        // initially current conn status is disconnected
        currentConnStatus = disconnectedIcon;

        // initializes the initial menu reference
        initialMenu = new MenuInitial();

        // initializes the game reference
        game = new Game();
        // initialize game resources
        game.create();

        // sets as initial state the initial menu state
        currentState = initialMenu;
        // calls create() to initialize initial state(initial menu) resources
        currentState.create();
        // updates current game state
        gameState = State.MENU_INITIAL;

        // sets the main module as input processor
        // to be able to pass control to each current state
        inputMultiplexer.addProcessor(this);

        // sets input multiplexer as input processor
        Gdx.input.setInputProcessor(inputMultiplexer);

        // frame rate debugger
        fps = new FrameRate();
        // initializes main batch
        mainBatch = new SpriteBatch();
    }

    /**
     * Gets language bundle for strings localization
     * @return the language bundle created to be used
     */
    public I18NBundle getLang() {
        return langBundle;
    }

    /**
     * Updates language bundle locale
     * by recreating it
     */
    public void updateLanguage() {
        // loads saved preferences
        Preferences prefs = Gdx.app.getPreferences("Preferences");
        // gets language from saved preferences
        String l_lang = prefs.getString("locale_lang", "pt");
        String l_country = prefs.getString("locale_country", "BR");
        // creates locale with saved preferences
        Locale locale = new Locale(l_lang, l_country);
        langBundle = I18NBundle.createBundle(langFileHandle, locale);
    }

    // getter for translatable buttons
    public ArrayList<TranslatableImageButton> getTButtons() {
        return tButtons;
    }

    // adds a translatable button to the list
    // of translatable buttons
    public void addTButton (TranslatableImageButton tButton) {
        tButtons.add(tButton);
    }

    /**
     *  Called when the Application is resized.
     *  This can happen at any point during a non-paused state
     *  but will never happen before a call to create()
     *
     * @param width the new width in pixels
     * @param height the new height in pixels
     */
    @Override
    public void resize(int width, int height) {
        // pass the resize control for the current state being rendered
        currentState.resize(width, height);
    }

    /**
     * Called when the Application should render itself.
     */
    @Override
    public void render() {
        // pass the render control for the current state being rendered
        currentState.render();

        // renders connection status in all screens (as of now)
        if(PucmonClient.getInstance().isConnected())
            currentConnStatus = connectedIcon;
        else
            currentConnStatus = disconnectedIcon;

        // draws current connection status
        mainBatch.begin();
        mainBatch.draw(currentConnStatus, 0, 0);
        mainBatch.end();

        // display fps if debug on
        if(Config.debug) {
            fps.update();
            fps.render();
        }
    }

    /**
     * Called when the Application is paused,
     * usually when it's not active or visible on screen.
     * An Application is also paused before it is destroyed.
     */
    @Override
    public void pause() {
        // pass the pause control for the current state being rendered
        currentState.pause();
    }

    /**
     * Called when the Application is resumed from a paused state,
     * usually when it regains focus.
     */
    @Override
    public void resume() {
        // pass the resume control for the current state being rendered
        currentState.resume();
    }

    /**
     * Called when the Application is destroyed.
     * Preceded by a call to pause().
     */
    @Override
    public void dispose() {
        // pass the dispose control for all states
        game.dispose();
        initialMenu.dispose();
        System.exit(-1);
    }

    /**************************************************/
    /* Input Control - Pass control to current state  */
    /**************************************************/

    /**
     * Adds input processors to the input multiplexer
     * that gathers all input processors considered in the game
     *
     * @param input the input processor to be added to the multiplexer
     */
    public void addInputProcessor(InputProcessor input) {
        inputMultiplexer.addProcessor(input);
    }

    /**
     * Removes input processors from the input multiplexer
     * that gathers all input processors considered in the game
     *
     * @param input the input processor to be removed from the multiplexer
     */
    public void removeInputProcessor(InputProcessor input) {
        inputMultiplexer.removeProcessor(input);
    }

    /**
     * Resets input processors list in multiplexer
     * adding this main module as first input processor
     */
    public void resetInputProcessor() {
        Gdx.input.setInputProcessor(null);
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public boolean keyDown(int keycode) {return false;}
    @Override
    public boolean keyUp(int keycode) {return false;}

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return currentState.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return currentState.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return currentState.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {return false;}

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    /**************************************************/
    /* Methods to change between possible game states */
    /**************************************************/

    /**
     * Changes the state to the game state
     */
    public void changeToGame() {
        currentState.pause();   // pauses the current state
        currentState = game;    // changes to new game state
        currentState.resume();  // resumes the game state
        // updates current game state
        gameState = State.GAME_STANDARD;
    }

    /**
     * Provides login information to game
     * and changes state to game state
     */
    public void loginToGame(MessageLogin loginMsg) {
        game.loadLanguagePassiveContent(); // makes sure to load correct language text
        game.loadPlayer(loginMsg);
        changeToGame();
    }

    /**
     * Changes the state to the initial menu state
     */
    public void changeToInitialMenu() {
        currentState.pause();       // pauses the current state
        currentState = initialMenu; // changes to new state
        currentState.resume();      // resume the game state
        // updates current game state
        gameState = State.MENU_INITIAL;
    }

    /**
     * Changes the state to the battle state received via parameter
     */
    public void changeToBattle(Battle battle) {
        currentState.pause();       // pauses the current state
        currentState = battle; // changes to new state
        battle.create();    // initialize battle resources
        currentState.resume();      // resume the game state
        // updates current game state
        gameState = State.BATTLE;
    }
}
