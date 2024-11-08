package com.mygdx.game.states.game.standard.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.architecture.GameState;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.enemy.Enemy;
import com.mygdx.game.states.game.standard.item.Equipment;
import com.mygdx.game.states.game.standard.item.factory.Factory;
import com.mygdx.game.states.game.standard.message.TextMessage;
import com.mygdx.game.states.game.standard.message.Texts;
import com.mygdx.game.states.game.standard.player.Player;
import com.mygdx.game.states.game.standard.player.PlayerAnimator;
import com.mygdx.game.states.game.standard.sfx.SFX;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that represents a battle that
 * can be of random occurrence or be
 * a fixed battle (bosses)
 *
 * @author Pedro Sampaio
 * @since 0.9
 */
public class Battle implements GameState {
    Enemy enemy; // the enemy that player will battle with
    Player player; // the player's reference
    private SpriteBatch battleBatch; // the spritebatch of battle to render
    private OrthographicCamera camera; // camera to adjust to screen resolution
    private StretchViewport viewport; // viewport to use with camera to adjust to screen res
    private Stage battleStage;	// the battle UI Stage
    private Music battleBGM;	// the background music for the battle
    private Texture battleBG; // background image for the battle
    private Texture battleHUD; // the battle hud source image
    private Texture battleEnemyHUD; // the battle enemy hud source image
    private int bgIndex; // the index for the battle background spawned
    private double lastAttack; // stores last attack time to calculate attack delta time
    private double deltaAttack; // time passed since last attack
    private int pCurrentHealth; // player current health in battle
    private int eCurrentHealth; // enemy current health in battle
    private float pAutoTimer = 0f; // timer to help control player auto attacks
    private float eAutoTimer = 0f; // timer to help control enemy auto attacks
    private ArrayList<DamageText> dmgTexts; // list with currently visible damage texts
    private BitmapFont battleFont; // battle text font
    private BitmapFont damageFont; // damage text font
    private TextMessage msgText; // text message that will contain reward information
    private boolean isTexting; // if a text message is currently being drawn
    private boolean isShrinking; // if loser is currently shrinking
    private float shrinkTimer; // timer to control battle loser shrinking
    private boolean win; // has player won the battle?
    private float playerScale; // player scale in battle
    private float enemyScale; // enemy scale in battle

    /**
     * Battle constructor
     *
     * @param enemy     the enemy that player will battle with
     * @param player    the player's reference
     */
    public Battle (Enemy enemy, Player player,  int bgIndex) {
        this.enemy = enemy;
        this.player = player;
        this.bgIndex = bgIndex;
        this.isTexting = false; // initially no text message is being drawn
        this.shrinkTimer = 0f; // initializes shrink timer
        this.isShrinking = false; // initially loser is not shrinking
        this.win = false; // no win yet
        playerScale = 1f; //initially scale is not altered;
        enemyScale = 1f; // initially scale is not altered;
        lastAttack = System.currentTimeMillis();
        pCurrentHealth = player.getAttributes().getMaxHealth(); // player initially at max health
        eCurrentHealth = enemy.getAttributes().getMaxHealth();  // enemy initially at max health
        deltaAttack = 0f;
        dmgTexts = new ArrayList<DamageText>();
    }

    /************************
     * Game State callbacks *
     ************************/

