package com.mygdx.game.states.game.standard.architecture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.game.states.game.standard.battle.Level;
import com.mygdx.game.states.game.standard.item.factory.Factory;
import com.mygdx.game.states.game.standard.sfx.SFX;
import com.mygdx.game.util.Config;

import java.util.ArrayList;

/**
 * Class that will hold game resources to be
 * loaded on game start and avoid reloading
 * of already loaded resources
 *
 * @author Pedro Sampaio
 * @since 1.1
 */
public class Resource {

    private static Resource instance = null; // singleton instance

    /**
     * Battle resources
     */
    public static ArrayList<Texture> battleBGs;       // array of battle backgrounds
    public static Texture battleHUD;                  // battle HUD texture
    public static Texture battleEnemyHUD;             // battle enemy HUD texture
    public static Music battleBGM;                    // battle bgm
    public static BitmapFont battleFont;              // battle font
    public static BitmapFont damageFont;              // battle text damage font

    /**
     * Item resources
     */
    public static Texture inventoryBG;              // the inventory background
    public static BitmapFont inventoryFont;         // inventory font
    public static Texture equipSheet;               // equipment spritesheet

    /**
     * GUI resources
     */
    public static Texture iconGame;         // icon buttons for game menus
    public static Texture messageBG;        // message background texture
    public static BitmapFont messageFont;   // message font
    public static Texture rankingBG;        // ranking background texture
    public static BitmapFont rankingTitleFont;   // ranking title font
    public static BitmapFont rankingFont;   // ranking font
    public static Texture lineSeparators;   // line separators texture
    public static Texture marketSkin;       // market skin
    public static BitmapFont marketFont;    // market font
    public static BitmapFont defaultFont;   // global font
    public static Skin gameSkin;            // game skin
    public static Texture pucMap;           // puc map texture
    public static Texture pMapBG;           // pervasive map (puc) background texture
    public static Texture pMapUI;           // pervasive map UI
    public static Texture locPhotos;        // location photos texture
    public static Texture sensorThumbs;     // sensor thumbnails texture
    public static BitmapFont gameFont;      // game font

    private Resource() {
        // defeats instantiation
    }

    public static Resource getInstance() {
        if(instance == null)
            instance = new Resource();

        return instance;
    }

    /**
     * Loads resources
     */
    public void create() {
        // TODO - add loading screen
        // loads battle resources
        loadBattleResources();
        // loads item resources
        loadItemResources();
        // load gui resources
        loadGUIResources();
        // load sfx resources
        loadSFXResources();
    }

    /**
     * loads battle resources
     */
    private void loadBattleResources() {
        // initializes array
        battleBGs = new ArrayList<Texture>();
        // loads battle backgrounds
        for(int i = 0 ; i < 5; i++)
            battleBGs.add(new Texture("imgs/battle/background/"+(i+1)+".png"));

        // loads battle hud texture
        battleHUD = new Texture("imgs/battle/hud/healthBar.png");

        // loads battle enemy hud texture
        battleEnemyHUD = new Texture("imgs/battle/hud/healthBarEnemy.png");

        // loads battle bgm
        battleBGM = Gdx.audio.newMusic(Gdx.files.internal("sfx/battle_bgm_aaron_krogh.mp3"));
        battleBGM.setVolume(1.0f);
        battleBGM.setLooping(true); // sets looping to true

        // loads battle fonts
        // loads battle text font
        battleFont = new BitmapFont();

        // loads game font
        gameFont = new BitmapFont();

        // loads damage font
        damageFont = new BitmapFont();

        // smooth damage font scaling
        damageFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear); // bilinear filtering

        // enable markup
        damageFont.getData().markupEnabled = true;

        // scales font
        damageFont.getData().setScale(Config.dmgTextScale);

        // set font color for battle
        battleFont.setColor(Config.battleFontColor);

