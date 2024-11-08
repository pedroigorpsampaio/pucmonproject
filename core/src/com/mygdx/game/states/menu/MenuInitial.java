package com.mygdx.game.states.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.Main;
import com.mygdx.game.messages.MessageContent;
import com.mygdx.game.messages.MessageFlag;
import com.mygdx.game.messages.MessageLogin;
import com.mygdx.game.messages.MessageSignUp;
import com.mygdx.game.messages.ServerMessages;
import com.mygdx.game.states.game.standard.architecture.ServerListener;
import com.mygdx.game.ui.TranslatableImageButton;
import com.mygdx.game.states.game.standard.architecture.GameState;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;
import com.sddl.client.PucmonClient;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Initial menu for the game
 * Containing a hub for initial interactions
 * where player can choose to initialize the game
 * or change some options
 *
 * @author	Pedro Sampaio
 * @since	0.2
 */
public class MenuInitial implements GameState, ServerListener, Serializable {

	private Stage menuStage;	// the menu UI Stage
	private Music menuBGM;	// the background music for the initial menu
	private Skin menuSkin;	// the UI Skin for the initial menu
	private Texture menuBG; // background image for the initial menu
	private SpriteBatch menuBatch; // the spriteBatch to be used in initial menu
	private OrthographicCamera camera; // camera to adjust to screen resolution
	private StretchViewport viewport; // viewport to use with camera to adjust to screen res
	private Table menuTable; // menu table of ui content

	// list of spawnable clouds
	private ArrayList<SpawnableCloud> clouds;
	// base chance for spawning clouds
	private int cloudBaseChance = 71; // from 0% to 100% of chance
	// time between each try to spawn a cloud;
	private int tb_spawnClouds = 2;
	// timer to control cloud spawn;
	private float timer_spawnClouds = 2;

	/**
	 * Menu entities
	 */
	private Label pucmonLabel;
	private TextField username;
	private TextField password;
	private TextField character;
	private Label infoLabel;
	private TranslatableImageButton newUser;
	private TranslatableImageButton play;
	private TranslatableImageButton changeLang;
	private TranslatableImageButton create;
	private TranslatableImageButton back;

	/**
	 * Constructor starts listening to server
	 */
	public MenuInitial() {
		// subscribe to start listening to server
		subscribeToServer();
	}

	/**************************************************/
    /* Input Control - receives control from Main     */
	/**************************************************/
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	/**************************************************/
    /* LibGDX gameloop - controlled by the main module*/
	/**************************************************/
	@Override
	public void create() {
		// sets camera for menu scaling on different screens
		camera = new OrthographicCamera(Config.baseWidth, Config.baseHeight);
		camera.setToOrtho(false, Config.baseWidth, Config.baseHeight);

		// initializes menu sprite batch
		menuBatch = new SpriteBatch();
		// initializes background image
		menuBG = new Texture("imgs/menu_initial/puc_celshaded.png");

		// sets viewport behaviour on scaling for different screens
		viewport = new StretchViewport(Config.baseWidth, Config.baseHeight, camera);
		viewport.apply();

		// initializes menu UI stage2d
		menuStage = new Stage(new StretchViewport(Config.baseWidth, Config.baseHeight));

		// initialize spawnable clouds list
		clouds = new ArrayList<SpawnableCloud>();

		// adds menu stage as an input processor
		Main.getInstance().addInputProcessor(menuStage);

		// load the menu background "music"
		menuBGM = Gdx.audio.newMusic(Gdx.files.internal("sfx/pucmon_menu_bgm_v3.wav"));

		// start the playback of the background music immediately
		menuBGM.setLooping(true); // set looping to loop bgm
		menuBGM.play();

		// loads the skin for the menu
		menuSkin = new Skin(Gdx.files.internal("skin/freezing-ui.json"));

		menuTable = new Table(); // menu table of ui content

		// Pucmon label
		pucmonLabel = new Label(Main.getInstance().getLang().get("game"), menuSkin);
		pucmonLabel.setFontScale(2f);
		pucmonLabel.getStyle().font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
				Texture.TextureFilter.Linear); // bilinear filtering
		pucmonLabel.setColor(Color.WHITE);

