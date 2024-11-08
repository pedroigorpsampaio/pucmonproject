package com.mygdx.game.states.game.standard.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.battle.Level;
import com.mygdx.game.states.game.standard.item.Equipment;
import com.mygdx.game.states.game.standard.item.Item;
import com.mygdx.game.states.game.standard.physics.Collider;
import com.mygdx.game.states.game.standard.player.Player;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Represents player's inventory that
 * stores player's golds and items
 *
 * @author  Pedro Sampaio
 * @since   1.1
 */
public class Inventory {
    private long gold; // the amount of gold player has

    // the array containing the current worn equipments by player
    Equipment[] wornEquipment = new Equipment[Equipment.Slot.values().length];

    // the grid of items present in player's inventory are stored in pages
    ArrayList<Item[][]> pages;

    // the mask of item slot triggers to help identify player touch
    Collider[][] itemSlotTriggers;

    // the mask of inventory pages triggers to manage page changes
    Collider[] pageTriggers;

    // the mask of worn equipment triggers to equip changes
    Collider[] wornEquipmentTriggers;

    // the current selected item of inventory
    private Item selectedItem;

    // saves selected item coordinates for optimizations for item slot management
    private Vector2 selectedItemIdx;

    // also saves selected item page for the same reasons
    private int selectedItemPage;

    // if item selected was from equipment, store equipment position
    private Equipment.Slot selectedItemSlot;

    // if item selected is from inventory
    private boolean fromInventory;

    // inventory items selected page
    private int invSelectedPage;

    // Inventory font
    BitmapFont inventoryFont;

    /**
     * Texture regions
     */
    private TextureRegion invTex;               // inventory background TR
    private TextureRegion selectedSwitch;       // selected switch TR
    private TextureRegion normalSwitch;         // normal switch TR
    private TextureRegion selectionFrame;       // selection frame TR
    private TextureRegion possibleSlotFrame;    // possible slot frame TR

    /**
     * Glyph layouts
     */
    private GlyphLayout invDefaultGL;       // inventory default glyph layout

    /**
     * Language static texts
     */
    private String pHealthText;     // max health text
    private String pExpText;        // experience text
    private String pAtkText;        // attack text
    private String pDefText;        // defense text
    private String pAutoAtkSpdText; // auto attack speed text
    private String pAutoAtkText;    // auto attack text
    private String pCritChanceText; // critical chance text
    private String pCritMultText;   // critical multiplier text
    private String invLabText;      // inventory string
    private String profLabelText;   // profile string

    /**
     * Sprites
     */
    private Sprite itemSpr;     // items sprites
    private Sprite frameSpr;    // frame sprites
    private Sprite switchSpr;   // switch sprites

    /**
     * Inventory constructor
     * @param gold  the amount of gold player has
     * @param pages the pages of items of player's inventory (in case of saved progress)
     */
    public Inventory (long gold, ArrayList<Item[][]> pages) {
        this.gold = gold;
        selectedItem = null; // initially no items are selected
        selectedItemSlot = null; // so is the selected item slot in case of equipment selection
        selectedItemIdx = null; // and selected item indexes
        this.invSelectedPage = 0; // initially selected page is the first one
        this.selectedItemPage = 0; // initially no item is selected
        this.pages = pages;
        // initializes item slot trigger mask
        itemSlotTriggers = new Collider[Config.inventoryMaxItemsY][Config.inventoryMaxItemsX];
        // builds item slot triggers
        buildItemSlotTriggers();
        // initializes page switches trigger mask
        pageTriggers = new Collider[Config.inventoryNPages];
        // builds page switches triggers
        buildPageTriggers();
        // initializes worn equipments trigger mask
        wornEquipmentTriggers = new Collider[Equipment.Slot.values().length];
        // builds page switches triggers
        buildWornEquipmentTriggers();
        // loads texture regions
        loadTextureRegions();
        // load glyph layouts
        loadGlyphLayouts();
        // load language texts
        loadLangTexts();
        // load sprites
        loadSprites();
    }

    /**
     * Load sprites
     */
    private void loadSprites() {
        itemSpr = new Sprite();
        frameSpr = new Sprite();
        switchSpr = new Sprite();
    }

    /**
     * loads language texts
     */
    private void loadLangTexts() {
        // max health text
        pHealthText = Main.getInstance().getLang().format("statsMaxHealth");

        // experience text
        pExpText = Main.getInstance().getLang().format("statsExp");

        // attack text
        pAtkText = Main.getInstance().getLang().format("statsAtk");

        // defense text
        pDefText = Main.getInstance().getLang().format("statsDef");

        // auto attack speed text
        pAutoAtkSpdText = Main.getInstance().getLang().format("statsAutoAtkSpd");

        // auto attack text
        pAutoAtkText = Main.getInstance().getLang().format("statsAutoAtk");

        // critical chance text
        pCritChanceText = Main.getInstance().getLang().format("statsCritChance");

        // critical multiplier text
        pCritMultText = Main.getInstance().getLang().format("statsCritMult");

        // inventory string
        invLabText = Main.getInstance().getLang().get("inventoryStr");

        // profile string
        profLabelText = Main.getInstance().getLang().get("profileStr");
    }

    /**
     * Loads glyph layouts
     */
    private void loadGlyphLayouts() {
        // gets font
        inventoryFont = Resource.inventoryFont;

        // inventory default glyph layout
        invDefaultGL = new GlyphLayout(inventoryFont, "defaultLayout");
    }

    /**
     * Loads inventory texture regions
     */
    private void loadTextureRegions() {
        // inventory texture region
        invTex = new TextureRegion(Resource.inventoryBG, 0,0,Config.inventoryWidth, Config.inventoryHeight);

        // gets selected switch region
        selectedSwitch = new TextureRegion(Resource.inventoryBG, 0, 620,
                Config.inventorySwitchW, Config.inventorySwitchH);
        // gets normal switch region
        normalSwitch = new TextureRegion(Resource.inventoryBG, 38, 620,
                Config.inventorySwitchW, Config.inventorySwitchH);

        // selection frame region
        selectionFrame = new TextureRegion(Resource.inventoryBG, 0, 651, 74, 76);

        // possible slot frame
        possibleSlotFrame = new TextureRegion(Resource.inventoryBG, 78, 621, 53, 62);
    }

    /**
     * Gets items from current selected page
     * @return the grid of items present in current selected page
     */
    private Item[][] getItems() {
        return pages.get(invSelectedPage);
    }

    /**
     * Gets items from page received in parameter
     * Clamps page parameter to avoid getting out of bounds
     * @param page the inventory page to get items from
     * @return the grid of items present in page received in parameter
     */
    private Item[][] getItems(int page) {
        // clamp page value
        if(page < 0)
            page = 0;
        if(page > Config.inventoryNPages - 1)
            page = Config.inventoryNPages - 1;
        return pages.get(page);
    }

