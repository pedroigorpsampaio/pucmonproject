package com.mygdx.game.states.game.standard.ranking;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Main;
import com.mygdx.game.messages.MessageContent;
import com.mygdx.game.messages.MessageFlag;
import com.mygdx.game.messages.MessageRanking;
import com.mygdx.game.messages.ServerMessages;
import com.mygdx.game.states.game.standard.architecture.Resource;
import com.mygdx.game.states.game.standard.architecture.ServerListener;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;
import com.sddl.client.PucmonClient;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that represents the ranking
 * visualization containing top players
 * of game
 *
 * @author  Pedro Sampaio
 * @since   1.7
 */
public class Ranking implements ServerListener{
    private final float rankX;  // ranking x position on screen
    private final float rankY;  // ranking y position on screen
    private final float rankW;  // ranking width on screen
    private final float rankH;  // ranking height on screen
    private final Rectangle scissors;   // ranking scissors for clipping
    private final Rectangle clipBounds; // clipping bounds rect
    private final float titleY; // position of ranking titles
    private final String positionStr;   // position title
    private final String levelStr;  // level title
    private final String nameStr; // name title
    private final float gapY; // gap between each ranking entry
    private final String rankingStr; // ranking title
    private boolean isRetrieving; // is data being retrieved?
    private LinkedHashMap<String, Integer> ranking; // the ranking data retrieved from server
    private String character; // player's character name
    private int position;   // player`s position on ranking
    private float deltaY = 0; // delta of ranking in the y axis
    private String infoStr; // string containing information to display to player
    private TextureRegion lineTitle; // line separator for title
    private TextureRegion lineEnd;  // line separator for ending
    private TextureRegion lineEntry; // line separator for entries
    private int charLevel; // current player character level

    /**
     * Ranking constructor
     * Subscribes to server to be able to communicate with it
     * and create ranking and clipping bounds
     */
    public Ranking () {
        // subscribe to server to be able to communicate with it
        subscribeToServer();
        // calculates ranking coords
        rankX = (Config.baseWidth / 2) - (Resource.rankingBG.getWidth() * Config.rankingScale / 2);
        rankY = (Config.baseHeight / 2) - (Resource.rankingBG.getHeight() * Config.rankingScale / 2);
        rankW = Resource.rankingBG.getWidth() * Config.rankingScale;
        rankH = Resource.rankingBG.getHeight() * Config.rankingScale;
        titleY = rankY + rankH * 0.90f;
        // ranking column titles
        rankingStr =  Main.getInstance().getLang().get("rankingStr"); // ranking title
        positionStr =  Main.getInstance().getLang().get("positionStr"); // position title
        nameStr =  Main.getInstance().getLang().get("nameStr"); // name title
        levelStr =  Main.getInstance().getLang().get("levelStr"); // level title
        // gap between each ranking entry
        gapY = new GlyphLayout(Resource.rankingFont, nameStr).height + (rankH*0.03f);
        // create clipping scissors
        scissors = new Rectangle();
        float clipY = rankY+(rankW * 0.23f);
        float clipH = (titleY-clipY)-gapY;
        clipBounds = new Rectangle(rankX,clipY,rankW,clipH);
        // cuts line separators
        lineTitle = new TextureRegion(Resource.lineSeparators, 0, 0, 340, 12);
        lineEnd = new TextureRegion(Resource.lineSeparators, 0, 10, 340, 11);
        lineEntry = new TextureRegion(Resource.lineSeparators, 10, 22, 317, 9);
    }

    /**
     * Latency tests
     */
    private long svRankingTS;

    /**
     * Retrieves updated ranking data
     * by send a ranking message to the server
     * @param character the player's character name
     * @param charLevel the player's character level
     */
    public void retrieveData(String character, int charLevel) {
        this.character = character; // stores player character name
        this.charLevel = charLevel; // stores players level
        // if not connected do not try to send message to server
        if(!PucmonClient.getInstance().isConnected()) {
            return;
        }
        isRetrieving = true; // sets retrieving data flag
        // sends message to server to retrieve ranking data
        // ranking message
        MessageRanking rankMsg = new MessageRanking(character);
        // wraps ranking message in message content class
        MessageContent msg = new MessageContent(this.getClass().toString(), rankMsg, MessageContent.Type.RANKING);
        // sends message to server
        PucmonClient.getInstance().sendMessage(msg);
        // latency test
        svRankingTS = System.currentTimeMillis();
    }

