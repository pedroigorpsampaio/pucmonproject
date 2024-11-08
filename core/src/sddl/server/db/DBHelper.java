package sddl.server.db;


import com.mygdx.game.messages.MessageFlag;
import com.mygdx.game.states.game.standard.market.MarketItem;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;

/**
 * Class that serves as bridge to help
 * server database manipulations
 *
 * @author  Pedro Sampaio
 * @since   1.5
 */
public class DBHelper {

    // factory of data source for db connection
    DataSource ds;

    /**
     * DBHelper constructor creates data source connection data
     */
    public DBHelper() {
        ds = DataSourceFactory.getMySQLDataSource();
    }

    /**
     * Executes queries on database returning
     * the result in a cached row set
     * @param sqlQuery  the sql query to be executed on database
     * @return the cached row set containing the results of the query received in parameter
     */
    private CachedRowSet query(String sqlQuery) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        CachedRowSet rowSet = null;

        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(sqlQuery);
            rowSet = new CachedRowSetImpl(); // stores
            rowSet.populate(rs); // the result set in a cached row set to return it
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                if(rs != null) rs.close();
                if(stmt != null) stmt.close();
                if(con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return rowSet;
    }

    /**
     * Executes operations on database which may be an INSERT, UPDATE,
     * or DELETE statement or an SQL statement that returns nothing, such as an SQL DDL statement.
     * @param sqlQuery  the sql query to be executed on database
     * @return the number of lines affected (0 if none was affected, -1 if error occurred)
     */
    private int execute(String sqlQuery) {
        Connection con = null;
        Statement stmt = null;
        int n_affected = -1;

        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            n_affected = stmt.executeUpdate(sqlQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            try {
                if(stmt != null) stmt.close();
                if(con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return n_affected;
    }

    /**
     * Returns if account name is already taken or not
     * @param account   the account name to check availability
     * @return  true if account name is free, false otherwise
     */
    public boolean isAccountNameAvailable(String account) {
        CachedRowSet rs = query("SELECT * FROM `accounts` WHERE name = '"+account+"'");
        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, account name is free to be created
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
        }

        // otherwise, there are results so account name is not free
        return false;
    }

    /**
     * Returns if character name is already taken or not
     * @param character   the character name to check availability
     * @return  true if character name is free, false otherwise
     */
    public boolean isCharacterNameAvailable(String character) {
        CachedRowSet rs = query("SELECT * FROM `players` WHERE name = '"+character+"'");
        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, character name is free to be created
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
        }

        // otherwise, there are results so character name is not free
        return false;
    }

    /**
     * Creates new account on database
     * @param name   the account name
     * @param password the account password
     * @return  true if account was successfully created, false otherwise
     */
    public boolean createAccount(String name, String password) {
        int lines = execute("INSERT INTO `accounts` (name, password) VALUES ('"+name+"', '"+password+"')");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, everything is ok
        return true;
    }

    /**
     * Removes an account from database
     * @param name   the account name
     * @return  true if account was successfully removed, false otherwise
     */
    public boolean removeAccount(String name) {
        int lines = execute("DELETE FROM `accounts` WHERE name = '"+name+"'");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, everything is ok
        return true;
    }

    /**
     * Creates new player on database
     * @param name   the character name
     * @param worldMap the world map ID of player
     * @param accName   the account name of player to get ID foreign key
     * @param level     the level to create player
     * @param exp       the amount of experience to create player
     * @param posx      the x position of player in the map of worldMap id
     * @param posy      the y position of player in the map of worldMap id
     * @param gold      the amount of gold to create player with
     * @return  true if player was successfully created, false otherwise
     */
    public boolean createPlayer(String name, int worldMap, String accName, int level,
                                long exp, int posx, int posy, long gold) {
        int lines = execute("INSERT INTO `players` (name, world_map, account_id, level, " +
                            "experience, posx, posy, gold) VALUES ('"+name+"', "+worldMap+", " +
                            "(SELECT id from `accounts` WHERE name='"+accName+"'), "+level+", " +
                            +exp+", "+posx+", "+posy+", "+gold+")");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, everything is ok
        return true;
    }

    /**
     * Updates player volatile data on database
     * @param name      the character name of player that will be updated
     * @param worldMap  the current world map ID of player
     * @param level     the current level of player
     * @param exp       the current amount of experience of player
     * @param posx      the current x position of player in current worldMap
     * @param posy      the current y position of player in current worldMap
     * @param gold      the current amount of gold of player
     * @return  true if player was successfully updated, false otherwise
     */
    public boolean updatePlayer(String name, int worldMap, int level,
                                long exp, int posx, int posy, long gold) {
        int lines = execute("UPDATE `players` SET world_map = "+worldMap+", level = "+level+", " +
                            "experience = "+exp+", posx = "+posx+", posy = "+posy+"," +
                            "gold = "+gold+", first_login = 0, online = true WHERE name = '"+name+"';");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, everything is ok
        return true;
    }

    /**
     * Tries to login with given account name and password
     * returning player information in case of success
     * @param name      the account name
     * @param password  the account password
     * @return  the cached row set containing player information in case of success, null otherwise
     */
    public CachedRowSet login(String name, String password) {
        // query db for a match in account name and password
        CachedRowSet rs = query("SELECT * FROM `players` WHERE account_id = (SELECT id FROM " +
                                "`accounts` WHERE name = '"+name+"' AND password = '"+password+"')");
        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, account name and password did not match any entry
                return null; // return null indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return null;
        }

        // otherwise, return
        return rs;
    }

