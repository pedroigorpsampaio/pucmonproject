package com.mygdx.game.states.game.standard.market;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.Main;
import com.mygdx.game.messages.MessageContent;
import com.mygdx.game.messages.MessageFlag;
import com.mygdx.game.messages.MessageMarket;
import com.mygdx.game.messages.ServerMessages;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.architecture.ServerListener;
import com.mygdx.game.states.game.standard.item.Equipment;
import com.mygdx.game.states.game.standard.inventory.Inventory;
import com.mygdx.game.states.game.standard.item.Item;
import com.mygdx.game.states.game.standard.item.factory.Factory;
import com.mygdx.game.states.game.standard.physics.Collider;
import com.mygdx.game.ui.ColliderButton;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;
import com.sddl.client.PucmonClient;

import java.util.ArrayList;

/**
 * Provides a visualization and controls
 * the interaction with items market
 *
 * @author Pedro Sampaio
 * @since 1.8
 */
public class Market implements ServerListener {

    private float marketX;  // market x position on screen
    private float marketY;  // market y position on screen
    private float marketW;  // market width on screen
    private float marketH;  // market height on screen
    private Rectangle scissors;   // market scissors for clipping
    private Rectangle clipBounds; // clipping bounds rect
    private String character; // player's character name
    private boolean isRetrieving; // is data being retrieved?
    private String infoStr; // string containing information to display to player
    private ArrayList<MarketItem> marketItems; // list of market items being sold at retrieve moment
    private ArrayList<MarketItem> inventoryItems; // list of player inventory items
    private ArrayList<MarketItem> listings; // list player listings (items sold by player atm)
    private Inventory playerInventory; // player's inventory reference
    private float tabY; // market tab Y position
    private float entryX;  // market entry x position on screen
    private float entryW;  // market entry width on screen
    private float entryH;  // market entry height on screen
    private float gapY; // market entry gaps on Y axis
    private float titleY; // market title Y position
    private float baseTabX; // market tab base X position
    private float tabW; // market tab width
    private float tabH; // market tab height
    private Stage marketStage; // market stage
    public enum Tab {Buy, Sell, Listings} // available tabs of market interface
    private Tab selectedTab; // current selected tab of market interface
    private ArrayList<Collider> tabColliders; // list of tab colliders to respond to touch input
    private float buyDeltaY; // delta Y on buy tab
    private float sellDeltaY; // delta Y on sell tab
    private float listingsDeltaY; // delta Y on listings tab
    private float pGoldX;   // player gold x position in market
    private float pGoldY;   // player gold y position in market
    private float goldIconW; // player gold icon width
    private float goldIconH; // player gold icon height
    private float infoTimer = 0f; // information message timer
    private final float normalFontHeight; // normal font size height
    private final float reducedFontHeight; // reduced font size height
    private TextField lastInputField; // last input field for selling tab
    private boolean active; // is market active ?

    /**
     * Market Texture vars
     */

    private TextureRegion backgroundTex;    // market background texture
    private TextureRegion selectedTabTex;   // market selected tab texture
    private TextureRegion tabTex;           // market tab texture
    private TextureRegion pressedButtonTex; // market pressed button texture
    private TextureRegion buttonTex;        // market button texture
    private TextureRegion itemSlotTex;      // market item slot texture
    private TextureRegion itemHolderTex;    // market item holder texture

    /**
     * Market Buttons
     */

    private ColliderButton refreshButton; // refresh server data button
    private ArrayList<ColliderButton> buyButtons; // list of buy tab buttons
    private ArrayList<ColliderButton> sellButtons; // list of sell tab buttons
    private ArrayList<ColliderButton> listingsButtons; // list of listings tab buttons
    private ArrayList<ColliderButton> buyEntryButtons; // list of buy tab entry buttons
    private ArrayList<ColliderButton> sellEntryButtons; // list of sell tab entry buttons
    private ArrayList<TextField> sellEntryInputs;   // list of sell tab entry price inputs
    private ArrayList<ColliderButton> listingsEntryButtons; // list of listings tab entry buttons

    // TODO - Add filters to the market (level, rarity...),  sorting (level) and search (name)

    /**
     * Market constructor
     * Subscribes to server to be able to communicate with it
     * and initializes some properties like clipping values
     */
    public Market() {
        // subscribe to server to be able to communicate with it
        subscribeToServer();
        // initially selected tab is the first
        selectedTab = Tab.Buy;
        // initially all delta Y's are 0
        buyDeltaY = 0; sellDeltaY = 0; listingsDeltaY = 0;
        // cuts skin to obtain market texture regions
        backgroundTex = new TextureRegion(Resource.marketSkin, 0, 252, 462, 678);
        selectedTabTex = new TextureRegion(Resource.marketSkin, 0, 45, 122, 42);
        tabTex = new TextureRegion(Resource.marketSkin, 0, 0, 122, 42);
        buttonTex = new TextureRegion(Resource.marketSkin, 123, 47, 129, 41);
        pressedButtonTex = new TextureRegion(Resource.marketSkin, 123, 2, 129, 41);
        itemSlotTex = new TextureRegion(Resource.marketSkin, 252, 0, 87, 87);
        itemHolderTex = new TextureRegion(Resource.marketSkin, 0, 90, 445, 160);
        // calculates market coords
        marketX = (Config.baseWidth / 2) - (backgroundTex.getRegionWidth() * Config.marketScale / 2);
        marketY = (Config.baseHeight / 2) - (backgroundTex.getRegionHeight()  * Config.marketScale / 2);
        marketW = backgroundTex.getRegionWidth() * Config.marketScale;
        marketH = backgroundTex.getRegionHeight() * Config.marketScale;
        tabY = marketY + (marketH*0.997f);
        baseTabX = marketX;
        tabW = tabTex.getRegionWidth() * Config.marketScale * Config.marketTabScaleX;
        tabH = tabTex.getRegionHeight() * Config.marketScale * Config.marketTabScaleY;
        // define dimensions and base coords for market item entries
        entryW = marketW * 0.93f;
        entryH = marketH * 0.25f;
        entryX = marketX + (marketW * 0.5f) - (entryW * 0.5f);
        gapY = entryH + Config.marketGapBetweenItemsY;
        // create clipping scissors
        scissors = new Rectangle();
        float clipY = marketY+(marketH * 0.15f);
        float clipH = marketH * 0.77f;
        titleY = clipY + clipH;
        clipBounds = new Rectangle(marketX,clipY,marketW,clipH);
        // create market buttons
        createButtons();
        // build colliders
        buildTabColliders();
        // initializes lists
        marketItems = new ArrayList<MarketItem>();
        listings = new ArrayList<MarketItem>();
        // player gold info coords and dimensions
        pGoldX = marketX + marketW * 0.06f;
        pGoldY =  marketY + marketW * 0.1179f;
        goldIconW = marketW * 0.04f;
        goldIconH = marketW * 0.04f;
        // font heights
        normalFontHeight = new GlyphLayout(Resource.marketFont, "height").height;
        reducedFontHeight = normalFontHeight * 0.81f;
        // initialize inventory items market list
        inventoryItems = new ArrayList<MarketItem>();
        // initializes market stage
        marketStage = new Stage(new StretchViewport(Config.baseWidth, Config.baseHeight));
        // adds game stage as an input processor
        Main.getInstance().addInputProcessor(marketStage);
        // initially market is not active
        active = false;
    }

