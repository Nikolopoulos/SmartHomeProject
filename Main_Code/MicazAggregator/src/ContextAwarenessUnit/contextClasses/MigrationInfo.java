/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContextAwarenessUnit.contextClasses;

import SensorsCommunicationUnit.MicazMote;
import java.util.ArrayList;

/**
 *
 * @author billaros
 */
public class MigrationInfo {
    float currentLoad;
    ArrayList<String> supportedDrivers;
    ArrayList<MicazMote> visibleSensors;
    String ip;
    String port;

    public float getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(float currentLoad) {
        this.currentLoad = currentLoad;
    }

    public ArrayList<String> getSupportedDrivers() {
        return supportedDrivers;
    }

    public void setSupportedDrivers(ArrayList<String> supportedDrivers) {
        this.supportedDrivers = supportedDrivers;
    }

    public ArrayList<MicazMote> getVisibleSensors() {
        return visibleSensors;
    }

    public void setVisibleSensors(ArrayList<MicazMote> visibleSensors) {
        this.visibleSensors = visibleSensors;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public MigrationInfo(float currentLoad, ArrayList<String> supportedDrivers, ArrayList<MicazMote> visibleSensors, String ip, String port) {
        this.currentLoad = currentLoad;
        this.supportedDrivers = supportedDrivers;
        this.visibleSensors = visibleSensors;
        this.ip = ip;
        this.port = port;
    }
    
    
}