    /**
     * Builds triggers that will help to detect
     * player inventory touches and management
     */
    private void buildItemSlotTriggers() {
        // gets inventory information
        TextureRegion invTex = new TextureRegion(Resource.inventoryBG, 0,0, Config.inventoryWidth, Config.inventoryHeight);
        float invX = Config.inventoryPos.x - (invTex.getRegionWidth()* Config.inventoryScale)/2f;
        float invY = Config.inventoryPos.y - (invTex.getRegionHeight()* Config.inventoryScale)/2f;
        float invW = invTex.getRegionWidth()* Config.inventoryScale;
        float invH = invTex.getRegionHeight()* Config.inventoryScale;

        // iterates through mask of item slots to populate it
        for(int i = 0; i < itemSlotTriggers.length; i++) {
            for (int j = 0; j < itemSlotTriggers[i].length; j++) {
                float baseItemW = invW / (Config.inventoryWidth/Config.inventoryItemSlotW); // the base trigger width
                float baseItemH = invH / (Config.inventoryHeight/Config.inventoryItemSlotH); // the base trigger height
                float baseItemX = invX + Config.inventoryFirstSlotX * Config.inventoryScale; // the base trigger X
                float baseItemY = invY + Config.inventoryFirstSlotY * Config.inventoryScale; // the base trigger Y
                float padX = Config.inventoryPadX * Config.inventoryScale; // padding on each slot in X axis
                float padY = -Config.inventoryPadY * Config.inventoryScale; // padding on each slot in Y axis
                // creates trigger collider with gathered information
                itemSlotTriggers[i][j] = new Collider(baseItemX+(j*padX),baseItemY+(i*padY),
                                            baseItemW,baseItemH, false, this);
            }
        }
    }

    /**
     * Builds triggers that will help to detect
     * player page switches touches and management
     */
    private void buildPageTriggers() {
        // gets inventory information
        TextureRegion invTex = new TextureRegion(Resource.inventoryBG, 0,0, Config.inventoryWidth, Config.inventoryHeight);
        float invX = Config.inventoryPos.x - (invTex.getRegionWidth()* Config.inventoryScale)/2f;
        float invY = Config.inventoryPos.y - (invTex.getRegionHeight()* Config.inventoryScale)/2f;
        float invW = invTex.getRegionWidth()* Config.inventoryScale;
        float invH = invTex.getRegionHeight()* Config.inventoryScale;

        // iterates through mask of page switches to populate it
        for(int i = pageTriggers.length-1; i >= 0; i--) {
            float targetSwitchW = invW / (Config.inventoryWidth/Config.inventorySwitchW); // the target Switch width
            float targetSwitchH = invH / (Config.inventoryHeight/Config.inventorySwitchH); // the target Switch height
            float targetSwitchX = invX + Config.inventoryFirstSwitchX * Config.inventoryScale; // the target Switch X
            float targetSwitchY = invY + Config.inventoryFirstSwitchY * Config.inventoryScale; // the target Switch Y
            float padX = -Config.inventorySwitchPadX * Config.inventoryScale; // padding on each Switch in X axis
            // creates trigger collider with gathered information
            pageTriggers[i] = new Collider(targetSwitchX+(padX*(Config.inventoryNPages-1-i)),targetSwitchY,
                                                targetSwitchW,targetSwitchH, false, this);
        }
    }

    /**
     * Builds triggers that will help to detect
     * player worn equipment touches and management
     */
    private void buildWornEquipmentTriggers() {

        // gets inventory information
        TextureRegion invTex = new TextureRegion(Resource.inventoryBG, 0,0, Config.inventoryWidth, Config.inventoryHeight);
        float invX = Config.inventoryPos.x - (invTex.getRegionWidth()* Config.inventoryScale)/2f;
        float invY = Config.inventoryPos.y - (invTex.getRegionHeight()* Config.inventoryScale)/2f;
        float invW = invTex.getRegionWidth()* Config.inventoryScale;
        float invH = invTex.getRegionHeight()* Config.inventoryScale;

        // sets initial values
        float equipW = invW / 12.85714f; // the target equipment width
        float equipH = invH / 10.67241f; // the target equipment height
        float equipX = invX + 28.1f * Config.inventoryScale; // the base target equipment X
        float equipY = invY + 477f * Config.inventoryScale; // the base target equipment Y
        float padY = -equipH - 11.11f * Config.inventoryScale; // Y padding in slots
        float padX = equipW + 237.1f * Config.inventoryScale; // X padding from left to right slots

        // iterates to build triggers
        for(int i = 0; i < 4 ; i++) {
            for(int j = 0; j < 2; j++) {
                float targetX = equipX + (j*padX); // the target X
                float targetY = equipY + (i*padY); // the target Y
                wornEquipmentTriggers[i*2+j] = new Collider(targetX,targetY,equipW,equipH, false, this);
            }
        }
    }

    /**
     * Equips an equipment into its correspondent equipment slot
     * If it is not empty trades place with new equipment
     * @param equipment the equipment to be equipped by player
     */
    public void equip(Equipment equipment)
    {
        // ignores if requipment received is null
        if(equipment == null) {
            System.err.println("Trying to equip a null equipment!");
            return;
        }
        // if empty, just equip and remove from inventory
        if(wornEquipment[equipment.getEquipmentSlot().ordinal()] == null) {
            wornEquipment[equipment.getEquipmentSlot().ordinal()] = equipment;
            removeItem(equipment);
        }
        else {
            // gets old equipment
            Equipment oldEquipment = wornEquipment[equipment.getEquipmentSlot().ordinal()];
            // equip new equipment
            wornEquipment[equipment.getEquipmentSlot().ordinal()] = equipment;
            // put old equipment in new equipment inventory slot
            Vector3 result = searchItem(equipment);
            getItems((int)result.z)[(int)result.x][(int)result.y] = oldEquipment;
        }
    }

    /**
     * Removes an equipment from its equipment slot
     * if there is a equipment on equipment slot received in parameter
     * @param slot the equipment slot to remove equipment from
     */
    public void unEquip(Equipment.Slot slot)
    {
        // if slot not empty, removes equipment from slot
        if(wornEquipment[slot.ordinal()] != null) {
            // removes current item
            wornEquipment[slot.ordinal()] = null;
        }
    }

    /**
     * Gets current equipment equipped in received slot
     * @param equipmentSlot the equipment slot to be checked
     * @return  the equipment currently worn in the equipment slot received via parameter
     */
    public Equipment get(Equipment.Slot equipmentSlot)
    {
        return wornEquipment[equipmentSlot.ordinal()];
    }

    /**
     * Adds gold to player's current gold amount
     * @param amount the amount of gold to be added
     */
    public void addGold(long amount) {gold+=amount;}

    /**
     * Removes gold from player's current gold amount
     * @param amount the amount of gold to be removed
     */
    public void removeGold(long amount) {
        gold-=amount;
        if(gold < 0) gold = 0;
    }

    /**
     * Getters and setters
     */

    public long getGold() {
        return gold;
    }

    public Item getSelectedItem() {return selectedItem;}

    public void setSelectedItem(Item selectedItem) {this.selectedItem = selectedItem;}

    public Equipment[] getWornEquipment() {return wornEquipment;}

    public ArrayList<Item[][]> getPages() {return pages;}

    public void setWornEquipment(Equipment[] wornEquipment) {this.wornEquipment = wornEquipment;}

    /**
     * Creates a new inventory for player
     * @return the newly created inventory
     */
    public static Inventory createNewInventory() {
        // new inventory with 0 gold and no items
        ArrayList<Item[][]> pages = new ArrayList<Item[][]>();
        // adds n config pages
        for(int i = 0 ; i < Config.inventoryNPages; i++)
            pages.add(new Item[Config.inventoryMaxItemsY][Config.inventoryMaxItemsX]);
        return new Inventory(0, pages);
    }

