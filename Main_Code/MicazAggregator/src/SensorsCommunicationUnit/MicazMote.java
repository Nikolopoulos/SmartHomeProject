/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SensorsCommunicationUnit;

import SharedMemory.SharedMemory;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Util;

/**
 *
 * @author billaros
 */
public class MicazMote {

    //This class models a micaz mote
    private int id;
    private int services;
    private boolean tempService = false;
    private boolean photoService = false;
    private boolean switchService = false;
    private ArrayList<Service> servicesList;
    private int callsSinceLastMonitoring;
    private int highestCritSinceLastMonitoring;
    
    private boolean push = false;

    private enum voltage {

        LOW, HIGH
    };

    private double tempReading;
    private double photoReading;

    private long latestActivity;
    private int switchState;

    public int getServicesInt() {
        return services;
    }
    
    public MicazMote(int id, int services, long latestActivity) {
        this.servicesList = new ArrayList<Service>();
        this.id = id;
        this.services = services;
        if (services / Constants.PIN_S > 0.9) {
            services -= 4;
            this.switchService = true;
            switchState = -1;
            servicesList.add(new Service("switch", "Switch service for accuator", "/switch", "boolean"));
        }
        if (services / Constants.PHOTO_S > 0.9) {
            services -= 2;
            this.photoService = true;
            servicesList.add(new Service("photo", "Light levels service", "/photo", "lum?"));
        }
        if (services % 2 == Constants.TEMP_S) {
            services -= Constants.TEMP_S;
            this.tempService = true;
            servicesList.add(new Service("temp", "Temperature levels service", "/temp", "Celsious"));
        }
        this.photoReading = -1;
        this.tempReading = -1;
        this.switchState = -1;
        this.latestActivity = latestActivity;
    }

