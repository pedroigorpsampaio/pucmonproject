package com.mygdx.game.states.game.standard.pervasive;

import com.mygdx.game.Main;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents a pervasive mission
 * with all its information
 *
 * @author  Pedro Sampaio
 * @since   1.9
 */
public class Mission {

    private String source;          // the name representing the source of this mission in missions data file
    private int id;                 // this mission ID (in the context of its location)
    private String locationKey;     // this mission location key
    private int locationID;         // this mission location ID
    private String name;            // name of this mission
    private String description;     // description of this mission
    private boolean isRepeatable;   //  if the mission is repeatable
    private long interval;          // time interval (in minutes) that this mission can be repeated, if repeatable
    private long expReward;         // the reward of this mission in experience points
    private long goldReward;        // the reward of this mission in gold
    private ArrayList<Integer> itemReward; // the reward of this mission in items
    private final long equipLevel;  // equipment level of this item reward
    private ArrayList<Integer> preReqs; // this mission prequesistes (missions that should be done before this one is available)

    /**
     * Constructor that sets basic information of a mission
     * @param source         the name representing the source of this mission in missions data file
     * @param id             the unique id of this mission
     * @param name           the name of this mission
     * @param description    description of this mission
     * @param isRepeatable   if the mission is repeatable
     * @param interval       time interval (in minutes) that this mission can be repeated, if repeatable
     * @param expReward      the reward of this mission in experience points (can be 0)
     * @param goldReward     the reward of this mission in gold (can be 0)
     * @param itemReward     the reward of this mission in items (can be null)
     * @param equipLevel     the level of the equipments rewarded in this mission
     * @param preReqs        this mission prequesistes (missions that should be done before this one is available)
     */
    public Mission(String source, int id, String name, String description, boolean isRepeatable, long interval, long expReward,
                   long goldReward, ArrayList<Integer> itemReward, long equipLevel, ArrayList<Integer> preReqs) {
        this.source = source;
        this.id = id;
        this.name = name;
        this.description = description;
        this.isRepeatable = isRepeatable;
        this.interval = interval;
        this.expReward = expReward;
        this.goldReward = goldReward;
        this.itemReward = itemReward;
        this.equipLevel = equipLevel;
        this.preReqs = preReqs;

        if(this.itemReward == null)
            this.itemReward = new ArrayList<Integer>();

        if(this.preReqs == null)
            this.preReqs = new ArrayList<Integer>();

        // clamps for safety
        if(this.expReward < 0)
            this.expReward = 0;
        if(this.goldReward < 0)
            this.goldReward = 0;
    }

    /**
     * Returns if this mission prerequisites were fulfilled,
     * therefore this mission availability
     * @return true if all prerequisite missions is met, false otherwise
     */
   public boolean preReqMet() {
       // iterates through list of prereqs
       for(int i = 0; i < preReqs.size(); i++) {
           if(!Common.getInstance().getMissionsDone().containsKey(preReqs.get(i))) {
               return false;
           }
       }
       // if no prereq was not met, return true indicating it
       return true;
   }

    /**
     * Returns if this mission was completed by the player already
     * @return true if this mission was already completed by the player, false otherwise
     */
   public boolean missionCompleted () {
       return Common.getInstance().getMissionsDone().containsKey(this.id);
   }

    /**
     * Returns this mission current cooldown if this mission is repeatable
     * @return the current cooldown of this mission in minutes
     */
   public long getCooldown() {
       // if it is not repeatable return 0 cooldown
       if(!this.isRepeatable)
           return 0;

       // gets timestamp of mission (if player has done mission)
       Timestamp ts = Common.getInstance().getMissionsDone().get(this.id);

       // player has not completed this mission yet, return 0 cooldown
       if(ts == null)
           return 0;

       // gets timestamp and current time in milliseconds
       long missionMilli = ts.getTime();
       long currentMilli = System.currentTimeMillis();

       System.out.println("TS: " + ts.toString());

       // calculates the difference in minutes
       long diffMinutes = (currentMilli - missionMilli) / (60 * 1000);
       System.out.println("currentMilli - MissionMilli = " + currentMilli + " - " + missionMilli);

       // calculates cooldown in minutes
       long cooldown = interval - diffMinutes;
       System.out.println("interval - diffMinutes = " + interval + " - " + diffMinutes);

       // clamp when cooldown is surpassed
       if(cooldown < 0) cooldown = 0;

       // return cooldown
       return cooldown;
   }

