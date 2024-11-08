package com.mygdx.game.messages;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * The model for the message containing mission data information
 *
 * @author  Pedro Sampaio
 * @since   2.0
 */
public class MessageMissionData implements Serializable{

    // Types of actions that can be requested to server in this message
    public enum Action {RETRIEVE_MISSIONS, COMPLETE_MISSION}

    private Action action; // the action to request to server in this message

    private HashMap<Integer, Timestamp> missions; // missions data

    private String character; // player character name

    private int mission_id; // id of mission in case of mission completion

    /**
     * Other sensor obtained data
     */
    private String sensor_id; // sensor ID
    private String input_type; // sensor type
    private HashMap<String, Double[]> sensorData; // sensor collected data
    private HashMap<String, String> inputData; // player sensing data

    /**
     * Mission data message constructor
     * @param character the name of player character
     */
    public MessageMissionData(String character, Action action) {
        this.character = character;
        this.action = action;
        missions = new HashMap<Integer, Timestamp>();
    }

    public void setMissions(HashMap<Integer, Timestamp> missions) {
        this.missions = missions;
    }

    public HashMap<Integer, Timestamp> getMissions() {
        return missions;
    }

    public String getCharacter() {return character;}

    public void setCharacter(String character) {this.character = character;}

    public Action getAction() {return action;}

    public void setAction(Action action) {this.action = action;}

    public int getMission_id() {return mission_id;}

    public void setMission_id(int mission_id) {this.mission_id = mission_id;}

    public String getSensor_id() {return sensor_id;}

    public void setSensor_id(String sensor_id) {this.sensor_id = sensor_id;}

    public String getInput_type() {return input_type;}

    public void setInput_type(String input_type) {this.input_type = input_type;}

    public void setSensorData(HashMap<String,Double[]> sensorData) {this.sensorData = sensorData;}

    public void setInputData(HashMap<String,String> inputData) {this.inputData = inputData;}

    public HashMap<String, Double[]> getSensorData() {return sensorData;}

    public HashMap<String, String> getInputData() {return inputData;}
}
