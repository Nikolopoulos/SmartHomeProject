/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import Logging.MyLogger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import oscilloscope.Messaging;
import sensorPlatforms.AssociatedHardware;
import sensorPlatforms.MicazMote;
import sensorPlatforms.Service;

/**
 *
 * @author billaros
 */
public class SensorManager {

    private static Thread dropDaemon, populate;
    private static ArrayList<MicazMote> sensorsList;
    private static Messaging messages;

    public static void reportReadingOfSensor(final MicazMote sensor) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    boolean found = false;
                    MicazMote foundSensor = null;
                    for (MicazMote m : sensorsList) {
                        if (m.getId() == sensor.getId()) {
                            foundSensor = m;
                            m.setLatestActivity(Util.getTime());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        sensorsList.add(sensor);
                        String jsonReply = "";
                        if (UnitConfig.getUid().length() > 0) {
                            try {
                                String services = "{\"services\":[";

                                services += "{\"uri\" : \"/sensor/" + sensor.getId() + "\", \"description\" : \"returns data of specific sensor with id  " + sensor.getId() + "\"}";
                                for (Service s : sensor.getServices()) {
                                    services += ",{\"uri\" : \"/sensor/" + sensor.getId() + s.getURI() + "\", \"description\" : \"" + s.getDescription() + "  " + sensor.getId() + "\"}";
                                }
                                services += "]}";
                                if (UnitConfig.isDebug()) {
                                    MyLogger.log("My uid at update is " + UnitConfig.getUid());
                                }

                                JSONObject obj;
                                obj = ConstructedRequests.UpdateServices(services);
                            } catch (Exception ex) {
                                Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                                //MyLogger.log("Error parsing this" + jsonReply);
                            }
                        }

                    } else {
                        //Change existing services to latest standing
                        for (Service serviceFromFoundSensor : foundSensor.getServices()) {
                            for (Service ser : sensor.getServices()) {
                                if (ser.getName().contentEquals(serviceFromFoundSensor.getName())) {
                                    serviceFromFoundSensor.setDecimalValue(ser.getDecimalValue());
                                    if (ser.getName().contains("Bluetooth")) {
                                        //if (!serviceFromFoundSensor.getHw().containsKey(ser.getDecimalValue().split(" ")[0])) {
                                        serviceFromFoundSensor.getHw().put(ser.getDecimalValue().split(" ")[0], new AssociatedHardware(ser.getDecimalValue().split(" ")[0], ser.getDecimalValue().split(" ")[1]));
                                        //} else {
                                        //serviceFromFoundSensor.getHw().put(ser.getDecimalValue().split(" ")[0], new AssociatedHardware(ser.getDecimalValue().split(" ")[0], ser.getDecimalValue().split(" ")[1]));
                                        //}//add to hashmap the tag read
                                    }
                                }
                            }
                        }

                        //Add missing services
                        for (Service serviceFromReadSensor : sensor.getServices()) {
                            boolean exists = false;
                            for (Service ser : foundSensor.getServices()) {
                                if (ser.getName().contentEquals(serviceFromReadSensor.getName())) {
                                    exists = true;
                                }
                            }
                            if (!exists) {
                                foundSensor.getServices().add(serviceFromReadSensor);
                            }
                        }

                    }
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        );
        try {
            CoreManagement.getLeastBusyCore().addThread(t);
        } catch (Exception ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        t.start();
    }

    public static ArrayList<MicazMote> getSensorsList() {
        return sensorsList;
    }

    public static void setSensorsList(ArrayList<MicazMote> sensorsList) {
        SensorManager.sensorsList = sensorsList;
    }

    public static void init() {
        sensorsList = new ArrayList<MicazMote>();
        try {
            messages = new Messaging();
        } catch (Exception e) {
            MyLogger.log("Messaging init failed: " + e);
        }
        dropDaemon = Daemons.createDropDaemon();
        dropDaemon.start();
        populate = Daemons.constructPollDaemon();
        populate.start();
    }

    public void sendReadingRequest(int id, int type, String ServiceURI) {
        for (MicazMote m : sensorsList) {
            if (m.getId() == id) {
                m.RequestServiceReading(UnitConfig.getUid(), UnitConfig.isDebug(), messages);
            }
        }
    }

    public static Messaging getMessages() {
        return messages;
    }

    public static void reportReading(int id, int messageType, int[] Readings) {
        for (MicazMote m : sensorsList) {
            if (m.getId() == id) {
                switch (messageType) {
                    case lib.Constants.TEMP: {
                        m.setTempReading(Util.median(Readings));
                        System.out.println("tried to set reading to " + Util.median(Readings));
                        break;
                    }
                    case lib.Constants.PHOTO: {
                        m.setPhotoReading(Util.median(Readings));
                        break;
                    }
                    default: {
                        break;
                    }

                }

            }
        }
    }

    public static void reportSwitch(int id, int state) {
        for (MicazMote m : sensorsList) {
            if (m.getId() == id) {
                m.setLatestActivity(Util.getTime());
                m.setSwitchState(state);
                System.out.println("State changed to " + state);
            }
        }
    }

    public static void reportPollAck(MicazMote mote) {
        boolean found = false;
        for (MicazMote m : sensorsList) {
            if (m.getId() == mote.getId()) {
                m.setLatestActivity(Util.getTime());
                found = true;
                break;
            }
        }
        if (!found) {
            sensorsList.add(mote);            
            if (UnitConfig.getUid().length() > 0) {
                try {
                    String services = "{\"services\":[";

                    services += "{\"uri\" : \"/sensor/" + mote.getId() + "\", \"description\" : \"returns data of specific sensor with id  " + mote.getId() + "\"}";

                    for (Service s : mote.getServices()) {
                        services += ",{\"uri\" : \"/sensor/" + mote.getId() + s.getURI() + "\", \"description\" : \"" + s.getDescription() + "\"}";
                    }

                    services += "]}";
                    System.out.println("My uid at update is " + UnitConfig.getUid());
                    //jsonReply = HTTPRequest.sendPost("http://" + registryUnitIP, registryPort, URLEncoder.encode("uid=" + uid + "&services=" + services), "/update", addr);
                    //the above line is changed by a constructed update request, if it fails, look into it further
                  
                    JSONObject obj;
                    obj = new JSONObject(ConstructedRequests.UpdateServices(services));
                    //check for success?
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}
