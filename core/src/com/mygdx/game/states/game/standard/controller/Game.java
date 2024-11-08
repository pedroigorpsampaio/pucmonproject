package com.mygdx.game.states.game.standard.controller;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.Main;
import com.mygdx.game.messages.MessageContent;
import com.mygdx.game.messages.MessageLogin;
import com.mygdx.game.messages.MessageLogoff;
import com.mygdx.game.messages.MessageSave;
import com.mygdx.game.messages.ServerMessages;
import com.mygdx.game.sensors.SensorInfo;
import com.mygdx.game.states.game.standard.architecture.GameState;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.architecture.ServerListener;
import com.mygdx.game.states.game.standard.battle.Attributes;
import com.mygdx.game.states.game.standard.battle.Battle;
import com.mygdx.game.states.game.standard.battle.BattleSpawn;
import com.mygdx.game.states.game.standard.camera.GameCamera;
import com.mygdx.game.states.game.standard.item.Equipment;
import com.mygdx.game.states.game.standard.item.Item;
import com.mygdx.game.states.game.standard.item.factory.Factory;
import com.mygdx.game.states.game.standard.market.Market;
import com.mygdx.game.states.game.standard.map.*;
import com.mygdx.game.states.game.standard.inventory.Inventory;
import com.mygdx.game.states.game.standard.message.TextMessage;
import com.mygdx.game.states.game.standard.message.Texts;
import com.mygdx.game.states.game.standard.pervasive.PervasiveMap;
import com.mygdx.game.states.game.standard.pervasive.SensorUI;
import com.mygdx.game.states.game.standard.player.Player;
import com.mygdx.game.states.game.standard.physics.Transform;
import com.mygdx.game.states.game.standard.ranking.Ranking;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;
import com.sddl.client.PucmonClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

// TODO - add logout option
// TODO - add options ?

/**
 * The game state that represents the non-pervasive
 * game view, where players can explore and battle
 * monsters in a tile map based on PUC-Rio
 *
 * @author Pedro Sampaio
 * @since 0.3
 */
public class Game implements GameState, GestureDetector.GestureListener, ServerListener {
    SpriteBatch worldBatch;  // the sprite batch to be used in game world rendering
    SpriteBatch uiBatch;  // the sprite batch to be used in game UI rendering
    private Music gameBGM;	// the background music for the game
    private Stage gameStage;	// the game UI Stage
    private com.mygdx.game.states.game.standard.map.Map gameMap; // the game`s current map
    private OrthographicCamera camera; // camera to adjust to screen resolution
    private StretchViewport viewport; // viewport to use with camera to adjust to screen res
    private OrthographicCamera uiCamera; // camera to adjust UI to screen resolution
    private StretchViewport uiViewport; // viewport to use with UI camera to adjust to screen res
    private Player player;      // the player object
    private Texture enemySheet; // enemies spritesheet
    private float battleSpawnTimer = 0f; // timer to control spawn battle
    private float saveTimer = 0f; // timer to control player saving
    private float sensorTimer = Config.sensorTime; // timer to control sensor data collection
    private float accelTimer = Config.sensorTime; // timer to control accelerometer input
    private volatile boolean sync;   // true if all data is synchronized with server, false otherwise
    private boolean openedInventory; // is inventory opened?
    private boolean openedRanking; // is ranking opened?
    private boolean openedMarket; // is market opened?
    private boolean openedMap; // is map opened?
    private boolean openedSearch; // is search opened?
    private Skin gameSkin;  // the skin for the game UI

    // Pucmon SDDL client reference, that will act as
    // a bridge to server communications
    private PucmonClient client = null;

    // game camera that will follow player
    private GameCamera gameCam;

    private Label inventoryLabel; // inventory icon button label
    private boolean invLabelShifted = false; // if inventory label is shifted
    private Label rankingLabel; // ranking icon button label
    private boolean rankLabelShifted = false; // if ranking label is shifted
    private Label marketLabel; // market icon button label
    private boolean marketLabelShifted = false; // if market label is shifted
    private Label mapLabel; // map icon button label
    private boolean mapLabelShifted = false; // if map label is shifted
    private Label searchLabel; // search icon button label
    private boolean searchLabelShifted = false; // if search label is shifted
    private float labelOffY = 3f; // how much shift is needed on Y axis when icon button is pressed
    private float labelOffX = 1f; // how much shift is needed on X axis when icon button is pressed
    private float iconPadX = 5f; // how much padding between each icon button in x axis

    private MessageLogin loginMsg; // login data of player

    private TextMessage textMsg; // a text message that can be displayed at any time and can be of any type
    private boolean isTexting; // bool that represents if a text message is currently being draw on screen

    private Ranking ranking; // ranking object submenu that will provide visualization of game top players
    private Market market; // market object submenu that will provide visualization of market of items
    private SensorUI sensorUI; // sensor UI object submenu that will provide visualization of sensor interface

    private boolean accelAvaialble; // is accelerometer available in this device?

    /**
     * Pervasive Objects
     */
    private PervasiveMap pMap;

    /**************************************************/
    /* Input Control - receives control from Main     */
    /**************************************************/
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(isTexting) { // if message text is being drawn, sends touch to message only
            textMsg.touchDown();
            return false;
        }

        if(openedInventory) { // if inventory opened, do not allow player movement
            Vector2 coords = Common.convertToDefaultViewport(new Vector2(screenX, screenY));
            player.getInventory().touchDown((int)coords.x, (int)coords.y); // and send touch to inventory
            return false;
        }

