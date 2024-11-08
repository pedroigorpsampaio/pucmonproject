package com.mygdx.game.sensors;

import java.io.Serializable;

/**
 * Compact sensor data for server communication
 *
 * @author  Pedro Sampaio
 * @since   2.0
 */
public class SensorCompact implements Serializable {
    String sensor_id; // sensor ID
    String sensor_type; // sensor type
    int mission_id; // mission ID of this sensor (if sensor type is mission)
    int thumbnail_id; // thumbnail id of this sensor
    String sensor_code; // sensor code to unlock linking
    int n_inputs; // sensor number of required inputs
    String input_type; // type of input that this sensor requires ("integer", "string"...)

    /**
     * Getters and Setters
     */

    public String getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(String sensor_id) {
        this.sensor_id = sensor_id;
    }

    public String getSensor_type() {
        return sensor_type;
    }

    public void setSensor_type(String sensor_type) {
        this.sensor_type = sensor_type;
    }

    public int getMission_id() {
        return mission_id;
    }

    public void setMission_id(int mission_id) {
        this.mission_id = mission_id;
    }

    public int getThumbnail_id() {
        return thumbnail_id;
    }

    public void setThumbnail_id(int thumbnail_id) {
        this.thumbnail_id = thumbnail_id;
    }

    public String getSensor_code() {return sensor_code;}

    public void setSensor_code(String sensor_code) {this.sensor_code = sensor_code;}

    public int getN_inputs() {return n_inputs;}

    public void setN_inputs(int n_inputs) {this.n_inputs = n_inputs;}

    public String getInput_type() {return input_type;}

    public void setInput_type(String input_type) {this.input_type = input_type;}
}