        // smooth font scaling
        battleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear); // bilinear filtering

        // scales font
        battleFont.getData().setScale(Config.battleFontScale);

        // exp table debug
        for(int i = 1 ; i <= 100; i++)
            System.out.println("Level " + i + ": " + Level.expLog(i));
    }

    /**
     * loads item resources
     */
    private void loadItemResources() {
        // loads inventory background texture
        inventoryBG = new Texture("imgs/item/Inventory.png");

        // loads inventory font
        inventoryFont = new BitmapFont();

        // smooth inventory font scaling
        inventoryFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear); // bilinear filtering

        // enable markup
        inventoryFont.getData().markupEnabled = true;

        // loads equip sheet
        equipSheet = new Texture("imgs/item/equips.png");
    }

    /**
     * loads GUI resources
     */
    private void loadGUIResources() {
        // loads game icons texture
        iconGame = new Texture("imgs/hud/icons.png");

        // loads message background texture
        messageBG = new Texture("imgs/hud/message.png");
        // loads message font
        messageFont = new BitmapFont();
        // sets font color
        messageFont.setColor(Config.msgTextColor);
        // smooth message font scaling
        messageFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear); // bilinear filtering
        // enable markup
        messageFont.getData().markupEnabled = true;
        // scales font
        messageFont.getData().setScale(Config.msgTextScale);

        // loads ranking background texture
        rankingBG = new Texture("imgs/ranking/rankingBG.png");
        // loads ranking title font
        rankingTitleFont = new BitmapFont();
        // sets font color
        rankingTitleFont.setColor(Config.rankingTitleColor);
        // smooth ranking title font scaling
        rankingTitleFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear); // bilinear filtering
        // enable markup
        rankingTitleFont.getData().markupEnabled = true;
        // scales ranking title font
        rankingTitleFont.getData().setScale(Config.rankingTitleScale);
        // loads ranking font
        rankingFont = new BitmapFont();
        // sets font color
        rankingFont.setColor(Config.rankingTextColor);
        // smooth ranking font scaling
        rankingFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear); // bilinear filtering
        // enable markup
        rankingFont.getData().markupEnabled = true;
        // scales ranking font
        rankingFont.getData().setScale(Config.rankingTextScale);
        // loads line separators texture
        lineSeparators = new Texture("imgs/ranking/lineSeparators.png");

        // loads market skin texture
        marketSkin = new Texture("imgs/market/market.png");
        // loads market font
        marketFont = new BitmapFont();
        // sets font color
        marketFont.setColor(Config.marketTextColor);
        // smooth market font scaling
        marketFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear); // bilinear filtering
        // enable markup
        marketFont.getData().markupEnabled = true;
        // scales market font
        marketFont.getData().setScale(Config.marketTextScale);

        // loads the skin for the game UI
        gameSkin = new Skin(Gdx.files.internal("skin/freezing-ui.json"));

        // loads puc map texture
        pucMap = new Texture("imgs/puc/map_pucrio_1.png");

        // loads pervasive map (puc) background texture
        pMapBG = new Texture("imgs/puc/pMapBG_1.png");

        // loads pervasive map UI texture
        pMapUI = new Texture("imgs/puc/PMapUI.png");

        // loads locations photos texture
        locPhotos = new Texture("imgs/puc/photos.png");

        // loads sensor thumbnails texture
        sensorThumbs = new Texture("imgs/sensors/s_thumbs.png");

        // loads global font
        defaultFont = new BitmapFont();
        // sets font color
        defaultFont.setColor(Config.globalFontColor);
        // smooth global font scaling
        defaultFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear); // bilinear filtering
        // enable markup
        defaultFont.getData().markupEnabled = true;
        // scales global font
        defaultFont.getData().setScale(Config.globalFontScale);
    }

    /**
     * Load SFX resources
     */
    private void loadSFXResources() {
        // loads sound effects and bgms of game
        SFX.getInstance().loadSFX();
    }

    /**
     * Disposes resources
     */
    public void dispose() {
        disposeBattleResources();
        disposeItemResources();
        disposeGUIResources();
        disposeSFXResources();
    }

    /**
     * disposes battle resources
     */
    private void disposeBattleResources() {
        for(int i = 0 ; i < 5; i++)
            battleBGs.get(i).dispose(); // disposes battle backgrounds
        battleBGM.dispose(); // disposes battle bgm
        battleHUD.dispose(); // disposes battle hud texture
        battleEnemyHUD.dispose(); // disposes battle enemy hud texture
        battleFont.dispose(); // disposes battle text font
        damageFont.dispose(); // disposes damage text font
    }

    /**
     * dispose item resources
     */
    private void disposeItemResources() {
        inventoryBG.dispose(); // disposes inventory background texture
        Factory.getInstance().dispose(); // dispose item sheets
    }

    /**
     * disposes GUI resources
     */
    private void disposeGUIResources() {
        iconGame.dispose();     // disposes icons texture
        messageBG.dispose();    // disposes message background texture
        messageFont.dispose();  // disposes message font
        rankingBG.dispose();    // disposes ranking background texture
        rankingFont.dispose();  // disposes ranking text font
        rankingTitleFont.dispose(); // disposes ranking title font
        lineSeparators.dispose(); // disposes line separators texture
        marketFont.dispose(); // disposes market font
        marketSkin.dispose(); // disposes market skin texture
        gameSkin.dispose(); // disposes game skin
        pucMap.dispose(); // disposes puc map texture
        pMapBG.dispose(); // disposes pervasive map background texture
        pMapUI.dispose(); // disposes pervasive map UI
        locPhotos.dispose(); // disposes location photos texture
        sensorThumbs.dispose(); // disposes sensor thumbs texture
    }

    /**
     * Disposes SFX resources
     */
    private void disposeSFXResources() {
        SFX.getInstance().dispose();
    }
}
