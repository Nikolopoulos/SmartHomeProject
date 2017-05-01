/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ControlUnit;

import Libraries.Core;
import java.util.logging.Level;
import java.util.logging.Logger;
import SensorsCommunicationUnit.MicazMote;
import ControlUnit.Control;
import DecisionMaking.ServiceEstimation;
import SharedMemory.SharedMemory;
import java.util.ArrayList;

/**
 *
 * @author billaros
 */
public class RequestExecutionThread implements Runnable {

    private int id;
    private String sirh;
    private int criticality;
    private CoreDefinition core;
    private boolean running;
    String url;

    public RequestExecutionThread(int id, int criticality, String url) {
        this.id = id;
        this.sirh = "";
        this.criticality = criticality;
        this.url = url;
        running = false;
    }

    public int getId() {
        return id;
    }

    public int getCriticality() {
        return criticality;
    }

    public void setCriticality(int criticality) {
        this.criticality = criticality;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public CoreDefinition getWhatCore() {
        return core;
    }

    public void setWhatCore(CoreDefinition whatCore) {
        this.core = whatCore;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReturnVal() {
        return sirh;
    }

    public void setReturnVal(String returnVal) {
        this.sirh = returnVal;
    }

    @Override
    public void run() {
        running = true;
        try {
            core.attachTo(this);
        } catch (Exception ex) {
            Logger.getLogger(RequestExecutionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        int ID = Integer.parseInt(url.split("/")[2]);
        String ServiceURI = "/" + url.split("/")[url.split("/").length - 1];
        for (MicazMote m : SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == ID) {
                if (criticality > 2.5) {
                    sirh += m.RequestServiceReading(ServiceURI.split("\\?")[0], false);
                } else {
                    sirh += m.RequestServiceReading(ServiceURI.split("\\?")[0], true);
                }
                SharedMemory.<String,Control>get("MCU").setResponseOfRequest(id, sirh);
                System.out.println(core);
                System.out.println(core.getLoad());
                core.setLoad(core.getLoad()-1);
                
                break;                
            }
        }

    }

}
