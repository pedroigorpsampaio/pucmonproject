package com.mygdx.game.messages;

import com.mygdx.game.sensors.SensorCompact;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The model for the message containing sensor data information
 *
 * @author  Pedro Sampaio
 * @since   2.0
 */
public class MessageSensor implements Serializable{

    private ArrayList<SensorCompact> sensors; // sensor data

    /**
     * Sensor data message constructor
     */
    public MessageSensor() {
        sensors = new ArrayList<SensorCompact>();
    }

    public void setSensors(ArrayList<SensorCompact> sensors) {
        this.sensors = sensors;
    }

    public ArrayList<SensorCompact> getSensors() {
        return sensors;
    }
}
