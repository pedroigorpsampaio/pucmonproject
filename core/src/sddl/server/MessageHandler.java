package sddl.server;

import com.badlogic.gdx.Gdx;
import com.mygdx.game.messages.MessageContent;
import com.mygdx.game.messages.MessageFlag;
import com.mygdx.game.messages.MessageLogin;
import com.mygdx.game.messages.MessageLogoff;
import com.mygdx.game.messages.MessageMarket;
import com.mygdx.game.messages.MessageMissionData;
import com.mygdx.game.messages.MessageRanking;
import com.mygdx.game.messages.MessageSave;
import com.mygdx.game.messages.MessageSensor;
import com.mygdx.game.messages.MessageSignUp;
import com.mygdx.game.sensors.SensorCompact;
import com.mygdx.game.states.game.standard.item.Item;
import com.mygdx.game.states.game.standard.market.MarketItem;
import com.mygdx.game.util.Config;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.sql.rowset.CachedRowSet;

import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.serialization.Serialization;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import sddl.server.db.DBHelper;

/**
 * Will handle all messages received by the server
 * in order to determine what actions should be done
 * Pattern:
 * Singleton
 *
 * @author  Pedro Sampaio
 * @since   0.1
 */
public class MessageHandler {

    private static MessageHandler instance = null;  // reference for the singleton instance
    private SddlLayer core;     // sddl core
    /**
     * Database
     */
    DBHelper dbHelper; // database helper module to help database manipulations

    /**
     * Constructor to defeat instantiation
     */
    protected MessageHandler() {
        // initalize db helper
        dbHelper = new DBHelper();
    }

    /**
     * Returns the instance of the MessageHandler if
     * it exists, otherwise creates the instance and
     * then proceed to return it, singleton pattern
     *
     * @return the MessageHandler singleton instance
     */
    public static MessageHandler getInstance() {
        if(instance == null)
            instance = new MessageHandler();

        return instance;
    }

    public void handleMessage(SddlLayer core, Message message, MessageContent msgContent) {

        // stores SDDL core reference
        this.core = core;

        /**
         * Switch between types of message received
         */
        switch (msgContent.getType()) {
            case ACK:
                handleAckRequest(message, msgContent);
                break;
            case SIGNUP:
                handleSignUpRequest(message, msgContent);
                break;
            case RANKING:
                handleRankingRequest(message, msgContent);
                break;
            case SAVE:
                handleSaveRequest(message, msgContent);
                break;
            case LOGIN:
                handleLoginRequest(message, msgContent);
                break;
            case LOGOFF:
                handleLogoffRequest(message, msgContent);
                break;
            case MARKET:
                handleMarketRequest(message, msgContent);
                break;
            case MISSION_DATA:
                handleMissionDataRequest(message, msgContent);
                break;
            case SENSOR:
                handleSensorRequest(message, msgContent);
                break;
            default:
                Gdx.app.log("SERVER_MESSAGE_HANDLER_ERROR", "Unknown message type");
                break;
        }
    }