    public int getSwitchState() {
        for (Service s : servicesList) {
            if (s.getName().equals("switch")) {
                return Integer.parseInt(s.getDecimalValue());
            }
        }
        return -1;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    
    
    public void setSwitchState(int switchState) {
        for (Service s : servicesList) {
            //if (s.getName().equals("temp")) { //hotfix maybe it;s wrong
            if (s.getName().equals("switch")) {
                s.setDecimalValue(switchState+"");
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Service> getServices() {
        return servicesList;
    }

    public void setServices(int services) {
        this.services = services;
    }

    public boolean isTempService() {
        return tempService;
    }

    public void setTempService(boolean tempService) {
        this.tempService = tempService;
    }

    public boolean isPhotoService() {
        return photoService;
    }

    public void setPhotoService(boolean photoService) {
        this.photoService = photoService;
    }

    public boolean isSwitchService() {
        return switchService;
    }

    public void setSwitchService(boolean switchService) {
        for (Service s : servicesList) {
            if (s.getName().equals("switch")) {
                s.setDecimalValue(((Integer.parseInt(s.getDecimalValue()) + 1) % 2) + "");
                s.setLatestReading(Util.getTime());
            }
        }

    }

    public double getTempReading() {
        for (Service s : servicesList) {
            if (s.getName().equals("temp")) {
                return s.getDoubleUnits();
            }
        }

        return 0.0d;
    }

    public void setTempReading(double tempReading) {
        for (Service s : servicesList) {
            //System.out.println("Setting temp to " + tempReading);
            if (s.getName().equals("temp")) {
                //System.out.println("Found service " + tempReading);
                s.setDecimalValue(util.Util.a2d2celsius(tempReading) + "");
                //System.out.println("Set temp to " + s.getDecimalValue());
                s.setLatestReading(Util.getTime());
            }
        }
    }

    public double getPhotoReading() {
        for (Service s : servicesList) {
            if (s.getName().equals("photo")) {
                return s.getDoubleUnits();
            }

        }
        return 0.0;
    }

    public void setPhotoReading(double photoReading) {
        for (Service s : servicesList) {
            if (s.getName().equals("photo")) {
                s.setLatestReading(Util.getTime());
                s.setDecimalValue(photoReading + "");
            }
        }
    }

    public long getLatestActivity() {
        return latestActivity;
    }

    public void setLatestActivity(long latestActivity) {
        this.latestActivity = latestActivity;
    }

    @Override
    public String toString() {
        String toReturn = id + " sensor provides: \n\t";
        for (Service s : servicesList) {
            toReturn += s.getName() + " service\n\t";
        }
        return toReturn;
    }

    public String JSONDescription() {
        //return "{"+id+"\t Micaz provides: " + (tempService?"temperature service ":"")+ (photoService?"photo service ":"") + (switchService?"switch service ":"") ;
        String reply = "{\"ID\":\"" + getId() + "\", ";
        for (Service s : servicesList) {
            reply += "\"" + s.getName() + "\":\"true\"";
        }
        reply += "\"}";
        return reply;
    }

    public String JSONObject() {
        String reply = "\"ID\":\"" + getId() + "\", \"TemperatureService\":\"" + this.isTempService() + "\", \"LightService\":\"" + this.isPhotoService() + "\", \"SwitchService\":\"" + this.isSwitchService() + "\", \"Temperature\":\"" + util.Util.a2d2celsius((int) this.getTempReading()) + "\", \"Light\":\"" + this.getPhotoReading() + "\", \"Switch\":\"" + this.getSwitchState() + "\" ";
        return reply;
    }

    public String JSONLight() {
        String reply = "\"ID\":\"" + getId() + "\", \"Light\":\"" + this.getPhotoReading() + "\" ";
        return reply;
    }

    public String JSONTemp() {
        String reply = "\"ID\":\"" + getId() + "\", \"Temperature\":\"" + util.Util.a2d2celsius((int) this.getTempReading()) + "\" ";
        return reply;
    }

    public int getCallsSinceLastMonitoring() {
        return callsSinceLastMonitoring;
    }

    public void upCallsSinceLastMonitoring() {
        this.callsSinceLastMonitoring++;
    }

    public void reset() {
        this.callsSinceLastMonitoring = 0;
        this.highestCritSinceLastMonitoring = 0;
        //this.push=false;
    }
    public int getHighestCritSinceLastMonitoring() {
        return highestCritSinceLastMonitoring;
    }

    public void setHighestCritSinceLastMonitoring(int highestCritSinceLastMonitoring) {
        if(highestCritSinceLastMonitoring > this.highestCritSinceLastMonitoring)
            this.highestCritSinceLastMonitoring = highestCritSinceLastMonitoring;
    }

    public String RequestServiceReading(String ServiceURI, boolean cached, int criticality) {
    
        setHighestCritSinceLastMonitoring(criticality);
        upCallsSinceLastMonitoring();
        String reply = "genericError";
        int type = -99;
        for (Service s : servicesList) {
            //System.out.println(s.getURI() + " vs " + ServiceURI);
            //30000 is cache
            long cacheMs = 10000;
            if ((s.getURI().contains(ServiceURI)&& isPush()) || (s.getURI().contains(ServiceURI) && cached && System.currentTimeMillis() - s.getLatestReading() < cacheMs && s.getDecimalValue() != null)) {
                reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
            } else if (s.getURI().contains(ServiceURI)) {
                if (s.getURI().contains("/temp")) {
                    //System.out.println("ELSE INNER s.uri vs serviceuri " + s.getURI() + " vs " + ServiceURI + " is contained? " + s.getURI().contains(ServiceURI));
                    //System.out.println("Uri contains/temp " + ServiceURI);
                    type = Constants.TEMP;
                    SharedMemory.<String,SensorsCommunicationUnit>get("SCU").sendReadingRequest(id, type);

                    //System.out.println("temp");
                } else if (s.getURI().contains("/photo")) {
                    type = Constants.PHOTO;
                    SharedMemory.<String,SensorsCommunicationUnit>get("SCU").sendReadingRequest(id, type);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MicazMote.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
                } else if (s.getURI().contains("/switch")) {
                    SharedMemory.<String,SensorsCommunicationUnit>get("SCU").sendSwitchToggle(id);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MicazMote.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MicazMote.class.getName()).log(Level.SEVERE, null, ex);
                }

                reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
                

            }
        }

        return reply;
    }
}
