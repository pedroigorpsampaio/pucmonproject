package com.mygdx.game.states.game.standard.message;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

/**
 * Represents a message containing
 * the message background texture region
 * and the message text content
 *
 * @author Pedro Sampaio
 * @since 1.6
 */
public class TextMessage {

    private String text;                // the message text content
    private BitmapFont font;            // the message text font
    private TextureRegion background;   // the message background texture region
    private TextureRegion arrow;        // the message arrow texture region
    private String currText;            // the current text content to be displayed (for incremental text animation)
    private boolean isFinished;         // bool that represents if all text content has been displayed
    private boolean toClose;            // bool that represents if user wants to close message
    public enum Anchor {TOP, MIDDLE, BOTTOM} // possible positions for message in screen
    private Anchor anchor;              // the anchor position of this message
    private boolean isWaiting;          // bool that represents if message is waiting for user input to continue displaying
    private Vector2 position;           // the position to draw the message
    private float width;                // the width of the message
    private float height;               // the height of the message
    private int maxChars;               // the maximum number of characters displayed at the same time in message
    private int nCharsDrawn;            // the number of characters already drawn
    private float charTimer;            // timer to help control character animation of text
    private float animOffset;           // y offset of arrow animation
    private int animDir;                // current animation direction (-1 for down, 1 for up)
    private int charRangeDrawn;         // quantity of chars drawn

    /**
     * Constructor that receives the text content,
     * anchor, and batch to draw into, while
     * initializing other message properties
     * @param text  the text content of the message
     * @param anchor  the message anchor position on screen
     */
    public TextMessage(String text, Anchor anchor) {
        this.text = text; // sets text
        this.font = Resource.messageFont;   // sets the message text font
        background = new TextureRegion(Resource.messageBG, 0, 0, 574, 221); // cuts message background
        arrow = new TextureRegion(Resource.messageBG, 0, 223, 21, 18); // cuts message arrow
        currText = ""; // initialize current text
        this.anchor = anchor; // sets anchor position
        isFinished = false; // not finished initially
        isWaiting = false; // not waiting user input initially
        toClose = false;    // not to be closed initially
        nCharsDrawn = 0; // initially no characters are drawn
        charTimer = 0f; // initially timer is at 0
        animOffset = 0f; // initially animation offset of arrow is 0
        animDir = 1; // initially animation direction is up
        charRangeDrawn = 1; // initially quantity of chars drawn each time is the default 1
        create(); // defines the remaining message data
    }

    /**
     * Initialize remaining message data
     */
    private void create() {

        float msg_x; // message x position
        float msg_y; // message y position
        /**
         * switches between anchor position
         * to define message screen y coordinate
         */
        switch (anchor) {
            case TOP:
                msg_y = Config.baseHeight - (background.getRegionHeight() * Config.msgScaleY);
                break;
            case MIDDLE:
                msg_y = Config.baseHeight/2 - (background.getRegionHeight() * Config.msgScaleY)/2;
                break;
            case BOTTOM:
                msg_y = 0;
                break;
            default:
                msg_y = 0;
                System.err.println("Unknown type of anchor position: " + anchor + ". Using bottom as default");
                break;
        }
        // msg_x position calculation (centered)
        msg_x = Config.baseWidth/2 - (background.getRegionWidth() * Config.msgScaleX)/2;
        // sets message width
        width = (background.getRegionWidth() * Config.msgScaleX);
        // sets message height
        height = (background.getRegionHeight() * Config.msgScaleY);
        // sets message position
        position = new Vector2(msg_x, msg_y);
        // calculates the maximum number of characters in a message
        maxChars = MathUtils.floor((width / (15*font.getScaleX())) * (height / (15*font.getScaleY())));
    }

    /**
     * Receives touch down input data
     */
    public void touchDown() {
        // if message is finished, then proceed sets user wants to close message boolean
        if(isFinished) {
            toClose = true; // sets bool representing that message should be closed
            charRangeDrawn = 1; // reset char drawn quantity to the default
            return; // return since message job is done
        }
        // if it is not finished...
        // if message is waiting touch input to change message portion to display
        if(isWaiting) {
            currText = ""; // change message portion to be displayed by resetting current string
            // if after reset next character is space, ignore it as it is unnecessary
            if(text.charAt(nCharsDrawn) == ' ')
                nCharsDrawn++;
            // if after reset next character is break line, ignore it as it is unnecessary
            if(text.charAt(nCharsDrawn) == '\n')
                nCharsDrawn++;
            charRangeDrawn = 1; // reset char drawn quantity to the default
            isWaiting = false; // top waiting for user input
        } else { // if not waiting input nor is finished, hurry text drawing
            charRangeDrawn = Config.msgCharSpeedUp; // increase char drawn quantity to speed up text
        }

    }

