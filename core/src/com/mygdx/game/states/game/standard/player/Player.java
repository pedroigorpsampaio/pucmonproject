package com.mygdx.game.states.game.standard.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Main;
import com.mygdx.game.states.game.standard.architecture.Collision;
import com.mygdx.game.states.game.standard.battle.Attributes;
import com.mygdx.game.states.game.standard.battle.Damage;
import com.mygdx.game.states.game.standard.camera.GameCamera;
import com.mygdx.game.states.game.standard.architecture.GameLoop;
import com.mygdx.game.states.game.standard.enemy.Enemy;
import com.mygdx.game.states.game.standard.item.Equipment;
import com.mygdx.game.states.game.standard.inventory.Inventory;
import com.mygdx.game.states.game.standard.map.Field;
import com.mygdx.game.states.game.standard.map.Map;
import com.mygdx.game.states.game.standard.map.MapConfig;
import com.mygdx.game.states.game.standard.physics.Collider;
import com.mygdx.game.states.game.standard.physics.Transform;
import com.mygdx.game.states.game.standard.sfx.SFX;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

/**
 * Class that represents the player in the game world
 *
 * @author Pedro Sampaio
 * @since  0.4
 */
public class Player implements GameLoop, Collision {

    private String name;            // player's name

    private Transform transform; // player`s transform
    private SpriteBatch batch;  // sprite batch to render player
    private GameCamera gameCam;      // game camera
    private Vector2 direction;  // players direction

    private boolean isActive;   // if player is currently active
    private Vector2 touchPos;   // touch screen position
    private boolean isTouching; // if touch on screen is happening
    private boolean isAttacking; // if player is attacking
    private PlayerAnimator pAnimator; // the player animator that controls sprite animation
    private Vector2 battlePos;  // player battle position
    private Sprite playerSpr; // player sprite

    public PlayerAnimator getAnimator() {return pAnimator;} // gets player animator
    Collider collider;  // Player collider
    Attributes playerAttr; // player battle`s attribute

    Inventory inventory; // Player inventory

    private Color currentColor; // players sprite current tint color

    // for collider debug
    ShapeRenderer shapeRenderer;

    /**
     * player`s constructor
     *
     * @param name      the player's character name
     * @param transform the transform component with player's physical info
     * @param batch     the batch to render player
     * @param playerAttr player attributes
     * @param inventory player inventory
     */
    public Player(String name, Transform transform, SpriteBatch batch, Attributes playerAttr, Inventory inventory) {
        this.name = name;
        this.transform = transform;
        this.batch = batch;
        if(playerAttr != null) // not a new player
            this.playerAttr = playerAttr;
        else // creates new player's attribute
            this.playerAttr = Attributes.createNewPlayer();
        if(inventory != null) // not a new player
            this.inventory = inventory;
        else // create new player's inventory
            this.inventory = Inventory.createNewInventory();

        // initially no touch is happening
        isTouching = false;
        // initially player is not attacking
        isAttacking = false;
        // init touch position vector2
        touchPos = new Vector2();
        // init player direction;
        direction = new Vector2(1,0);
        // activates player
        isActive = true;
        // for debug of collider
        shapeRenderer = new ShapeRenderer();
    }

    /**
     * Sets game camera
     */
    public void setGameCamera(GameCamera gameCam) {
        this.gameCam = gameCam;
    }