    @Override
    public void create() {
        // initializes spritebatch
        battleBatch = new SpriteBatch();

        // sets camera for battle scaling on different screens
        camera = new OrthographicCamera(Config.baseWidth, Config.baseHeight);
        camera.setToOrtho(false, Config.baseWidth, Config.baseHeight);

        // sets viewport behaviour on scaling for different screens
        viewport = new StretchViewport(Config.baseWidth, Config.baseHeight, camera);
        viewport.apply();

        // gets background randomly chosen
        battleBG = Resource.battleBGs.get(bgIndex);

        // gets battle hud texture
        battleHUD = Resource.battleHUD;

        // gets battle enemy hud texture
        battleEnemyHUD = Resource.battleEnemyHUD;

        // gets battle text font
        battleFont = Resource.battleFont;

        // gets damage font
        damageFont = Resource.damageFont;

        // gets battle bgm
        battleBGM = Resource.battleBGM;

        // plays background music
        battleBGM.play(); // starts playing

        // initializes battle UI stage2d
        battleStage = new Stage(new StretchViewport(Config.baseWidth, Config.baseHeight));

        // adds battle stage as an input processor
        Main.getInstance().addInputProcessor(battleStage);

        // send battle idle command to player animator to start battle in idle state
        player.getAnimator().triggerCommand(PlayerAnimator.animCommands.enterIdleBattle);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        battleStage.getViewport().update(width, height);
    }

    @Override
    public void render() {
        // update auto attacks if not texting
        if(!isTexting && !isShrinking)
            updateAutoAtk();

        // if shrinking, updates shrink scale
        if(isShrinking) {
            if(win) // if player has win, shrink enemy
                enemyScale -= Config.shrinkFactor * Gdx.graphics.getDeltaTime();
            else // shrink players instead, in case of defeat
                playerScale -= Config.shrinkFactor * Gdx.graphics.getDeltaTime();

            // clamp scales
            if(enemyScale < 0)
                enemyScale = 0;
            if(playerScale < 0)
                playerScale = 0;

            // if shrinking has finished, stop shrinking and start message texting
            if(enemyScale <= 0f || playerScale <= 0f) {
                isShrinking = false;
                endBattle(win);
            }
        }

        // clear graphics
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // sets camera for batch matrix
        battleBatch.setProjectionMatrix(camera.combined);

        // starts battle sprite batch
        battleBatch.begin();

        // renders battle background
        battleBatch.draw(battleBG, 0, 0);

        // renders battle HUD
        renderHUD();

        // updates player to adapt animator
        player.update();

        // draws player in battle
        player.renderBattle(battleBatch, playerScale);

        // updates enemy to adapt animator
        enemy.update();

        // draws enemy in battle
        enemy.renderBattle(battleBatch, enemyScale);

        // renders damages texts
        renderDamage();

        // if is texting, the battle has ended
        if(isTexting) {
            // draws msg text
            msgText.render(battleBatch);

            // if user closes the msg text, go back to game
            if(msgText.isToBeClosed()) {
                // stop playing any bgm (victory or defeat)
                SFX.stopBGM(SFX.BGM.Fanfare_Victory);
                SFX.stopBGM(SFX.BGM.Fanfare_Defeat);
                Main.getInstance().changeToGame(); // get back to game state
                player.getAnimator().triggerCommand(PlayerAnimator.animCommands.stopWalking);
                dispose(); // disposes resource
                return;
            }
        }

        // ends battle sprite batch
        battleBatch.end();

        // renders UI stage
        battleStage.act();
        battleStage.draw();

        // if at any point(except when battle ending has been dealt), player health is lower or equal than 0
        // player has been defeated and battle should be ended with no reward
        if(pCurrentHealth <= 0 && !isShrinking && !isTexting) {
            pCurrentHealth = 0; // clamp to minimum
            shrink(false); // sets shrinking to loser
        }
        // if it is the enemy health that is lower or equal than 0
        // battle should be ended with a reward
        if(eCurrentHealth <= 0 && !isShrinking && !isTexting) {
            eCurrentHealth = 0; // clamp to minimum
            shrink(true); // sets shrinking to loser
        }
    }

