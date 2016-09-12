/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DecisionMaking;

import affinitySupport.Core;
import java.util.logging.Level;
import java.util.logging.Logger;
import sensorPlatforms.MicazMote;
import util.Control;
import util.CoreManagement;

/**
 *
 * @author billaros
 */
public class SuperSmahtThread extends Thread {

    private int myId;
    private String sirh;
    private ServiceEstimation tsoulou;
    private Core whatCore;
    private Control c;
    private boolean running;
    String ServiceArguements;

    public SuperSmahtThread(int id, ServiceEstimation tsoulou, Control myControl, String ServiceArguements) {
        this.myId = id;
        this.sirh = "";
        this.tsoulou = tsoulou;
        this.ServiceArguements = ServiceArguements;
        c = myControl;
        running = false;
    }

    public int getMyId() {
        return myId;
    }

    public ServiceEstimation getTsoulou() {
        return tsoulou;
    }

    public void setTsoulou(ServiceEstimation tsoulou) {
        this.tsoulou = tsoulou;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Core getWhatCore() {
        return whatCore;
    }

    public void setWhatCore(Core whatCore) {
        this.whatCore = whatCore;
        try {
            CoreManagement.getLeastBusyCore().addThread(this);
        } catch (Exception ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Control getC() {
        return c;
    }

    public void setC(Control c) {
        this.c = c;
    }

    public void setId(int id) {
        this.myId = id;
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
        int ID = Integer.parseInt(ServiceArguements.split("/")[2]);
        String ServiceURI = "/" + ServiceArguements.split("/")[ServiceArguements.split("/").length - 1];
        for (MicazMote m : util.SensorManager.getSensorsList()) {
            if (m.getId() == ID) {
                if (this.getTsoulou().getClEstimation() > 2.5) {
                    sirh += m.RequestServiceReading(ServiceURI.split("\\?")[0], false,util.SensorManager.getMessages());
                } else {
                    sirh += m.RequestServiceReading(ServiceURI.split("\\?")[0], true,util.SensorManager.getMessages());
                }
                break;
            }
        }

    }

}