    /**
     * Renders inventory on screen
     * @param batch the batch to draw inventory
     * @param player the player reference for getting sprite and stats
     */
    public void render(SpriteBatch batch, Player player) {
        // draws inventory background
        float invX = Config.inventoryPos.x - (invTex.getRegionWidth()* Config.inventoryScale)/2f;
        float invY = Config.inventoryPos.y - (invTex.getRegionHeight()* Config.inventoryScale)/2f;
        float invW = invTex.getRegionWidth()* Config.inventoryScale;
        float invH = invTex.getRegionHeight()* Config.inventoryScale;
        batch.draw(invTex, invX, invY, invW, invH);

        // draw inventory page switches
        renderPageSwitches(batch, invX, invY, invW, invH);

        // player texture region
        TextureRegion playerSprite = new TextureRegion(player.getAnimator().getCurrentSprite());

        // draws player on profile part of inventory
        float pWidth = playerSprite.getRegionWidth() * Config.inventoryScale * Config.inventoryPlayerScale;
        float pHeight = playerSprite.getRegionHeight() * Config.inventoryScale * Config.inventoryPlayerScale;
        float pSprX = invX + (invW/3.00f) - pWidth/2;
        float pSprY = invY + (invH/1.65f) - pHeight/2;
        if(playerSprite.isFlipY())
            playerSprite.flip(false, true);
        batch.draw(playerSprite, pSprX, pSprY, pWidth, pHeight);

        /**
         * draws player stats
         */

        // x padding of stat name
        float x_pad_stat = 1.44f;
        float x_pad_stat_amt = 1.32f;

        // draws player max health text
        // uses glyphlayout to get text bounds and align
        inventoryFont.getData().setScale(Config.inventoryStatsScale * Config.inventoryScale);
        float alignW = 100.251f * Config.inventoryStatsScale * Config.inventoryScale; // text align width
        float pHealthTextX = invX + (invW/x_pad_stat);
        float pHealthTextY = invY + (invH/1.132f) - (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pHealthText, Config.inventoryStatColor, alignW, Align.left, true);
        inventoryFont.draw(batch, invDefaultGL , pHealthTextX, pHealthTextY);

        // draws player max health amount text
        // uses glyphlayout to get text bounds and align
        String pHealthAmtText = Integer.toString(player.getAttributes().getMaxHealth());
        float pHealthAmtTextX = invX + (invW/x_pad_stat_amt);
        float pHealthAmtTextY = invY + (invH/1.132f) - (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pHealthAmtText, Config.inventoryStatValueColor, alignW, Align.right, true);
        inventoryFont.draw(batch, invDefaultGL , pHealthAmtTextX, pHealthAmtTextY);

        // draws player experience text
        // uses glyphlayout to get text bounds and align
        float pExpTextX = invX + (invW/x_pad_stat);
        float pExpTextY = invY + (invH/1.133f) - (invH/18f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pExpText, Config.inventoryStatColor, alignW, Align.left, true);
        inventoryFont.draw(batch, invDefaultGL , pExpTextX, pExpTextY);

        // draws player experience amount text
        // uses glyphlayout to get text bounds and align
        String pExpAmtText = String.format("%.2f", Level.expPercent(player)*100f) + "%";

        float pExpAmtTextX = invX + (invW/x_pad_stat_amt);
        float pExpAmtTextY = invY + (invH/1.133f) - (invH/18f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pExpAmtText, Config.inventoryStatValueColor, alignW, Align.right, true);
        inventoryFont.draw(batch, invDefaultGL , pExpAmtTextX, pExpAmtTextY);

        // draws player attack text
        // uses glyphlayout to get text bounds and align
        float pAtkTextX = invX + (invW/x_pad_stat);
        float pAtkTextY = invY + (invH/1.133f) - (invH/9.1f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pAtkText, Config.inventoryStatColor, alignW, Align.left, true);
        inventoryFont.draw(batch, invDefaultGL , pAtkTextX, pAtkTextY);

        // draws player attack amount text
        // uses glyphlayout to get text bounds and align
        String pAtkAmtText = Integer.toString(player.getAttributes().getAttack());
        float pAtkAmtTextX = invX + (invW/x_pad_stat_amt);
        float pAtkAmtTextY = invY + (invH/1.133f) - (invH/9.1f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pAtkAmtText, Config.inventoryStatValueColor, alignW, Align.right, true);
        inventoryFont.draw(batch, invDefaultGL , pAtkAmtTextX, pAtkAmtTextY);

        // draws player defense text
        // uses glyphlayout to get text bounds and align
        float pDefTextX = invX + (invW/x_pad_stat);
        float pDefTextY = invY + (invH/1.133f) - (invH/6.2f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pDefText, Config.inventoryStatColor, alignW, Align.left, true);
        inventoryFont.draw(batch, invDefaultGL , pDefTextX, pDefTextY);

        // draws player defense amount text
        // uses glyphlayout to get text bounds and align
        String pDefAmtText = Integer.toString(player.getAttributes().getDefense());
        float pDefAmtTextX = invX + (invW/x_pad_stat_amt);
        float pDefAmtTextY = invY + (invH/1.133f) - (invH/6.2f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pDefAmtText, Config.inventoryStatValueColor, alignW, Align.right, true);
        inventoryFont.draw(batch, invDefaultGL , pDefAmtTextX, pDefAmtTextY);

        // draws player auto attack speed text
        // uses glyphlayout to get text bounds and align
        float pAutoAtkSpdTextX = invX + (invW/x_pad_stat);
        float pAutoAtkSpdTextY = invY + (invH/1.133f) - (invH/4.65f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pAutoAtkSpdText, Config.inventoryStatColor, alignW, Align.left, true);
        inventoryFont.draw(batch, invDefaultGL , pAutoAtkSpdTextX, pAutoAtkSpdTextY);

        // draws player auto attack speed amount text
        // uses glyphlayout to get text bounds and align
        String pAutoAtkSpdAmtText = String.format("%.2f", 10f/player.getAttributes().getAutoSpeed());
        float pAutoSpdAmtTextX = invX + (invW/x_pad_stat_amt);
        float pAutoSpdAmtTextY = invY + (invH/1.133f) - (invH/4.65f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pAutoAtkSpdAmtText, Config.inventoryStatValueColor, alignW, Align.right, true);
        inventoryFont.draw(batch, invDefaultGL , pAutoSpdAmtTextX, pAutoSpdAmtTextY);

        // draws player auto attack text
        // uses glyphlayout to get text bounds and align
        float pAutoAtkTextX = invX + (invW/x_pad_stat);
        float pAutoAtkTextY = invY + (invH/1.133f) - (invH/3.65f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pAutoAtkText, Config.inventoryStatColor, alignW, Align.left, true);
        inventoryFont.draw(batch, invDefaultGL , pAutoAtkTextX, pAutoAtkTextY);

        // draws player auto attack amount text
        // uses glyphlayout to get text bounds and align
        String pAutoAtkAmtText = Integer.toString(player.getAttributes().getAutoAttack());
        float pAutoAtkAmtTextX = invX + (invW/x_pad_stat_amt);
        float pAutoAtkAmtTextY = invY + (invH/1.133f) - (invH/3.65f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pAutoAtkAmtText, Config.inventoryStatValueColor, alignW, Align.right, true);
        inventoryFont.draw(batch, invDefaultGL , pAutoAtkAmtTextX, pAutoAtkAmtTextY);

        // draws player critical chance text
        // uses glyphlayout to get text bounds and align
        float pCritChanceX = invX + (invW/x_pad_stat);
        float pCritChanceY = invY + (invH/1.137f) - (invH/3.05f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pCritChanceText, Config.inventoryStatColor, alignW, Align.left, true);
        inventoryFont.draw(batch, invDefaultGL , pCritChanceX, pCritChanceY);

        // draws player critical chance amount text
        // uses glyphlayout to get text bounds and align
        String pCritChanceAmtText = String.format("%.2f", player.getAttributes().getCriticalChance()) + "%";
        float pCritChanceAmtTextX = invX + (invW/x_pad_stat_amt);
        float pCritChanceAmtTextY = invY + (invH/1.137f) - (invH/3.05f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pCritChanceAmtText, Config.inventoryStatValueColor, alignW, Align.right, true);
        inventoryFont.draw(batch, invDefaultGL , pCritChanceAmtTextX, pCritChanceAmtTextY);

        // draws player critical multiplier text
        // uses glyphlayout to get text bounds and align
        float pCritMultX = invX + (invW/x_pad_stat);
        float pCritMultY = invY + (invH/1.133f) - (invH/2.58f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pCritMultText, Config.inventoryStatColor, alignW, Align.left, true);
        inventoryFont.draw(batch, invDefaultGL , pCritMultX, pCritMultY);

        // draws player critical multiplier amount text
        // uses glyphlayout to get text bounds and align
        String pCritMultAmtText = "x "+String.format("%.2f", player.getAttributes().getCriticalMultiplier());
        float pCritMultAmtTextX = invX + (invW/x_pad_stat_amt);
        float pCritMultAmtTextY = invY + (invH/1.133f) - (invH/2.58f)- (invDefaultGL.height/2);
        invDefaultGL.setText(inventoryFont, pCritMultAmtText, Config.inventoryStatValueColor, alignW, Align.right, true);
        inventoryFont.draw(batch, invDefaultGL , pCritMultAmtTextX, pCritMultAmtTextY);

        /**
         * Draws inventory label, gold and item description
         */
        // draws player gold amount text
        // uses glyphlayout to get text bounds and align
        inventoryFont.getData().setScale(Config.inventoryGoldScale * Config.inventoryScale);
        String pGoldText = Long.toString(gold);
        double amount = Double.parseDouble(pGoldText);
        DecimalFormat formatter = new DecimalFormat("#,###");
        pGoldText = formatter.format(amount);
        invDefaultGL.setText(inventoryFont, pGoldText, Color.GOLDENROD, 100f, Align.right, true);
        float pGoldX = invX + (invW/2.95f);
        float pGoldY = invY + (invH/15.1f);
        inventoryFont.draw(batch, invDefaultGL , pGoldX, pGoldY);

        // draws inventory label
        // uses glyphlayout to get text bounds and align
        inventoryFont.getData().setScale(Config.inventoryLabelScale * Config.inventoryScale);
        invDefaultGL.setText(inventoryFont, invLabText, Color.TAN, 100f, Align.left, true);
        float invLabX = invX + (invW/16.35f);
        float invLabY = invY + (invH/2.58f);
        inventoryFont.draw(batch, invDefaultGL , invLabX, invLabY);

        /**
         * Draws player profile info
         */
        // draws player name
        // uses glyphlayout to get text bounds and align
        inventoryFont.getData().setScale(Config.inventoryProfileScale * Config.inventoryScale);
        String pNameText = player.getName();
        invDefaultGL.setText(inventoryFont, pNameText, Color.FOREST, 100f, Align.left, true);
        float pNameX = invX + (invW/22.55f);
        float pNameY = invY + (invH/1.077f);
        inventoryFont.draw(batch, invDefaultGL , pNameX, pNameY);

        // draws player level
        // uses glyphlayout to get text bounds and align
        inventoryFont.getData().setScale(Config.inventoryProfileScale * Config.inventoryScale);
        String pLevelText = Main.getInstance().getLang().format("levelInfo", String.valueOf(player.getAttributes().getLevel()));
        invDefaultGL.setText(inventoryFont, pLevelText, Color.WHITE, 0, Align.right, true);
        float pLevelX = invX + (invW/1.72f);
        float pLevelY = invY + (invH/1.077f);
        inventoryFont.draw(batch, invDefaultGL , pLevelX, pLevelY);

        // draws profile label
        // uses glyphlayout to get text bounds and align
        inventoryFont.getData().setScale(Config.inventoryProfileLabelScale * Config.inventoryScale);
        invDefaultGL.setText(inventoryFont, profLabelText, new Color(253/255f, 255/255f, 201/255f, 255/255f),
                                    100f, Align.center, true);
        float profLabelX = invX + (invW/4.69f);
        float profLabelY = invY + (invH/1.018f);
        inventoryFont.draw(batch, invDefaultGL , profLabelX, profLabelY);

        // draw selected item info (if there is a selected item)
        if(selectedItem != null) {
            renderSelectedItemInfo(batch, invX, invY, invW, invH);
        }

        // renders inventory items
        renderInventoryItems(batch, invX, invY, invW, invH);

        // renders worn equipments
        renderWornEquipments(batch, invX, invY, invW, invH);

        // renders possible slots for current selected item (if it is equipment)
        renderPossibleSlotFrames(batch);
    }

