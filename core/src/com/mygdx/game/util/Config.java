package com.mygdx.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * configurations module
 */
public class Config {

    /******************
     * Game constants *
     ******************/
    public static boolean debug = true;		// option for debug messages to be shown
    public static float baseWidth = 480; // the base width of the app
    public static float baseHeight = 800; // the base height of the app
    public static float defaultZoom = 2f; // default zoom of camera
    public static float zoomSpeed = 0.025f; // speed of zooming
    public static float maxZoom = 3.5f;     // maximum game world zoom
    public static float minZoom = 1.5f;   // minimum game world zoom
    public static float playerBaseSpeed = 150f; // player speed (without equipment effects)
    public static float playerSpeed = 150f; // player speed
    public static float saveTime = 5f; // time between each save of player progress
    public static float responseTimeout = 5f; // maximum time to wait for server response
    public static int initialMapID = 2; // initial map id
    public static int initialMapPosx = 364; // initial position x on initial map
    public static int initialMapPosy = 2208; // initial position y on initial map
    public static Color globalFontColor = Color.BLACK; // global font color
    public static float globalFontScale = 1.35f; // global font scale

    /*********************
     * Text Message constants *
     *********************/
    public static float msgTextScale = 1.45f; // scale for message texts
    public static Color msgTextColor = Color.DARK_GRAY; // color of message texts
    public static float msgScaleY = 0.8f; // scale for messages in Y axis
    public static float msgScaleX = 0.8f; // scale for messages in X axis
    public static float msgCharSpeed = 0.024f; // the time between each character drawn in message (the smaller the faster)
    public static int msgCharSpeedUp = 15; // the number of chars drawn each interval on speed up touch (the bigger, the faster the text)
    public static float msgTextPadX = 40f; // padding of message text in X axis
    public static float msgTextPadY = 25f; // padding of message text in Y axis
    public static float msgArrowScaleW = 1f; // the width scale of message arrow icon
    public static float msgArrowScaleH = 1f; // the height scale of message arrow icon
    public static float msgArrowSpeed = 12f; // speed of message arrow animation
    public static float msgArrowMoveLength = 6f; // length of message arrow animatino movement

    /***********************
     * Inventory constants *
     ***********************/
    public static Vector2 inventoryPos = new Vector2(baseWidth/2, baseHeight/2);  // inventory GUI position when opened (centered)
    public static float inventoryScale = 0.75f;    // scaling of inventory GUI
    public static float inventoryPlayerScale = 1.66f; // scaling of player sprite in inventory profile
    public static float inventoryStatsScale = 1.33f; // scaling for stats font in inventory
    public static Color inventoryStatColor = Color.LIGHT_GRAY; // color for stats names
    public static Color inventoryStatValueColor = Color.WHITE; // color for stats value
    public static float inventoryGoldScale = 1.18f; // scale of gold text in inventory
    public static float inventoryLabelScale = 1.51f; // scale of inventory label text in inventory
    public static float inventoryProfileScale = 1.35f; // scale of player profile data text in inventory
    public static float inventoryProfileLabelScale = 1.42f; // scale of profile label in inventory
    public static int inventoryMaxItemsX = 5; // maximum number of items inventory can hold in X axis
    public static int inventoryMaxItemsY = 2; // maximum number of items inventory can hold in Y axis
    public static int inventoryWidth    = 630;  // width of inventory in source image
    public static int inventoryHeight = 619;    // height of inventory in source image
    public static int inventoryItemSlotW = 58; // the width of item slot in inventory source image
    public static int inventoryItemSlotH = 60; // the height of item slot in inventory source image
    public static int inventoryFirstSlotX = 23; // inventory first slot x coord in source image
    public static int inventoryFirstSlotY = 140; // inventory first slot y coord in source image
    public static int inventoryPadX = 71;   // inventory padding between slots in x axis
    public static int inventoryPadY = 76;   // inventory padding between slots in y axis
    public static int inventorySwitchW = 36; // the width of Switch in inventory source image
    public static int inventorySwitchH = 30; // the height of Switch in inventory source image
    public static int inventoryFirstSwitchX = 335; // inventory first Switch x coord (inverted - right most pixel)
    public static int inventoryFirstSwitchY = inventoryHeight - 405; // inventory first Switch y coord
    public static int inventorySwitchPadX = inventorySwitchW + 3;   // inventory padding between Switches in x axis
    public static float inventoryPageNumberScale = 1.21f; // the scale of numbers in page switches
    public static Color inventoryPageNumberColor = Color.WHITE; // the color of numbers in page switches
    public static int inventoryNPages = 6;  // number of pages in inventory items
    public static float inventoryMinVelocityPageSwitch = 450f; // minimum velocity to consider in a fling for page switch
    public static float inventorySelItemDescScale = 1.15f; // scaling for item description text in inventory
    public static float inventorySelItemNameScale = 1.28f; // scaling for item name text in inventory
    public static float inventorySelItemSubInfoScale = 1.20f; // scaling for item sub info text in inventory
    public static Color inventoryPossibleSlotsColor = Color.GOLDENROD; // the color of the frame that surrounds possible slot of a selected equip

