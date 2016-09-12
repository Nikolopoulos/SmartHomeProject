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
import sensorPlatforms.MicazMote;
import sensorPlatforms.Service;

/**
 *
 * @author billaros
 */
public class Daemons {

    private static boolean debug = UnitConfig.isDebug();

    public static synchronized Thread createDropDaemon() {
        final Thread t;
        t = new Thread(new Runnable() {

            @Override
            public void run() {

                ArrayList<MicazMote> toRemove = new ArrayList<MicazMote>();
                if (debug) {
                    MyLogger.log("Drop daemon started");
                }
                while (true) {
                    for (MicazMote m : SensorManager.getSensorsList()) {

                        if (m.getLatestActivity() < Util.getTime() - 10000) {
                            if (debug) {
                                MyLogger.log("dropin " + m);
                            }

                            toRemove.add(m);
                        } else if (debug) {
                            //MyLogger.log("Latest activity " + m.getLatestActivity());
                        }
                    }
                    for (MicazMote m : toRemove) {
                        try {
                            String services = "{\"services\":[";

                            services += "{\"uri\" : \"/sensor/" + m.getId() + "\", \"description\" : \"returns data of specific sensor with id  " + m.getId() + "\"}";
                            for (Service s : m.getServices()) {
                                services += ",{\"uri\" : \"/sensor/" + m.getId() + s.getURI() + "\", \"description\" : \"" + s.getDescription() + "  " + m.getId() + "\"}";
                            }
                            services += "]}";
                            if (debug) {
                                MyLogger.log("My uid at update is " + UnitConfig.getUid());
                            }
                            JSONObject obj;

                            obj = new JSONObject(ConstructedRequests.DeleteServices(services));
                        } catch (Exception ex) {
                            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        SensorManager.getSensorsList().remove(m);
                        //sendDeleteRequestToRU
                    }
                    toRemove.clear();
                    if (debug) {
                        //MyLogger.log("CurrentTime " + Util.getTime());
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        try {
            CoreManagement.getLeastBusyCore().addThread(t);
        } catch (Exception ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
        return t;
    }

    public static synchronized Thread constructPollDaemon() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                Thread serial = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            //System.out.println("polling");
                            SensorManager.getMessages().sendPoll();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                });
                serial.start();
            }
        });

        return t;
    }

}