    /**
     * Refreshes market stage
     */
    public void refreshStage() {
        if(inventoryItems.size() < marketStage.getActors().size)
            marketStage.getActors().removeRange(inventoryItems.size(), marketStage.getActors().size-1);
    }

    /**
     * Creates market buttons
     */
    private void createButtons() {
        // buttons properties
        float refreshButtonW = buttonTex.getRegionWidth() * Config.marketButtonsScaleX;
        float refreshButtonH = buttonTex.getRegionHeight() * Config.marketButtonsScaleY;
        float refreshButtonX = marketX + marketW - refreshButtonW * 1.15f;
        float refreshButtonY = marketY + refreshButtonW * 0.15f;

        // creates buttons
        refreshButton = new ColliderButton(refreshButtonX, refreshButtonY, refreshButtonW, refreshButtonH,
                        buttonTex, pressedButtonTex, Resource.marketFont,
                        "marketButtonRefresh") {
            @Override
            public void onPress() {
            }

            @Override
            public void onRelease() {
                // refreshes market items depending on current tab
                if(selectedTab == Tab.Buy) {
                    refreshMarketItems();
                } else if(selectedTab == Tab.Listings) {
                    refreshPlayerListings();
                }
            }
        };

        // initializes lists
        buyButtons = new ArrayList<ColliderButton>();
        sellButtons = new ArrayList<ColliderButton>();
        listingsButtons = new ArrayList<ColliderButton>();
        buyEntryButtons= new ArrayList<ColliderButton>();
        sellEntryButtons = new ArrayList<ColliderButton>();
        sellEntryInputs = new ArrayList<TextField>();
        listingsEntryButtons = new ArrayList<ColliderButton>();

        // adds buttons to respective lists
        buyButtons.add(refreshButton);
        listingsButtons.add(refreshButton);
    }

    /**
     * Builds market tabs colliders that
     * will respond to touch inputs
     */
    private void buildTabColliders() {
        tabColliders = new ArrayList<Collider>(); // initializes array list
        // iterates tab enum to render each existent tab
        for(int i = 0; i < Tab.values().length; i++) {
            // creates trigger collider with gathered information to respond to touch inputs
            tabColliders.add(new Collider(baseTabX + (tabW * i), tabY, tabW, tabH, false, this));
        }
    }

    /**
     * Retrieves updated market data
     * by sending a market message to the server
     * @param character the player's character name
     */
    public void retrieveData(String character, Inventory playerInventory) {
        this.character = character; // stores character name
        this.playerInventory = playerInventory; // stores player inventory do be able to add items bought
        // if not connected do not try to send message to server
        if(!PucmonClient.getInstance().isConnected()) {
            return;
        }
        // refresh market items data
        refreshMarketItems();

        // create list of inventory items from inventory pages
        loadInventoryItems();
    }

    /**
     * Loads the player inventory items
     * into the list of market items to sell
     */
    private void loadInventoryItems() {
        inventoryItems.clear(); // clears inventory items
        ArrayList<Item[][]> pages = playerInventory.getPages(); // get pages
        // iterate through inventory pages
        for(int p = 0 ; p < pages.size(); p++) {
            for(int i = 0; i < pages.get(p).length; i++) {
                for(int j = 0; j < pages.get(p)[i].length; j++) {
                    if(pages.get(p)[i][j] != null) { // adds existent items
                        // creates market item from player inventory item
                        MarketItem sellableItem = new MarketItem();
                        sellableItem.setName(pages.get(p)[i][j].getName());
                        sellableItem.setLevel(0);
                        sellableItem.setUid(pages.get(p)[i][j].getId());
                        if(pages.get(p)[i][j].isEquipable()) {
                            sellableItem.setLevel(((Equipment) pages.get(p)[i][j]).getLevel());
                            sellableItem.setUid(((Equipment) pages.get(p)[i][j]).getUniqueID());
                        }
                        sellableItem.setDescription(pages.get(p)[i][j].getDescription());
                        sellableItem.setQuality(pages.get(p)[i][j].getQuality());
                        sellableItem.setSeller(character);
                        sellableItem.setPage(p);
                        sellableItem.setIdxI(i);
                        sellableItem.setIdxJ(j);
                        inventoryItems.add(sellableItem);
                    }
                }
            }
        }
    }