    /**
     * Draws the ranking visualization on screen
     * @param batch the batch to draw the ranking
     * @param camera camera to calculate clipping
     */
    public void render(SpriteBatch batch, Camera camera) {
        // renders ranking background
        batch.draw(Resource.rankingBG, rankX, rankY, rankW, rankH);

        // if not connected, draw info string informing it and return
        if(!PucmonClient.getInstance().isConnected()) {
            infoStr = Main.getInstance().getLang().get("notConnected");
            renderInfoText(batch, rankX, rankY, rankW, rankH);
            return;
        }

        // if not retrieving data
        if(!isRetrieving) {
            // if rank is rank was successfully obtained
            if(ranking != null) {
                // title coords
                float nameW = new GlyphLayout(Resource.rankingTitleFont, nameStr).width;
                float rankingX = rankX + (rankW / 2) - new GlyphLayout(Resource.rankingTitleFont, rankingStr).width/2;
                float posX = rankX + (rankW * 0.15f);
                float nameX = rankX + (rankW / 2) - (nameW / 2);
                float levelX = nameX + nameW + (rankW * 0.2f);

                // draw titles
                Resource.rankingTitleFont.draw(batch, rankingStr, rankingX, rankY + (rankH*0.98f));
                Resource.rankingTitleFont.draw(batch, positionStr, posX, titleY);
                Resource.rankingTitleFont.draw(batch, nameStr, nameX, titleY);
                Resource.rankingTitleFont.draw(batch, levelStr, levelX, titleY);
                // draw title line separator
                float lineTitleX = rankX + (rankW / 2) - (lineTitle.getRegionWidth() / 2);
                float lineTitleY = titleY - (gapY * 1.0f);
                batch.draw(lineTitle, lineTitleX, lineTitleY);
                // draw end line separator
                float lineEndX = rankX + (rankW / 2) - (lineEnd.getRegionWidth() / 2);
                float lineEndY = rankY + (rankH * 0.139f);
                batch.draw(lineEnd, lineEndX, lineEndY);
                // draw player ranking information
                float charRankY = lineEndY - (rankH * 0.012f);
                String charRankInfo = Main.getInstance().getLang().format("charRankInfo", position);
                Resource.rankingTitleFont.draw(batch, charRankInfo, posX, charRankY);
                // ends batch to avoid clipping
                batch.flush();
                batch.end(); // ends batch

                batch.begin();
                // stacks scissors to draw each top player only in clipping bounds
                ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
                ScissorStack.pushScissors(scissors);
                // iterates through each player in ranking
                Iterator it = ranking.entrySet().iterator();
                // position in raking
                int pos = 1;
                while (it.hasNext()) {
                    float rankOff = 0.021f;
                    Map.Entry rankingMap = (Map.Entry) it.next();
                    Resource.rankingFont.draw(batch, String.valueOf(pos), posX,
                            titleY - (rankH*rankOff) - (gapY*pos) + deltaY);
                    Resource.rankingFont.draw(batch, rankingMap.getKey().toString(),
                            nameX, titleY - (rankH*rankOff) - (gapY*pos) + deltaY);
                    Resource.rankingFont.draw(batch, rankingMap.getValue().toString(),
                            levelX, titleY - (rankH*rankOff) - (gapY*pos) + deltaY);
                    // draw entry line separator
                    float lineEntryX = rankX + (rankW / 2) - (lineEntry.getRegionWidth() / 2);
                    float lineEntryY = titleY - (rankH*rankOff) - (gapY*pos) + deltaY + (gapY * 0.1f);
                    batch.draw(lineEntry, lineEntryX, lineEntryY);
                    pos++; // increases position
                }
                // pop scissors to end clipping
                batch.flush();
                ScissorStack.popScissors();
            } else {
                infoStr = Main.getInstance().getLang().get("generalError");
                renderInfoText(batch, rankX, rankY, rankW, rankH);
            }
        } else { // still retrieving ranking
            // draws waiting server response text
            infoStr = Main.getInstance().getLang().get("waitingServerResponse");
            renderInfoText(batch, rankX, rankY, rankW, rankH);
        }
    }

    /**
     * Draws the current information string centralized in the ranking interface
     * @param batch the batch to draw
     * @param rankX the x position of ranking menu
     * @param rankY the y position of ranking menu
     * @param rankW the width of ranking menu
     * @param rankH the height of ranking menu
     */
    private void renderInfoText(SpriteBatch batch, float rankX, float rankY, float rankW, float rankH) {
        final GlyphLayout svWaitLayout = new GlyphLayout(Resource.rankingTitleFont, infoStr);
        float svWaitX = rankX + (rankW/2) - (svWaitLayout.width/2);
        float svWaitY = rankY + (rankH/2) - (svWaitLayout.height/2);
        Resource.rankingTitleFont.draw(batch, svWaitLayout , svWaitX, svWaitY);
    }

    /**
     * Server callbacks
     */
    @Override
    public void subscribeToServer() {
        ServerMessages.getInstance().subscribe(this);
    }

    @Override
    public void handleServerMessage(MessageContent msg) {

        if(Config.debug)
            Common.printLatency("Ranking retrieving", System.currentTimeMillis() - svRankingTS);

        // stores received message data
        MessageFlag svResponseFlag = msg.getFlag();
        Object svResponseObject = msg.getContent();
        MessageContent.Type svResponseType = msg.getType();

        // switches types of messages received
        switch(svResponseType) {
            case RANKING:
                // server response was ok?
                if(svResponseFlag == MessageFlag.OKIDOKI) {
                    // gets ranking data
                    ranking = ((MessageRanking)svResponseObject).getRanking();
                    position = ((MessageRanking)svResponseObject).getPosition();
                    isRetrieving = false; // retrieve complete
                } else {
                    infoStr = Main.getInstance().getLang().get("generalError");
                }
                break;
            default:
                System.err.println("Unknown type of message received: " + svResponseType);
                break;
        }
    }

    /**
     * Called when the user drags a finger over the screen.
     * @param deltaX the difference in pixels to the last drag event on x
     * @param deltaY the difference in pixels to the last drag event on y
     */
    public void pan(float deltaX, float deltaY) {
        // calculates movement on Y axis
        this.deltaY -= deltaY * Config.rankingSensitivityY * Gdx.graphics.getDeltaTime();
        if(this.deltaY < 0) // clamp delta Y to 0, the start position of ranking that displays first position -> nPosShown
            this.deltaY = 0;
        // calculates max delta on account of ranking entries quantity
        if(ranking != null) {
            int nEntriesShown = MathUtils.floor(clipBounds.height / gapY);
            float maxDeltaY = (ranking.size()-nEntriesShown) * gapY;
            if(maxDeltaY < 0) maxDeltaY = 0;
            if (this.deltaY > maxDeltaY) // clamps to avoid surpassing max delta Y
                this.deltaY = maxDeltaY;
        }
    }

}
