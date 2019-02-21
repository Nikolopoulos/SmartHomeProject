/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ControlUnit;

import DecisionMakingUnit.DecisionMakingUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import SensorsCommunicationUnit.MicazMote;
import SharedMemory.SharedMemory;
import java.util.ArrayList;
import util.CustomException;

/**
 *
 * @author billaros
 */
public class RequestExecutionThread implements Runnable {

    private int id;
    private String returnValueString;
    private int criticality;
    private CoreDefinition core;
    private boolean running;
    String url;

    public RequestExecutionThread(int id, int criticality, String url) {
        this.id = id;
        this.returnValueString = "";
        this.criticality = criticality;
        //System.out.println("Set criticality to "+criticality);
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
        //System.out.println("Changed criticality to "+criticality);
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
        return returnValueString;
    }

    public void setReturnVal(String returnVal) {
        this.returnValueString = returnVal;
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
        boolean found = false;
        for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == ID) {
                found = true;
                if (criticality > 4) {
                    //System.out.println("Should go to push");
                }

                if (criticality > 4) {
                    returnValueString += m.RequestServiceReading(ServiceURI.split("\\?")[0], false, criticality);
                    SharedMemory.<String, DecisionMakingUnit>get("DMU").reconfigure(new CustomException("Mote", m.getId() + "", "PushCondition"));
                } else if (criticality > 2 && !m.isPush()) {
                    returnValueString += m.RequestServiceReading(ServiceURI.split("\\?")[0], false, criticality);
                } else {
                    returnValueString += m.RequestServiceReading(ServiceURI.split("\\?")[0], true, criticality);
                }
                SharedMemory.<String, Control>get("MCU").setResponseOfRequest(id, returnValueString);
                //System.out.println(core);
                //System.out.println(core.getLoad());
                //

                break;
            }
        }
        if (!found) {
            SharedMemory.<String, Control>get("MCU").setResponseOfRequest(id, "");
        }

    }

}