    /**
     * Returns if this mission is available for the current player
     * based on mission prerequisites, time cooldown and completion
     * @return true if missions is available, false otherwise
     */
   public boolean isAvailable() {
       // checks if prereqs were not met, mission is not available
       if(!preReqMet())
           return false;

       // checks if cooldown is not depleted, in case of repeatable missions, if so, mission is not available
       if(isRepeatable) {
           if (getCooldown() > 0)
               return false;
       } else { // if it is not repeatable, check if mission is already done
           if(missionCompleted())
               return false;
       }

       // tests were ok, mission is available
       return true;
   }

    /**
     * Getters and setters
     */

    public String getName() {
        // updates name to match current language
        name = Main.getInstance().getLang().get(source + "_Name");
        // adds repeatable information in case mission is repeatable
        if (isRepeatable) {
            StringBuilder builtName = new StringBuilder("["+Config.missionRepeatableReady.toString()+"]"+name);
            builtName.append(" (");
            builtName.append(Main.getInstance().getLang().get("Mission_Repeatable"));
            builtName.append(")");
            name = builtName.toString();
        } else {
            StringBuilder builtName = new StringBuilder("["+Config.missionUnfinished.toString()+"]"+name);
            name = builtName.toString();
        }

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public String getDescription() {
        // updates description to match current language
        description = Main.getInstance().getLang().get(source + "_Desc");

        // adds repeatable information in case mission is repeatable
        if (isRepeatable) {
            StringBuilder builtDescription = new StringBuilder("["+Config.missionRepeatableReady.toString()+"]");
            builtDescription.append(Main.getInstance().getLang().get("Mission_Interval"));
            builtDescription.append(": "); builtDescription.append(interval + " ");
            builtDescription.append(Main.getInstance().getLang().get("Mission_TimeUnit"));
            builtDescription.append("\n");
            builtDescription.append(Main.getInstance().getLang().get("Mission_TimeRemaining"));
            builtDescription.append(": "); builtDescription.append(getCooldown() + " ");
            builtDescription.append(Main.getInstance().getLang().get("Mission_TimeUnit"));
            builtDescription.append("\n");
            builtDescription.append("\n");
            builtDescription.append(description);
            description = builtDescription.toString();
        } else {
            StringBuilder builtDescription = new StringBuilder("["+Config.missionUnfinished.toString()+"]");
            builtDescription.append(description);
            description = builtDescription.toString();
        }

        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getExpReward() {
        return expReward;
    }

    public void setExpReward(long expReward) {
        this.expReward = expReward;
    }

    public long getGoldReward() {
        return goldReward;
    }

    public void setGoldReward(long goldReward) {
        this.goldReward = goldReward;
    }

    public ArrayList<Integer> getItemReward() {
        return itemReward;
    }

    public void setItemReward(ArrayList<Integer> itemReward) {
        this.itemReward = itemReward;
    }

    public String getLocationKey() {return locationKey;}

    public void setLocationKey(String locationKey) {this.locationKey = locationKey;}

    public boolean isRepeatable() {return isRepeatable;}

    public long getInterval() {return interval;}

    public ArrayList<Integer> getPreReqs() {return preReqs;}

    public void setPreReqs(ArrayList<Integer> preReqs) {this.preReqs = preReqs;}

    public long getEquipLevel() {return equipLevel;}
}