    /**
     * Renders page switches on inventory
     * @param batch the sprite batch to draw page switches
     * @param invX inventory x position on screen
     * @param invY inventory y position on screen
     * @param invW inventory width on screen
     * @param invH inventory height on screen
     */
    private void renderPageSwitches(SpriteBatch batch, float invX, float invY, float invW, float invH) {
        // iterates through number of pages in config
        // starts from right to correct align with inventory
        for(int i = Config.inventoryNPages-1 ; i >= 0; i--) {
            int pageNumber = i+1; // the number of page to be draw
            // gets switch sprite
            if(invSelectedPage == i)
                switchSpr.setRegion(selectedSwitch);
            else
                switchSpr.setRegion(normalSwitch);
            // draws switches backgrounds
            float targetSwitchW = invW / (Config.inventoryWidth/Config.inventorySwitchW); // the target Switch width
            float targetSwitchH = invH / (Config.inventoryHeight/Config.inventorySwitchH); // the target Switch height
            float targetSwitchX = invX + Config.inventoryFirstSwitchX * Config.inventoryScale; // the target Switch X
            float targetSwitchY = invY + Config.inventoryFirstSwitchY * Config.inventoryScale; // the target Switch Y
            float padX = -Config.inventorySwitchPadX * Config.inventoryScale; // padding on each Switch in X axis
            switchSpr.setPosition(targetSwitchX+(padX*(Config.inventoryNPages-1-i)), targetSwitchY);
            switchSpr.setSize(targetSwitchW, targetSwitchH);
            switchSpr.draw(batch);
            // draws switches numbers
            inventoryFont.getData().setScale(Config.inventoryPageNumberScale * Config.inventoryScale);
            String numberText = String.valueOf(pageNumber);
            invDefaultGL.setText(inventoryFont, numberText, Config.inventoryPageNumberColor,
                    (Config.inventorySwitchW * Config.inventoryScale)*2.1f, Align.center, true);
            float numberX = targetSwitchX+(padX*(Config.inventoryNPages-1-i)) - (targetSwitchW/2.1f);
            float numberY = targetSwitchY+ (targetSwitchH/2f);
            inventoryFont.draw(batch, invDefaultGL, numberX, numberY);
        }
    }