    /**
     * Keeps market entry buttons updated for selected tab
     */
    public void refreshEntryButtons() {
        switch(selectedTab) {
            case Buy:
                buyDeltaY = 0; // resets delta to avoid mess with clipping when removing item
                buyEntryButtons.clear(); // clears list to rebuild it
                // for each item entry, adds entry button
                for(int i = 0; i < marketItems.size(); i++) {
                    final int finalI = i;
                    buyEntryButtons.add(
                            new ColliderButton(0, 0, 0, 0,
                                    buttonTex, pressedButtonTex, Resource.marketFont,
                                    "marketButtonBuy") {
                                @Override
                                public void onPress() {
                                }

                                @Override
                                public void onRelease() {
                                    buyMarketItem(marketItems.get(finalI));
                                }
                            });
                }
                break;
            case Sell:
                sellDeltaY = 0; // resets delta to avoid mess with clipping when removing item
                sellEntryButtons.clear(); // clears list to rebuild it
                refreshStage(); // refreshes stage
                // for each item entry, adds entry button
                for(int i = 0; i < inventoryItems.size(); i++) {
                    final int finalI = i;
                    sellEntryButtons.add(
                            new ColliderButton(0, 0, 0, 0,
                                    buttonTex, pressedButtonTex, Resource.marketFont,
                                    "marketButtonSell") {
                                @Override
                                public void onPress() {
                                }

                                @Override
                                public void onRelease() {
                                    // only proceeds if text exists
                                    if(sellEntryInputs.get(finalI).getText() == "") {
                                        timedInfo(Main.getInstance().getLang().get("marketPriceBiggerThanZero"));
                                        return;
                                    }
                                    // gets user input price
                                    long price = Long.parseLong(sellEntryInputs.get(finalI).getText());
                                    // price cannot be zero or lower
                                    if(price <= 0) {
                                        timedInfo(Main.getInstance().getLang().get("marketPriceBiggerThanZero"));
                                        return;
                                    }
                                    // if price is ok (bigger than zero - digits limit is forced to user)
                                    MarketItem item = inventoryItems.get(finalI);
                                    // sets item price
                                    item.setPrice(price);

                                    // registers item into market
                                    registerMarketItem(item.getPage(), item.getIdxI(),
                                                        item.getIdxJ(), item.getPrice());

                                    // saves last input field to clear it if registering is ok
                                    lastInputField = sellEntryInputs.get(finalI);
                                }
                            });
                    // add sell entry price input
                    TextField price = new TextField("", Resource.gameSkin);
                    price.setMaxLength(Config.marketSellMaxDigits);
                    price.setMessageText(Main.getInstance().getLang().get("marketPrice"));
                    price.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
                    price.setAlignment(Align.center);
                    marketStage.addActor(price);
                    sellEntryInputs.add(price);
                }
                break;
            case Listings:
                listingsDeltaY = 0; // resets delta to avoid mess with clipping when removing item
                listingsEntryButtons.clear(); // clears list to rebuild it
                // for each item entry, adds entry button
                for(int i = 0; i < listings.size(); i++) {
                    final int finalI = i;
                    // if it is sold, add collect button
                    if(listings.get(i).isSold()) {
                        listingsEntryButtons.add(
                                new ColliderButton(0, 0, 0, 0,
                                        buttonTex, pressedButtonTex, Resource.marketFont,
                                        "marketButtonCollect") {
                                    @Override
                                    public void onPress() {
                                    }

                                    @Override
                                    public void onRelease() {
                                        collectListing(listings.get(finalI));
                                    }
                                });
                    } else { // else, add remove button
                        listingsEntryButtons.add(
                                new ColliderButton(0, 0, 0, 0,
                                        buttonTex, pressedButtonTex, Resource.marketFont,
                                        "marketButtonRemove") {
                                    @Override
                                    public void onPress() {
                                    }

                                    @Override
                                    public void onRelease() {
                                        removeListing(listings.get(finalI));
                                    }
                                });
                    }
                }
                break;
            default:
                System.err.println("Unknown tab selected: " + selectedTab);
                break;
        }
    }

    /**
     * Draws market visualization on screen
     * @param batch the batch to draw the market
     * @param camera camera to calculate clipping
     */
    public void render(SpriteBatch batch, Camera camera) {
        // if market is rendering, market is active
        active = true;

        // renders market background
        batch.draw(backgroundTex, marketX, marketY, marketW, marketH);

        // renders market tabs
        renderMarketTabs(batch);

        // if not connected, draw info string informing it and return
        if (!PucmonClient.getInstance().isConnected()) {
            infoStr = Main.getInstance().getLang().get("notConnected");
            renderInfoText(batch, marketX, marketY, marketW, marketH);
            return;
        }

        // if is retrieving data, draw info text to inform retrieving status
        if(isRetrieving) {
            // draws waiting server response text
            infoStr = Main.getInstance().getLang().get("waitingServerResponse");
            renderInfoText(batch, marketX, marketY, marketW, marketH);
        } else {
            switch (selectedTab) {
                case Buy:
                    renderBuyTab(batch, camera);
                    break;
                case Sell:
                    // stage act (if in sell tab)
                    marketStage.act();
                    renderSellTab(batch, camera);
                    break;
                case Listings:
                    renderListingsTab(batch, camera);
                    break;
                default:
                    System.err.println("Unknown tab selected in market: " + selectedTab.toString());
                    break;
            }
        }
    }

    /**
     * Renders buy market tab with all its components
     * @param batch the sprite to render buy tab
     * @param camera camera to calculate clipping
     */
    private void renderBuyTab(SpriteBatch batch, Camera camera) {
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
        int firstEntry = MathUtils.floor(buyDeltaY / gapY);
        // render market item entries for buy tab
        for(int i = 0; i < nEntriesShown; i++) {
            if(marketItems.size() <= 0) break;
            // clamp for safety
            int idx = MathUtils.clamp(i + firstEntry, 0, marketItems.size() - 1);
            renderMarketEntry(batch, marketItems.get(idx), idx, entryX,
                                (titleY - entryH) - (gapY * (idx)) + buyDeltaY,
                                entryW * Config.marketItemHolderScale, entryH * Config.marketItemHolderScale);
        }

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();

        // renders tab title
        renderTabTitle(batch, Main.getInstance().getLang().get("marketTabBuy"));

        // renders buy tab other buttons
        for(int i = 0; i < buyButtons.size(); i++)
            buyButtons.get(i).render(batch);

        // renders player gold amount info
        renderGold(batch, playerInventory.getGold(), pGoldX, pGoldY, goldIconW, goldIconH, true);

        // renders market info string
        renderMarketInfo(batch);
    }

    /**
     * Renders sell market tab with all its components
     * @param batch the sprite to render sell tab
     * @param camera camera to calculate clipping
     */
    private void renderSellTab(SpriteBatch batch, Camera camera) {
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
        int firstEntry = MathUtils.floor(sellDeltaY / gapY);
        // render market item entries for buy tab
        for(int i = 0; i < nEntriesShown; i++) {
            if(inventoryItems.size() <= 0) break;
            // clamp for safety
            int idx = MathUtils.clamp(i + firstEntry, 0, inventoryItems.size() - 1);
            renderMarketEntry(batch, inventoryItems.get(idx), idx, entryX,
                    (titleY - entryH) - (gapY * (idx)) + sellDeltaY,
                    entryW * Config.marketItemHolderScale, entryH * Config.marketItemHolderScale);
        }

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();

        // renders tab title
        renderTabTitle(batch, Main.getInstance().getLang().get("marketTabSell"));

        // renders sell tab buttons
        for(int i = 0; i < sellButtons.size(); i++)
            sellButtons.get(i).render(batch);

        // renders player gold amount info
        renderGold(batch, playerInventory.getGold(), pGoldX, pGoldY, goldIconW, goldIconH, true);

        // renders market info string
        renderMarketInfo(batch);
    }

    /**
     * Renders listings market tab with all its components
     * @param batch the sprite to render listings tab
     * @param camera camera to calculate clipping
     */
    private void renderListingsTab(SpriteBatch batch, Camera camera) {
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
        int firstEntry = MathUtils.floor(listingsDeltaY / gapY);
        // render market item entries for buy tab
        for(int i = 0; i < nEntriesShown; i++) {
            if(listings.size() <= 0) break;
            // clamp for safety
            int idx = MathUtils.clamp(i + firstEntry, 0, listings.size() - 1);
            renderMarketEntry(batch, listings.get(idx), idx, entryX,
                    (titleY - entryH) - (gapY * (idx)) + listingsDeltaY,
                    entryW * Config.marketItemHolderScale, entryH * Config.marketItemHolderScale);
        }

        // pop scissors to end clipping
        batch.flush();
        ScissorStack.popScissors();

        // renders tab title
        renderTabTitle(batch, Main.getInstance().getLang().get("marketTabListings"));

        // renders listings tab buttons
        for(int i = 0; i < listingsButtons.size(); i++)
            listingsButtons.get(i).render(batch);

        // renders player gold amount info
        renderGold(batch, playerInventory.getGold(), pGoldX, pGoldY, goldIconW, goldIconH, true);

        // renders market info string
        renderMarketInfo(batch);
    }