    /**
     * Renders text with character by character animation
     * with message background bounds as text length limitation
     * @param batch the batch to draw the message
     */
    public void render(SpriteBatch batch) {
        // do not draw nothing if this message is to be closed
        if(isToBeClosed())
            return;

        // checks if drawn all characters of text content
        if(nCharsDrawn >= text.length())
            isFinished = true; // finished drawing all text data

        // renders message background
        batch.draw(background, (int)position.x, (int)position.y, width, height);

        // text limit on X axis
        float xLimit = width - (Config.msgTextPadX*Config.msgScaleX)*2;
        // text limit on Y axis
        float yLimit = height - (Config.msgTextPadY*Config.msgScaleY)*2;

        // checks if another character should be added to the drawn string
        if(charTimer >= Config.msgCharSpeed && !isFinished && !isWaiting) {
            // resets timer
            charTimer = 0f;
            for(int i = 0; i < charRangeDrawn; i++) {
                if (text.charAt(nCharsDrawn) == '\\') { // char that forces new page and user input
                    isWaiting = true; // if so, waits for user input to reset drawn string
                    nCharsDrawn++; // increment to ignore \ char
                    break; // stops drawing of current batch of characters
                }
                // adds character to string drawn
                currText += text.charAt(nCharsDrawn);
                // checks if char is space
                if (text.charAt(nCharsDrawn) == ' ') {
                    // gets text dimensions with next concats
                    GlyphLayout textLayout = Common.getNextWordConcatLayout(currText, nCharsDrawn, text, font);
                    // checks if a line break is needed
                    if (textLayout.width >= xLimit)
                        currText = Common.insertBreakLine(currText);
                    // updates on account of break line inserts
                    textLayout = Common.getNextWordConcatLayout(currText, nCharsDrawn, text, font);
                    // checks if next concat will surpass y limit
                    if (textLayout.height >= yLimit) {
                        isWaiting = true; // if so, waits for user input to reset drawn string
                        break; // stops drawing of current batch of characters
                    }
                }
                // increment character drawn counter
                nCharsDrawn++;

                if(nCharsDrawn >= text.length())
                    break; // if reached string limit, stop drawing
            }
        }

        // draws text
        // text X position
        int textX = (int)(position.x + Config.msgTextPadX*Config.msgScaleX);
        // text Y position
        int textY = (int)(position.y + height - Config.msgTextPadX*Config.msgScaleY);
        // draws text
        font.draw(batch, currText, textX, textY);

        // if waiting for user input, draw arrow symbolizing it
        if(isWaiting) {
            // calculates anim offset
            animOffset += Config.msgArrowSpeed * animDir * Gdx.graphics.getDeltaTime();

            // calculate coords and dimensions
            int arrowW = (int)(width*0.05f*Config.msgArrowScaleW);
            int arrowH = (int)(height*0.1f*Config.msgArrowScaleH);
            int arrowX = (int)(position.x + width*0.95f - arrowW);
            float arrowY = position.y + arrowH + animOffset; // applies animation offset
            // draws arrow
            batch.draw(arrow, arrowX, arrowY, arrowW, arrowH);
            // checks if animation movement length has been surpassed
            if(animOffset >= Config.msgArrowMoveLength)
                animDir = -1; // starts moving arrow down
            if(animOffset <= 0) // if offset reaches minimum
                animDir = 1; // starts moving arrow up
        }

        // draws rotated arrow symbolizing that text is finished
        if(isFinished) {
            // calculate coords and dimensions
            int arrowW = (int)(width*0.05f*Config.msgArrowScaleW);
            int arrowH = (int)(height*0.1f*Config.msgArrowScaleH);
            int arrowX = (int)(position.x + width*0.95f - arrowW);
            float arrowY = position.y + arrowH;
            Sprite rotArrow = new Sprite(arrow);
            rotArrow.setPosition(arrowX, arrowY);
            rotArrow.setSize(arrowW, arrowH);
            rotArrow.setRotation(90);
            // draws rotated arrow
            rotArrow.draw(batch);
        }

        // updates char timer
        charTimer += Gdx.graphics.getDeltaTime();
    }

    /**
     * Returns if message is to be closed
     * @return true if message should be closed, false otherwise
     */
    public boolean isToBeClosed() {
        return toClose;
    }
}