    /**
     * Player's gameloop
     */
    @Override
    public void create() {
        // initially player tint color is white, no effects on sprites
        currentColor = Color.WHITE;

        // creates player animator (wraps animator processes)
        pAnimator = new PlayerAnimator((int)transform.getWidth(), (int)transform.getHeight(),
                                        transform.getScale());
        // starts animator on idle state
        pAnimator.startAnimator(PlayerAnimator.animStates.idleRight);

        // adjust initial position of player
        if(transform.getPosition().x < pAnimator.getScaledSprWidth() /4)
            transform.getPosition().x = pAnimator.getScaledSprWidth() /2;
        // adjust initial position of player
        if(transform.getPosition().y < pAnimator.getScaledSprHeight() /2)
            transform.getPosition().y = pAnimator.getScaledSprHeight() /2;

        // creates player collider
        collider = new Collider(transform, true, this);
        // adjusts player collider to fit player sprite
        collider.setScaleWidth(0.45f);
        collider.setScaleHeight(0.60f);
        collider.setOffsetX(-pAnimator.getScaledSprWidth()/5.5f);
        collider.setOffsetY(-pAnimator.getScaledSprHeight()/8.10f);

        // calculates battle pos
        TextureRegion currSprite = pAnimator.getCurrentSprite();
        playerSpr = new Sprite(currSprite);
        battlePos = new Vector2(Config.baseWidth*0.2f - playerSpr.getWidth()/2, Config.baseHeight*0.22f);
    }

    @Override
    public void update() {
        // if player is active
        if(isActive) {
            // updates animator
            pAnimator.update();
            // updates transform
            transform.update();
            // updates attributes to adjust to player level
            // and currently worn equipment
            playerAttr.update(inventory.getWornEquipment());
            // update remaining equipment effects
            updateEquipmentEffects();

            // if we are in standard game state, we must move player based on touch input
            if(Main.getInstance().getGameState() == Main.State.GAME_STANDARD) {
                // updates player transform position (touch is the means of moving)
                if (isTouching) {
                    // gets direction of touch in relation to player screen position, in
                    // order to move in direction ot touch
                    direction.x = touchPos.x - gameCam.worldToScreenPosition(transform.getPosition()).x;
                    direction.y = touchPos.y - gameCam.worldToScreenPosition(transform.getPosition()).y;
                    // normalizes direction to avoid direction magnitude altering move speed
                    direction.nor();

                    // creates move vector with normalized direction, movespeed and dt
                    Vector2 move = new Vector2();
                    move.x = direction.x * Config.playerSpeed * Gdx.graphics.getDeltaTime();
                    move.y = direction.y * Config.playerSpeed * Gdx.graphics.getDeltaTime();

                    // delegates movement to transform,
                    // that checks for collisions and
                    // trigger collision callbacks
                    transform.move(move);
                    // player walked left
                    if (direction.x < 0)
                        pAnimator.triggerCommand(PlayerAnimator.animCommands.walkLeft); // trigger walk left command
                    else if (direction.x > 0) // player walked right
                        pAnimator.triggerCommand(PlayerAnimator.animCommands.walkRight); // trigger walk right command
                } else { // no touching
                    // send idle command to animator
                    pAnimator.triggerCommand(PlayerAnimator.animCommands.stopWalking);
                }
            } else if(Main.getInstance().getGameState() == Main.State.BATTLE) { // in battle
                // always try to interpolate to white, in case of damaged tint color is applied
                currentColor = Common.lerpColor(currentColor, Color.WHITE, Config.dmgColorSpeed);

                // checks if attacking has ended
                if(isAttacking) {
                    if(pAnimator.getCurrentAnimation().isFinished() ||
                            !pAnimator.getCurrentState().equals(PlayerAnimator.animStates.battleAttack))
                        isAttacking = false; // sets attack to false if attack has ended
                }
                isTouching = false; // avoid getting out of battle walking
            }
        }
    }

    @Override
    public void render() {
        // calculates position of player
        Vector2 playerPos = gameCam.worldToScreenPosition(transform.getPosition());
        // current sprite depending on current animation state and frame
        TextureRegion currSprite = pAnimator.getCurrentSprite();

        // calculates sprite offset to center player
        float offset_x =(pAnimator.getScaledSprWidth() / 2.75f) ;
        float offset_y = (pAnimator.getScaledSprHeight() / 2) ;
        // draws player sprite at player position
        batch.draw(currSprite, playerPos.x - offset_x, playerPos.y - offset_y,
                pAnimator.getScaledSprWidth(), pAnimator.getScaledSprHeight());
    }