    /********************
     * Battle constants *
     ********************/
    public static float battleChance = 24.91f; // chance of spawning a battle while player is walking
    public static float timeToTryBattleSpawn = 1f; // time between each try of spawning a battle
    public static float battleSprScale = 2.5f;  // the scale to apply to player sprites on battle
    public static float minDeltaAttack = 35f; // minimum delta time between attacks to consider for attack anim speed scaling
    public static float maxDeltaAttack = 185f; // maximum delta time between attacks to consider for attack anim speed scaling
    public static float attackSpeedFactor = 11000f; // attack speed factor that applies delta anim attack(the bigger, the faster)
    public static float btMinDeltaTouch = 66f; // minimum delta time to consider touch input in battle (in ms)
    public static Vector2 playerHUDPos = new Vector2(5, 790); // location of player hud in battle
    public static float playerHUDScale = 1.79f; // scale of player hud in battle
    public static Vector2 enemyHUDPos = new Vector2(475, 700); // location of enemy hud in battle
    public static float enemyHUDScale = 1.79f; // scale of enemy hud in battle
    public static float battleFontScale = 1f; // scale of battle text font
    public static Color battleFontColor = Color.WHITE; // color of battle text font
    public static Color enemyDamagedColor = new Color(0.6f, 0.1f, 0.1f, 1.0f); // enemy damaged tint color
    public static Color playerDamagedColor = new Color(0.1f, 0.5f, 0.2f, 1.0f); // player damaged tint color
    public static float dmgColorSpeed = 3f; // the damage color tint speed to tend to white
    public static float playerHeadScale = 0.66f; // scale applied to player head in player battle hud
    public static float enemyHeadScale = 0.36f; // scale enemy visualization in battle hud
    public static int maxEnemyNameChars = 13; // maximum number of characters in enemy generated name
    public static float minAutoAtkSpeed = 0.075f; // the minimum attack speed for both player and enemy auto attacks
    public static float shrinkFactor = 1f; // factor to apply to shrink speed of loser (the bigger, the faster)
    public static int maxNumberOfDrops = 5; // maximum number of drops of a defeated enemy
    public static int maxDropTries = 6; // maximum number of drop tries for each type of drop

    /*********************************
     * New Player initial attributes *
     *********************************/
    public static int npMaxHealth = 45; // the maximum health of an entity
    public static int npAttack = 8; // the attack attribute of an entity
    public static int npDefense = 5; // the defense attribute of an entity
    public static float npCriticalChance = 3.25f; // the chance of hitting a critical hit
    public static float npCriticalMultiplier = 3f; // the multiplier of damage in case of critical hit
    public static int npAutoAttack = 3; // the auto attack attribute of an entity
    public static float npAutoSpeed = 2; // the auto attack speed attribute of an entity
    public static int npLevel = 1; // the level of a new player
    public static long npExp = 0;    // the experience of a new player

    /*******************
     * Enemy constants *
     ******************/
    public static float eScaleMinFactor = 0.85f; // enemy min difficulty scaling based on player attributes
    public static float eScaleMaxFactor = 1.9f; // enemy max difficulty scaling based on player attributes
    public static float eBaseAutoAtk = 0.75f;    // base of enemy attack
    public static float eBaseMaxHealth = 100f;    // base of enemy max health
    public static float eBaseDefense = 8f;      // base of enemy defense
    public static float eBaseAtkSpeed = 0.11f;     // base of enemy attack speed (the smaller the faster)
    public static float eScaleLevel = 1.11f;        // scaling of enemy level
    public static float eMaxHealthScale = 16.5f;   // scaling of max health changes based on level
    public static float eAtkScale = 0.12f;   // scaling of attack changes based on level
    public static float eDefScale = 2.1f;   // scaling of defense changes based on level
    public static float eAtkSpeedScale = 0.001f;  // scaling of enemy attack speed changes based on level
    public static float eScaleLevelFactor = 2.36f;  // the scale factor enemy level has on other attributes
    public static float eSprInitialScale = 0.9f; // initial sprite scale of enemies in battle(lvl 1 enemy)
    public static float eSprLevelScale = 0.002f;  // progression of sprite scaling based on enemy level