    /**
     * Renders battle hud with player and enemy information
     */
    private void renderHUD() {
        // gets texture region for hud container
        TextureRegion hudContainer = new TextureRegion(battleHUD, 0, 0, 202, 61);
        // calculates player health percentage
        float healthPercent = (float)pCurrentHealth / (float)player.getAttributes().getMaxHealth();
        // clamps for safety
        if(healthPercent < 0) healthPercent = 0;
        // applies percentage to health hud
        int healthHUDXLimit = MathUtils.round(healthPercent * 133);
        // calculates player experience percentage
        float expPercent = Level.expPercent(player);

        // applies percentage to exp hud
        int expHUDXLimit = MathUtils.round(expPercent * 122);

        // gets texture region for health
        TextureRegion hudHealth = new TextureRegion(battleHUD, 0, 61, healthHUDXLimit, 11);

        // gets texture region for exp
        TextureRegion hudExp = new TextureRegion(battleHUD, 0, 74, expHUDXLimit, 6);

        // draws player information
        // hud container drawing
        float pHUDY = Config.playerHUDPos.y - (hudContainer.getRegionHeight()*Config.playerHUDScale);
        battleBatch.draw(hudContainer, Config.playerHUDPos.x, pHUDY,
                hudContainer.getRegionWidth()*Config.playerHUDScale,
                hudContainer.getRegionHeight()*Config.playerHUDScale);

        // hud health drawing
        // offsets
        float off_x = 67f * Config.playerHUDScale;
        float off_y = 24f * Config.playerHUDScale;
        battleBatch.draw(hudHealth, Config.playerHUDPos.x+off_x, pHUDY+off_y,
                healthHUDXLimit*Config.playerHUDScale,
                hudHealth.getRegionHeight()*Config.playerHUDScale);

        // draws player health amount text
        // uses glyphlayout to get text bounds
        String pHealthText = pCurrentHealth + " / " + player.getAttributes().getMaxHealth();
        final GlyphLayout textLayout = new GlyphLayout(battleFont, pHealthText);
        float pHealthTextX = Config.playerHUDPos.x+off_x + (66f*Config.playerHUDScale) - (textLayout.width/2);
        float pHealthTextY = pHUDY+off_y + (5*Config.playerHUDScale) + (textLayout.height/2);
        battleFont.getData().setScale(Config.battleFontScale*Config.playerHUDScale*0.5f);
        battleFont.draw(battleBatch, pHealthText , pHealthTextX, pHealthTextY);

        // hud EXP drawing
        // offsets
        float exp_off_x = 64f * Config.playerHUDScale;
        float exp_off_y = 11f * Config.playerHUDScale;
        battleBatch.draw(hudExp, Config.playerHUDPos.x+exp_off_x, pHUDY+exp_off_y,
                expHUDXLimit*Config.playerHUDScale,
                hudExp.getRegionHeight()*Config.playerHUDScale);

        // draws player experience percent to next level
        // uses glyphlayout to get text bounds
        String pExpText = String.format("%.2f", expPercent*100f) + "%";
        final GlyphLayout hudTextLayout = new GlyphLayout(battleFont, pExpText);
        float pExpTextX = Config.playerHUDPos.x+exp_off_x + (64f*Config.playerHUDScale) - (hudTextLayout.width/2);
        float pExpTextY = pHUDY+exp_off_y + (3*Config.playerHUDScale) + (hudTextLayout.height/2);
        battleFont.getData().setScale(Config.battleFontScale*Config.playerHUDScale*0.45f);
        battleFont.setColor(Color.BLACK);
        battleFont.draw(battleBatch, pExpText , pExpTextX, pExpTextY);
        battleFont.setColor(Config.battleFontColor);
        battleFont.draw(battleBatch, pExpText , pExpTextX+1, pExpTextY+1);

        // draws player sprite on hud
        TextureRegion playerHead = new TextureRegion(player.getAnimator().getSpriteSheet(),
                                    695, 896 , 56, 66);
        float pHeadX = Config.playerHUDPos.x + 32f * Config.playerHUDScale -
                            (playerHead.getRegionWidth()* Config.playerHUDScale * Config.playerHeadScale)/2;
        float pHeadY = pHUDY + 32f * Config.playerHUDScale -
                            (playerHead.getRegionHeight()* Config.playerHUDScale * Config.playerHeadScale)/2;
        float pHeadW = playerHead.getRegionWidth() * Config.playerHUDScale * Config.playerHeadScale;
        float pHeadH =  playerHead.getRegionHeight() * Config.playerHUDScale * Config.playerHeadScale;
        battleBatch.draw(playerHead, pHeadX, pHeadY, pHeadW, pHeadH);

        // player hud info text
        // gets i18n string for hud info
        String hudInfoText = Main.getInstance().getLang().format("hudInfo", player.getName(), player.getAttributes().level);
        final GlyphLayout hudTextLay = new GlyphLayout(battleFont, hudInfoText);
        float pHudTextX = Config.playerHUDPos.x + (84f*Config.playerHUDScale) - (hudTextLay.width/2);
        float pHudTextY = pHUDY + (45*Config.playerHUDScale) + (hudTextLay.height/2);
        battleFont.getData().setScale(Config.battleFontScale*Config.playerHUDScale*0.5f);
        battleFont.setColor(Color.BLACK);
        battleFont.draw(battleBatch, hudInfoText , pHudTextX, pHudTextY);
        battleFont.setColor(Config.battleFontColor);
        battleFont.draw(battleBatch, hudInfoText , pHudTextX+1, pHudTextY+1);

        // draws enemy HUD

        // flips enemy hud container
        TextureRegion eHudContainer = new TextureRegion(battleEnemyHUD, 0, 0, 202, 61);
        eHudContainer.flip(true, false);

        // calculates enemy health percentage
        float eHealthPercent = (float)eCurrentHealth / (float)enemy.getAttributes().getMaxHealth();
        // clamps for safety
        if(eHealthPercent < 0) eHealthPercent = 0;
        // applies percentage to enemy health hud
        int eHealthHUDXLimit = MathUtils.round(eHealthPercent * 133);

        // gets texture region for enemy health
        TextureRegion eHudHealth = new TextureRegion(battleEnemyHUD, 0, 61, eHealthHUDXLimit, 11);

        // hud container drawing
        float eHUDY = Config.enemyHUDPos.y - (eHudContainer.getRegionHeight()*Config.enemyHUDScale);
        float eHUDX = Config.enemyHUDPos.x - (eHudContainer.getRegionWidth()*Config.enemyHUDScale);
        battleBatch.draw(eHudContainer, eHUDX, eHUDY,
                eHudContainer.getRegionWidth()*Config.enemyHUDScale,
                eHudContainer.getRegionHeight()*Config.enemyHUDScale);

        // hud health drawing
        // offsets
        float e_off_x = 135f * Config.enemyHUDScale;
        float e_off_y = 24f * Config.enemyHUDScale;
        battleBatch.draw(eHudHealth, eHUDX+e_off_x, eHUDY+e_off_y,
                -eHealthHUDXLimit*Config.enemyHUDScale,
                eHudHealth.getRegionHeight()*Config.enemyHUDScale);

        // draws enemy health amount text
        // uses glyphlayout to get text bounds
        String eHealthText = eCurrentHealth + " / " + enemy.getAttributes().getMaxHealth();
        final GlyphLayout eTextLayout = new GlyphLayout(battleFont, eHealthText);
        float eHealthTextX = eHUDX+e_off_x + (-63f*Config.enemyHUDScale) - (eTextLayout.width/2);
        float eHealthTextY = eHUDY+e_off_y + (5*Config.enemyHUDScale) + (eTextLayout.height/2);
        battleFont.getData().setScale(Config.battleFontScale*Config.enemyHUDScale*0.5f);
        battleFont.draw(battleBatch, eHealthText , eHealthTextX, eHealthTextY);

        // draws enemy sprite in HUD
        TextureRegion enemySpr = enemy.getAnimator().getCurrentSprite();
        float eHeadX = eHUDX + 171f * Config.enemyHUDScale -
                (enemySpr.getRegionWidth()* Config.enemyHUDScale * Config.enemyHeadScale)/2;
        float eHeadY = eHUDY + 32f * Config.enemyHUDScale -
                (enemySpr.getRegionHeight()* Config.enemyHUDScale * Config.enemyHeadScale)/2;
        float eHeadW = enemySpr.getRegionWidth() * Config.enemyHUDScale * Config.enemyHeadScale;
        float eHeadH =  enemySpr.getRegionHeight() * Config.enemyHUDScale * Config.enemyHeadScale;
        battleBatch.draw(enemySpr, eHeadX, eHeadY, eHeadW, eHeadH);

        // enemy hud info text
        // gets i18n string for hud info
        String eHudInfoText = Main.getInstance().getLang().format("hudInfo", enemy.getName(), enemy.getAttributes().level);
        final GlyphLayout eHudTextLay = new GlyphLayout(battleFont, eHudInfoText);
        float eHudTextX = eHUDX+ (103f*Config.enemyHUDScale) - (eHudTextLay.width/2);
        float eHudTextY = eHUDY + (45*Config.enemyHUDScale) + (eHudTextLay.height/2);
        battleFont.getData().setScale(Config.battleFontScale*Config.enemyHUDScale*0.5f);
        battleFont.setColor(Color.BLACK);
        battleFont.draw(battleBatch, eHudInfoText , eHudTextX, eHudTextY);
        battleFont.setColor(Config.battleFontColor);
        battleFont.draw(battleBatch, eHudInfoText , eHudTextX+1, eHudTextY+1);
    }