    /**
     * Renders player's current selected item info
     * @param batch the sprite batch to draw selected item info
     * @param invX inventory x position on screen
     * @param invY inventory y position on screen
     * @param invW inventory width on screen
     * @param invH inventory height on screen
     */
    private void renderSelectedItemInfo(SpriteBatch batch, float invX, float invY, float invW, float invH) {
        // gets rarity color
        Color color = Common.getQualityColor(selectedItem.getQuality());

        // Draw selected item name
        inventoryFont.getData().setScale(Config.inventorySelItemNameScale * Config.inventoryScale);
        String itemName = selectedItem.getName();
        invDefaultGL.setText(inventoryFont, itemName, color,
                203f * Config.inventorySelItemDescScale * Config.inventoryScale, Align.left, true);
        float nameX = invX + 405f * Config.inventoryScale;
        float nameY = invY + 254f * Config.inventoryScale;
        inventoryFont.draw(batch, invDefaultGL , nameX, nameY);
        float itemNameLayoutHeight = invDefaultGL.height;

        // Draw selected item level (if it is an equipment)
        if(selectedItem.isEquipable()) {
            inventoryFont.getData().setScale(Config.inventorySelItemSubInfoScale * Config.inventoryScale);
            String itemLevel = Main.getInstance().getLang().format("levelInfo",
                                String.valueOf(((Equipment)selectedItem).getLevel()));
            invDefaultGL.setText(inventoryFont, itemLevel, color,
                    203f * Config.inventorySelItemSubInfoScale * Config.inventoryScale, Align.right, true);
            float itemLevelX = invX + 354f * Config.inventoryScale;
            float itemLevelY = invY - invDefaultGL.height + 248f * Config.inventoryScale;
            inventoryFont.draw(batch, invDefaultGL, itemLevelX, itemLevelY);
        }

        // draw selected item quality
        String itemQuality = Common.capitalize(selectedItem.getQuality().toString());
        invDefaultGL.setText(inventoryFont, itemQuality, color,
                203f * Config.inventorySelItemSubInfoScale * Config.inventoryScale, Align.left, true);
        float itemQualityX = invX + 405f * Config.inventoryScale;
        float itemQualityY = invY - invDefaultGL.height + 248f * Config.inventoryScale;
        inventoryFont.draw(batch, invDefaultGL, itemQualityX, itemQualityY);
        float itemQualityLayoutHeight = invDefaultGL.height;

        // draw selected item description
        inventoryFont.getData().setScale(Config.inventorySelItemDescScale * Config.inventoryScale);
        // if it is equipment, get equipment description, else get base description
        String itemText;
        if(selectedItem.isEquipable())
            itemText = ((Equipment)selectedItem).getEquipmentDescription();
        else
            itemText = selectedItem.getDescription();
        invDefaultGL.setText(inventoryFont, itemText, Color.WHITE,
                195f * Config.inventorySelItemDescScale * Config.inventoryScale, Align.left, true);
        float itemX = invX + 405f * Config.inventoryScale;
        float itemY = invY - itemNameLayoutHeight - itemQualityLayoutHeight + 237f * Config.inventoryScale;
        inventoryFont.draw(batch, invDefaultGL , itemX, itemY);
    }

    /**
     * Renders player's current inventory items in current page
     * @param batch the sprite batch to draw items
     * @param invX inventory x position on screen
     * @param invY inventory y position on screen
     * @param invW inventory width on screen
     * @param invH inventory height on screen
     */
    private void renderInventoryItems(SpriteBatch batch, float invX, float invY, float invW, float invH) {

        // iterates through player's grid of inventory items
        for(int i = 0; i < getItems().length; i++) {
            for (int j = 0; j < getItems()[i].length; j++) {
                if(getItems()[i][j] == null) // ignores slot that has no items
                    continue;

                Item item = getItems()[i][j]; // the item in question
                itemSpr.setRegion(item.getSprite()); // gets item sprite
                float targetItemW = invW / (Config.inventoryWidth/Config.inventoryItemSlotW); // the target item width
                float targetItemH = invH / (Config.inventoryHeight/Config.inventoryItemSlotH); // the target item height
                float targetItemX = invX + Config.inventoryFirstSlotX * Config.inventoryScale; // the target item X
                float targetItemY = invY + Config.inventoryFirstSlotY * Config.inventoryScale; // the target item Y
                float padX = Config.inventoryPadX * Config.inventoryScale; // padding on each slot in X axis
                float padY = -Config.inventoryPadY * Config.inventoryScale; // padding on each slot in Y axis
                itemSpr.setPosition(targetItemX+(j*padX), targetItemY+(i*padY));
                itemSpr.setSize(targetItemW, targetItemH);
                itemSpr.draw(batch);

                // draws selection frame on item if an item from inventory is the selected one
                if(selectedItem != null && fromInventory) {
                    if (selectedItemPage == invSelectedPage &&
                            selectedItemIdx.equals(new Vector2(i, j))) {
                        Sprite frameSpr = new Sprite(selectionFrame);
                        frameSpr.setPosition(targetItemX+(j*padX), targetItemY+(i*padY));
                        frameSpr.setSize(targetItemW, targetItemH);
                        frameSpr.draw(batch);
                    }
                }
            }
        }
    }

