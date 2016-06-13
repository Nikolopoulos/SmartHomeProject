/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensorPlatforms;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.Constants;
import oscilloscope.Messaging;
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

    private enum voltage {

        LOW, HIGH
    };

    private double tempReading;
    private double photoReading;

    private long latestActivity;
    private int switchState;

    public MicazMote(int id, int services, long latestActivity) {
        this.servicesList = new ArrayList<Service>();
        this.id = id;
        this.services = services;
        if (services / lib.Constants.PIN_S > 0.9) {
            services -= 4;
            this.switchService = true;
            switchState = -1;
            servicesList.add(new Service("switch", "Switch service for accuator", "/switch", "boolean"));
        }
        if (services / lib.Constants.PHOTO_S > 0.9) {
            services -= 2;
            this.photoService = true;
            servicesList.add(new Service("photo", "Light levels service", "/photo", "lum?"));
        }
        if (services % 2 == lib.Constants.TEMP_S) {
            services -= lib.Constants.TEMP_S;
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

    public void setSwitchState(int switchState) {
        for (Service s : servicesList) {
            if (s.getName().equals("temp")) {
                s.setDecimalValue("1");
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
            System.out.println("Setting temp to " + tempReading);
            if (s.getName().equals("temp")) {
                System.out.println("Found service " + tempReading);
                s.setDecimalValue(tempReading + "");
                System.out.println("Set temp to " + s.getDecimalValue());
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
        reply += "\"} ";
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

    public String RequestServiceReading(String ServiceURI, boolean cached, Messaging messages) {
        String reply = "genericError";
        int type = -99;
        for (Service s : servicesList) {
<<<<<<< HEAD
            System.out.println(s.getURI() + " vs " + ServiceURI);
            if (s.getURI().contains(ServiceURI) && cached && s.getLatestReading() - System.currentTimeMillis() < 30000 && s.getDecimalValue()!=null) {
                reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
            } else if (s.getURI().contains(ServiceURI)) {
=======
            System.out.println("s.uri vs serviceuri " + s.getURI() + " vs " + ServiceURI + " is contained? " + s.getURI().contains(ServiceURI));
            if (s.getURI().contains(ServiceURI) && cached && System.currentTimeMillis() -s.getLatestReading()< 30000) {
>>>>>>> 82451c03364367e64fec0d55dd35693b6fa233bc
                if (s.getURI().contains("/temp")) {
                    System.out.println("ELSE INNER s.uri vs serviceuri " + s.getURI() + " vs " + ServiceURI + " is contained? " + s.getURI().contains(ServiceURI));
                    System.out.println("Uri contains/temp " + ServiceURI);
                    type = Constants.TEMP;
                    messages.sendReadingRequest(id, type);
<<<<<<< HEAD
                    System.out.println("temp");
=======
                    reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + Util.a2d2celsius(Integer.parseInt(s.getDecimalValue().substring(0, s.getDecimalValue().length()-2))) + "\" ";
                }
                else
                    reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
                System.out.println("INNER s.uri vs serviceuri " + s.getURI() + " vs " + ServiceURI + " is contained? " + s.getURI().contains(ServiceURI));
            } else if (s.getURI().contains(ServiceURI)) {
                System.out.println("ELSE s.uri vs serviceuri " + s.getURI() + " vs " + ServiceURI + " is contained? " + s.getURI().contains(ServiceURI));
                if (s.getURI().contains("/temp")) {
                    System.out.println("ELSE INNER s.uri vs serviceuri " + s.getURI() + " vs " + ServiceURI + " is contained? " + s.getURI().contains(ServiceURI));
                    System.out.println("Uri contains/temp " + ServiceURI);
                    type = Constants.TEMP;
                    messages.sendReadingRequest(id, type);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MicazMote.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + Util.a2d2celsius(Integer.parseInt(s.getDecimalValue().substring(0, s.getDecimalValue().length()-2))) + "\" ";
>>>>>>> 82451c03364367e64fec0d55dd35693b6fa233bc
                }
                else if (s.getURI().contains("/photo")) {
                    type = Constants.PHOTO;
                    messages.sendReadingRequest(id, type);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MicazMote.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
                }
                else if (s.getURI().contains("/switch")) {
                    messages.sendSwitchToggle(id);
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
<<<<<<< HEAD
                else{
                    System.out.println(s.getURI());
                }

                reply = "\"ID\":\"" + getId() + "\", \"" + s.getName() + "\":\"" + s.getDecimalValue() + "\" ";
=======
                
>>>>>>> 82451c03364367e64fec0d55dd35693b6fa233bc
            }
        }

        return reply;
    }
}