    /**
     * Renders market item entry visualization on screen
     * in given coordinates and dimensions received in parameter
     *
     * @param batch the sprite batch to render
     * @param item the market item to be rendered
     * @param itemIdx the index of item in the list of market items
     * @param x the x coordinate on screen
     * @param y the y coordinate on screen
     * @param w the width of market entry visualization
     * @param h the height of market entry visualization
     */
    private void renderMarketEntry(SpriteBatch batch, MarketItem item, int itemIdx, float x,
                                   float y, float w, float h) {
        // draws market item holder (background texture of market entries)
        batch.draw(itemHolderTex, x, y, w, h);

        // draws item slot on item holder for visualization of item sprite
        float slotW = w * 0.25f;
        float slotH = w * 0.25f;
        float slotX = x + (w * 0.03f);
        float slotY = y + (h/2) - (slotH/2);
        batch.draw(itemSlotTex, slotX, slotY, slotW, slotH);

        // draws item sprite on item slot for visualization
        TextureRegion itemSpriteTex = Factory.getItemSprite(item.getUid());
        float sprW = slotW * 0.8f;
        float sprH = slotH * 0.8f;
        float sprX = slotX + (slotW/2) - (sprW/2);
        float sprY = slotY + (slotH/2) - (sprH/2);
        batch.draw(itemSpriteTex, sprX, sprY, sprW, sprH);

        // gets action button based on current tab
        ColliderButton actionButton = null;
        switch (selectedTab) {
            case Buy:
                // for safety, do nothing if buttons are not correctly refreshed
                if(buyEntryButtons.size() <= 0 || itemIdx >= buyEntryButtons.size())
                    return;
                actionButton = buyEntryButtons.get(itemIdx);
                break;
            case Sell:
                // for safety, do nothing if buttons are not correctly refreshed
                if(sellEntryButtons.size() <= 0  || itemIdx >= sellEntryButtons.size())
                    return;
                actionButton = sellEntryButtons.get(itemIdx);
                break;
            case Listings:
                // for safety, do nothing if buttons are not correctly refreshed
                if(listingsEntryButtons.size() <= 0  || itemIdx >= listingsEntryButtons.size())
                    return;
                actionButton = listingsEntryButtons.get(itemIdx);
                break;
            default:
                System.err.println("Unknown selected tab: " + selectedTab);
                break;
        }

        // draws action button
        float btnW = w * 0.30f;
        float btnH = h * 0.20f;
        float btnX = (x + w) - btnW - (w * 0.05f);
        float btnY = y + h * 0.10f;
        actionButton.setPosition(btnX, btnY);
        actionButton.setDimension(btnW, btnH);
        actionButton.render(batch);

        // draws item name
        String nameStr = Factory.getItemName(item.getUid());
        Resource.marketFont.setColor(Common.getQualityColor(item.getQuality()));
        float nameX = slotX + slotW + (w * 0.066f);
        float nameY = (y + h) - normalFontHeight  - (h * 0.01f);
        Resource.marketFont.draw(batch, nameStr, nameX, nameY);

        // scales font for other data
        Resource.marketFont.getData().setScale(Config.marketTextScale * 0.81f);

        // draw item rarity
        String itemQuality = Common.capitalize(item.getQuality().toString());
        float qX = nameX;
        float qY = nameY - normalFontHeight - (h * 0.024f);

        // draw item level (if it is an equipment)
        if(item.getLevel() > 0) {
            String itemLevel = Main.getInstance().getLang().format("levelInfo", item.getLevel());
            itemQuality += "  " + itemLevel;
        }

        // draws item quality (with level in case of equipment)
        Resource.marketFont.draw(batch, itemQuality, qX, qY);

        // draws item seller str and price info (if not in sell tab)
        if(selectedTab != Tab.Sell) {
            // item seller info
            String itemSeller = Main.getInstance().getLang().format("marketSoldBy", item.getSeller());
            float sellerX = nameX;
            float sellerY = btnY + reducedFontHeight + (h * 0.27f);
            Resource.marketFont.setColor(Config.marketTextColor);
            Resource.marketFont.draw(batch, itemSeller, sellerX, sellerY);
            // renders price info
            float priceX = nameX;
            float priceY = qY - normalFontHeight - (h * 0.079f);
            float goldIconW = w * 0.046f;
            float goldIconH = w * 0.046f;
            renderGold(batch, item.getPrice(), priceX, priceY, goldIconW, goldIconH, false);
        } else { // in case of sell tab, draws input text field to retrieve item price
            TextField priceInput = sellEntryInputs.get(itemIdx);
            float inputW = w * 0.30f;
            float inputH = h * 0.20f;
            float inputX = x + (w * 0.32f);
            float inputY = y + h * 0.10f;
            priceInput.setPosition(inputX, inputY);
            priceInput.setWidth(inputW);
            priceInput.setHeight(inputH);
            priceInput.getStyle().fontColor = Color.GOLD;
            priceInput.getStyle().disabledFontColor = Color.ORANGE;
            priceInput.setColor(Color.FOREST);
            batch.setColor(Color.FOREST);
            priceInput.draw(batch, 1f);
            priceInput.setColor(Color.WHITE);
            batch.setColor(Color.WHITE);
        }

        // resets font
        Resource.marketFont.setColor(Config.marketTextColor);
        Resource.marketFont.getData().setScale(Config.marketTextScale);
    }

    /**
     * Renders clipping frame to wrap market entries
     *
     * @param batch the sprite batch to render
     */
    private void renderClippingFrame(SpriteBatch batch) {
        float clipY = marketY+(marketH * 0.15f);
        float clipH = marketH * 0.77f;
        float expansionY = 2f;
        batch.setColor(Config.marketClippingFrameColor); // tint next drawing
        batch.draw(backgroundTex, entryX, clipY-expansionY, entryW*Config.marketItemHolderScale, clipH+(2f*expansionY));
        batch.setColor(Color.WHITE); // resets tint
    }