    /**
     * Renders player's current worn equipments
     * @param batch the sprite batch to draw items
     * @param invX inventory x position on screen
     * @param invY inventory y position on screen
     * @param invW inventory width on screen
     * @param invH inventory height on screen
     */
    private void renderWornEquipments(SpriteBatch batch, float invX, float invY, float invW, float invH) {

        float equipW = invW / 12.85714f; // the target equipment width
        float equipH = invH / 10.67241f; // the target equipment height
        float equipX = invX + 28.1f * Config.inventoryScale; // the base target equipment X
        float equipY = invY + 477f * Config.inventoryScale; // the base target equipment Y
        float padY = -equipH - 11.11f * Config.inventoryScale; // Y padding in slots
        float padX = equipW + 237.1f * Config.inventoryScale; // X padding from left to right slots

        Equipment.Slot equipSelected = null; // slot selected, in case an equipment slot is selected

        // gets selection item slot in case a equipment is selected
        if(selectedItem != null && !fromInventory) {
            equipSelected = ((Equipment)selectedItem).getEquipmentSlot();
            frameSpr.setRegion(selectionFrame);
        }

        // renders helmet
        Equipment helmet = get(Equipment.Slot.HELMET); // the player helmet
        // only renders helmet if player has a helmet equipped
        if(helmet != null) {
            itemSpr.setRegion(helmet.getSprite()); // gets equipment sprite
            itemSpr.setPosition(equipX, equipY);
            itemSpr.setSize(equipW, equipH);
            itemSpr.draw(batch);
            // updates frame sprite if it is the selected one
            if(equipSelected != null && equipSelected.equals(Equipment.Slot.HELMET)) {
                frameSpr.setPosition(equipX, equipY);
                frameSpr.setSize(equipW, equipH);
            }
        }

        // renders armor
        Equipment armor = get(Equipment.Slot.ARMOR); // the player helmet
        // only renders armor if player has an armor equipped
        if(armor != null) {
            itemSpr.setRegion(armor.getSprite()); // gets equipment sprite
            itemSpr.setPosition(equipX+padX, equipY);
            itemSpr.setSize(equipW, equipH);
            itemSpr.draw(batch);
            // updates frame sprite if it is the selected one
            if(equipSelected != null && equipSelected.equals(Equipment.Slot.ARMOR)) {
                frameSpr.setPosition(equipX+padX, equipY);
                frameSpr.setSize(equipW, equipH);
            }
        }

        // renders weapon
        Equipment weapon = get(Equipment.Slot.WEAPON); // the player weapon
        // only renders weapon if player has a weapon equipped
        if(weapon != null) {
            itemSpr.setRegion(weapon.getSprite()); // gets equipment sprite
            itemSpr.setPosition(equipX, equipY+padY);
            itemSpr.setSize(equipW, equipH);
            itemSpr.draw(batch);
            // updates frame sprite if it is the selected one
            if(equipSelected != null && equipSelected.equals(Equipment.Slot.WEAPON)) {
                frameSpr.setPosition(equipX, equipY+padY);
                frameSpr.setSize(equipW, equipH);
            }
        }

        // renders shield
        Equipment shield = get(Equipment.Slot.SHIELD); // the player shield
        // only renders shield if player has an shield equipped
        if(shield != null) {
            itemSpr.setRegion(shield.getSprite()); // gets equipment sprite
            itemSpr.setPosition(equipX+padX, equipY+padY);
            itemSpr.setSize(equipW, equipH);
            itemSpr.draw(batch);
            // updates frame sprite if it is the selected one
            if(equipSelected != null && equipSelected.equals(Equipment.Slot.SHIELD)) {
                frameSpr.setPosition(equipX+padX, equipY+padY);
                frameSpr.setSize(equipW, equipH);
            }
        }

        // renders legs
        Equipment legs = get(Equipment.Slot.LEGS); // the player legs
        // only renders legs if player has legs equipped
        if(legs != null) {
            itemSpr.setRegion(legs.getSprite()); // gets equipment sprite
            itemSpr.setPosition(equipX, equipY+(padY*2));
            itemSpr.setSize(equipW, equipH);
            itemSpr.draw(batch);
            // updates frame sprite if it is the selected one
            if(equipSelected != null && equipSelected.equals(Equipment.Slot.LEGS)) {
                frameSpr.setPosition(equipX, equipY+(padY*2));
                frameSpr.setSize(equipW, equipH);
            }
        }

        // renders boots
        Equipment boots = get(Equipment.Slot.BOOTS); // the player boots
        // only renders boots if player has boots equipped
        if(boots != null) {
            itemSpr.setRegion(boots.getSprite()); // gets equipment sprite
            itemSpr.setPosition(equipX+padX, equipY+(padY*2));
            itemSpr.setSize(equipW, equipH);
            itemSpr.draw(batch);
            // updates frame sprite if it is the selected one
            if(equipSelected != null && equipSelected.equals(Equipment.Slot.BOOTS)) {
                frameSpr.setPosition(equipX+padX, equipY+(padY*2));
                frameSpr.setSize(equipW, equipH);
            }
        }

        // renders ring
        Equipment ring = get(Equipment.Slot.RING); // the player ring
        // only renders ring if player has a ring equipped
        if(ring != null) {
            itemSpr.setRegion(ring.getSprite()); // gets equipment sprite
            itemSpr.setPosition(equipX, equipY+(padY*3));
            itemSpr.setSize(equipW, equipH);
            itemSpr.draw(batch);
            // updates frame sprite if it is the selected one
            if(equipSelected != null && equipSelected.equals(Equipment.Slot.RING)) {
                frameSpr.setPosition(equipX, equipY+(padY*3));
                frameSpr.setSize(equipW, equipH);
            }
        }

        // renders amulet
        Equipment amulet = get(Equipment.Slot.AMULET); // the player amulet
        // only renders amulet if player has an amulet equipped
        if(amulet != null) {
            itemSpr.setRegion(amulet.getSprite()); // gets equipment sprite
            itemSpr.setPosition(equipX+padX, equipY+(padY*3));
            itemSpr.setSize(equipW, equipH);
            itemSpr.draw(batch);
            // updates frame sprite if it is the selected one
            if(equipSelected != null && equipSelected.equals(Equipment.Slot.AMULET)) {
                frameSpr.setPosition(equipX+padX, equipY+(padY*3));
                frameSpr.setSize(equipW, equipH);
            }
        }

        // draws frame on slot selected, if any slot is selected
        if(equipSelected != null && frameSpr != null) {
            frameSpr.draw(batch);
        }
    }

    /**
     * Renders possible slots for current selected item (if it is equipment)
     * @param batch the sprite batch to draw frame
     */
    public void renderPossibleSlotFrames(SpriteBatch batch) {
        // draws frames possible slots for a selected item if it is a equipment from inventory
        if (selectedItem instanceof Equipment && fromInventory) {
            Equipment.Slot possibleSlot = ((Equipment)selectedItem).getEquipmentSlot();
            int possibleIndex = 0;
            switch (possibleSlot) {
                case HELMET:
                    possibleIndex = 0;
                    break;
                case ARMOR:
                    possibleIndex = 1;
                    break;
                case LEGS:
                    possibleIndex = 4;
                    break;
                case BOOTS:
                    possibleIndex = 5;
                    break;
                case WEAPON:
                    possibleIndex = 2;
                    break;
                case SHIELD:
                    possibleIndex = 3;
                    break;
                case RING:
                    possibleIndex = 6;
                    break;
                case AMULET:
                    possibleIndex = 7;
                    break;
                default:
                    System.err.println("Unknown slot type. Check item/equipments.java enum t" +
                            "to see the available ones. Unknown Slot: "+possibleSlot);
                    break;
            }

            // uses collider to draw frame
            Collider col = wornEquipmentTriggers[possibleIndex];
            batch.draw(possibleSlotFrame, col.getX(),
                                            col.getY(), col.getWidth(), col.getHeight());
        }
    }