    /**
     * renders damage texts currently visible (on dmg text list)
     */
    private void renderDamage() {
        for(int i = dmgTexts.size()-1 ; i >= 0 ; i--) {
            // render damage
            dmgTexts.get(i).render();

            // checks if damage has finished rendering(surpassed distance limit)
            if(dmgTexts.get(i).isFinished())
                dmgTexts.remove(i); // removes from list of damage texts to be rendered
        }
    }

    /**
     * Performs both player and enemy auto attacks
     */
    private void updateAutoAtk() {
        // updates auto timers
        pAutoTimer += Gdx.graphics.getDeltaTime();
        eAutoTimer += Gdx.graphics.getDeltaTime();

        // tries to preform player auto attack
        if(pAutoTimer >= player.getAttributes().getAutoSpeed()) {
            pAutoTimer = 0f; // resets player auto timer
            // add damage to be displayed
            int damage = Damage.getAutoDmg(player.getAttributes(), enemy.getAttributes());
            dmgTexts.add(new DamageText(battleBatch, damageFont, enemy, DamageText.TargetType.Enemy,
                                        damage, false));
            enemy.hurt(); // calls enemy hurt to trigger enemy hurt operations
            eCurrentHealth -= damage;
        }

        // tries to preform enemy auto attack
        if(eAutoTimer >= enemy.getAttributes().getAutoSpeed()) {
            eAutoTimer = 0f; // resets enemy auto timer
            // add damage to be displayed
            int damage = Damage.getAutoDmg(enemy.getAttributes(), player.getAttributes());
            dmgTexts.add(new DamageText(battleBatch, damageFont, player, DamageText.TargetType.Player,
                    damage, false));
            player.hurt(); // calls player hurt to trigger player hurt operations
            pCurrentHealth -= damage;
        }
    }