    /**
     * handles ack request (ping test to check connectivity)
     *
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleAckRequest(Message message, MessageContent msgContent) {
        // creates private message with sender info
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setGatewayId(message.getGatewayId());
        privateMessage.setNodeId(message.getSenderId());

        // creates ack return message
        MessageContent retMsg = new MessageContent(null, "ack", MessageContent.Type.ACK);

        // embodies the serializable content message to send
        // to the client that requested the ranking
        ApplicationMessage appMsg = new ApplicationMessage();
        appMsg.setContentObject(retMsg);
        privateMessage.setMessage(Serialization.toProtocolMessage(appMsg));

        // sends the private message to the client
        core.writeTopic(PrivateMessage.class.getSimpleName(), privateMessage);
    }

    /**
     * handles ranking visualization request
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleRankingRequest(Message message, MessageContent msgContent) {
        // response flag
        MessageFlag responseFlag = MessageFlag.OKIDOKI; // initially okidoki

        // gets player character from client to pinpoint and store position in ranking
        String character = ((MessageRanking)msgContent.getContent()).getCharacter();

        // gets ranking data
        CachedRowSet rankingData = dbHelper.getRanking();

        // creates ranking structure
        LinkedHashMap<String, Integer> ranking = new LinkedHashMap<String, Integer>();

        // creates a message for the ranking
        MessageRanking msgRanking = new MessageRanking(character);

        // builds ranking structure with retrieved data, if there are any
        if(rankingData != null) {
            int n = 0; // number of players read

            try {
                // iterates through result set
                while (rankingData.next()) {
                    // if number does not exceed ranking limit of top players, add to ranking structure
                    if(n < Config.rankingNTopPlayers)
                        ranking.put(rankingData.getString("name"), rankingData.getInt("level"));

                    // keeps iterating through result set to find position of client player
                    if(character.equals(rankingData.getString("name"))) {
                        msgRanking.setPosition(n+1);
                        if(n >= Config.rankingNTopPlayers) // if we were just looking for client player position
                            break; // stop iterating once position is found
                    }

                    n++; // increments number of considered players in ranking
                }
            } catch (SQLException e) {
                System.err.println("SQL error while reading ranking data");
                e.printStackTrace();
                responseFlag = MessageFlag.GENERAL_ERROR;
            }
        } else { // ranking was not retrieved, puts flag of general error
            responseFlag = MessageFlag.GENERAL_ERROR;
        }

        // puts ranking data in message
        msgRanking.setRanking(ranking);

        // wraps ranking message in the content message
        MessageContent retMsg = new MessageContent(msgContent.getListener(), msgRanking, MessageContent.Type.RANKING);
        retMsg.setFlag(responseFlag);

        // sends response message to client
        sendMessageToClient(message, retMsg);
    }

    /**
     * Handles sign up requests from client
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleSignUpRequest(Message message, MessageContent msgContent) {
        // the flag representing the result of this request
        MessageFlag requestFlag = null;

        // gets msg data
        String account = ((MessageSignUp) msgContent.getContent()).getAccount();
        String password = ((MessageSignUp) msgContent.getContent()).getPassword();
        String character = ((MessageSignUp) msgContent.getContent()).getCharacter();

        // tries to create new account but only
        // if account and character names are available
        if(dbHelper.isAccountNameAvailable(account)) {
            if(dbHelper.isCharacterNameAvailable(character)) {
                //character and account name are available
                // tries to creates account
                if(!dbHelper.createAccount(account, password)) {
                    // if creation was not successful stores general error flag
                    requestFlag = MessageFlag.GENERAL_ERROR;
                } else { // account was successfully created, tries to create player
                    if(!dbHelper.createPlayer(character, 0, account, 1, 0, 0, 0, 0)) {
                        requestFlag = MessageFlag.GENERAL_ERROR; // could not create player sucessfully
                        dbHelper.removeAccount(account); // deletes created account
                    }
                    else // everything was ok!
                        requestFlag = MessageFlag.OKIDOKI;
                }
            }
            else { // character name not available
                requestFlag = MessageFlag.CHARACTER_NAME_TAKEN;
            }
        } else { // account name not available
            requestFlag = MessageFlag.ACCOUNT_TAKEN;
        }

        // creates a message for the sign up response
        MessageSignUp msgSignUp = new MessageSignUp(null, null, null);

        // wraps sign up message in the content message
        MessageContent retMsg = new MessageContent(msgContent.getListener(), msgSignUp, MessageContent.Type.SIGNUP);

        // sets the flag representing the request result
        retMsg.setFlag(requestFlag);

        // sends message of response to the client
        sendMessageToClient(message, retMsg);
    }

    /**
     * Handles save requests from client
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleSaveRequest(Message message, MessageContent msgContent) {
        /**
         *  gets msg data
          */
        // gets player character name
        String character = ((MessageSave) msgContent.getContent()).getCharacter();
        // gets player world map id
        int worldMap = ((MessageSave) msgContent.getContent()).getWorldMap();
        // gets player level
        int level = ((MessageSave) msgContent.getContent()).getLevel();
        // gets player experience
        long exp = ((MessageSave) msgContent.getContent()).getExperience();
        // gets player position x
        int posx = ((MessageSave) msgContent.getContent()).getPosx();
        // gets player position y
        int posy = ((MessageSave) msgContent.getContent()).getPosy();
        // gets player gold
        long gold = ((MessageSave) msgContent.getContent()).getGold();
        // gets serialized string containing player inventory data
        String invInfo = ((MessageSave) msgContent.getContent()).getInventoryData();
        // gets serialized string containing player worn equipment data
        String eqInfo = ((MessageSave) msgContent.getContent()).getEquipmentData();