    /*************************
     * Damage Text constants *
     *************************/
    public static int dmgMoveDistance = 120;             // the amount of distance a damage texts moves before being deleted
    public static float dmgTextScale = 2f;                 // global scale of damage text
    public static float dmgTextSpeed = 120f;             // speed of movement for damage texts

    /*************************
     * Reward rate constants *
     *************************/
    public static int baseGoldAmount = 1;       // the gold base amount to be applied with level and rate factors
    public static int baseExpAmount = 2;       // the exp base amount to be applied with level and rate factors
    public static float levelGoldFactor = 0.65f;// the weight level has as factor of gold dropping
    public static float levelExpFactor = 0.41f; // the weight level has as factor of exp rewarding
    public static float levelEquipmentDropFactor = 0.015f; // the weight equipment level has as factor of drop (the smaller, the harder in bigger levels)
    public static float baseExpRate = 1f;       // base exp rate global modifier (without equipment effects, change this one)
    public static float baseGoldRate = 1f;      // base gold rate global modifier (without equipment effects, change this one)
    public static float baseDropRate = 0.20f;    // base drop rate global modifier (without equipment effects, change this one)
    public static float expRate = 1f;           // exp rate global modifier
    public static float goldRate = 1f;          // gold rate global modifier
    public static float dropRate = 1f;          // drop rate global modifier

    /****************************
     * Level formulas constants *
     ****************************/
    /**
     * Formula Observation:
     * If the level gap rises too fast for your taste increase constA,
     * decrease constB if you want the initial level gap to be higher,
     * and finally set constC ~= exp((1-constB)/constA),
     * in order to properly start at level 1.
     */
    public static float logConstA = 6.2f;   // formula const A
    public static float logConstB = -26f;   // formula const B
    public static float logConstC = 78f;   //  formula const C

    /******************************
     * Level attributes constants *
     ******************************/
    public static float levelUpAtkInc = 5.30f; // level up attack increment
    public static float levelUpDefInc = 1.2f; // level up defense increment
    public static float levelUpHealthInc = 3.52f; // level up max health increment
    public static float levelUpCritInc = 0.01f; // level up critical increment
    public static float levelUpCritChanceInc = 0.02f; // level up critical chance increment
    public static float levelUpAutoAtkInc = 1.45f; // level up auto attack increment
    public static float levelUpAutoSpdInc = 0.01f; // level up auto attack speed increment
    public static float levelUpScaleLevel = 0.98f; // the affect level has on increments

    /******************
     * Item constants *
     ******************/
    public static int maxStackOfItems = 99;         // the maximum numbers of a item in a stack
    public static float itemLevelFactor = 0.15f;    // the weight of level to apply to item effects (extra value)
    public static float scaleItemLevelMinFactor = 0.12f;   // item level minimum factor of scale
    public static float scaleItemLevelMaxFactor = 1.2f;    // item level maximum factor of scale
    public static Color legendaryItemColor = Color.ORANGE; // legendary item text color
    public static Color rareItemColor = Color.YELLOW; // rare item text color
    public static Color uncommonItemColor = Color.TEAL; // uncommon item color
    public static Color normalItemColor = Color.GRAY; // normal item color

    /*********************
     * Ranking constants *
     *********************/
    public static float rankingTitleScale = 1.50f; // scale for ranking title texts
    public static Color rankingTitleColor = Color.BLACK; // color of ranking title texts
    public static float rankingTextScale = 1.35f; // scale for ranking texts
    public static Color rankingTextColor = Color.DARK_GRAY; // color of ranking texts
    public static float rankingScale = 1.1f;  // scale of ranking
    public static int   rankingNTopPlayers = 100; // number of positions displayed on ranking
    public static float rankingSensitivityY = 30f; // sensitivity of pan gesture on Y axis in ranking

    /********************
     * Market constants *
     ********************/
    public static float marketTextScale = 1.35f; // scale for market texts
    public static Color marketTextColor = new Color(0.15f, 0.15f, 0.15f, 1.0f); // color of market texts
    public static float marketScale = 0.9f;  // scale of market
    public static float marketSensitivityY = 30f; // sensitivity of pan gesture on Y axis in market
    public static float marketMinVelocityTabSwitch =  1256f; // minimum velocity to consider in a fling for tab switch
    public static float marketItemHolderScale = 1f; // scale of market item holder component
    public static float marketGapBetweenItemsY = 5f; // gap between each market item in Y axis
    public static float marketInfoStrTime = 5f; // duration of info messages in market
    public static Color marketInfoColor = Color.ORANGE; // color of market info message
    public static Color marketClippingFrameColor = Color.SKY; // color of market clipping frame drawing
    public static float marketTabScaleX = 1f; // scale of tab texture in X axis
    public static float marketTabScaleY = 1f; // scale of tab texture in Y axis
    public static float marketButtonsScaleX = 1f; // scale of buttons texture in X axis
    public static float marketButtonsScaleY = 1f; // scale of buttons texture in Y axis
    public static int marketSellMaxDigits = 12; // maximum number of digits in sell input