    /**
     * Renders market information string on market interface
     * @param batch the sprite batch to draw
     */
    private void renderMarketInfo(SpriteBatch batch) {
        if(infoStr == "" || infoStr == null) // ignore if there is no info message
            return;

        // increases infoTimer
        infoTimer += Gdx.graphics.getDeltaTime();

        // sets color of info message
        Resource.marketFont.setColor(Config.marketInfoColor);
        // sets coordinates of info message
        float infoX = marketX + marketW/2f - new GlyphLayout(Resource.marketFont, infoStr).width/2f;
        float infoY = marketY + marketH * 0.14f;
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
     * Renders market tab title
     * @param batch the batch to render
     * @param title the title to be rendered in current tab
     */
    private void renderTabTitle(SpriteBatch batch, String title) {
        // calculate coordinates
        float tabTitleX = marketX + marketW/2f - new GlyphLayout(Resource.marketFont, title).width/2f;
        float tabTitleY = marketY + marketH - normalFontHeight - (marketH*0.001f);
        Resource.marketFont.setColor(Color.GOLDENROD);
        // draws text outlines in outline color
        Resource.marketFont.draw(batch, title, tabTitleX+1, tabTitleY-1);
        Resource.marketFont.draw(batch, title, tabTitleX-1, tabTitleY+1);
        // draws text in foreground color
        Resource.marketFont.setColor(Color.GRAY);
        Resource.marketFont.draw(batch, title, tabTitleX, tabTitleY);
        // resets font color
        Resource.marketFont.setColor(Config.marketTextColor);
    }

    /**
     * Renders gold amount alongside with
     * gold icon sprite with parameters position and dimension
     * @param batch the sprite batch to draw
     * @param gold  the amount of gold to render
     * @param x     the x position on screen
     * @param y     the y position on screen
     * @param iconW gold icon desired width
     * @param iconH gold icon desired height
     * @param bg    render with background?
     */
    private void renderGold(SpriteBatch batch, long gold, float x, float y, float iconW, float iconH, boolean bg) {
        // gets string from gold amount
        String itemPrice = Main.getInstance().getLang().format("marketCost", gold);

        // renders background if wanted
        if(bg) {
            float bgExpansionX = 18f; float bgExpansionY = 14f;
            float bgW = new GlyphLayout(Resource.marketFont, itemPrice).width + iconW + iconW * 0.5f + bgExpansionX;
            float bgH = normalFontHeight + bgExpansionY;
            float bgX = x - bgExpansionX/2f;
            float bgY = y + bgExpansionY/2f - normalFontHeight - bgExpansionY;
            batch.draw(pressedButtonTex, bgX, bgY, bgW, bgH);
        }

        // draws item cost str
        Resource.marketFont.getData().setScale(Config.marketTextScale);
        Resource.marketFont.setColor(Color.GOLD);
        Resource.marketFont.draw(batch, itemPrice, x, y);

        // draws gold icon next to item cost str
        TextureRegion goldIcon = new TextureRegion(Resource.inventoryBG, 134, 621, 15, 16);
        float goldIconX = x + new GlyphLayout(Resource.marketFont, itemPrice).width + iconW * 0.46f;
        float goldIconY = y - normalFontHeight - iconH * 0.082f;
        batch.draw(goldIcon, goldIconX, goldIconY, iconW, iconH);

        // resets font
        Resource.marketFont.setColor(Config.marketTextColor);
    }

    /**
     * Renders market tabs on market interface
     * @param batch the batch to draw market tabs
     */
    private void renderMarketTabs(SpriteBatch batch) {
        // gets selected tab ordinal in enum
        int selectedOrdinal = selectedTab.ordinal();

        // iterates tab enum to render each existent tab
        for(int i = 0; i < Tab.values().length; i++) {
            if(selectedOrdinal == i) { // renders selected tab with selected tab texture
                batch.draw(selectedTabTex, baseTabX + (tabW * i), tabY, tabW, tabH);
            } else { // renders other tabs with normal tab texture
                batch.draw(tabTex, baseTabX + (tabW * i), tabY, tabW, tabH);
            }
            // draws tab text based on tab enum
            String tabText = Main.getInstance().getLang().get("marketTab"+Tab.values()[i].toString());
            GlyphLayout tabTextLayout = new GlyphLayout(Resource.marketFont, tabText);
            float tabTextX = baseTabX + (tabW * i) + (tabW/2) - (tabTextLayout.width/2);
            float tabTextY = tabY + (tabH*0.6f);
            Resource.marketFont.draw(batch, tabText, tabTextX, tabTextY);
        }
    }

    /**
     * Draws the current information string centralized in the market interface
     * @param batch the batch to draw
     * @param marketX the x position of market menu
     * @param marketY the y position of market menu
     * @param marketW the width of market menu
     * @param marketH the height of market menu
     */
    private void renderInfoText(SpriteBatch batch, float marketX, float marketY, float marketW, float marketH) {
        final GlyphLayout svInfoLayout = new GlyphLayout(Resource.marketFont, infoStr);
        float svInfoX = marketX + (marketW/2) - (svInfoLayout.width/2);
        float svInfoY = marketY + (marketH/2) - (svInfoLayout.height/2);
        Resource.marketFont.draw(batch, svInfoLayout , svInfoX, svInfoY);
        // TODO - be careful with width bounds (resize on necessity?)
        // TODO - improve the FPS on market rendering
    }

    /**
     * For latency tests
     */
    private long svMarketTS;

    /**
     * Refreshes list of market items
     * by sending a message to server
     * to receive the updated list
     */
    public void refreshMarketItems() {
        isRetrieving = true; // sets retrieving data flag
        // sends message to server to retrieve market data
        // market message
        MessageMarket marketMsg = new MessageMarket(character, MessageMarket.Action.RETRIEVE_ITEMS);
        // wraps market message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), marketMsg, MessageContent.Type.MARKET);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
        //for latency test
        svMarketTS = System.currentTimeMillis();
    }

    /**
     * Refreshes list of player listings
     * by sending a message to server
     * to receive the updated list
     */
    public void refreshPlayerListings() {
        isRetrieving = true; // sets retrieving data flag
        // sends message to server to retrieve listings data
        // market message
        MessageMarket marketMsg = new MessageMarket(character, MessageMarket.Action.SHOW_LISTINGS);
        // wraps market message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), marketMsg, MessageContent.Type.MARKET);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
    }

    /**
     * Tries to buy market item.
     * If player has enough gold, send server message.
     * If item is not sold by the time server receives message,
     * the handle message callback will deal with item bought
     *
     * @param item the item to try to buy
     */
    public void buyMarketItem(MarketItem item) {
        // first, checks if player has enough gold and if not, return after informing
        if(item.getPrice() > playerInventory.getGold()) {
            timedInfo(Main.getInstance().getLang().get("marketNotEnoughGold"));
            return;
        }

        // also checks if player inventory is full, and if so, return after informing
        if(playerInventory.isFull()) {
            timedInfo(Main.getInstance().getLang().get("marketFullInv"));
            return;
        }

        isRetrieving = true; // sets retrieving data flag
        // market message
        MessageMarket marketMsg = new MessageMarket(character, MessageMarket.Action.BUY_ITEM);
        // sets item to be bought (try)
        marketMsg.setItem(item);
        // wraps market message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), marketMsg, MessageContent.Type.MARKET);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
    }

    /**
     * Tries to register item to market
     *
     * @param page the page of item to register into market
     * @param idxi the idxi of item in page to register into market
     * @param idxj the idxj of item in page to register into market
     * @param price the price of item to register into market
     */
    private void registerMarketItem(int page, int idxi, int idxj, long price) {
        isRetrieving = true; // sets retrieving data flag
        // gets item
        Item item = playerInventory.getPages().get(page)[idxi][idxj];
        // market message
        MessageMarket marketMsg = new MessageMarket(character, MessageMarket.Action.REGISTER_ITEM);
        // builds market item
        MarketItem marketItem = new MarketItem();
        // checks if is equippable to set correct properties
        if(!item.isEquipable()) {
            marketItem.setUid(item.getId());
            marketItem.setLevel(0);
        }
        else {
            marketItem.setUid(((Equipment) item).getUniqueID());
            marketItem.setLevel(((Equipment) item).getLevel());
        }
        // sets item seller
        marketItem.setSeller(character);
        // sets item price
        marketItem.setPrice(price);
        // sets item quality
        marketItem.setQuality(item.getQuality());
        // sets item page
        marketItem.setPage(page);
        // sets item indexes
        marketItem.setIdxI(idxi); marketItem.setIdxJ(idxj);
        // sets item to be registered
        marketMsg.setItem(marketItem);
        // wraps market message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), marketMsg, MessageContent.Type.MARKET);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
    }

    /**
     * Removes market item received in parameter from player listings
     * @param listing the item to be removed from player listings
     */
    public void removeListing(MarketItem listing) {
        // checks if player inventory is full, and if so, return after informing
        // that remove from listing is not possible since there is no space in inventory to removed item
        if(playerInventory.isFull()) {
            timedInfo(Main.getInstance().getLang().get("marketFullInvRemove"));
            return;
        }

        isRetrieving = true; // sets retrieving data flag
        // sends message to server to remove listing
        // market message
        MessageMarket marketMsg = new MessageMarket(character, MessageMarket.Action.REMOVE_ITEM);
        // sets item to be removed from listings
        marketMsg.setItem(listing);
        // wraps market message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), marketMsg, MessageContent.Type.MARKET);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
    }

    /**
     * Collects market item sold received in parameter from player listings
     * @param listing the item to have gold equivalent to its price collected from player listings
     */
    public void collectListing(MarketItem listing) {
        isRetrieving = true; // sets retrieving data flag
        // sends message to server to remove listing
        // market message
        MessageMarket marketMsg = new MessageMarket(character, MessageMarket.Action.COLLECT);
        // sets item to be removed from listings
        marketMsg.setItem(listing);
        // wraps market message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), marketMsg, MessageContent.Type.MARKET);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
    }

    @Override
    public void subscribeToServer() {
        ServerMessages.getInstance().subscribe(this); // subscribes to server messages to be able to listen to them
    }

    @Override
    public void handleServerMessage(MessageContent msg) {
        if(Config.debug)
            Common.printLatency("Market retrieving", System.currentTimeMillis() - svMarketTS);

        // stores received message data
        MessageFlag svResponseFlag = msg.getFlag();
        Object svResponseObject = msg.getContent();
        MessageContent.Type svResponseType = msg.getType();

        // switches types of messages received
        switch(svResponseType) {
            case MARKET:
                // cast object to message market
                MessageMarket msgMarket = ((MessageMarket) svResponseObject);
                isRetrieving = false; // retrieve complete
                // server response was ok?
                if(svResponseFlag == MessageFlag.OKIDOKI) {
                    // switch between types of actions to correctly handle message
                    switch(msgMarket.getAction()) {
                        case RETRIEVE_ITEMS:
                            handleMarketRetrieving(msgMarket);
                            break;
                        case BUY_ITEM:
                            handleMarketItemBought(msgMarket);
                            break;
                        case REGISTER_ITEM:
                            handleMarketItemRegistered(msgMarket);
                            break;
                        case REMOVE_ITEM:
                            handleRemovedListing(msgMarket);
                            break;
                        case SHOW_LISTINGS:
                            handleListingsRetrieving(msgMarket);
                            break;
                        case COLLECT:
                            handleCollectedListing(msgMarket);
                            break;
                        default:
                            System.err.println("Unknown type of market action performed: " + msgMarket.getAction());
                            break;
                    }

                } else { // if not server transaction was not successfully complete, show info
                    switch(msgMarket.getAction()) {
                        case BUY_ITEM:
                            if(svResponseFlag == MessageFlag.ITEM_ALREADY_BOUGHT)
                                timedInfo(Main.getInstance().getLang().get("itemBought"));
                            else
                                timedInfo(Main.getInstance().getLang().get("generalError"));
                            break;
                        case RETRIEVE_ITEMS:
                            timedInfo(Main.getInstance().getLang().get("marketNoItemsSold"));
                            break;
                        case REMOVE_ITEM:
                            if(svResponseFlag == MessageFlag.ITEM_ALREADY_SOLD)
                                timedInfo(Main.getInstance().getLang().get("marketAlreadySold"));
                            break;
                        case SHOW_LISTINGS:
                            if(svResponseFlag == MessageFlag.NO_ITEMS_SOLD_BY_PLAYER)
                                timedInfo(Main.getInstance().getLang().get("marketNoListings"));
                            break;
                        case COLLECT:
                            if(svResponseFlag == MessageFlag.ITEM_ALREADY_COLLECTED)
                                timedInfo(Main.getInstance().getLang().get("marketAlreadyCollected"));
                            break;
                        default:
                            System.err.println("An error has occurred");
                            timedInfo(Main.getInstance().getLang().get("generalError"));
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
     * Handles successful player listing collection
     * @param msgMarket the market message containing data of the collected listing
     */
    private void handleCollectedListing(MessageMarket msgMarket) {
        // collects gold for the respective collected item
        playerInventory.addGold(msgMarket.getItem().getPrice());
        // remove listing from player listings
        removeMarketItemFromList(listings, msgMarket.getItem().getMid());
        // refresh entry buttons
        refreshEntryButtons();
        // info ok
        timedInfo(Main.getInstance().getLang().get("marketCollectionOk"));
    }

    /**
     * Handles successful player listing removal
     * @param msgMarket the market message containing data of the removed listing
     */
    private void handleRemovedListing(MessageMarket msgMarket) {
        // we only reach this point if inventory is not full, add removed item to inventory
        playerInventory.addItem(Factory.createItem(msgMarket.getItem().getUid(), msgMarket.getItem().getLevel(), false));
        // remove listing from player listings
        removeMarketItemFromList(listings, msgMarket.getItem().getMid());
        // refresh entry buttons
        refreshEntryButtons();
        // info ok
        timedInfo(Main.getInstance().getLang().get("marketRemovingOk"));
    }

    /**
     * Handles successful player listings retrieving
     * @param msgMarket the market message containing data of player listings received from server
     */
    private void handleListingsRetrieving(MessageMarket msgMarket) {
        listings = msgMarket.getItems(); // stores updated player listings received from server
        // refresh entry buttons
        refreshEntryButtons();

        System.out.println("Player Listings: ");
        for (int i = 0; i < listings.size(); i++) {
            // updates item name and description with factory information
            listings.get(i).setName(Factory.getItemName(listings.get(i).getUid()));
            listings.get(i).setDescription(Factory.getItemDescription(listings.get(i).getUid(), listings.get(i).getLevel()));
            System.out.println("Item Name: " + listings.get(i).getName());
            System.out.println("Item Level: " + listings.get(i).getLevel());
            System.out.println("Item Description: " + listings.get(i).getDescription());
            System.out.println("Item Quality: " + listings.get(i).getQuality().toString());
            System.out.println("Item Price: " + listings.get(i).getPrice());
            System.out.println("Item Seller: " + listings.get(i).getSeller());
            System.out.println("Item UID: " + listings.get(i).getUid());
            System.out.println("Item MID: " + listings.get(i).getMid());
            System.out.println("Item Sold?: " + listings.get(i).isSold());
        }

        // sets info string informing success of retrieving
        timedInfo(Main.getInstance().getLang().get("marketListingsRetrievingOk"));
    }

    /**
     * Handles successful item registration in market by player
     * @param msgMarket the market message containing data of registered item received from server
     */
    private void handleMarketItemRegistered(MessageMarket msgMarket) {
        // removes item from player inventory as it has been successfully added to market
        playerInventory.removeItem(msgMarket.getItem().getPage(), msgMarket.getItem().getIdxI(),
                        msgMarket.getItem().getIdxJ());
        // refreshes inventory items
        loadInventoryItems();
        // refresh entry buttons
        refreshEntryButtons();
        // info ok
        timedInfo(Main.getInstance().getLang().get("marketRegisteringOk"));
        // clears input text
        if(lastInputField != null) {
            lastInputField.setText("");
            marketStage.setKeyboardFocus(null);
        }
    }

    /**
     * Handles market items being bought by player
     * @param msgMarket the market message containing market item data received from server
     */
    private void handleMarketItemBought(MessageMarket msgMarket) {
        // removes gold amount equivalent of item bought price, we only reach this point if player has enough gold to buy item
        playerInventory.removeGold(msgMarket.getItem().getPrice());
        // we only reach this point if inventory is not full, add bought item to inventory
        playerInventory.addItem(Factory.createItem(msgMarket.getItem().getUid(), msgMarket.getItem().getLevel(), false));
        // refreshes list of market items if items was successfully bought
        removeMarketItemFromList(marketItems, msgMarket.getItem().getMid());
        // refreshes entry buttons
        refreshEntryButtons();
        // inform success of buying
        timedInfo(Main.getInstance().getLang().format("marketBoughtItem",
                  Factory.getItemName(msgMarket.getItem().getUid()),
                  Main.getInstance().getLang().format("levelInfo", msgMarket.getItem().getLevel())));
    }

    /**
     * Handles market items data retrieving
     * @param msgMarket the market message containing market items data received from server
     */
    private void handleMarketRetrieving(MessageMarket msgMarket) {
        marketItems = msgMarket.getItems(); // stores updated market items received from server
        // refresh entry buttons
        refreshEntryButtons();

        if(Config.debug)
            System.out.println("Market Items: ");
        for (int i = 0; i < marketItems.size(); i++) {
            // updates item name and description with factory information
            marketItems.get(i).setName(Factory.getItemName(marketItems.get(i).getUid()));
            marketItems.get(i).setDescription(Factory.getItemDescription(marketItems.get(i).getUid(), marketItems.get(i).getLevel()));
            if(Config.debug) {
                System.out.println("Item Name: " + marketItems.get(i).getName());
                System.out.println("Item Level: " + marketItems.get(i).getLevel());
                System.out.println("Item Description: " + marketItems.get(i).getDescription());
                System.out.println("Item Quality: " + marketItems.get(i).getQuality().toString());
                System.out.println("Item Price: " + marketItems.get(i).getPrice());
                System.out.println("Item Seller: " + marketItems.get(i).getSeller());
                System.out.println("Item UID: " + marketItems.get(i).getUid());
                System.out.println("Item MID: " + marketItems.get(i).getMid());
            }
        }

        // sets info string informing success of retrieving
        timedInfo(Main.getInstance().getLang().get("marketRetrievingOk"));
    }

    /**
     * Removes an element of MarketItem type from an array list of MarketItem type
     * by its market id (mid) unique identifier
     * @param marketItemList the market item list to remove a market item by its mid
     * @param mid the market id of the market item to be removed from market item list received via parameter
     */
    private void removeMarketItemFromList(ArrayList<MarketItem> marketItemList, int mid) {
        for(int i = 0; i < marketItemList.size(); i++) { // iterates through list
            if(marketItemList.get(i).getMid() == mid) { // if mid is found
                marketItemList.remove(i); // removes item with mid received in parameter
                return; // stops loop as mid is unique and it was already found
            }
        } // does not care if mid was not found in list
    }

    /**
     * Input management
     */

    /**
     * Check if touches are on tab colliders and performs needed operations
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    private void checkTabTouch(int screenX, int screenY) {
        // iterates through tab colliders
        for(int i = 0; i < tabColliders.size(); i++) {
            // if collision happens to a tab
            if(tabColliders.get(i).checkTouchCollision(new Vector2(screenX, screenY))) {
                selectTab(Tab.values()[i]); // updates selected tab
            }
        }
    }

    /**
     * Selects a tab from market interface refreshing its data
     * @param tab   the tab to be selected
     */
    private void selectTab(Tab tab) {
        selectedTab = tab;

        // switch tabs to correct refresh server data in case of necessity
        switch (selectedTab) {
            case Buy:
                infoStr = ""; // resets info string
                isRetrieving = false; // stops retrieving old data
                Common.setTouchableStage(marketStage, Touchable.disabled); // sets stage touchable policy
                refreshMarketItems();
                break;
            case Sell:
                infoStr = ""; // resets info string
                isRetrieving = false; // stops retrieving old data
                Common.setTouchableStage(marketStage, Touchable.enabled); // sets stage touchable policy
                loadInventoryItems(); // refresh inventory items
                refreshEntryButtons(); // refresh entry sell buttons to match player inventory items
                break;
            case Listings:
                infoStr = ""; // resets info string
                isRetrieving = false; // stops retrieving old data
                Common.setTouchableStage(marketStage, Touchable.disabled); // sets stage touchable policy
                refreshPlayerListings();
                break;
            default:
                System.err.println("Unknown tab selected in market: " + selectedTab.toString());
                break;
        }

        refreshEntryButtons();
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
     * Input callbacks
     */

    /**
     * Called when there is a touch on screen
     * and market is opened.
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    public void touchDown(int screenX, int screenY) {
        // deal with market tab collision
        checkTabTouch(screenX, screenY);
        // deal with market button touches
        checkButtonTouch(screenX, screenY);
    }

    /**
     * Check market button touches and handles it
     * @param screenX   the x position of touch
     * @param screenY   the y position of touch
     */
    private void checkButtonTouch(int screenX, int screenY) {
        // current tab buttons
        ArrayList<ColliderButton> currentButtons;
        ArrayList<ColliderButton> currentEntryButtons;
        // switch tabs to store current tab buttons
        switch (selectedTab) {
            case Buy:
                currentButtons = buyButtons;
                currentEntryButtons = buyEntryButtons;
                break;
            case Sell:
                currentButtons = sellButtons;
                currentEntryButtons = sellEntryButtons;
                break;
            case Listings:
                currentButtons = listingsButtons;
                currentEntryButtons = listingsEntryButtons;
                break;
            default:
                currentButtons = buyButtons;
                currentEntryButtons = buyEntryButtons;
                System.err.println("Unknown tab selected in market: " + selectedTab.toString());
                break;
        }

        // sends touch data to each button of current tab
        for(int i = 0; i < currentButtons.size(); i++) {
            currentButtons.get(i).touchDown(screenX, screenY);
        }
        // sends touch data to each entry button of current tab
        for(int i = 0; i < currentEntryButtons.size(); i++) {
            currentEntryButtons.get(i).touchDown(screenX, screenY);
        }
    }

    /**
     * Called when there is a touch lift on screen
     * and market is opened.
     * @param screenX   the x position of touch lift
     * @param screenY   the y position of touch lift
     */
    public void touchUp(int screenX, int screenY) {
        // deal with market button lift
        checkButtonLift(screenX, screenY);
    }

    /**
     * Check market button lifts and handles it
     * @param screenX   the x position of lift
     * @param screenY   the y position of lift
     */
    private void checkButtonLift(int screenX, int screenY) {
        // current tab buttons
        ArrayList<ColliderButton> currentButtons;
        ArrayList<ColliderButton> currentEntryButtons;

        // switch tabs to store current tab buttons
        switch (selectedTab) {
            case Buy:
                currentButtons = buyButtons;
                currentEntryButtons = buyEntryButtons;
                break;
            case Sell:
                currentButtons = sellButtons;
                currentEntryButtons = sellEntryButtons;
                break;
            case Listings:
                currentButtons = listingsButtons;
                currentEntryButtons = listingsEntryButtons;
                break;
            default:
                currentButtons = buyButtons;
                currentEntryButtons = buyEntryButtons;
                System.err.println("Unknown tab selected in market: " + selectedTab.toString());
                break;
        }

        // sends touch data to each button of current tab
        for(int i = 0; i < currentButtons.size(); i++) {
            currentButtons.get(i).touchUp(screenX, screenY);
        }
        // sends touch data to each entry button of current tab
        for(int i = 0; i < currentEntryButtons.size(); i++) {
            currentEntryButtons.get(i).touchUp(screenX, screenY);
        }
    }

    /**
     * Called when there is a fling gesture on screen
     * (user drag touch and lift it) and market is opened
     * @param velocityX velocity on x axis in seconds
     * @param velocityY velocity on y axis in seconds
     */
    public void fling(float velocityX, float velocityY) {
        // check if velocity is bigger than minimum to consider for tab switch
        if(Math.abs(velocityX) >= Config.marketMinVelocityTabSwitch) {
            // gets ordinal of selected tab
            int selOrdinal = selectedTab.ordinal();
            // if it is, check direction of fling
            if(velocityX > 0) // right direction, should decrease tab number
                selOrdinal--;
            else // left direction, should increase tab number
                selOrdinal++;

            // avoid tab number getting out of bounds
            if(selOrdinal < 0)
                return;
            if(selOrdinal > Tab.values().length - 1)
                return;

            // sets the new selected tab
            selectTab(Tab.values()[selOrdinal]);
        }
    }

    /**
     * Called when the user drags a finger over the screen and market is opened.
     * @param deltaX the difference in pixels to the last drag event on x
     * @param deltaY the difference in pixels to the last drag event on y
     */
    public void pan(float deltaX, float deltaY) {

        float selectedDelta = 0;
        ArrayList<MarketItem> selectedItems;

        // switches selected tab to proper navigate respective items
        switch (selectedTab) {
            case Buy:
                selectedDelta = this.buyDeltaY;
                selectedItems = marketItems;
                break;
            case Sell:
                selectedDelta = this.sellDeltaY;
                selectedItems = inventoryItems;
                break;
            case Listings:
                selectedDelta = this.listingsDeltaY;
                selectedItems = listings;
                break;
            default:
                selectedDelta = this.buyDeltaY;
                selectedItems = marketItems;
                System.err.println("Unknown tab selected in market: " + selectedTab.toString());
                break;
        }

        // calculates movement on Y axis
       selectedDelta -= deltaY * Config.marketSensitivityY * Gdx.graphics.getDeltaTime();
        if (selectedDelta < 0) // clamp delta Y inferior limit
            selectedDelta = 0;
        // calculates max delta based on the current selected tab amount of items
        if (selectedItems != null) {
            int nEntriesShown = MathUtils.floor(clipBounds.height / gapY);
            float maxDeltaY = (selectedItems.size() - nEntriesShown) * gapY - (entryH);
            if(maxDeltaY < 0) maxDeltaY = 0;

            if (selectedDelta > maxDeltaY) // clamps to avoid surpassing max delta Y
                selectedDelta = maxDeltaY;
        }

        if(selectedTab == Tab.Listings)
            this.listingsDeltaY = selectedDelta;
        else if(selectedTab == Tab.Sell)
            this.sellDeltaY = selectedDelta;
        else
            this.buyDeltaY = selectedDelta;
    }

    /**
     * returns if market is active or not
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * disable market
     */
    public void disable () {
        Common.setTouchableStage(marketStage, Touchable.disabled);
        active = false;
    }

    /**
     * enables market
     */
    public void enable () {
        Common.setTouchableStage(marketStage, Touchable.enabled);
        active = true;
    }

    /**
     * Returns the current selected tab
     * @return the current selected tab
     */
    public Tab getSelectedTab() {
        return selectedTab;
    }
}
