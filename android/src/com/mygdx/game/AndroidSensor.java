package com.mygdx.game;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.mygdx.game.sensors.Sensor;
import com.mygdx.game.sensors.SensorCompact;
import com.mygdx.game.sensors.SensorInfo;
import com.mygdx.game.util.Common;
import com.mygdx.game.util.Config;

import java.util.ArrayList;

import br.pucrio.inf.lac.mhub.models.locals.SensorData;
import br.pucrio.inf.lac.mhub.services.S2PAService;

/**
 * Class that implements android platform specific
 * operations that discovers and connects to m-hub
 * objects through BLE technology using Mobile Hub API
 *
 * @author  Pedro Sampaio
 * @since   2.0
 */
public class AndroidSensor implements Sensor{

    /**
     * Singleton instance
     */
    public static AndroidSensor instance = null;

    /**
     * The reference to the android application
     */
    AndroidApplication application;

    /**
     * The list of current accessible sensors
     */
    private ArrayList<SensorInfo> sensors;

    /**
     * Is S2PA service running?
     */
    private boolean isS2PARunning;

    // defeats instantiation (singleton)
    private AndroidSensor() {
        // initialize array list of sensors
        sensors = new ArrayList<>();
        // initially service is not running
        isS2PARunning = false;
    }

    public static AndroidSensor getInstance() {
        if (instance == null)
            instance = new AndroidSensor();

        return instance;
    }

    /**
     * Sets the android application to be used for android operations
     * @param application   the android application to be used for android operations
     */
    public void setApplication(AndroidApplication application) {this.application = application;}

    @Override
    public void startS2PAService() {
        if (!isS2PARunning) {
            application.startService(new Intent(application.getBaseContext(), S2PAService.class));
            isS2PARunning = true;
        }
    }

    @Override
    public void stopS2PAService() {
        if (isS2PARunning) {
            application.stopService(new Intent(application.getBaseContext(), S2PAService.class));
            isS2PARunning = false;
        }
    }

    /**
     * Gets the list of sensors currently accessible
     * @return  the list of sensors accessible in SensorData model
     */
    @Override
    public ArrayList<SensorInfo> getSensors() {
        return sensors;
    }

    @Override
    public ArrayList<SensorInfo> getSensors(ArrayList<SensorCompact> serverSensors) {
        // if serverSensors info is not yet set, just return all sensors
        if(serverSensors == null)
            return sensors;

        // new built sensor list of sensors that are also registered in server
        ArrayList<SensorInfo> registeredSensors = new ArrayList<SensorInfo>();

        // loops through sensors and server sensors adding matches to list of registered sensors
        for(int i = 0; i < sensors.size(); i++) {
            String sFoundID = sensors.get(i).getMouuid();
            for (int j = 0; j < serverSensors.size(); j++) {
                if (sFoundID.equals(serverSensors.get(j).getSensor_id())) {
                    registeredSensors.add(sensors.get(i));
                }
            }
        }
        // return registered sensors
        return registeredSensors;
    }

    /**
     * Updates data of sensors currently accessible in the list of sensors
     * @param sensor    the sensor data in SensorData model to update sensor info list
     */
    public void updateSensor(SensorData sensor) {
        // gets sensor unique ID
        String mObjUUID = sensor.getMouuid();

        // gets current sensor action
        String action = sensor.getAction();

        // boolean that represents if sensor is already on list
        boolean isOnList = false;

        // index of sensor in list, in case sensor is on list
        int sensorIdx = -1;

        // checks if sensor is on list via its unique UUID
        for(int i = 0; i < sensors.size(); i++) {
            if(mObjUUID.equals(sensors.get(i).getMouuid())) {
                isOnList = true;
                sensorIdx = i;
                break;
            }
        }

        // switches between possible actions
        switch (action) {
            case SensorData.FOUND:
                // if it is not on list, create sensor info object and add it to the list
                if(!isOnList) {
                    SensorInfo sInfo = new SensorInfo();
                    sInfo.setMouuid(mObjUUID);
                    sInfo.setSignal(sensor.getSignal());
                    sInfo.setAction(sensor.getAction());
                    sInfo.setTimestamp(System.currentTimeMillis());
                    sensors.add(sInfo);
                } else { // if it is already on list
                    // only updates signal, action and timestamp
                    sensors.get(sensorIdx).setSignal(sensor.getSignal());
                    sensors.get(sensorIdx).setAction(sensor.getAction());
                    sensors.get(sensorIdx).setTimestamp(System.currentTimeMillis());
                }
                break;
            case SensorData.CONNECTED:
                // if it is not on list, create sensor info object and add it to the list
                if(!isOnList) {
                    SensorInfo sInfo = new SensorInfo();
                    sInfo.setMouuid(mObjUUID);
                    sInfo.setAction(sensor.getAction());
                    sInfo.setTimestamp(System.currentTimeMillis());
                    sensors.add(sInfo);
                } else { // if it is already on list
                    // only updates action and timestamp
                    sensors.get(sensorIdx).setAction(sensor.getAction());
                    sensors.get(sensorIdx).setTimestamp(System.currentTimeMillis());
                }
                break;
            case SensorData.DISCONNECTED:
                // only disconnects (remove from list) if sensor is on list
                if(isOnList) {
                    sensors.remove(sensorIdx);
                }
                break;
            case SensorData.READ:
                // if it is not on list, create sensor info object and add it to the list
                if(!isOnList) {
                    SensorInfo sInfo = new SensorInfo();
                    sInfo.setMouuid(mObjUUID);
                    sInfo.setSignal(sensor.getSignal());
                    sInfo.setAction(sensor.getAction());
                    sInfo.setTimestamp(System.currentTimeMillis());
                    sInfo.getServices().put(sensor.getSensorName(), sensor.getSensorValue());
                    sensors.add(sInfo);
                } else { // if it is already on list
                    // only updates signal, action, timestamp and service values
                    sensors.get(sensorIdx).setSignal(sensor.getSignal());
                    sensors.get(sensorIdx).setAction(sensor.getAction());
                    sensors.get(sensorIdx).getServices().put(sensor.getSensorName(), sensor.getSensorValue());
                    sensors.get(sensorIdx).setTimestamp(System.currentTimeMillis());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void refreshSensors(float timeLimit, float signalLimit) {
        // searches and removes sensors that did not made contact surpassing time limit
        for(int i = sensors.size() - 1; i >= 0 ; i--) {
            // calculates time elapsed since last sensor communication
            double timeElapsed = (System.currentTimeMillis() - sensors.get(i).getTimestamp())/1000f;

            // checks if surpassed time limit, and if so, removes it
            if(timeElapsed > timeLimit)
                sensors.remove(i);
            // also, for each sensor, check current signal strength, removing if it is lower than minimum limit
            else {
                if (sensors.get(i).getSignal() == null) continue;
                if (sensors.get(i).getSignal() < signalLimit)
                    sensors.remove(i);
            }
        }
    }

    @Override
    public boolean isBluetoothOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) { // device does not support bluetooth
            return false;
        } else { // returns if bluetooth is enable or not
            return mBluetoothAdapter.isEnabled();
        }
    }
}