    /***************************
     * Pervasive Map Constants *
     ***************************/
    public static float pervasiveMapDefaultZoom = 0.8f; // default zoom of pervasive map visualization
    public static float pervasiveMapZoomSpeed = 0.025f; // pervasive map zoom speed
    public static float pervasiveMapMinZoom = 0.8f; // pervasive minimum possible zoom
    public static float pervasiveMapMaxZoom = 3.0f; // pervasive map maximum possible zoom
    public static float pervasiveMapSensitivityX = 18f; // pervasive map panning sensitivity on X axis
    public static float pervasiveMapSensitivityY = 18f; // pervasive map panning sensitivity on Y axis
    public static float pervasiveMapGapY = 80f; // the gap on Y axis that map render will ignore (below)

    /**********************
     * Location Constants *
     **********************/
    public static float locationDialogScaleX = 1f; // the scale of the location dialog on X axis
    public static float locationDialogScaleY = 1.66f; // the scale of the location dialog on Y axis
    public static float locationPictureScale = 0.66f;  // the scale of the location picture in dialog
    public static float locationNameScale = 1.1f; // the scale of the location name text
    public static Color locationFontColor = Color.DARK_GRAY; // the color of location text
    public static float locationPicDescGapX = 10f; // the gap between picture and description content
    public static float locationPicMGapY = 20f; // the gap between picture and mission content
    public static float locationSensitivityY = 30f; // the sensitivity on y axis of panning interaction
    public static float locationSensitivityX = 30f; // the sensitivity on x axis of panning interaction
    public static float locationCloseBtnScale = 0.75f; // the scale of location dialog close button
    public static float locationCloseBtnGapScale = 1.42f; // the gap of close button in relation to location dialog frontier

    /*********************
     * Mission Constants *
     *********************/
    public static String missionUnfinished = "#211f20"; // the color hex of unfinished mission text
    public static String missionFinished = "#726d6f"; // the color hex of unfinished mission text
    public static String missionRepeatableReady = "#15660c"; // the color hex of repeatable ready mission
    public static String missionRepeatableNotReady = "#72121f"; // the color hex of repeatable not ready mission

    /********************
     * Sensor Constants *
     ********************/
    public static float sensorTime = 0.5f; // time between sensor data collection updates
    public static float sensorTimeLimit = 15f; // time in seconds to consider communication-less sensors in list
    public static float sensorSignalLimit = -90f; // minimum signal strenght to consider sensor in search
    public static float sensorUIScale = 0.9f;  // scale of sensor UI
    public static float SensorUIGapBetweenEntriesY = 5f; // gap between each market item in Y axis
    public static int sensorCodeMaxLength = 15; // maximum number of characters in sensor code
    public static int sensorInputMaxLength = 15; // maximum number of characters in sensor input
    public static Color sensorUIClippingFrameColor = Color.SKY; // color of sensor UI clipping frame drawing
    public static float sensorUIEntryHolderScale = 1f; // scale of sensor ui entry holder component
    public static Color sensorUIInfoColor = Color.ORANGE; // color of sensor ui info message
    public static Color sensorUITextColor = new Color(0.15f, 0.15f, 0.15f, 1.0f); // color of sensor ui texts
    public static float sensorUIInfoStrTime = 5f; // duration of info messages in sensor UI
    public static float sensorUITextScale = 1.35f; // scale for sensor ui texts
    public static float sensorUISensitivityY = 30f; // sensitivity of pan gesture on Y axis in sensor UI
    public static float sensorAccelTimeInterval = 0.352f; // time between each accelerometer valid input
    public static float sensorMinAccel  = 398f; // minimum acceleration of accelerometer to consider for sensor search
    public static Color sensorUnavailableColor = Color.GRAY; // color of tint in unavailable sensors
    public static Color sensorWeakestColor = new Color(219/255f, 66/255f, 10/255f, 1.0f);
    public static Color sensorWeakColor = new Color(229/255f, 146/255f, 39/255f, 1.0f);
    public static Color sensorModerateColor = new Color(242/255f, 213/255f, 50/255f, 1.0f);
    public static Color sensorStrongColor = new Color(215/255f, 232/255f, 32/255f, 1.0f);
    public static Color sensorStrongestColor = new Color(137/255f, 219/255f, 24/255f, 1.0f);
}