    /**
     * Gets every item a character has in inventory (excluding worn equipments)
     * @param character the character name to get items
     * @return cached row set containing all items from character received in parameter,
     *         null in case of empty entries
     */
    public CachedRowSet getPlayerItems(String character) {
        CachedRowSet rs = query("SELECT * FROM `player_items` WHERE player_id = " +
                "(SELECT id FROM players WHERE name = '"+character+"')");

        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, player has no items
                return null; // return null indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return null;
        }

        // otherwise, return
        return rs;
    }

    /**
     * Inserts items into character item database
     * @param character the character name to insert items
     * @param uid  the unique id of item to be added
     * @param level the level of item to be added
     * @param page the page from inventory of item to be added
     * @param idxi the index i from inventory of item to be added
     * @param idxj the index j from inventory of item to be added
     * @return true if player item was successfully added, false otherwise
     */
    public boolean addPlayerItem(String character, int uid, int level, int page, int idxi, int idxj) {
        int lines = execute("INSERT INTO `player_items` (uid, player_id, level, page, idxi, idxj) " +
                "VALUES ("+uid+", " + "(SELECT id from `players` WHERE name='"+character+"'), "
                +level+", "+page+", "+idxi+", "+idxj+")");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, return
        return true;
    }

    /**
     * Resets player items by deleting all entries of player received in parameter
     * @param character the character to delete all items from
     * @return true if player items were successfully reset, false otherwise
     */
    public boolean resetPlayerItems(String character) {
        int rowsDeleted = execute("DELETE FROM `player_items` WHERE player_id = " +
                                    "(SELECT id FROM `players` where name = '"+character+"')");

        // resets ID from table to not overflow indexes
        execute("ALTER TABLE player_items AUTO_INCREMENT = 1;");

        if(rowsDeleted < 0) // something went wrong
            return false;

        // otherwise, return
        return true;
    }

    /**
     * Gets every equipment a character has equipped
     * @param character the character name to get equipments
     * @return cached row set containing all equipments from character received in parameter,
     *         null in case of empty entries
     */
    public CachedRowSet getPlayerEquipments(String character) {
        CachedRowSet rs = query("SELECT * FROM `player_equipments` WHERE player_id = " +
                "(SELECT id FROM players WHERE name = '"+character+"')");

        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, player has no items
                return null; // return null indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return null;
        }

        // otherwise, return
        return rs;
    }

    /**
     * Inserts equipments into character equipment database
     * @param character the character name to insert equipment
     * @param uid  the unique id of equipment to be added
     * @param level the level of equipment to be added
     * @param slot the equipment slot of the equipment to be added
     * @return true if player equipment was successfully added, false otherwise
     */
    public boolean addPlayerEquipment(String character, int uid, int level, int slot) {
        int lines = execute("INSERT INTO `player_equipments` (uid, player_id, level, slot) " +
                "VALUES ("+uid+", " + "(SELECT id from `players` WHERE name='"+character+"'), "
                +level+", "+slot+")");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, return
        return true;
    }

    /**
     * Resets player equipments by deleting all entries of player received in parameter
     * @param character the character to delete all equipments from
     * @return true if player equipments were successfully reset, false otherwise
     */
    public boolean resetPlayerEquipments(String character) {
        int rowsDeleted = execute("DELETE FROM `player_equipments` WHERE player_id = " +
                "(SELECT id FROM `players` where name = '"+character+"')");

        // resets ID from table to not overflow indexes
        execute("ALTER TABLE player_equipments AUTO_INCREMENT = 1;");

        if(rowsDeleted < 0) // something went wrong
            return false;

        // otherwise, return
        return true;
    }

    /**
     * Checks if a character is online
     * @param character the character name to check if it is online
     * @return true if player is online, false otherwise (or if there was a problem)
     */
    public boolean isCharacterOnline(String character) {
        CachedRowSet rs = query("SELECT online FROM `players` WHERE name = '"+character+"'");

        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, player was not found
                return false; // return false indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return false;
        }

        boolean online = false;
        try {
            while (rs.next()) {
                online = rs.getBoolean("online");
            }
        } catch (SQLException e) {
            System.err.println("Error reading character online data: "+ character);
            e.printStackTrace();
            return false;
        }

        // otherwise, return
        return online;
    }

    /**
     * Updates online status of a character
     * @param name      the character name of player that will be updated
     * @param online      the online status to update player with
     * @return  true if player was successfully updated, false otherwise
     */
    public boolean updatePlayerOnlineStatus(String name, boolean online) {
        int lines = execute("UPDATE `players` SET online = "+online+" WHERE name = '"+name+"'");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, everything is ok
        return true;
    }

    /**
     * Reset online status of all players to offline
     * @return true if status was successfully reset, false otherwise
     */
    public boolean resetOnlineStatus() {
        int lines = execute("UPDATE players SET online = 0");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, everything is ok
        return true;
    }

    /**
     * Gets ranking of players by retrieving list of players
     * in order of their gained experience (thus, their levels)
     * @return  the cached row set containing ranking information in case of success, null otherwise
     */
    public CachedRowSet getRanking() {
        // query db for getting descending order of players based on their experience
        CachedRowSet rs = query("SELECT name,level FROM `players` ORDER BY experience DESC");
        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, there are no players in game yet
                return null; // return null indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return null;
        }

        // otherwise, return
        return rs;
    }

    /**
     * Retrieves all items being sold in market
     * excluding the ones being sold by client player character
     * @param character the name of client character
     * @return the cached row set containing market items information in case of success, null otherwise
     */
    public CachedRowSet retrieveMarketItems(String character) {
        // query db to get all entries of market items except the ones being sold by client character
        CachedRowSet rs = query("SELECT  market.*, players.name FROM `market` " +
                "JOIN players ON market.player_id = players.id WHERE player_id != " +
                "(SELECT id FROM `players` where name = '"+character+"') AND sold = 0");
        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, there are no items being sold at the moment that d
                return null; // return null indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return null;
        }

        // otherwise, return
        return rs;
    }

    /**
     * Execute operations to buy market items
     * if items were not bought yet
     * @param mid the market id of item to be bought
     * @return message flag containing the flag result of this operation
     */
    public MessageFlag buyMarketItem(int mid) {
        // query availability of market item
        CachedRowSet rs = query("SELECT sold FROM `market` WHERE id = "+mid+"");

        boolean sold = false;
        try {
            while (rs.next()) {
                sold = rs.getBoolean("sold"); // gets sold flag
            }
        } catch (SQLException e) {
            System.err.println("Error reading market item data of id: "+ mid);
            e.printStackTrace();
            return MessageFlag.GENERAL_ERROR; // returns general error flag
        }

        // if item was already sold, return flag representing it
        if(sold)
            return MessageFlag.ITEM_ALREADY_BOUGHT;
        else { // if not sold, update status to sold as it will be bought by client request
            int lines = execute("UPDATE `market` SET sold = 1 WHERE id = "+mid+"");

            if(lines < 1) // something went wrong
                return MessageFlag.GENERAL_ERROR;
        }

        // return ok flag
        return MessageFlag.OKIDOKI;
    }

    /**
     * Register an item to be sold in market item
     * by adding the entry to the market item db table
     * @param item the item to be added to the list of market items being sold
     * @return message flag containing the flag result of this operation
     */
    public MessageFlag registerMarketItem(MarketItem item) {
        // execute insert into database market table
        int lines = execute("INSERT INTO `market` (uid, player_id, level, price, quality, sold) " +
                "VALUES ("+item.getUid()+",(SELECT id from `players` WHERE name='"+item.getSeller()+"'), "
                +item.getLevel()+", "+item.getPrice()+", "+item.getQuality().ordinal()+" , 0)");

        if(lines < 1) // something went wrong
            return MessageFlag.GENERAL_ERROR; // return general error flag

        // deletes item entry from player items in database table as it has been put in market
        int rowsDeleted = execute("DELETE FROM `player_items` WHERE player_id = " +
                "(SELECT id FROM `players` WHERE name = '"+item.getSeller()+"') AND " +
                "page = "+item.getPage()+" AND idxi = "+item.getIdxI()+" AND idxj = "+item.getIdxJ()+"");

        // otherwise, return ok flag
        return MessageFlag.OKIDOKI;
    }

    /**
     * Removes item from market
     * @param mid the market id of item to be removed
     * @return message flag containing the flag result of this operation
     */
    public MessageFlag removeItemFromMarket(int mid) {
        // deletes item entry from market
        int rowsDeleted = execute("DELETE FROM `market` WHERE id = " + mid);

        if(rowsDeleted < 0) // something went wrong (item was not there anymore, sold already)
            return MessageFlag.ITEM_ALREADY_SOLD; // return flag representing that item was already sold

        return MessageFlag.OKIDOKI; // return flag representing that remove operation was successful
    }

    /**
     * Retrieves all items being sold in market by client player
     * @param character the name of client character
     * @return the cached row set containing market items information in case of success,
     *          null if no items are sold by player or sql error happens
     */
    public CachedRowSet retrieveMarketListings(String character) {
        // query db to get all entries of market items sold by client character
        CachedRowSet rs = query("SELECT * FROM `market` WHERE player_id = " +
                "(SELECT id FROM `players` where name = '"+character+"')");
        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, there are no items being sold at the moment that by client player
                return null; // return null indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return null;
        }

        // otherwise, return
        return rs;
    }

    /**
     * Collects sold item from market
     * @param mid the market id of item to be collected
     * @return message flag containing the flag result of this operation
     */
    public MessageFlag collectItemFromMarket(int mid) {
        // deletes item entry from market
        int rowsDeleted = execute("DELETE FROM `market` WHERE id = " + mid);

        if(rowsDeleted < 0) // something went wrong (item was not there anymore, collected already)
            return MessageFlag.ITEM_ALREADY_COLLECTED; // return flag representing that item was already sold

        return MessageFlag.OKIDOKI; // return flag representing that remove operation was successful
    }

    /**
     * Gets missions completed by player.
     *
     * @param character the name of client character
     * @return the cached row set containing missions completed by the player in case of success,
     *          empty row set if player has not completed any missions yet or null if sql error happens
     */
    public CachedRowSet getPlayerCompletedMissions(String character) {
        // query db to get mission entry by client character
        CachedRowSet rs = query("SELECT * FROM `mission_storage` WHERE player_id = " +
                                "(SELECT id FROM `players` where name = '"+character+"')");
        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, player has not done any mission yet
                return rs; // return null indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return null;
        }

        // otherwise, return missions data
        return rs;
    }

    /**
     * Gets existing sensors data in server db
     *
     * @return the cached row set containing sensors data in case of success,
     *          null if an error has occurred
     */
    public CachedRowSet getSensors() {
        // query db to get existing sensors data
        CachedRowSet rs = query("SELECT * FROM `sensors`");
        try {
            // if cached row set is empty, isBeforeFirst is false
            if (!rs.isBeforeFirst() ) {
                // in this case, there are no sensors registered
                return null; // return null indicating it
            }
        } catch (SQLException e) {
            System.err.println("Error reading sql row set");
            e.printStackTrace();
            return null;
        }

        // otherwise, return missions data
        return rs;
    }

    /**
     * Inserts mission completion record into mission storage database
     * @param character the character name of player that completed mission
     * @param mission_id  the mission id of the completed mission
     * @return true if mission was successfully added, false otherwise
     */
    public boolean completeMission(String character, int mission_id) {
        int lines = execute("INSERT INTO `mission_storage` (mission_id, player_id, timestamp) " +
                "VALUES ("+mission_id+", (SELECT id from `players` WHERE name='"+character+"'), NULL)");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, return
        return true;
    }


    /**
     * Registers in the database sensors obtained inputs,
     * via automatic sensing or player sensing
     * @param sensor_id     the id of the sensor responsible for collecting the data
     * @param input_type    the type of this input
     * @param input_name    the name of this input
     * @param input_data    the data of this input
     * @return true if everything went ok on database registration, false otherwise
     */
    public boolean registerSensorInput(String sensor_id, String input_type, String input_name, String input_data) {
        int lines = execute("INSERT INTO `sensors_input` (sensor_id, input_type, input_name, input_data, timestamp) " +
                "VALUES ('"+sensor_id+"','"+input_type+"','"+input_name+"','"+input_data+"',NULL)");

        if(lines < 1) // something went wrong
            return false;

        // otherwise, return
        return true;
    }
}