        if(openedMarket) { // if market opened, do not allow player movement
            Vector2 coords = Common.convertToDefaultViewport(new Vector2(screenX, screenY));
            market.touchDown((int)coords.x, (int)coords.y); // and send touch to inventory
            return false;
        }

        if(openedMap) { // if map opened, do not allow player movement
            Vector2 coords = Common.convertToDefaultViewport(new Vector2(screenX, screenY));
            pMap.touchDown((int)coords.x, (int)coords.y); // and send touch to pervasive map
            return false;
        }

        if(openedRanking) // if ranking opened, do not allow player movement
            return false;

        if(openedSearch) {
            Vector2 coords = Common.convertToDefaultViewport(new Vector2(screenX, screenY));
            sensorUI.touchDown((int)coords.x, (int)coords.y); // and send touch to pervasive map
            return false;
        }

        // converts input coords to match game camera viewport and zoom
        Vector2 coords = Common.convertToZoomedViewport(new Vector2(screenX, screenY), gameCam);
        // pass touch information to player
        player.touchDown((int)coords.x, (int)coords.y, pointer, button);

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(openedMarket) { // if market opened, do not allow player movement
            Vector2 coords = Common.convertToDefaultViewport(new Vector2(screenX, screenY));
            market.touchUp((int)coords.x, (int)coords.y); // and send touch up to inventory
            return false;
        }

        if(openedMap) { // if map opened, do not allow player movement
            Vector2 coords = Common.convertToDefaultViewport(new Vector2(screenX, screenY));
            pMap.touchUp((int)coords.x, (int)coords.y); // and send touch to pervasive map
            return false;
        }

        if(openedSearch) { // if sensor ui opened, send touch up info
            Vector2 coords = Common.convertToDefaultViewport(new Vector2(screenX, screenY));
            sensorUI.touchUp((int)coords.x, (int)coords.y); // and send touch to pervasive map
            return false;
        }