        /**
         * updates player general data
          */
        dbHelper.updatePlayer(character, worldMap, level, exp, posx, posy, gold);

        /**
         * updates player inventory items
         */

        // split strings for each item existent
        String[] items = invInfo.split("\\.");
        // resets player item db
        dbHelper.resetPlayerItems(character);
        // iterates through player items strings
        for(int i = 1; i < items.length; i++) { // ignores first data that is trash
            // split string again, to separate item data
            String[] data = items[i].split(";");
            // gets item uID
            int uID = Integer.parseInt(data[0].substring(3));
            // gets item level
            int item_level = Integer.parseInt(data[1].substring(3));
            // gets item inventory page
            int page = Integer.parseInt(data[2].substring(5));
            // gets item i index in inventory
            int i_idx = Integer.parseInt(data[3].substring(2));
            // gets item ji index in inventory
            int j_idx = Integer.parseInt(data[4].substring(2));
            // inserts item into database in player
            dbHelper.addPlayerItem(character, uID, item_level, page, i_idx, j_idx);
        }

        /**
         * updates player worn equipments
         */
        // split strings for each equipment existent
        String[] equips = eqInfo.split("\\.");
        // resets player equipments db
        dbHelper.resetPlayerEquipments(character);
        // iterates through player items strings
        for(int i = 1; i < equips.length; i++) { // ignores first data that is trash
            // split string again, to separate item data
            String[] data = equips[i].split(";");
            // gets equipment uID
            int uID = Integer.parseInt(data[0].substring(3));
            // gets equipment level
            int item_level = Integer.parseInt(data[1].substring(3));
            // gets equipment slot
            int slot = Integer.parseInt(data[2].substring(5));
            // inserts item into database in player
            dbHelper.addPlayerEquipment(character, uID, item_level, slot);
        }