    /**
     * Searches for an item in inventory
     * returning inventory grid indexes and page if found, or null otherwise
     * @param item  the item to be searched
     * @return the indexes and page of item in inventory if found, null otherwise
     */
    public Vector3 searchItem(Item item) {
        // searches for item in inventory
        for(int p = 0; p < pages.size(); p++) {
            for (int i = 0; i < getItems(p).length; i++) {
                for (int j = 0; j < getItems(p)[i].length; j++) {
                    if (getItems(p)[i][j] != null) { // only compares if slot has item
                        if (getItems(p)[i][j].equals(item)) { // if item is found
                            return new Vector3(i, j, p);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Removes a item from inventory(if it is in inventory)
     * @param item  the item to be removed from inventory
     */
    public void removeItem(Item item) {
        if(item == null) // ignore null entries
            return;

        // searches for item in inventory
        for(int p = 0; p < pages.size(); p++) {
            for (int i = 0; i < getItems(p).length; i++) {
                for (int j = 0; j < getItems(p)[i].length; j++) {
                    if (getItems(p)[i][j] != null) { // only compares if slot has item
                        if (getItems(p)[i][j].equals(item)) { // if item is found
                            getItems(p)[i][j] = null; // removes item
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes item from inventory at a specific location in a specific page.
     * Beware with indexes passed as they are clamped in
     * method implementation to avoid getting out of bounds
     *
     * @param page  the inventory page to remove item from
     * @param ind_i the position i of grid to remove item
     * @param ind_j the position j of grid to remove item
     */
    public void removeItem(int page, int ind_i, int ind_j) {
        // clamp for safety
        if(page < 0)
            page = 0;
        if(page > Config.inventoryNPages - 1)
            page = Config.inventoryNPages - 1;
        if(ind_i > Config.inventoryMaxItemsY)
            ind_i = Config.inventoryMaxItemsY;
        if(ind_i < 0)
            ind_i = 0;
        if(ind_j > Config.inventoryMaxItemsX)
            ind_j = Config.inventoryMaxItemsX;
        if(ind_j < 0)
            ind_j = 0;

        if(getItems(page)[ind_i][ind_j] != null)
            getItems(page)[ind_i][ind_j] = null; // removes item at position
    }

    /**
     * Adds a item to the inventory in the next free slot.
     * Does not add if there are no more free slots.
     *
     * @param item the item to be added to inventory
     * @return true if item was successfully added to inventory, false otherwise
     */
    public boolean addItem(Item item) {
        // searches grid for empty space
        for(int p = 0; p < pages.size(); p++) {
            for (int i = 0; i < getItems(p).length; i++) {
                for (int j = 0; j < getItems(p)[i].length; j++) {
                    if (getItems(p)[i][j] == null) { // if empty slot is found
                        getItems(p)[i][j] = item;
                        return true;
                    }
                }
            }
        }
        return false; // no slot was found
    }

    /**
     * Adds a item to inventory in a specified position (if position is free)
     * @param item  the item to be added to inventory
     * @param page  the page to add item to
     * @param ind_i the position i of grid to add item
     * @param ind_j the position j of grid to add item
     *
     * @return true if item was successfully added to inventory, false otherwise
     */
    public boolean addItem(Item item, int page, int ind_i, int ind_j) {
        // clamp for safety
        if(page < 0)
            page = 0;
        if(page > Config.inventoryNPages - 1)
            page = Config.inventoryNPages - 1;
        if(ind_i > Config.inventoryMaxItemsY-1)
            ind_i = Config.inventoryMaxItemsY-1;
        if(ind_i < 0)
            ind_i = 0;
        if(ind_j > Config.inventoryMaxItemsX-1)
            ind_j = Config.inventoryMaxItemsX-1;
        if(ind_j < 0)
            ind_j = 0;

        if(getItems(page)[ind_i][ind_j] != null) { // if there is an item already
            if(getItems(page)[ind_i][ind_j].getId() == item.getId()) { // same item, adds to stack(if stackable)
                getItems(page)[ind_i][ind_j].addToStack(1);
                return true;
            }
        } else { // if there are no items in slot
            getItems(page)[ind_i][ind_j] = item; // adds item to items slot
            return true;
        }
        return false;
    }

    /**
     * Checks if inventory is full (no empty slots) or
     * not (has empty slots)
     *
     * @return true if inventory is full, false otherwise
     */
    public boolean isFull() {
        // searches grid for empty space
        for(int p = 0; p < pages.size(); p++) {
            for (int i = 0; i < getItems(p).length; i++) {
                for (int j = 0; j < getItems(p)[i].length; j++) {
                    if (getItems(p)[i][j] == null) { // if empty slot is found
                        return false; // return that inventory is not full
                    }
                }
            }
        }
        return true; // no slot was found, return that inventory is full
    }

    /**
     * Checks how many empty slots is left in inventory
     *
     * @return the number of empty slots left
     */
    public int nEmptySlots() {
        // empty slots counter
        int emptyCounter = 0;
        // searches grid for empty space
        for(int p = 0; p < pages.size(); p++) {
            for (int i = 0; i < getItems(p).length; i++) {
                for (int j = 0; j < getItems(p)[i].length; j++) {
                    if (getItems(p)[i][j] == null) { // if empty slot is found
                        emptyCounter++; // increase counter
                    }
                }
            }
        }
        return emptyCounter; // return empty slots counter
    }

    /**
     * disposes inventory remaining resources
     */
    public void dispose() {
    }

    /**
     * Touch behaviours
     */

    /**
     * Deals with inventory slots touches
     *
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    public void doSlotTouch(int screenX, int screenY) {
        // checks if there is collision with inventory item slots
        int i_col = -1; int j_col = -1; // initially there are no collided slot
        // iterates through mask of item slot triggers
        for (int i = 0; i < itemSlotTriggers.length; i++) {
            for (int j = 0; j < itemSlotTriggers[i].length; j++) {
                // if collider collides with touch
                if(itemSlotTriggers[i][j].checkTouchCollision(new Vector2(screenX, screenY))) {
                    // get collider grid indexes
                    i_col = i;
                    j_col = j;
                    // breaks loop on first collision
                    break;
                }
            }
        }

        // if did not collide with any slot trigger, do nothing
        if(i_col == -1 && j_col == -1)
            return;

        // deals with item slot operations
        manageItemSlotTouched(i_col, j_col);
    }

    /**
     * Manages item slot operations when
     * there are slot touches on inventory
     * @param i the i-index of item slot touched in inventory
     * @param j the j-index of item slot touched in inventory
     */
    private void manageItemSlotTouched(int i, int j) {
        // checks if slot from current page has item
        boolean hasItem = getItems()[i][j] == null ? false : true;
        // checks if there is already a selected item
        boolean itemSelected = selectedItem == null ? false : true;
        // selected item is coming from equipment slots?
        fromInventory = selectedItemSlot == null ? true : false;
        // touched item is an equipment?
        boolean equipTouched = hasItem && getItems()[i][j] instanceof Equipment ? true : false;
        // casts to equipment if a equipment is the touched item
        Equipment touchedEquip = ((Equipment)getItems()[i][j]);
        // is the selected item the same item as the touched one?
        boolean sameItem = (itemSelected && hasItem && fromInventory) && (invSelectedPage == selectedItemPage)
                && ((int)selectedItemIdx.x == i) && ((int)selectedItemIdx.y == j);

        /**
         * Conditions management
         */

        // if there is no item selected yet and slot touch has item
        // or there is an item selected, an item in slot touched and item selected
        // is not from equipment slot
        if(!itemSelected && hasItem || (itemSelected && hasItem && fromInventory)) {
            // select item from slot, overriding old selected item if it is not from equip slot
            selectedItem = getItems()[i][j];
            selectedItemIdx = new Vector2(i, j);
            selectedItemPage = invSelectedPage;
            fromInventory = true;
        }

        // if there is an item selected, slot touched is free and it is not from equip slot
        if(itemSelected && !hasItem && fromInventory) {
            // stores item selected in new position
            getItems()[i][j] = selectedItem;
            // erases selected item from old position
            removeItem(selectedItemPage, (int)selectedItemIdx.x, (int)selectedItemIdx.y);
            // erases selected item reference
            selectedItem = null;
        }

        // if there is an item selected and it is from equipment slot and slot touched is free
        if(itemSelected && !hasItem && !fromInventory) {
            // saves equipment
            Equipment removedEquip = get(((Equipment)selectedItem).getEquipmentSlot());
            // unequips old equipment
            unEquip(removedEquip.getEquipmentSlot());
            // stores equipment selected in inventory on touch indexes
            getItems()[i][j] = removedEquip;
            // erases selected item reference
            selectedItem = null;
            selectedItemSlot = null;
        }

        // if there is an item selected and it is from equipment slot and slot touched has item
        if(itemSelected && hasItem && !fromInventory) {
            // touched equipment matches selected equipment slot type?
            boolean touchedMatch = equipTouched && touchedEquip.getEquipmentSlot().equals
                                    (((Equipment)selectedItem).getEquipmentSlot()) ? true : false;
            if(touchedMatch) { // if types match, exchanges inventory equipment with equipped equipment
                // saves equipment
                Equipment removedEquip = get(((Equipment) selectedItem).getEquipmentSlot());
                // unequips old equipment
                unEquip(removedEquip.getEquipmentSlot());
                // equips the inventory touched equipment
                equip(touchedEquip);
                // stores equipment selected in inventory on touch indexes
                getItems()[i][j] = removedEquip;
                // erases selected item reference
                selectedItem = null;
                selectedItemSlot = null;
            } else { // just selects the touched item in inventory
                // select item from slot, overriding old selected item if it is not from equip slot
                selectedItem = getItems()[i][j];
                selectedItemIdx = new Vector2(i, j);
                selectedItemPage = invSelectedPage;
                fromInventory = true;
                selectedItemSlot = null;
            }
        }

        // if selected item is the item touched, erases selection
        if(sameItem) {
            // erases selected item reference
            selectedItem = null;
            selectedItemSlot = null;
        }
    }

    /**
     * Deals with page switch touches
     *
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    public void doPageTouch(int screenX, int screenY) {
        // checks if there is collision with page switches
        int page = -1;  // initially there are no collided page
        // iterates through mask of page switches triggers
        for (int i = 0; i < pageTriggers.length; i++) {
                // if collider collides with touch
                if(pageTriggers[i].checkTouchCollision(new Vector2(screenX, screenY))) {
                    // get collider index that relates to page
                    page = i;
                    // breaks loop on first collision
                    break;
                }
        }

        // if did not collide with any page switches, do nothing
        if(page == -1)
            return;

        // if collides, update selected inventory page
        invSelectedPage = page;
    }

    /**
     * Deals with equipment slots touches
     *
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    public void doEquipmentTouch(int screenX, int screenY) {
        // checks if there is collision with equipment item slots
        int idx = -1; // initially there are no collided slot
        // iterates through mask of worn equipment slot triggers
        for (int i = 0; i < wornEquipmentTriggers.length; i++) {
            // if collider collides with touch
            if(wornEquipmentTriggers[i].checkTouchCollision(new Vector2(screenX, screenY))) {
                // get collider grid indexes
                idx = i;
                // breaks loop on first collision
                break;
            }
        }

        // if did not collide with any equipment slot trigger, do nothing
        if(idx == -1)
            return;

        // deals with item equipment slot operations
        manageEquipmentSlotTouched(Equipment.Slot.values()[idx]);
    }

    /**
     * Manages equipment slot operations when
     * there are equipment touches on inventory
     * @param slot the equipment slot touched in inventory
     */
    private void manageEquipmentSlotTouched(Equipment.Slot slot) {
        // checks if equipment slot has item
        boolean hasItem = wornEquipment[slot.ordinal()] == null ? false : true;
        // checks if there is already a selected item
        boolean itemSelected = selectedItem == null ? false : true;
        // selected item is an equipment?
        boolean equipSelected = selectedItem instanceof Equipment ? true : false;
        // casts to equipment if a equipment is the selected item
        Equipment selectedEquip = ((Equipment)selectedItem);
        // selected equipment matches slot type?
        boolean selectedMatch = equipSelected && selectedEquip.getEquipmentSlot().equals(slot) ? true : false;

        /**
         * Conditions management
         */

        // if there is no item selected yet and equipment slot touched has item
        // or there is an item selected, an equipment in slot touched and item selected
        // is not an equipment or does not match slot type -> selects equip in slot touched
        if(!itemSelected && hasItem || (itemSelected && hasItem && (!equipSelected || !selectedMatch))) {
            // select equipment from slot, overriding old selected item if it is not an equipment
            // or does not match with slot type
            selectedItem = get(slot);
            selectedItemSlot = slot;
            fromInventory = false;
        }

        // if there is an equipment selected, slot touched is free, equip matches slot type
        // and equipment selected is coming from inventory -> equips equipment and remove from inventory
        if(itemSelected && !hasItem && equipSelected && selectedMatch && fromInventory) {
            // equips the equipment
            equip(selectedEquip);
            // gets indexes of equipment in inventory to remove it faster
            int p = selectedItemPage;
            int i = (int)selectedItemIdx.x;
            int j = (int)selectedItemIdx.y;
            // removes item from inventory
            removeItem(p, i, j);
            // erases selected item reference
            selectedItem = null;
            selectedItemSlot = null;
        }

        // if there is an equipment that match slot selected, slot has item and equipment
        // comes from inventory, switch equipped equipment with selected inventory equipment
        if(itemSelected && hasItem && equipSelected && selectedMatch && fromInventory) {
            // saves old equipment
            Equipment oldEquipment = get(slot);
            // unequip old equipment
            unEquip(slot);
            // equips the selected equipment
            equip(selectedEquip);
            // gets indexes of equipment in inventory to remove it faster
            // and add old equipped equipment to same position
            int p = selectedItemPage;
            int i = (int)selectedItemIdx.x;
            int j = (int)selectedItemIdx.y;
            // removes item from inventory
            removeItem(p, i, j);
            // adds old equipped equipment to inventory
            getItems(p)[i][j] = oldEquipment;
            // erases selected item reference
            selectedItem = null;
            selectedItemSlot = null;
        }

        // if clicking on equipment slot that is the selected item, removes selection
        if(itemSelected && hasItem && equipSelected && selectedMatch && !fromInventory) {
            // erases selected item reference
            selectedItem = null;
            selectedItemSlot = null;
        }
    }

    /**
     * Called when there is a touch on screen
     * and inventory is opened.
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    public void touchDown(int screenX, int screenY) {
        // deal with inventory slot trigger collision
        doSlotTouch(screenX, screenY);
        // deal with page switches touch trigger collision
        doPageTouch(screenX, screenY);
        // deal with equipment slots trigger collision
        doEquipmentTouch(screenX, screenY);
    }

    /**
     * Called when there is a fling gesture on screen
     * (user drag touch and lift it) and inventory is opened
     * @param velocityX velocity on x axis in seconds
     * @param velocityY velocity on y axis in seconds
     */
    public void fling(float velocityX, float velocityY) {
        // check if velocity is bigger than minimum to consider for page switch
        if(Math.abs(velocityX) >= Config.inventoryMinVelocityPageSwitch) {
            // if it is, check direction of fling
            if(velocityX > 0) // right direction, should decrease page number
                invSelectedPage--;
            else // left direction, should increase page number
                invSelectedPage++;

            // clamp page number to avoid getting out of bounds
            if(invSelectedPage < 0)
                invSelectedPage = 0;
            if(invSelectedPage > Config.inventoryNPages - 1)
                invSelectedPage = Config.inventoryNPages - 1;
        }
    }
}
