package com.mygdx.game.states.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Class that represents a spawnable moving cloud
 * that can be random spawned at a position in a given range
 * and dynamically move to a direction
 *
 * @author Pedro Sampaio
 * @since   0.3
 */
public class SpawnableCloud {

    private float moveSpeed;    // the move speed of the cloud
    private float speedScale = 5f;   // scales speed
    private float scaleFactor = 0.01f; // cloud scale factor
    private int dir_x;          // the direction in x axis (left or right)
    private int cloud_id;       // the id of the cloud to identify what sprite to use
    private Vector2 position;   // cloud position in x and y axis
    private int maxY;           // the maxY value for the random y spawn pos
    private int minY;           // the minY value for the random y spawn pos
    private int minCloudID = 1; // the min cloud ID existent
    private int maxCloudID = 3; // the max cloud ID existent
    private float scale;        // scale of cloud image
    private boolean active;     // boolean that represents if cloud is active
    private Texture cloudIMG;   // the cloud image to be rendered
    private Viewport viewport;  // viewport to spawn cloud

    /**
     * Cloud constructor that receives all needed info
     *
     * @param moveSpeed the move speed of the cloud
     * @param dir_x     the direction in x axis (left or right)
     * @param cloud_id  the id of the cloud to identify what sprite to use
     * @param maxY      the maxY value for the random y spawn pos
     * @param minY      the minY value for the random y spawn pos
     * @param scale     scale of the cloud image
     */
    public SpawnableCloud(float moveSpeed, int dir_x, int cloud_id, int maxY, int minY, float scale) {
        this.moveSpeed = moveSpeed;
        this.dir_x = dir_x;
        // clamps ID between min and max
        this.cloud_id = Math.max(minCloudID, Math.min(cloud_id, maxCloudID));
        this.maxY = maxY;
        this.minY = minY;
        this.scale = scale * scaleFactor;
    }

    /**
     * Cloud constructor that receives all needed info
     * except for the cloud_id, that is random generated
     * @param moveSpeed the move speed of the cloud
     * @param dir_x     the direction in x axis (left or right)
     * @param maxY      the maxY value for the random y spawn pos
     * @param minY      the minY value for the random y spawn pos
     * @param scale     scale of the cloud image
     * @param viewport  the viewport to spawn clouds
     */
    public SpawnableCloud(float moveSpeed, int dir_x, int maxY, int minY, float scale, Viewport viewport) {
        this.moveSpeed = moveSpeed;
        this.dir_x = dir_x;
        cloud_id = MathUtils.random(minCloudID,maxCloudID); // random id between start
                                                            // (inclusive) and end (inclusive).
        this.maxY = maxY;
        this.minY = minY;
        this.scale = scale * scaleFactor;
        this.viewport = viewport;
    }

    /**
     * Getter for cloud position
     * @return the cloud position in the x and y axis
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Getter for cloud image
     * @return the cloud image
     */
    public Texture getCloudIMG() {
        return cloudIMG;
    }

    /**
     * Getter for cloud direction
     * @return the cloud direction
     */
    public int getDirection() {
        return dir_x;
    }

    /**
     * Getter for the cloud scale
     * @return the cloud scale
     */
    public float getScale() {
        return scale;
    }

    /**
     * gets the scaled width of the cloud image
     * @return  the scaled width of cloud image
     */
    public float getScaledWidth() {
        return cloudIMG.getWidth()*scale;
    }

    /**
     * gets the scaled height of the cloud image
     * @return  the scaled height of cloud image
     */
    public float getScaledHeight() {
        return cloudIMG.getHeight()*scale;
    }

    /**
     * loads libgdx resources on creation
     */
    public void create() {
        // loads cloud sprite
        cloudIMG = new Texture("imgs/menu_initial/clouds/cloud_"+cloud_id+".png");
        // offset depending on image and scale for max Y
        float offset_y = -cloudIMG.getHeight() * scale;
        // initial cloud position (random Y between min and max)
        if(dir_x == -1) // if I`m going left on x_axis, start at the most right pos
            position = new Vector2(viewport.getWorldWidth(), MathUtils.random(minY, maxY+offset_y));
        else // starts on left decreasing image width
            position = new Vector2(-cloudIMG.getWidth()*scale, MathUtils.random(minY, maxY));
        active = true;  // sets active to true
    }

    /**
     * updates cloud position if active
     * on cloud direction with cloud movespeed
     */
    public void update() {
        // if active, needs update in pos
        if(active) {
            // creates move (only moves on x-axis)
            float move = moveSpeed*dir_x*speedScale;
            // adds move to cloud position
            position.x += move * Gdx.graphics.getDeltaTime();
        }
    }

    public void pause() {
        active = false; // deactivates cloud
    }

    public void resume() {
        active = true;  // reactivates cloud
    }

    public void dispose() {
        cloudIMG.dispose(); // disposes cloud texture
    }

}