        // uses save to update player online tick
        ServerState.getInstance().updateOnlinePlayer(character);
    }

    /**
     * Handles login requests from client
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleLoginRequest(Message message, MessageContent msgContent) {
        // the flag representing the result of this request
        MessageFlag requestFlag = MessageFlag.OKIDOKI; // initially everything is ok

        // gets msg data
        String account = ((MessageLogin) msgContent.getContent()).getAccount();
        String password = ((MessageLogin) msgContent.getContent()).getPassword();

        // tries to login and get player general data
        CachedRowSet playerData = dbHelper.login(account, password);

        // the response message
        MessageContent response;

        // login was not made, due to account and password mismatch
        if(playerData == null) {
            // builds message only to inform about password and account mistmatch
            response = new MessageContent(msgContent.getListener(), null, MessageContent.Type.LOGIN);
            response.setFlag(MessageFlag.ACCOUNT_PASSWORD_DO_NOT_MATCH);
            // sends message to client
            sendMessageToClient(message, response);
            // return
            return;
        }

        // login message response that will contain all login needed player info
        MessageLogin loginMsg = new MessageLogin(account, password);

        // player data was sucessfully retrieved, gets all player general information
        try {
            while (playerData.next()) {
                // gather all player general info into response login message
                loginMsg.setCharacter(playerData.getString("name"));
                loginMsg.setWorldMap(playerData.getInt("world_map"));
                loginMsg.setLevel(playerData.getInt("level"));
                loginMsg.setExperience(playerData.getLong("experience"));
                loginMsg.setPosx(playerData.getInt("posx"));
                loginMsg.setPosy(playerData.getInt("posy"));
                loginMsg.setGold(playerData.getLong("gold"));
                loginMsg.setFirstLogin(playerData.getBoolean("first_login"));
                break;
            }
        } catch (SQLException e) {
            System.err.println("Could not read player information from login");
            requestFlag = MessageFlag.GENERAL_ERROR;
            e.printStackTrace();
        }

        // if character is already online, return informing about the error
        if(dbHelper.isCharacterOnline(loginMsg.getCharacter())) {
            // builds message only to inform about password and account mismatch
            response = new MessageContent(msgContent.getListener(), null, MessageContent.Type.LOGIN);
            response.setFlag(MessageFlag.CHARACTER_ALREADY_ONLINE);
            // sends message to client
            sendMessageToClient(message, response);
            // return
            return;
        }

        // gets character items
        CachedRowSet playerItems = dbHelper.getPlayerItems(loginMsg.getCharacter());
        String itemsData = ""; // inventory items data
        if(playerItems != null) { // if player has items
            try {
                // builds inventory items dada string
                while (playerItems.next()) {
                    itemsData += ".id_"; // item separator token
                    itemsData += playerItems.getString("uid") + ";"; // stores item uID
                    itemsData += "lv_"+playerItems.getString("level") + ";"; // stores item level
                    itemsData += "page_" + playerItems.getString("page") + ";"; // stores item inventory page index
                    itemsData += "i_" + playerItems.getString("idxi") + ";"; // stores item inventory i - index
                    itemsData += "j_" + playerItems.getString("idxj"); // stores item inventory j - index - last info
                }
            } catch (SQLException e) {
                System.err.println("Could not read items data");
                requestFlag = MessageFlag.GENERAL_ERROR;
                e.printStackTrace();
            }
        }
        // sets inventory items data in login response message
        loginMsg.setInventoryData(itemsData);

        // gets player equipments
        CachedRowSet playerEquips = dbHelper.getPlayerEquipments(loginMsg.getCharacter());
        String equipsData = ""; // player equipments data
        if(playerEquips != null) { // if player has equipped equipments
            try {
                // builds worn equipment data string
                while (playerEquips.next()) {
                    equipsData += ".id_"; // equipment separator token
                    equipsData += playerEquips.getString("uid") + ";"; // stores equipment uID
                    equipsData += "lv_"+playerEquips.getString("level") + ";"; // stores equipment level
                    equipsData += "slot_" + playerEquips.getString("slot"); // stores equipment slot
                }
            } catch (SQLException e) {
                System.err.println("Could not read equipments data");
                requestFlag = MessageFlag.GENERAL_ERROR;
                e.printStackTrace();
            }
        }
        // sets player worn equipments data in login response message
        loginMsg.setEquipmentData(equipsData);

        // if everything is ok, adds to online list
        if(requestFlag == MessageFlag.OKIDOKI) {
            // updates online players data
            ServerState.getInstance().updateOnlinePlayer(loginMsg.getCharacter());
            // updates database
            dbHelper.updatePlayerOnlineStatus(loginMsg.getCharacter(), true);
        }

        // builds message content with login message
        response = new MessageContent(msgContent.getListener(), loginMsg, MessageContent.Type.LOGIN);
        // sets request result flag
        response.setFlag(requestFlag);
        // send response to client
        sendMessageToClient(message, response);
    }

    /**
     * Handle character logoff messages from client
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleLogoffRequest(Message message, MessageContent msgContent) {
        // gets msg data
        String character = ((MessageLogoff) msgContent.getContent()).getCharacter();
        // updates character online status to offline
        dbHelper.updatePlayerOnlineStatus(character, false);
    }

    /**
     * Updates player online status
     * @param character       the character to update
     * @param online    the online status to update
     */
    public void handleStatusUpdate(String character, boolean online) {
        // updates character online status
        dbHelper.updatePlayerOnlineStatus(character, online);
    }

    /**
     * Handle market client requests
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleMarketRequest(Message message, MessageContent msgContent) {
        MessageMarket msgMarket = ((MessageMarket) msgContent.getContent()); // gets market msg content
        MessageMarket.Action msgAction = msgMarket.getAction(); // gets market action

        MessageFlag responseFlag = MessageFlag.OKIDOKI; // initially everything is ok

        // creates responses market message with respective action
        MessageMarket msgMarketResponse = new MessageMarket(msgMarket.getCharacter(), msgMarket.getAction());

        // switches between market actions to properly execute client request
        // and fill the response message with the correct content
        switch (msgAction) {
            case RETRIEVE_ITEMS:
                CachedRowSet rs = dbHelper.retrieveMarketItems(msgMarket.getCharacter());
                if(rs == null) { // no items are being sold, not able to display market
                    responseFlag = MessageFlag.EMPTY_MARKET;
                    break;
                }
                // if not empty, populate list of market items with row set
                ArrayList<MarketItem> marketItems = new ArrayList<MarketItem>(); // array list of market items
                // iterates through row set
                try {
                    while (rs.next()) {
                        MarketItem marketItem = new MarketItem(); // the market item
                        marketItem.setMid(rs.getInt("id")); // sets item market ID
                        marketItem.setUid(rs.getInt("uid")); // sets item unique ID
                        marketItem.setSeller(rs.getString("name")); // sets item seller name
                        marketItem.setLevel(rs.getInt("level")); // sets market item level
                        marketItem.setPrice(rs.getLong("price")); // sets market item price
                        marketItem.setQuality(Item.Quality.values()[rs.getInt("quality")]); // sets market item quality
                        marketItems.add(marketItem); // adds market item to list of market items
                    }
                    // adds list of market items to response message
                    msgMarketResponse.setItems(marketItems);
                } catch (SQLException e) {
                    System.err.println("Could not read market items data");
                    responseFlag = MessageFlag.GENERAL_ERROR;
                    e.printStackTrace();
                }
                break;
            case BUY_ITEM:
                // tries to buy item
                responseFlag = dbHelper.buyMarketItem(msgMarket.getItem().getMid()); // already stores response flag based on result of operation
                msgMarketResponse.setItem(msgMarket.getItem()); // puts item in response msg for further necessities
                break;
            case REGISTER_ITEM:
                // tries to register item
                responseFlag = dbHelper.registerMarketItem(msgMarket.getItem()); // already stores response flag based on result of operation
                msgMarketResponse.setItem(msgMarket.getItem()); // puts item in response msg for further necessities
                break;
            case REMOVE_ITEM:
                // tries to remove item
                responseFlag = dbHelper.removeItemFromMarket(msgMarket.getItem().getMid()); // already stores response flag based on operation result
                msgMarketResponse.setItem(msgMarket.getItem()); // puts item in response msg for further necessities
                break;
            case SHOW_LISTINGS:
                // get listings database row set
                CachedRowSet listingsRS = dbHelper.retrieveMarketListings(msgMarket.getCharacter());
                if(listingsRS == null) { // no items are being sold by player, empty listings
                    responseFlag = MessageFlag.NO_ITEMS_SOLD_BY_PLAYER;
                    break;
                } // else return list of market items being sold by player

                ArrayList<MarketItem> listings = new ArrayList<MarketItem>(); // array list of player listings
                // iterates through row set
                try {
                    while (listingsRS.next()) {
                        MarketItem marketItem = new MarketItem(); // the market item
                        marketItem.setMid(listingsRS.getInt("id")); // sets item market ID
                        marketItem.setUid(listingsRS.getInt("uid")); // sets item unique ID
                        marketItem.setSeller(msgMarket.getCharacter()); // sets item seller name
                        marketItem.setLevel(listingsRS.getInt("level")); // sets market item level
                        marketItem.setPrice(listingsRS.getLong("price")); // sets market item price
                        marketItem.setQuality(Item.Quality.values()[listingsRS.getInt("quality")]); // sets market item quality
                        marketItem.setSold(listingsRS.getBoolean("sold")); // sets if item was sold and player can collect respective gold
                        listings.add(marketItem); // adds market item to list of market items
                    }
                    // adds player listings to response message
                    msgMarketResponse.setItems(listings);
                } catch (SQLException e) {
                    System.err.println("Could not read market items data");
                    responseFlag = MessageFlag.GENERAL_ERROR;
                    e.printStackTrace();
                }
                break;
            case COLLECT:
                // tries to collect item
                responseFlag = dbHelper.collectItemFromMarket(msgMarket.getItem().getMid()); // already stores response flag based on operation result
                msgMarketResponse.setItem(msgMarket.getItem()); // puts item in response msg for further necessities
                break;
            default:
                System.err.println("Unknown type of market action requested: " + msgMarket.getAction());
                responseFlag = MessageFlag.GENERAL_ERROR;
                break;
        }
        
        // wraps ranking message in the content message
        MessageContent retMsg = new MessageContent(msgContent.getListener(), msgMarketResponse, MessageContent.Type.MARKET);
        retMsg.setFlag(responseFlag);

        // sends response message to client
        sendMessageToClient(message, retMsg);
    }

    /**
     * handles mission data of player requests
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleMissionDataRequest(Message message, MessageContent msgContent) {

        MessageMissionData msgMission = ((MessageMissionData) msgContent.getContent()); // gets mission msg content
        MessageMissionData.Action msgAction = msgMission.getAction(); // gets mission action

        // response flag
        MessageFlag responseFlag = MessageFlag.OKIDOKI; // initially okidoki

        // gets player character from client
        String character = msgMission.getCharacter();

        // creates a message response for mission data
        MessageMissionData msgMissionResponse = new MessageMissionData(character, msgAction);

        switch (msgAction) {
            case RETRIEVE_MISSIONS:
                // gets player completed missions data
                CachedRowSet missionData = dbHelper.getPlayerCompletedMissions(character);

                // creates missions structure
                HashMap<Integer, Timestamp> missions = new HashMap<Integer, Timestamp>();

                // builds mission data structure with retrieved data, if there are any
                if (missionData != null) {
                    try {
                        // iterates through result set
                        while (missionData.next()) {
                            // puts each mission completed by the player in mission data structure
                            missions.put(missionData.getInt("mission_id"), missionData.getTimestamp("timestamp"));
                        }
                    } catch (SQLException e) {
                        System.err.println("SQL error while reading missions data");
                        e.printStackTrace();
                        responseFlag = MessageFlag.GENERAL_ERROR;
                    }
                } else { // mission data was not retrieved, puts flag of general error
                    System.err.println("error while reading missions data");
                    responseFlag = MessageFlag.GENERAL_ERROR;
                }

                // puts mission data in message
                msgMissionResponse.setMissions(missions);
                break;
            case COMPLETE_MISSION:
                boolean ret = dbHelper.completeMission(character, msgMission.getMission_id());
                if(!ret) {
                    System.err.println("Something went wrong when registering completed mission in database");
                    responseFlag = MessageFlag.GENERAL_ERROR;
                }
                // sets in response message the id of the completed mission
                // in order to client be able to perform correct operations
                msgMissionResponse.setMission_id(msgMission.getMission_id());

                Iterator it = msgMission.getSensorData().entrySet().iterator();
                while (it.hasNext()) {
                    java.util.Map.Entry pair = (java.util.Map.Entry) it.next();
                    Double[] values = (Double[]) pair.getValue();
                    if(Config.debug)
                        System.out.println("Sensor Service: " + pair.getKey() + " = " + Arrays.toString(values));
                    boolean dataRet = dbHelper.registerSensorInput(msgMission.getSensor_id(), msgMission.getInput_type(),
                                                    pair.getKey().toString(), Arrays.toString(values));
                    if(!dataRet) {
                        System.err.println("Something went wrong when registering sensor data: " + pair.getKey());
                    }
                }

                Iterator it2 = msgMission.getInputData().entrySet().iterator();
                while (it2.hasNext()) {
                    java.util.Map.Entry pair = (java.util.Map.Entry) it2.next();
                    if(Config.debug)
                        System.out.println("Sensor Input: " + pair.getKey() + " = " + pair.getValue());
                    boolean dataRet = dbHelper.registerSensorInput(msgMission.getSensor_id(), msgMission.getInput_type(),
                            pair.getKey().toString(), pair.getValue().toString());
                    if(!dataRet) {
                        System.err.println("Something went wrong when registering sensor data: " + pair.getKey());
                    }
                }
                break;
            default:
                System.err.println("Unknown type of mission data action requested: " + msgMission.getAction());
                responseFlag = MessageFlag.GENERAL_ERROR;
                break;
        }

        // wraps mission message in the content message
        MessageContent retMsg = new MessageContent(msgContent.getListener(), msgMissionResponse, MessageContent.Type.MISSION_DATA);
        retMsg.setFlag(responseFlag);

        // sends response message to client
        sendMessageToClient(message, retMsg);
    }

    /**
     * handles sensor requests
     * @param message       the message received
     * @param msgContent    the message content received
     */
    private void handleSensorRequest(Message message, MessageContent msgContent) {

        MessageSensor msgSensor = ((MessageSensor) msgContent.getContent()); // gets sensor msg content

        // response flag
        MessageFlag responseFlag = MessageFlag.OKIDOKI; // initially okidoki

        // creates sensors structure
        ArrayList<SensorCompact> sensors = new ArrayList<SensorCompact>();

        // gets player sensors data
        CachedRowSet sensorData = dbHelper.getSensors();

        // builds sensor data structure with retrieved data, if there are any
        if (sensorData != null) {
            try {
                // iterates through result set
                while (sensorData.next()) {
                    // create sensor data compact structure
                    SensorCompact s = new SensorCompact();
                    s.setSensor_id(sensorData.getString("sensor_id"));
                    s.setSensor_type(sensorData.getString("sensor_type"));
                    s.setMission_id(sensorData.getInt("mission_id"));
                    s.setThumbnail_id(sensorData.getInt("thumbnail_id"));
                    s.setSensor_code(sensorData.getString("code"));
                    s.setN_inputs(sensorData.getInt("n_inputs"));
                    s.setInput_type(sensorData.getString("input_type"));
                    // puts each sensor found in sensor data structure
                    sensors.add(s);
                }
            } catch (SQLException e) {
                System.err.println("SQL error while reading missions data");
                e.printStackTrace();
                responseFlag = MessageFlag.GENERAL_ERROR;
            }
        } else { // sensor data was not retrieved, puts flag of general error
            responseFlag = MessageFlag.GENERAL_ERROR;
        }
        // adds sensor data to msg
        msgSensor.setSensors(sensors);

        // wraps sensor message in the content message
        MessageContent retMsg = new MessageContent(msgContent.getListener(), msgSensor, MessageContent.Type.SENSOR);
        retMsg.setFlag(responseFlag);

        // sends response message to client
        sendMessageToClient(message, retMsg);
    }


    /**
     * Sends message to client
     * @param message       the message received to be able to send message to message`s client
     * @param msg           the msg of type MessageContent to send to client
     */
    private void sendMessageToClient(Message message, MessageContent msg) {
        // creates private message with needed info
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setGatewayId(message.getGatewayId());
        privateMessage.setNodeId(message.getSenderId());

        // embodies the serializable content message to send
        // to the client that requested the message
        ApplicationMessage appMsg = new ApplicationMessage();
        appMsg.setContentObject(msg);
        privateMessage.setMessage(Serialization.toProtocolMessage(appMsg));

        // sends the private message to the client
        core.writeTopic(PrivateMessage.class.getSimpleName(), privateMessage);
    }

    /**
     * Handle reset online status of all players to offline message
     */
    public void handleResetOnlineStatus() {
        dbHelper.resetOnlineStatus();
    }
}
