package com.mygdx.game.sensors;

import java.util.ArrayList;

/**
 * The interface that will serve as a bridge
 * to the platform specific (android) sensor
 * discovery and connection functionality
 *
 * @author  Pedro Sampaio
 * @since   2.0
 */
public interface Sensor {
    /**
     * Method that starts S2PA service that
     * scans and connects to m-hub objects via BLE technology
     */
    void startS2PAService();

    /**
     * Method that stops S2PA service finishing
     * the scan and connection of m-hub objects.
     * When app is killed, S2PA service is killed automatically.
     */
    void stopS2PAService();

    /**
     * Gets the list of sensors currently accessible
     * @return  the list of sensors accessible in SensorData model
     */
    ArrayList<SensorInfo> getSensors();

    /**
     * Gets the list of sensors currently accessible registered in server
     * @param serverSensors the list of registered sensors in server
     * @return  the list of sensors accessible in SensorData model that are registered in server
     */
    ArrayList<SensorInfo> getSensors(ArrayList<SensorCompact> serverSensors);

    /**
     * Refreshes sensor list by removing sensors that
     * surpassed the time limit received in parameter
     * without communication
     * @param timeLimit the time limit of a sensor without communication to be in sensor list
     * @param signalLimit the minimum signal strength to consider sensor in list of sensors
     */
    void refreshSensors(float timeLimit, float signalLimit);

    /**
     * Checks if bluetooth is available or not,
     * returning a bool indicating it
     * @return true if bluetooth is available, false otherwise
     */
    boolean isBluetoothOn();
}