    /**
     * renders player collider for debug
     */
    public void renderCollider(Camera camera) {
        // calculates position of collider in screen
        Vector2 collPos = gameCam.worldToScreenPosition(new Vector2(collider.x, collider.y));

        // prepares shape renderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        // renders player collider for debug
        shapeRenderer.rect(collPos.x, collPos.y, collider.getWidth(), collider.getHeight());
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        pAnimator.dispose(); // disposes spritesheet
        shapeRenderer.dispose(); // disposes shaperenderer
        inventory.dispose(); // calls inventory dispose to dispose remaining inventory resources
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // a touch is happening
        isTouching = true;
        // updates touch position
        touchPos.x = screenX;
        touchPos.y = screenY;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // touch ceases
        isTouching = false;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // updates touch pos on drag
        touchPos.x = screenX;
        touchPos.y = screenY;
        return false;
    }

    /**
     * Getters and setters
     */

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public void setActive(boolean isActive) { this.isActive = isActive; }

    public Inventory getInventory() {return inventory; }

    public void setInventory(Inventory inventory) {this.inventory = inventory;}

    public Attributes getAttributes() {
        return playerAttr;
    }

    public void setAttributes(Attributes playerAttr) {
        this.playerAttr = playerAttr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector2 getBattlePos() {
        return battlePos;
    }

    /**
     * Returns if player is currently moving or not
     * @return true if player is moving, false otherwise
     */
    public boolean isMoving() {
        return isTouching;
    }

    /**
     * Collision Callbacks
     */

    @Override
    public void onColliderEnter(Collider other) {

    }

    @Override
    public void onTriggerEnter(Collider other) {
        // collision with map static entity (tile indexes stored in vector2)
        if(other.getParent() instanceof Vector2) {
            Vector2 tileCollided = (Vector2) other.getParent();

            // get mask of special tiles (fields) of current map
            Field[][] fields = MapConfig.getInstance().getCurrentMap().getFields();
            // gets indexes of tile collided
            int i = (int)tileCollided.x; int j = (int)tileCollided.y;
            // checks if it is a special tile
            if(fields[i][j] != null) {
                // if it is a special field, decide what
                // to do depending on type of field
                fieldCollided(fields[i][j]);
            }
        }
    }

    @Override
    public void onTriggerStay(Collider other) {

    }

    @Override
    public void onTriggerExit(Collider other) {

    }

    /**
     * Reacts to collision with special tiles (fields)
     * depending on the type of field
     * @param field the field collided
     */
    private void fieldCollided(Field field) {
        // gets field information
        Field.FieldType type = field.getType();
        String id = field.getID();
        String complement = field.getComplement();
        // switch between types of fields
        switch(type) {
            case MAPTELEPORT:
                // old map (to find binding location
                Map oldMap = MapConfig.getInstance().getCurrentMap();
                // get new map in list of maps
                Map newMap = MapConfig.getInstance().getMapWithID(id);
                // loads new map into game
                MapConfig.getInstance().setCurrentMap(newMap);
                // updates camera to match new map constants
                gameCam.updateMap(newMap);
                // gets map binding location (the link between two maps)
                Vector2 mapPosition = MapConfig.getInstance().getCurrentMap().getTeleportBind(oldMap);
                // gets orientation offset of spawn
                Vector2 offset = Common.stringToVector2(complement);
                // performs teleport to map position with offset
                transform.teleport((mapPosition.y+offset.x)*MapConfig.getInstance().getCurrentMap().getTileSize() + 15f,
                                    (mapPosition.x+offset.y)*MapConfig.getInstance().getCurrentMap().getTileSize() + 15f);
                // sets is touching to false to lock player movement
                // when teleporting to avoid coming in and back from maps
                // player has to stop touching and start again to go back to moving
                isTouching = false;
                break;
            default:
                System.err.println("Unknown type of field: "+type);
                break;
        }
    }


    /*****************
     * Player battle *
     *****************/

    /**
     * Renders player in battle state
     * @param battleBatch the battle sprite batch to draw player
     * @param scale the scale to draw player with
     */
    public void renderBattle(SpriteBatch battleBatch, float scale) {
        // current sprite depending on current animation state and frame
        TextureRegion currSprite = pAnimator.getCurrentSprite();
        playerSpr.setRegion(currSprite);
        playerSpr.setSize(pAnimator.getScaledSprWidth()*Config.battleSprScale*scale,
                            pAnimator.getScaledSprHeight()*Config.battleSprScale*scale);

        // calculate player battle position
        battlePos.x = Config.baseWidth*0.2f - playerSpr.getWidth()/2;
        battlePos.y = Config.baseHeight*0.22f;
        playerSpr.setPosition(battlePos.x, battlePos.y);
        playerSpr.setColor(currentColor);
        playerSpr.draw(battleBatch);
    }

    /**
     * Do player hurt related operations
     */
    public void hurt() {
        // applies tint to enemy sprite
        currentColor = Config.playerDamagedColor.cpy();
    }


    /**
     * Attacks an enemy received in parameter
     *
     * @param enemy the enemy to be attacked
     * @param deltaAttack   the time between last attack
     * @param critical      it attack should be critical in case it happens
     * @return the damage dealt to enemy
     */
    public int attack(Enemy enemy, double deltaAttack, boolean critical) {
        // clamp delta attack between config bounds
        deltaAttack = MathUtils.clamp(deltaAttack, Config.minDeltaAttack, Config.maxDeltaAttack);

        // calculates damage dealt to enemy
        int damage = Damage.getNormalDmg(playerAttr, enemy.getAttributes());

        // sfx of attack
        SFX.Effect effect = SFX.Effect.Hit_Enemy;
        // animation command of attack
        PlayerAnimator.animCommands animComm = PlayerAnimator.animCommands.attack;

        // is it a critical hit?
        if(critical) {
            effect = SFX.Effect.Critical_Hit_1; // change effect to critical hit
            animComm = PlayerAnimator.animCommands.criticalAttack; // change anim command to critical attack
            damage *= playerAttr.getCriticalMultiplier(); // applies critical multiplier to damage
        }

        // trigger attack sound effect
        SFX.playSFX(effect, 1.0f);

        // only animates attacks if not attacking yet
        if(!isAttacking) {
            // is it a critical hit?
            if(critical) {
                animComm = PlayerAnimator.animCommands.criticalAttack; // change anim command to critical attack
            }

            // triggers attack animation
            pAnimator.triggerCommand(animComm);
            // scale attack animation speed depending on user delta attack (time between touches)
            pAnimator.getCurrentAnimation().setTimeBetweenFrames((float)deltaAttack/Config.attackSpeedFactor);
            // player is attacking
            isAttacking = true;
        }

        // return damage dealt
        return damage;
    }

    /**
     * Updates equipment effects that are not
     * updated in attributes already
     */
    public void updateEquipmentEffects() {
        // equipment effects
        float speed = 0;
        float dropRate = 0;
        float expRate = 0;
        float goldRate = 0;
        // for all equipments effects, get effects
        for(int i = 0; i < inventory.getWornEquipment().length; i++) {
            Equipment equip = inventory.getWornEquipment()[i];
            if(equip != null) { // if slot has equipment
                // sums with  each equipment effect
                speed += equip.getEffect().speed;
                dropRate += equip.getEffect().rateDrop;
                expRate += equip.getEffect().rateExp;
                goldRate += equip.getEffect().rateGold;
            }
        }
        // updates config values
        Config.playerSpeed = Config.playerBaseSpeed + speed;
        Config.dropRate = Config.baseDropRate + dropRate;
        Config.expRate = Config.baseExpRate + expRate;
        Config.goldRate = Config.baseGoldRate + goldRate;
    }
}