        // if texting or submenu opened, do not allow player movement
        if(isTexting || openedInventory || openedRanking) {
            return false;
        }
        // converts input coords to match game camera viewport and zoom
        Vector2 coords = Common.convertToZoomedViewport(new Vector2(screenX, screenY), gameCam);
        // pass touch information to player
        player.touchUp((int)coords.x, (int)coords.y, pointer, button);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // if texting or submenu opened, do not allow player movement
        if(isTexting || openedInventory || openedRanking || openedMarket || openedMap || openedSearch) {
            return false;
        }
        // converts input coords to match game camera viewport and zoom
        Vector2 coords = Common.convertToZoomedViewport(new Vector2(screenX, screenY), gameCam);
        // pass touch information to player
        player.touchDragged((int)coords.x, (int)coords.y, pointer);
        return false;
    }

    /**************************************************/
    /* LibGDX gameloop - controlled by the main module*/
    /**************************************************/
    @Override
    public void create() {
        // gets if accelerometer is available
        accelAvaialble = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);

        // initializes spritebatch
        worldBatch = new SpriteBatch();
        uiBatch = new SpriteBatch();

        // initializes enemy sheet
        enemySheet = new Texture("imgs/enemies/enemies.png");

        // load the menu background "music"
        gameBGM = Gdx.audio.newMusic(Gdx.files.internal("sfx/pucmon_second_idea.wav"));

        // start the playback of the background music immediately
        gameBGM.setLooping(true); // set looping to loop bgm

        gameSkin = Resource.gameSkin;

        // initializes game UI stage2d
        gameStage = new Stage(new StretchViewport(Config.baseWidth, Config.baseHeight));

        // adds game stage as an input processor
        Main.getInstance().addInputProcessor(gameStage);

        // adds gesture listener as an input processor
        Main.getInstance().addInputProcessor(new GestureDetector(this));

        // sets loglevel for debugging
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        // subscribe to server to be able to communicate with it
        subscribeToServer();

        // gets pucmon client instance
        // or creates one if does not exist yet
        client = PucmonClient.getInstance();

        // initially server and preferences should be synced
        sync = true;

        // initially all submenus are closed
        openedInventory = false;
        openedRanking = false;
        openedMarket = false;

        // initially no message texts are being drawn
        isTexting = false;

        // loads all map setting initial map as current map or last saved map
        Preferences prefs = Gdx.app.getPreferences("Save"); // loads save data preferences
        MapConfig.getInstance().loadMaps(Config.initialMapID);
        gameMap = MapConfig.getInstance().getCurrentMap();

        // creates new player ( to be updated later )
        Vector2 start = gameMap.getStartPoint();
        player = new Player("Model", new Transform(new Vector2((start.y * gameMap.getTileSize()) + 12,
                (start.x * gameMap.getTileSize())), 112, 98, new Vector2(0.5f, 0.5f), 0f, 1),
                worldBatch, null, null);

        // creates game camera with player as target
        gameCam = new GameCamera(player.getTransform(), Config.baseWidth, Config.baseHeight,
                gameMap.getTileSize(), Config.defaultZoom, gameMap.getMapSizeX(), gameMap.getMapSizeY());

        // sets player game camera reference
        player.setGameCamera(gameCam);

        // creates player resources
        player.create();

        // sets libgdx camera for game scaling on different screens
        camera = new OrthographicCamera(Config.baseWidth / Config.defaultZoom, Config.baseHeight / Config.defaultZoom);
        camera.setToOrtho(true, Config.baseWidth / Config.defaultZoom, Config.baseHeight / Config.defaultZoom); // invert coordinates

        // sets libgdx camera for game  UI scaling on different screens
        uiCamera = new OrthographicCamera(Config.baseWidth, Config.baseHeight);
        uiCamera.setToOrtho(false, Config.baseWidth, Config.baseHeight);

        // sets viewport behaviour on scaling for different screens
        viewport = new StretchViewport(Config.baseWidth / Config.defaultZoom, Config.baseHeight / Config.defaultZoom, camera);
        viewport.apply();

        // sets UI viewport behaviour on scaling for different screens
        uiViewport = new StretchViewport(Config.baseWidth, Config.baseHeight, uiCamera);
        uiViewport.apply();

        // initializes ranking
        ranking = new Ranking();

        // initializes market
        market = new Market();

        // intializes pervasive map
        pMap = new PervasiveMap();

        // initializes sensor UI
        sensorUI = new SensorUI();
    }

    /**
     * Loads texts in current language
     */
    public void loadLanguagePassiveContent() {
        // builds welcome text message
        textMsg = Texts.msgDefault("msgWelcome", TextMessage.Anchor.MIDDLE);
        // sets is texting to true to drawn welcome message text
        isTexting = true;
        // disable touch while texting
        Common.setTouchableStage(gameStage, Touchable.disabled);

        // loads game GUI
        loadGameGUI();

        // calls factory class to load game items in correct language
        Factory.getInstance();
    }

    /**
     * Loads player information into game
     * @param loginMsg login message containing player data
     */
    public void loadPlayer(MessageLogin loginMsg) {
        // stores login data
        this.loginMsg = loginMsg;
        Preferences prefs = Gdx.app.getPreferences("Save"); // loads save data preferences

        // sets player name
        player.setName(loginMsg.getCharacter());

        // if server is sync, load player data from server
        if (prefs.getBoolean(loginMsg.getCharacter()+"sync", true)) {
            // if it is first login, keep using new player configuration
            // else, load player data from server
            if((!loginMsg.isFirstLogin()) || (!prefs.getString(loginMsg.getCharacter(), "new").equals("new"))) {
                player.getTransform().setPosition(new Vector2(loginMsg.getPosx(), loginMsg.getPosy()));
                String playerMapName = com.mygdx.game.states.game.standard.map.Map.MapID.values()[loginMsg.getWorldMap()].toString();
                MapConfig.getInstance().setCurrentMap(MapConfig.getInstance().getMapWithID(playerMapName));
                player.getAttributes().setLevel(loginMsg.getLevel());
                player.getAttributes().setExp(loginMsg.getExperience());
                ArrayList<Item[][]> pages = Common.deserializeInventory(loginMsg.getInventoryData());
                player.setInventory(new Inventory(loginMsg.getGold(), pages));
                Equipment[] wornEquipment = Common.deserializeWornEquipment(loginMsg.getEquipmentData());
                player.getInventory().setWornEquipment(wornEquipment);
                player.getTransform().move(new Vector2(0,0)); // to force collider update
            }
        } else { // otherwise, load from saved preferences
            // if it is first login, keep using new player config
            // else, load player data from preferences
            if(!prefs.getString(loginMsg.getCharacter(), "new").equals("new")) {
                Player loadedPlayer = loadPlayerFromPref(prefs);
                player.getTransform().setPosition(new Vector2(loadedPlayer.getTransform().getPosition().x,
                                                                loadedPlayer.getTransform().getPosition().y));
                int mapID = prefs.getInteger("world_map", 0);
                String playerMapName = com.mygdx.game.states.game.standard.map.Map.MapID.values()[mapID].toString();
                MapConfig.getInstance().setCurrentMap(MapConfig.getInstance().getMapWithID(playerMapName));
                player.setAttributes(loadedPlayer.getAttributes());
                player.setInventory(loadedPlayer.getInventory());
            }
        }

        // retrieves updated player mission data
        pMap.retrieveData(player.getName());

        // sets player character in sensor UI to retrieve missions
        sensorUI.setPlayerName(player.getName());

        // sets in common reference to player
        Common.setPlayer(player);

        /**
         * For debug of equipments
         */
//        player.getInventory().addItem(Factory.createItem(Helmet.Golden_Skull_Helmet, 50, false));
//        player.getInventory().addItem(Factory.createItem(Armor.Golden_Skull_Armor, 50, false));
//        player.getInventory().addItem(Factory.createItem(Legs.Golden_Skull_Legs, 50, false));
//        player.getInventory().addItem(Factory.createItem(Boots.Golden_Skull_Boots, 50, false));
//        player.getInventory().addItem(Factory.createItem(Weapon.Golden_Axe_Weapon, 50, false));
//        player.getInventory().addItem(Factory.createItem(Shield.Golden_Shield, 50, false));
//        player.getInventory().addItem(Factory.createItem(Ring.Golden_Ring, 50, false));
//        player.getInventory().addItem(Factory.createItem(Amulet.Golden_Amulet, 50, false));

//        player.getInventory().addItem(Factory.createItem(Helmet.Royal_Knight_Helmet, 50, false));
//        player.getInventory().addItem(Factory.createItem(Armor.Royal_Knight_Armor, 50, false));
//        player.getInventory().addItem(Factory.createItem(Legs.Royal_Knight_Legs, 50, false));
//        player.getInventory().addItem(Factory.createItem(Boots.Royal_Knight_Boots, 50, false));
//        player.getInventory().addItem(Factory.createItem(Weapon.Royal_Spear_Weapon, 50, false));
//        player.getInventory().addItem(Factory.createItem(Shield.Royal_Shield, 50, false));
//        player.getInventory().addItem(Factory.createItem(Ring.Royal_Ring, 50, false));
//        player.getInventory().addItem(Factory.createItem(Amulet.Royal_Amulet, 50, false));
//
//        player.getInventory().addItem(Factory.createItem(Helmet.Leather_Helmet, 50, false));
//        player.getInventory().addItem(Factory.createItem(Armor.Leather_Armor, 50, false));
//        player.getInventory().addItem(Factory.createItem(Legs.Leather_Legs, 50, false));
//        player.getInventory().addItem(Factory.createItem(Boots.Leather_Boots, 50, false));
//        player.getInventory().addItem(Factory.createItem(Weapon.Club_Weapon, 50, false));
//        player.getInventory().addItem(Factory.createItem(Shield.Wooden_Shield, 50, false));
//        player.getInventory().addItem(Factory.createItem(Ring.Tribal_Ring, 50, false));
//        player.getInventory().addItem(Factory.createItem(Amulet.Tribal_Amulet, 50, false));
//
//        player.getInventory().addItem(Factory.createItem(Helmet.Scale_Helmet, 50, false));
//        player.getInventory().addItem(Factory.createItem(Armor.Scale_Armor, 50, false));
//        player.getInventory().addItem(Factory.createItem(Legs.Scale_Legs, 50, false));
//        player.getInventory().addItem(Factory.createItem(Boots.Scale_Boots, 50, false));
//        player.getInventory().addItem(Factory.createItem(Weapon.Amethyst_Sword_Weapon, 50, false));
//        player.getInventory().addItem(Factory.createItem(Shield.Plate_Shield, 50, false));
//        player.getInventory().addItem(Factory.createItem(Ring.Amethyst_Ring, 50, false));
//        player.getInventory().addItem(Factory.createItem(Amulet.Amethyst_Amulet, 50, false));
    }

    /**
     * Loads game gui icons and buttons
     */
    private void loadGameGUI() {
        // inventory icon button
        ImageButton openInventory = new ImageButton(new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 0, 191, 75, 71)));
        openInventory.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 0, 191, 75, 71));
        openInventory.getStyle().imageDown = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 81, 191, 75, 71));
        openInventory.setPosition(Config.baseWidth - openInventory.getWidth(), 0);
        openInventory.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return;

                if(openedInventory)
                    openedInventory = false;
                else {
                    openedInventory = true;
                    openedRanking = false;
                    openedMarket = false;
                    openedSearch = false;
                    // stops sensor search s2pa service
                    Main.getSensor().stopS2PAService();
                    openedMap = false;
                }
                if(invLabelShifted) {
                    inventoryLabel.setPosition(inventoryLabel.getX() - labelOffX, inventoryLabel.getY() + labelOffY);
                    invLabelShifted = false;
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return true;

                if(!invLabelShifted) {
                    inventoryLabel.setPosition(inventoryLabel.getX() + labelOffX, inventoryLabel.getY()-labelOffY);
                    invLabelShifted = true;
                }
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(isTexting)
                    return;

                if(invLabelShifted) {
                    inventoryLabel.setPosition(inventoryLabel.getX() - labelOffX, inventoryLabel.getY()+labelOffY);
                    invLabelShifted = false;
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(isTexting)
                    return;

                if(!invLabelShifted) {
                    inventoryLabel.setPosition(inventoryLabel.getX() + labelOffX, inventoryLabel.getY()-labelOffY);
                    invLabelShifted = true;
                }
            }
        });

        // ranking icon button
        ImageButton openRanking = new ImageButton(new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 0, 0, 75, 71)));
        openRanking.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 0, 0, 75, 71));
        openRanking.getStyle().imageDown = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 82, 0, 75, 71));
        openRanking.setPosition(openInventory.getX() - openRanking.getWidth() - iconPadX, 0);
        openRanking.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return;

                if(openedRanking)
                    openedRanking = false;
                else {
                    ranking.retrieveData(player.getName(), player.getAttributes().getLevel()); // retrieves updated ranking data
                    openedRanking = true;
                    openedInventory = false;
                    openedMarket = false;
                    openedSearch = false;
                    // stops sensor search s2pa service
                    Main.getSensor().stopS2PAService();
                    openedMap = false;
                }
                if(rankLabelShifted) {
                    rankingLabel.setPosition(rankingLabel.getX() - labelOffX, rankingLabel.getY() + labelOffY);
                    rankLabelShifted = false;
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return true;

                if(!rankLabelShifted) {
                    rankingLabel.setPosition(rankingLabel.getX() + labelOffX, rankingLabel.getY()-labelOffY);
                    rankLabelShifted = true;
                }
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(isTexting)
                    return;

                if(rankLabelShifted) {
                    rankingLabel.setPosition(rankingLabel.getX() - labelOffX, rankingLabel.getY()+labelOffY);
                    rankLabelShifted = false;
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(isTexting)
                    return;

                if(!rankLabelShifted) {
                    rankingLabel.setPosition(rankingLabel.getX() + labelOffX, rankingLabel.getY()-labelOffY);
                    rankLabelShifted = true;
                }
            }
        });

        // market icon button
        ImageButton openMarket = new ImageButton(new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 0, 94, 75, 71)));
        openMarket.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 0, 94, 75, 71));
        openMarket.getStyle().imageDown = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 81, 94, 75, 71));
        openMarket.setPosition(openRanking.getX() - openMarket.getWidth() - iconPadX, 0);
        openMarket.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return;

                if(openedMarket)
                    openedMarket = false;
                else {
                    if(market.getSelectedTab() == Market.Tab.Sell)
                        market.enable();
                    market.retrieveData(player.getName(), player.getInventory()); // retrieves updated market data
                    openedMarket = true;
                    openedRanking = false;
                    openedInventory = false;
                    openedSearch = false;
                    // stops sensor search s2pa service
                    Main.getSensor().stopS2PAService();
                    openedMap = false;
                }
                if(marketLabelShifted) {
                    marketLabel.setPosition(marketLabel.getX() - labelOffX, marketLabel.getY() + labelOffY);
                    marketLabelShifted = false;
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return true;

                if(!marketLabelShifted) {
                    marketLabel.setPosition(marketLabel.getX() + labelOffX, marketLabel.getY()-labelOffY);
                    marketLabelShifted = true;
                }
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(isTexting)
                    return;

                if(marketLabelShifted) {
                    marketLabel.setPosition(marketLabel.getX() - labelOffX, marketLabel.getY()+labelOffY);
                    marketLabelShifted = false;
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(isTexting)
                    return;

                if(!marketLabelShifted) {
                    marketLabel.setPosition(marketLabel.getX() + labelOffX, marketLabel.getY()-labelOffY);
                    marketLabelShifted = true;
                }
            }
        });

        // map icon button
        ImageButton openMap = new ImageButton(new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 166, 92, 76, 72)));
        openMap.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 166, 92, 76, 72));
        openMap.getStyle().imageDown = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 250, 92, 76, 72));
        openMap.setPosition(openMarket.getX() - openMap.getWidth() - iconPadX, 0);
        openMap.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return;

                if(openedMap)
                    openedMap = false;
                else {
                    pMap.retrieveData(player.getName()); // retrieves updated player mission data
                    openedMap = true;
                    openedSearch = false;
                    // stops sensor search s2pa service
                    Main.getSensor().stopS2PAService();
                    openedMarket = false;
                    openedRanking = false;
                    openedInventory = false;
                    // refreshes language
                    pMap.refreshLanguage();
                }
                if(mapLabelShifted) {
                    mapLabel.setPosition(mapLabel.getX() - labelOffX, mapLabel.getY() + labelOffY);
                    mapLabelShifted = false;
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return true;

                if(!mapLabelShifted) {
                    mapLabel.setPosition(mapLabel.getX() + labelOffX, mapLabel.getY()-labelOffY);
                    mapLabelShifted = true;
                }
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(isTexting)
                    return;

                if(mapLabelShifted) {
                    mapLabel.setPosition(mapLabel.getX() - labelOffX, mapLabel.getY()+labelOffY);
                    mapLabelShifted = false;
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(isTexting)
                    return;

                if(!mapLabelShifted) {
                    mapLabel.setPosition(mapLabel.getX() + labelOffX, mapLabel.getY()-labelOffY);
                    mapLabelShifted = true;
                }
            }
        });

        // search icon button
        ImageButton openSearch = new ImageButton(new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 0, 280, 70, 72)));
        openSearch.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 0, 280, 70, 72));
        openSearch.getStyle().imageDown = new TextureRegionDrawable(new TextureRegion(Resource.iconGame, 83, 280, 70, 72));
        openSearch.setPosition(openMap.getX() - openSearch.getWidth() - iconPadX, 0);
        openSearch.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return;

                if(openedSearch) {
                    openedSearch = false;
                    // stops sensor search s2pa service
                    Main.getSensor().stopS2PAService();
                }
                else {
                    // starts sensor search s2pa service
                    Main.getSensor().startS2PAService();
                    sensorUI.enable();
                    openedSearch = true;
                    openedMap = false;
                    openedMarket = false;
                    openedRanking = false;
                    openedInventory = false;
                }
                if(searchLabelShifted) {
                    searchLabel.setPosition(searchLabel.getX() - labelOffX, searchLabel.getY() + labelOffY);
                    searchLabelShifted = false;
                }
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if(isTexting)
                    return true;

                if(!searchLabelShifted) {
                    searchLabel.setPosition(searchLabel.getX() + labelOffX, searchLabel.getY()-labelOffY);
                    searchLabelShifted = true;
                }
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(isTexting)
                    return;

                if(searchLabelShifted) {
                    searchLabel.setPosition(searchLabel.getX() - labelOffX, searchLabel.getY()+labelOffY);
                    searchLabelShifted = false;
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(isTexting)
                    return;

                if(!searchLabelShifted) {
                    searchLabel.setPosition(searchLabel.getX() + labelOffX, searchLabel.getY()-labelOffY);
                    searchLabelShifted = true;
                }
            }
        });

        // TODO: ADJUST icon labels to centralize them - better the architecture for button icons

        // add icon actors
        gameStage.addActor(openInventory);
        gameStage.addActor(openRanking);
        gameStage.addActor(openMarket);
        gameStage.addActor(openMap);
        gameStage.addActor(openSearch);

        // create inventory label
        inventoryLabel = new Label(Main.getInstance().getLang().get("inventoryStr"),
                                            new Label.LabelStyle(Resource.gameFont, Color.BLACK));
        inventoryLabel.setPosition(openInventory.getX()+openInventory.getWidth()/2 - inventoryLabel.getWidth()/2,
                                    openInventory.getY()+openInventory.getHeight()*0.08f);
        inventoryLabel.setTouchable(Touchable.disabled);

        // create ranking label
        rankingLabel = new Label(Main.getInstance().getLang().get("rankingStr"),
                new Label.LabelStyle(Resource.gameFont, Color.BLACK));
        rankingLabel.setPosition(openRanking.getX()+openRanking.getWidth()/2 - rankingLabel.getWidth()/2,
                                    openRanking.getY()+openRanking.getHeight()*0.08f);
        rankingLabel.setTouchable(Touchable.disabled);

        // create market label
        marketLabel = new Label(Main.getInstance().getLang().get("marketStr"),
                new Label.LabelStyle(Resource.gameFont, Color.BLACK));
        marketLabel.setPosition(openMarket.getX()+openMarket.getWidth()/2 - marketLabel.getWidth()/2,
                                openMarket.getY()+openMarket.getHeight()*0.08f);
        marketLabel.setTouchable(Touchable.disabled);

        // create map label
        mapLabel = new Label(Main.getInstance().getLang().get("mapStr"),
                new Label.LabelStyle(Resource.gameFont, Color.BLACK));
        mapLabel.setPosition(openMap.getX()+openMap.getWidth()/2 - mapLabel.getWidth()/2,
                openMap.getY()+openMap.getHeight()*0.08f);
        mapLabel.setTouchable(Touchable.disabled);

        // create search label
        searchLabel = new Label(Main.getInstance().getLang().get("searchStr"),
                new Label.LabelStyle(Resource.gameFont, Color.BLACK));
        searchLabel.setPosition(openSearch.getX()+openSearch.getWidth()/2 - searchLabel.getWidth()/2,
                openSearch.getY()+openSearch.getHeight()*0.08f);
        searchLabel.setTouchable(Touchable.disabled);

        // add labels to stage
        gameStage.addActor(inventoryLabel);
        gameStage.addActor(rankingLabel);
        gameStage.addActor(marketLabel);
        gameStage.addActor(mapLabel);
        gameStage.addActor(searchLabel);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiViewport.update(width, height);

        if(openedMap) {
            pMap.resize(width, height);
        }
    }

    @Override
    public void render() {

        camera.update();
        uiCamera.update();
        // set projection matrix
        worldBatch.setProjectionMatrix(camera.combined);

        // clear graphics
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // begin world batch rendering
        worldBatch.begin();

        // updates player
        player.update();

        // updates game camera
        gameCam.update();

        // renders current map created with 2DMapBuilder
        gameMap = MapConfig.getInstance().getCurrentMap();
        gameMap.render(gameCam, camera, worldBatch);

        // renders player
        player.render();

        // ends world batch rendering
        worldBatch.end();

        // render colliders for debug
        if(Config.debug) {
            //gameMap.renderCollider(gameCam, camera);
            //player.renderCollider(camera);
        }

        // renders game stage for UI
        gameStage.act();
        gameStage.draw();

        // renders UI in different batch
        uiBatch.setProjectionMatrix(uiCamera.combined);
        uiBatch.begin();

        // update labels
        inventoryLabel.setText(Main.getInstance().getLang().get("inventoryStr"));
        rankingLabel.setText(Main.getInstance().getLang().get("rankingStr"));

        // renders inventory if it is opened
        if(openedInventory) {
            player.getInventory().render(uiBatch, player); // renders player inventory
        }

        // renders ranking if it is opened
        if(openedRanking) {
            ranking.render(uiBatch, uiCamera); // renders ranking
        }

        // renders market if it is opened
        if(openedMarket) {
            market.render(uiBatch, uiCamera); // renders market
        } else { // if not opened, check if it was already deactivated
            if(market.isActive()) // if not
                market.disable();  // deactivate it
        }


        // renders pervasive map if it is opened
        if(openedMap) {
            pMap.render(); // renders pervasive map
        }

        // renders sensor search interface if it is opened
        if(openedSearch) {
            // checks if sensor refresh time has been surpassed
            if(sensorTimer >= Config.sensorTime) {
                // refreshes sensors on account of communication-less time limit and signal strength
                sensorUI.refreshSensors();
                // resets timer
                sensorTimer = 0f;
            }

            sensorUI.render(uiBatch, uiCamera); // renders sensor ui
        } else { // if not opened, check if it was already deactivated
            if(sensorUI.isActive()) // if not
                sensorUI.disable();  // deactivate it
        }

        // if texting, draw message text
        if(isTexting && textMsg != null) {
            textMsg.render(uiBatch);
            // if message text is finished and was closed
            if(textMsg.isToBeClosed()) {
                isTexting = false; // stop drawing message text
                textMsg = null; // resets text message var
                Common.setTouchableStage(gameStage, Touchable.enabled); // re-enable stage touch
            }
        }

        // ends UI batch rendering
        uiBatch.end();

        // chance to spawn battle if player is moving,
        // current map enables random battling and if
        // time to try spawns has been surpassed
        if(player.isMoving() && MapConfig.getInstance().getCurrentMap().isRandomBattleEnabled() &&
                battleSpawnTimer >= Config.timeToTryBattleSpawn ) {
            battleSpawnTimer = 0f; // resets timer
            rollDiceForBattle(); // rolls dice for spawning a battle
        }

        // updates timers
        battleSpawnTimer+=Gdx.graphics.getDeltaTime();
        saveTimer+=Gdx.graphics.getDeltaTime();
        sensorTimer+=Gdx.graphics.getDeltaTime();
        accelTimer+=Gdx.graphics.getDeltaTime();

        // saves if it is time to automatic save
        if(saveTimer >= Config.saveTime) {
            saveTimer = 0f; // resets timer
            save(); // save
        }

        // checks if accelerometer generated a sensor search
        float accelX = Gdx.input.getAccelerometerX();
        float accelY = Gdx.input.getAccelerometerY();
        float accelZ = Gdx.input.getAccelerometerZ();
        Vector3 accel = new Vector3(accelX, accelY, accelZ);
        float accelMagnitude = accel.len2();

        // switch sensor UI interface
        if(accelMagnitude > Config.sensorMinAccel && accelTimer > Config.sensorAccelTimeInterval && !isTexting) {
            if(openedSearch) {
                openedSearch = false;
                // stops sensor search s2pa service
                Main.getSensor().stopS2PAService();
            }
            else {
                // starts sensor search s2pa service
                Main.getSensor().startS2PAService();
                sensorUI.enable();
                openedSearch = true;
                openedMap = false;
                openedMarket = false;
                openedRanking = false;
                openedInventory = false;
            }
            accelTimer = 0f;
        }
    }

    /**
     * Spawns battle at a predefined chance
     */
    private void rollDiceForBattle() {
        int dice = Common.randInt(0, 100); // rolls dice
        // if battle chance is achieved
        if(dice < Config.battleChance) {
            // spawns battle
            Battle battle = BattleSpawn.spawn(player, enemySheet);
            // change rendering game screen to battle screen
            Main.getInstance().changeToBattle(battle);
        }
    }

    @Override
    public void pause() {
        // if playing music
        if(gameBGM.isPlaying())
            gameBGM.stop(); // stop game music when leaving game

        Common.setTouchableStage(gameStage, Touchable.disabled); // disables game stage actors input
        save(); // saves if it is paused for safety reasons
    }

    @Override
    public void resume() {
        // if not playing music
        if(!gameBGM.isPlaying())
            gameBGM.play(); // play game music when resuming game

        Common.setTouchableStage(gameStage, Touchable.enabled); // re-enables game stage actors input
    }

    @Override
    public void dispose() {
        gameBGM.dispose();
        worldBatch.dispose();
        uiBatch.dispose();
        gameMap.dispose();
        player.dispose();
        Resource.getInstance().dispose(); // dispose remaining resources
        enemySheet.dispose();
        pMap.dispose(); // dispose pervasive map remaining resources
        // sends logoff message to server
        MessageLogoff logOffMsg = new MessageLogoff(player.getName());
        sendAsyncMessage(new MessageContent(this.getClass().toString(), logOffMsg, MessageContent.Type.LOGOFF));
    }


    /**
     * Loads player information from preferences
     * @param prefs the preference that contain player information
     * @return the loaded player with all loaded information
     */
    private Player loadPlayerFromPref(Preferences prefs) {
        /**
         * Gets player world information
         */
        float x_world = prefs.getFloat("player_x_world"); // gets player x position in world
        float y_world = prefs.getFloat("player_y_world"); // gets player y position in world
        Vector2 worldPos = new Vector2(x_world, y_world);

        /**
         * Gets player attributes
         */
        Attributes playerAttr = new Attributes(prefs.getInteger("player_level"), prefs.getLong("player_exp"));

        /**
         * Gets player inventory
         */
        // gets pages by deserializing inventory items string
        ArrayList<Item[][]> pages = Common.deserializeInventory(prefs.getString("inventory_items"));
        // creates inventory with loaded information
        Inventory playerInv = new Inventory(prefs.getLong("player_gold"), pages);

        // loads player worn equipments
        // gets player equipments information string
        String wornInfo =  prefs.getString("worn_equipments");
        // array of worn equipments to be loaded
        Equipment[] wornEquipment = Common.deserializeWornEquipment(wornInfo);
        // sets worn equipment of player
        playerInv.setWornEquipment(wornEquipment);

        // returns player with loaded information
        return new Player(loginMsg.getCharacter(), new Transform(worldPos,  112, 98, new Vector2(0.5f, 0.5f), 0f, 1),
                worldBatch, playerAttr, playerInv);
    }

    /**
     * Saves player current information in server.
     * If connection is currently offline, saves in
     * preferences. As soon as connection is regained,
     * a save is made to the server.
     */
    public void save() {
        //TODO - SAVE USING ANOTHER THREAD IF PREFERENCE SAVINGS GETS TOO LONG

        final Thread connThread = new Thread() {
            @Override
            public void run() {
                // loads savedata preferences
                Preferences prefs = Gdx.app.getPreferences("Save");

                // if there is a connection, save to server
                if(PucmonClient.getInstance().isConnected()) {
                    System.out.println("SAVING TO SERVER:");
                    saveToServer();
                    saveToPreferences(prefs);
                    // After save to server, data is synchronized
                    synchronized (this) {
                        sync = true;
                    }
                } else { // save to preferences if there is no connection
                    System.out.println("SAVING TO PREFERENCES:");
                    // saves player data into preferences
                    saveToPreferences(prefs);
                    synchronized (this) {
                        sync = false; // server is not in sync with last save data
                    }
                }

                // saves sync information in preferences
                // to be able to correctly load last data
                // in further executions of the game
                prefs.putBoolean(loginMsg.getCharacter()+"sync", sync);

                // a save for this character has been created
                // on server and local, or only local.
                // Stores character name on preference to symbolize it
                prefs.putString(loginMsg.getCharacter(), "notnew");

                // flush preference changes
                prefs.flush();
            }
        };
        connThread.start();
    }

    /**
     * Sends message to server to save player data into server database
     */
    private void saveToServer() {
        // gets world map ID
        int worldMap = com.mygdx.game.states.game.standard.map.Map.MapID.valueOf
                (MapConfig.getInstance().getCurrentMap()
                        .getName()).ordinal();
        // gets player character name
        String character = player.getName();
        // gets player level
        int level = player.getAttributes().getLevel();
        // gets player experience
        long exp = player.getAttributes().getExp();
        // gets player position x
        int posx = MathUtils.round(player.getTransform().getPosition().x);
        // gets player position y
        int posy = MathUtils.round(player.getTransform().getPosition().y);
        // gets player gold
        long gold = player.getInventory().getGold();
        // gets serialized string containing player inventory data
        String invInfo = Common.serializeInventory(player.getInventory().getPages());
        // gets serialized string containing player worn equipment data
        String eqInfo = Common.serializeWornEquipment(player.getInventory().getWornEquipment());
        // builds save message with info gathered
        MessageSave saveMsg = new MessageSave(character, worldMap, level, exp, posx, posy, gold, invInfo, eqInfo);
        // wraps save message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), saveMsg, MessageContent.Type.SAVE);
        // sends save message to server with save information
        sendAsyncMessage(msg);
    }

    /**
     * Saves player data into preferences
     */
    private void saveToPreferences(Preferences prefs) {;

        /**
         * Saves player world information
         */
        prefs.putFloat("player_x_world", player.getTransform().getPosition().x); // saves player x position in world
        prefs.putFloat("player_y_world", player.getTransform().getPosition().y); // saves player y position in world
        prefs.putInteger("world_map", com.mygdx.game.states.game.standard.map.Map.MapID.valueOf
                                            (MapConfig.getInstance().getCurrentMap()
                                                    .getName()).ordinal()); // saves current world map player is in

        /**
         * Saves player attributes
          */
        prefs.putInteger("player_level", player.getAttributes().getLevel());                // saves player level
        prefs.putLong("player_exp", player.getAttributes().getExp());                       // saves player experience

        /**
         * Saves player inventory
         */
        prefs.putLong("player_gold", player.getInventory().getGold());  // saves player gold
        // saves player inventory items
        ArrayList<Item[][]> invPages = player.getInventory().getPages(); // inventory items pages
        // string that will contain all player inventory items information
        String invInfo = Common.serializeInventory(invPages);
        prefs.putString("inventory_items", invInfo); // saves string built with player inventory items data

        /**
         * Saves player worn equipments
         */
        Equipment[] wornEquips = player.getInventory().getWornEquipment(); // gets worn equipments
        String wornData = Common.serializeWornEquipment(wornEquips);
        prefs.putString("worn_equipments", wornData); // saves string built with player worn equipments data

        // flush preference changes
        prefs.flush();
    }



    /******************************/
    /* Gesture listener callbacks */
    /******************************/

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        if(openedInventory) // if opened inventory, send fling data to navigate inventory pages
            player.getInventory().fling(velocityX, velocityY);
        if(openedMarket) // if opened market, send fling data to navigate market tabs
            market.fling(velocityX, velocityY);
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if(openedRanking) // if ranking opened, send pan data to navigate ranking top players
            ranking.pan(deltaX, deltaY);
        if(openedMarket) // if market opened, send pan data to navigate market items
            market.pan(deltaX, deltaY);
        if(openedMap) // if pervasive map opened, send pan data to navigate through pervasive map
            pMap.pan(deltaX, deltaY);
        if(openedSearch) // if sensor search UI opened, send pan data to navigate through sensor entries
            sensorUI.pan(deltaX, deltaY);

        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    // zooms the world
    @Override
    public boolean zoom(float initialDistance, float distance) {
        // if texting ignore zoom
        if(isTexting)
            return true;

        // if pervasive map is opened, apply zoom to the map instead
        if(openedMap) {
            return pMap.zoom(initialDistance, distance);
        }

        // deactivates player while zooming
        player.setActive(false);
        // calculates zoom alteration based on zoomSpeed
        float deltaZoom = (distance / initialDistance);
        // applies alteration to zoom
        float newZoom = deltaZoom * gameCam.getZoom();
        // gets zoom alteration and applies zoom speed
        float zoomDiff = (newZoom - gameCam.getZoom()) * Config.zoomSpeed;
        // sets new zoom clamping between min and max zoom
        gameCam.setZoom(MathUtils.clamp(gameCam.getZoom()+zoomDiff, Config.minZoom, Config.maxZoom));
        gameCam.update();
        // recreates camera to apply new zoom
        camera = new OrthographicCamera(Config.baseWidth / gameCam.getZoom(), Config.baseHeight / gameCam.getZoom());
        camera.setToOrtho(true, Config.baseWidth / gameCam.getZoom(),
                Config.baseHeight / gameCam.getZoom()); // invert coordinates

        return true;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
        // reactivates player when zooming stops
        player.setActive(true);
    }

    /**************************************************/
    /* Server communications and message handling     */
    /**************************************************/

    /**
     * Sends messages to the server without blocking and waiting for response
     */
    public void sendAsyncMessage(MessageContent msg) {
        client.sendMessage(msg);
    }

    /**
     * Handles server messages
     */

    @Override
    public void subscribeToServer() {
        ServerMessages.getInstance().subscribe(this);
    }

    @Override
    public void handleServerMessage(MessageContent msg) {
        // TODO - Only set server as sync if save response is OKIDOKI
    }
}