    @Override
    public void pause() {
        // stops music
        // if playing music
        if(battleBGM.isPlaying())
            battleBGM.stop(); // pause battle music when leaving battle

        // disable touch in menu actors
        Common.setTouchableStage(battleStage, Touchable.disabled);
    }

    @Override
    public void resume() {
        // if not playing yet
        if(!battleBGM.isPlaying())
            battleBGM.play();	// play battle music when resuming battle
        // disable touch in menu actors
        Common.setTouchableStage(battleStage, Touchable.enabled);
    }

    @Override
    public void dispose() {
        battleBatch.dispose(); // disposes battle sprite batch
        battleStage.dispose(); // disposes battle stage
        enemy.dispose(); // calls enemy dispose method
    }

    /**
     * Prepares to shrink battle loser
     * @param win true if player have defeated the enemy, false otherwise
     */
    private void shrink(boolean win) {
        this.win = win;
        isShrinking = true;
        // stop battle bgm
        battleBGM.stop();
        // play correspondent bgm
        if(win)
            SFX.playBGM(SFX.BGM.Fanfare_Victory, 1.0f, true);
        else
            SFX.playBGM(SFX.BGM.Fanfare_Defeat, 1.0f, true);
    }

    /**
     * Performs remaining end battle operations
     * such as message text drawing information
     *
     * @param win should be true if player have defeated enemy, false otherwise
     */
    private void endBattle(boolean win) {
        isTexting = true; // text message is gonna be drawn either cases
        if(win) // if player defeated enemy, give reward
            rewardPlayer();
        else // just display player has been defeated message
            msgText = Texts.msgDefault("msgDefeat", TextMessage.Anchor.BOTTOM);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(!isTexting && !isShrinking) // if not ended battle
            playerAttack(); // performs player attack operations
        else if(isTexting) // if texting, send input to message text
            msgText.touchDown();
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

    /**
     * Performs player attack operations
     */
    private void playerAttack() {
        deltaAttack = System.currentTimeMillis() - lastAttack; // calculate delta attack

        // only consider attack input if its bigger than minimum delta between input
        if(deltaAttack >= Config.btMinDeltaTouch) {
            // delegates attack to player passing enemy to attack and if it should be critical.
            // receives the calculated damage of player
            boolean critical = Common.rollDice(player.getAttributes().getCriticalChance());
            int damage = player.attack(enemy, deltaAttack, critical);
            // applies damage to enemy (if attack was made)
            if (damage != -1) {
                enemy.hurt(); // calls enemy hurt to trigger enemy hurt operations
                eCurrentHealth -= damage;
                // add damage to be displayed
                dmgTexts.add(new DamageText(battleBatch, damageFont, enemy, DamageText.TargetType.Enemy, damage, critical));
            }
            // updates last attack
            lastAttack = System.currentTimeMillis();
        }
    }

    /**
     * Rewards player in case of victory in the battle
     */
    private void rewardPlayer() {

        player.getAttributes().addExp(enemy.getReward().getExp()); // adds enemy exp to player
        player.getInventory().addGold(enemy.getReward().getGold()); // adds enemy gold to player

        // Item drops
        HashMap<String, Integer> drops = new HashMap<String, Integer>();

        // checks if player inventory is full
        boolean invFull = player.getInventory().isFull();
        // if it is not full, tries to drop items
        if(!invFull) {
            // tries to drop (limited by max drop item quantity)
            for (int i = 0; i < Config.maxNumberOfDrops; i++) {
                // tries to drop any type of equipment with max drop tries
                Equipment eDrop = Factory.rollEquipment(enemy.getAttributes().getLevel(), Config.maxDropTries);
                // if any equipment dropped, add to drops and to player inventory
                if (eDrop != null) {
                    // tries to add item to player inventory
                    if (player.getInventory().addItem(eDrop)) {
                        // drop description
                        String dropDesc = eDrop.getName() + " " +
                                Main.getInstance().getLang().format("levelInfo", eDrop.getLevel());
                        drops.put(dropDesc, 1);
                    } else {
                        System.out.println("hahaha FUCK YOU");
                    }
                }
            }
        } else { // if inventory is full
            drops = null; // sets drops as null representing that inventory is full
        }

        // builds reward message text
        msgText = Texts.msgReward(enemy.getReward().getExp(), enemy.getReward().getGold(),
                                    drops, TextMessage.Anchor.BOTTOM);

        // TODO - Transition to battle
        // TODO - Level UP notification
    }

}