		menuSkin.add("arial", new BitmapFont(), BitmapFont.class);

		// TODO - add option to remember username and password and store it in preferences if it is the case
		// user name input text field
		username = new TextField("", menuSkin);
		username.setMessageText(Main.getInstance().getLang().get("usernameStr"));
		username.setAlignment(Align.center);

		// password input text field
		password = new TextField("", menuSkin);
		password.setMessageText(Main.getInstance().getLang().get("passwordStr"));
		password.setAlignment(Align.center);
		password.setPasswordMode(true);
		password.setPasswordCharacter('*');

		// character name input text field
		character = new TextField("", menuSkin);
		character.setMessageText(Main.getInstance().getLang().get("characterName"));
		character.setAlignment(Align.center);

		// error label
		infoLabel = new Label("", menuSkin);
		infoLabel.setColor(Color.VIOLET);

		// translatable create new user button
		newUser = new TranslatableImageButton("newUserStr", menuSkin);
		newUser.addListener(new InputListener(){
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				buildNewUserMenu();
			}
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});

		// translatable create button
		create = new TranslatableImageButton("createStr", menuSkin);
		create.addListener(new InputListener(){
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				String user = username.getText(); // gets username
				String pass = password.getText(); // gets password
				String charName = character.getText(); // gets password
				if(user == "" || pass == "" || charName == "") {
					infoLabel.setText(Main.getInstance().getLang().get("infoBlankFields")); // inform error for user
					return;    // do nothing if info provided is not enough
				} else if(PucmonClient.getInstance().isConnected()){
					// tries to create account on server
					sendSignUpMessage(user, pass, charName);
				} else { // not connected
					infoLabel.setText(Main.getInstance().getLang().get("notConnected"));
				}
			}
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});

		// translatable create button
		back = new TranslatableImageButton("returnStr", menuSkin);
		back.addListener(new InputListener(){
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				buildLoginMenu();
			}
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});

		// translatable play button
		play = new TranslatableImageButton("playStr", menuSkin);
		play.addListener(new InputListener(){
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				String user = username.getText(); // gets username
				String pass = password.getText(); // gets password
				if(user == "" || pass == "") {
					infoLabel.setText(Main.getInstance().getLang().get("infoBlankFields")); // inform error for user
					return;    // do nothing if info provided is not enough
				}
				else if(PucmonClient.getInstance().isConnected()){
					// tries to login on server
					sendLoginMessage(user, pass);
				} else { // not connected
					infoLabel.setText(Main.getInstance().getLang().get("notConnected"));
				}
			}
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});

		// translatable change language button
		changeLang = new TranslatableImageButton("langStr", menuSkin);
		changeLang.addListener(new InputListener(){
			@Override
			public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
				// get preferences
				Preferences prefs = Gdx.app.getPreferences("Preferences");
				// check current language and change to the other
				if(prefs.getString("locale_lang").equals("pt")) {
					prefs.putString("locale_lang", "en");
					prefs.putString("locale_country", "US");
				} else {
					prefs.putString("locale_lang", "pt");
					prefs.putString("locale_country", "BR");
				}

				// TODO - decide if changing language should reload game to proper change all, or to store all text objects and change programatically

				// commit changes in pref
				prefs.flush();

				// updates language
				Main.getInstance().updateLanguage();
				// updates all translatable buttons texts
				Common.updateAllTButtonTexts();
				// updates text fields
				username.setMessageText(Main.getInstance().getLang().get("usernameStr"));
				password.setMessageText(Main.getInstance().getLang().get("passwordStr"));
				character.setMessageText(Main.getInstance().getLang().get("characterName"));
			}
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});

		// adds to the list of translatable buttons
		Main.getInstance().addTButton(newUser);
		Main.getInstance().addTButton(create);
		Main.getInstance().addTButton(play);
		Main.getInstance().addTButton(changeLang);
		Main.getInstance().addTButton(back);

		// initially builds login menu
		buildLoginMenu();

		menuStage.addActor(menuTable);
	}

	/**
	 * builds login menu with its buttons and other entities
	 */
	public void buildLoginMenu() {
		infoLabel.setText(""); // resets error label
		menuTable.remove();
		menuTable = new Table();
		menuTable.align(Align.top);
		menuTable.add(pucmonLabel);
		menuTable.row();
		menuTable.add(newUser).padTop(Config.baseHeight * 0.15f)
				.width(Config.baseWidth * 0.4f)
				.height(Config.baseHeight * 0.04f);
		menuTable.row();
		menuTable.add(username).padTop(Config.baseHeight * 0.05f)
				.width(Config.baseWidth * 0.4f)
				.height(Config.baseHeight * 0.04f);
		menuTable.row();
		menuTable.add(password).padTop(Config.baseHeight * 0.01f)
				.width(Config.baseWidth * 0.4f)
				.height(Config.baseHeight * 0.04f);
		menuTable.row();
		menuTable.add(infoLabel);
		menuTable.row();
		menuTable.add(play).padTop(Config.baseHeight * 0.035f);
		menuTable.row();
		menuTable.add(changeLang);
		menuTable.setFillParent(true);

		menuStage.addActor(menuTable);
	}

	/**
	 * builds new user menu with its buttons and other entities
	 */
	public void buildNewUserMenu() {
		infoLabel.setText(""); // resets error label
		menuTable.remove();
		menuTable = new Table();
		menuTable.align(Align.top);
		menuTable.add(pucmonLabel);
		menuTable.row();
		menuTable.add(username).padTop(Config.baseHeight * 0.2f)
				.width(Config.baseWidth * 0.4f)
				.height(Config.baseHeight * 0.04f);
		menuTable.row();
		menuTable.add(password).padTop(Config.baseHeight * 0.01f)
				.width(Config.baseWidth * 0.4f)
				.height(Config.baseHeight * 0.04f);
		menuTable.row();
		menuTable.add(character).padTop(Config.baseHeight * 0.01f)
				.width(Config.baseWidth * 0.4f)
				.height(Config.baseHeight * 0.04f);
		menuTable.row();
		menuTable.add(infoLabel);
		menuTable.row();
		menuTable.add(create).padTop(Config.baseHeight * 0.027f);
		menuTable.row();
		menuTable.add(back);
		menuTable.setFillParent(true);
		menuStage.addActor(menuTable);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		menuStage.getViewport().update(width, height);
	}

	@Override
	public void render() {
		// clear screen
		Gdx.gl.glClearColor( 1, 0, 0, 1 );
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

		// sets camera for batch matrix
		menuBatch.setProjectionMatrix(camera.combined);

		// start sprite batch
		menuBatch.begin();
		// draws background image
		menuBatch.draw(menuBG, 0, 0);
		// renders clouds
		renderClouds();
		// ends sprite batch
		menuBatch.end();

		// renders UI stage
		menuStage.act();
		menuStage.draw();
	}

	@Override
	public void pause() {
		// if playing music
		if(menuBGM.isPlaying())
			menuBGM.stop(); // pause menu music when leaving menu

		// disable touch in menu actors
		Common.setTouchableStage(menuStage, Touchable.disabled);
	}

	@Override
	public void resume() {
		// if not playing yet
		if(!menuBGM.isPlaying())
			menuBGM.play();	// play music when resuming menu

		// enable touch in menu actors
		Common.setTouchableStage(menuStage, Touchable.enabled);
	}

	@Override
	public void dispose() {
		menuBGM.dispose();	// disposes menu bgm
		menuSkin.dispose(); // disposes menu skin
		menuStage.dispose(); // disposes menu stage
		menuBG.dispose(); // disposes menu background image
		menuBatch.dispose(); // disposes menu sprite batch
	}


	/**
	 * Spawns a random cloud in background menu
	 * @param minY        the minY value for the random y spawn pos
	 * @param maxY        the maxY value for the random y spawn pos
	 * @param direction the direction in x axis (left(-1) or right(1))
	 * @param moveSpeed the move speed of the cloud
	 * @param scale		the scale of cloud image
	 */
	private void spawnCloud(int minY, int maxY, int direction, int moveSpeed, int scale) {
		// creates cloud
		SpawnableCloud spawnCloud = new SpawnableCloud(moveSpeed, direction, maxY, minY, scale, viewport);
		spawnCloud.create();
		// adds to the list of spawnable clouds
		clouds.add(spawnCloud);
	}

	/**
	 * Renders all clouds existent.
	 * Also tries to spawn more clouds
	 */
	private void renderClouds() {

		// updates timer of cloud spawn
		timer_spawnClouds += Gdx.graphics.getDeltaTime();
		// check if time between spawn tries has passed
		if(timer_spawnClouds >= tb_spawnClouds) {
			// tries to spawn a cloud depending on existing quantity and base chance
			int chance = cloudBaseChance / ((clouds.size()+1));
			if (MathUtils.random(0, 100) < chance) {
				// spawns cloud in sky y range, going left, with a random speed and scale
				spawnCloud((int) (viewport.getWorldHeight()/3), (int) viewport.getWorldHeight(), -1,
						MathUtils.random(10, 18), MathUtils.random(35, 91));
			}
			//resets spawn cloud timer
			timer_spawnClouds = 0f;
		}

		// renders existent clouds
		for(int i = 0; i < clouds.size(); i++) {
			SpawnableCloud cloud = clouds.get(i); // gets iteration cloud
			cloud.update(); // updates cloud position
			// draws cloud
			Sprite cloudSprite = new Sprite(cloud.getCloudIMG());
			menuBatch.draw(cloudSprite, cloud.getPosition().x, cloud.getPosition().y, 0, 0,
							cloudSprite.getWidth(), cloudSprite.getHeight(), cloud.getScale(),
							cloud.getScale(), 0);
		}

		// runs disposeClouds to search for non-visible clouds
		// that have gone through all the sky already
		disposeClouds();
	}

	/**
	 * Dispose clouds that do not appear anymore in screen
	 */
	private void disposeClouds() {
		// iterates through all existent clouds
		for(int i = 0; i < clouds.size(); i++) {
			// current iteration cloud
			SpawnableCloud cloud = clouds.get(i);
			// if going left, check if disappeared on left side
			if(cloud.getDirection() == -1) {
				if(cloud.getPosition().x < -cloud.getScaledWidth()) {
					cloud.dispose(); // disposes cloud
					clouds.remove(i); // removes from list of clouds
				}
			}
			else { // going right, check if disappeared on right side
				if(cloud.getPosition().x > (int)viewport.getWorldWidth()) {
					cloud.dispose(); // disposes cloud
					clouds.remove(i); // removes from list of clouds
				}
			}
		}
	}

	/**
	 * Server communication
	 */

	/**
	 * Sends a sign up message to server containing all data for signing up a new account
	 * @param account	the account to sign up
	 * @param password	the password of the account to sign up
	 * @param character	the character name of the account to sign up
	 */
	private void sendSignUpMessage(String account, String password, String character) {
		// sign up message
		MessageSignUp signUpMsg = new MessageSignUp(account, password, character);
		// wraps sign up message in message content class
		MessageContent msg = new MessageContent(this.getClass().toString(), signUpMsg, MessageContent.Type.SIGNUP);
		// sends sync message
		sendSyncMessage(msg);
	}

	/**
	 * Sends a login message to server containing all data for logging into a account
	 * @param account	the account to login with
	 * @param password	the password of the account to login with
	 */
	private void sendLoginMessage(String account, String password) {
		// login message
		MessageLogin loginMsg = new MessageLogin(account, password);
		// wraps login message in message content class
		MessageContent msg = new MessageContent(this.getClass().toString(), loginMsg, MessageContent.Type.LOGIN);
		// sends sync message
		sendSyncMessage(msg);
	}

	/**
	 * Latency tests
	 */
	private long svLoginTS;

	/**
	 * Sends messages to server disabling interaction until response is received
	 * @param msg	the message content object to send to server
	 */
	private void sendSyncMessage(MessageContent msg) {
		// disable stage actor touch
		Common.setTouchableStage(menuStage, Touchable.disabled);
		// use label to inform wait of server response
		infoLabel.setText(Main.getInstance().getLang().get("waitingServerResponse"));
		// sends message to server
		PucmonClient.getInstance().sendMessage(msg);
		// latency test
		svLoginTS = System.currentTimeMillis();
	}

	/**
	 * Subscribe to server to start listening
	 * for messages (should only be called once)
	 */
	@Override
	public void subscribeToServer() {
		ServerMessages.getInstance().subscribe(this);
	}

	/**
	 * Handles server messages
	 * @param msg   the message received form the server on the form of MessageContent
	 */
	@Override
	public void handleServerMessage(MessageContent msg) {
		if(Config.debug)
			Common.printLatency("Login", System.currentTimeMillis() - svLoginTS);
		// stores received message data
		MessageFlag svResponseFlag = msg.getFlag();
		Object svResponseObject = msg.getContent();
		MessageContent.Type svResponseType = msg.getType();

		// enables stage actor touch
		Common.setTouchableStage(menuStage, Touchable.enabled);
		// switches types of messages received
		switch(svResponseType) {
			case SIGNUP:
				handleSignUpMessage((MessageSignUp) svResponseObject, svResponseFlag); // handles sign up message
				break;
			case LOGIN:
				handleLoginMessage((MessageLogin) svResponseObject, svResponseFlag); // handles sign up message
				break;
			default:
				System.err.println("Unknown type of server response received: "+svResponseType);
				break;
		}
	}

	/**
	 * Handles messages received from server of LOGIN type
	 * @param loginMsg	the login message content received from server
	 * @param svFlag	the flag representing the result of request
	 */
	private void handleLoginMessage(MessageLogin loginMsg, MessageFlag svFlag) {
		// switches between sv flag response to update info label and login into game if everything is ok
		switch(svFlag) {
			case OKIDOKI:
				System.out.println("char: "+ loginMsg.getCharacter() + " | world: " + loginMsg.getWorldMap()
				+ " | level: " + loginMsg.getLevel() + " | exp: " + loginMsg.getExperience() +
				" | posx : " + loginMsg.getPosx() + " | posy: " + loginMsg.getPosy() + " | gold: " + loginMsg.getGold()
				+ " | InvINFO: " + loginMsg.getInventoryData() + " | EquipINFO: " + loginMsg.getEquipmentData());
				Main.getInstance().loginToGame(loginMsg);
				break;
			case ACCOUNT_PASSWORD_DO_NOT_MATCH:
				infoLabel.setText(Main.getInstance().getLang().get("loginAccPassMismatch"));
				break;
			case CHARACTER_ALREADY_ONLINE:
				infoLabel.setText(Main.getInstance().getLang().get("signUpCharOnline"));
				break;
			case GENERAL_ERROR:
				infoLabel.setText(Main.getInstance().getLang().get("generalError"));
				break;
			default:
				System.err.println("Unhandled server response flag: "+svFlag);
				break;
		}
	}

	/**
	 * Handles messages received from server of SIGNUP type
	 * @param signUpMsg	the sign up message content received from server
	 * @param svFlag	the flag representing the result of request
	 */
	private void handleSignUpMessage(MessageSignUp signUpMsg, MessageFlag svFlag) {
		// switches between sv flag response to update info label
		switch(svFlag) {
			case OKIDOKI:
				infoLabel.setText(Main.getInstance().getLang().get("signUpOk"));
				break;
			case ACCOUNT_TAKEN:
				infoLabel.setText(Main.getInstance().getLang().get("signUpAccTaken"));
				break;
			case CHARACTER_NAME_TAKEN:
				infoLabel.setText(Main.getInstance().getLang().get("signUpCharNameTaken"));
				break;
			case GENERAL_ERROR:
				infoLabel.setText(Main.getInstance().getLang().get("generalError"));
				break;
			default:
				System.err.println("Unhandled server response flag: "+svFlag);
				break;
		}
	}
}