/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logging;

import ControlUnit.CoreDefinition;
import SensorsCommunicationUnit.MicazMote;
import SharedMemory.SharedMemory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author billaros
 */
public class DumpVariables {

    public static File logfile;
    private static PrintWriter pw;
    private static boolean init = false;

    static int requestsTakenInInterval;
    static int requestsTakenInTotal;
    static int numberOfHighCriticalityRequestsInInterval;
    static int numberOfHighCriticalityRequestsInTotal;

    static int CompletedRequestsTakenInInterval;
    static int CompletedRequestsTakenInTotal;
    static int CompletedNumberOfHighCriticalityRequestsInInterval;
    static int CompletedNumberOfHighCriticalityRequestsInTotal;

    static int numberOfPushingSensors;
    static int numberOfPullingSensors;

    static int numberOfOverloadedCoresInInterval;
    static int numberOfCoresInInterval;
    static boolean overloaded;

    static float averageRequestServiceTimeInInterval;
    static float averageRequestServiceTimeInTotal;
    static float averageHighCriticalityRequestServiceTimeInInterval;
    static float averageHighCriticalityRequestServiceTimeInTotal;

    public static void init() {
        if (init) {
            return;
        }
        System.out.println("Initing text");
        init = true;
        logfile = new File(System.currentTimeMillis() + ".csv");
        try {
            logfile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(DumpVariables.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (logfile.exists()) {
            try {
                System.out.println("1");
                pw = new PrintWriter(logfile);
                System.out.println("2");
                pw.write("");
                System.out.println("3");
                pw.close();
                System.out.println("Initing log");
                BufferedWriter bw = null;
                bw = new BufferedWriter(new FileWriter(logfile, true));
                bw.write("Number of requests arrived in interval;"
                        + "Number of requests completed in interval;"
                        + "Number of requests arrived in total;"
                        + "Number of requests completed in total;"
                        + "Number of High criticality requests arrived in interval;"
                        + "Number of High criticality reuests completed in interval;"
                        + "Number of High criticality requests arrived in total;"
                        + "Number of High criticality reuests completed in total;"
                        + "Number of active cores in interval;"
                        + "Number of overloaded cores in interval;"
                        + "Number of sensors pushing data;"
                        + "Number of sensors we pull data;"
                        + "Overloaded system;"
                        + "Average High Criticality requests service time in interval;"
                        + "Average High Criticality requests service time in total;"
                        + "Average request service time in interval;"
                        + "Average request service time in total");
                bw.newLine();
                bw.close();
                System.out.println("Init happened");
                resetIntervalCounters();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DumpVariables.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public static void dump() {
        numberOfOverloadedCoresInInterval = 0;
        numberOfCoresInInterval = 0;
        
        if (SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores") == null || SharedMemory.<String, Boolean>get("OverLoadStatus") == null) {
            return;
        }
        for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
            if (core.isOverLoadLimit()) {
                numberOfOverloadedCoresInInterval++;
            }
        }
        for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
            if (core.getRunning()) {
                numberOfCoresInInterval++;
            }
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(logfile, true));
            bw.write("" + requestsTakenInInterval + ";"
                    + CompletedRequestsTakenInInterval + ";"
                    + requestsTakenInTotal + ";"
                    + CompletedRequestsTakenInTotal + ";"
                    + numberOfHighCriticalityRequestsInInterval + ";"
                    + CompletedNumberOfHighCriticalityRequestsInInterval + ";"
                    + numberOfHighCriticalityRequestsInTotal + ";"
                    + CompletedNumberOfHighCriticalityRequestsInTotal + ";"
                    + numberOfCoresInInterval + ";"
                    + numberOfOverloadedCoresInInterval + ";"                    
                    + numberOfPushingSensors + ";"
                    + numberOfPullingSensors + ";"
                    + SharedMemory.<String, Boolean>get("OverLoadStatus") + ";"
                    + averageHighCriticalityRequestServiceTimeInInterval + ";"
                    + averageHighCriticalityRequestServiceTimeInTotal + ";"
                    + averageRequestServiceTimeInInterval + ";"
                    + averageRequestServiceTimeInTotal
            );
            bw.newLine();
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(DumpVariables.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(DumpVariables.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        resetIntervalCounters();
    }

    public static void resetIntervalCounters() {

        requestsTakenInInterval = 0;
        numberOfHighCriticalityRequestsInInterval = 0;
        numberOfPullingSensors = 0;
        numberOfPushingSensors = 0;
        if (SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList") != null) {

            for (MicazMote mote : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
                if (mote.isPush()) {
                    numberOfPushingSensors++;
                } else {
                    numberOfPullingSensors++;
                }
            }
        }
        numberOfOverloadedCoresInInterval = 0;
        if (SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores") != null) {
            for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                if (core.isOverLoadLimit()) {
                    numberOfOverloadedCoresInInterval++;
                }
            }
        }
        overloaded = SharedMemory.<String, Boolean>get("OverLoadStatus");
        averageHighCriticalityRequestServiceTimeInInterval = 0;
        averageRequestServiceTimeInInterval = 0;
        CompletedNumberOfHighCriticalityRequestsInInterval = 0;
        CompletedRequestsTakenInInterval = 0;
    }

    public static void newRequest() {
        requestsTakenInInterval++;
        requestsTakenInTotal++;
    }

    public static void newHighCriticalityRequest() {
        numberOfHighCriticalityRequestsInInterval++;
        numberOfHighCriticalityRequestsInTotal++;
        newRequest();
    }

    public static void updateAverageRequestServiceTime(long millis) {
        CompletedRequestsTakenInInterval++;
        CompletedRequestsTakenInTotal++;
        //System.out.println("millis is " + millis);
        if (CompletedRequestsTakenInInterval == 0) {
            averageRequestServiceTimeInInterval = millis;
        } else {
            averageRequestServiceTimeInInterval = ((averageRequestServiceTimeInInterval * (CompletedRequestsTakenInInterval - 1)) + millis) / CompletedRequestsTakenInInterval;
        }
        if (CompletedRequestsTakenInTotal == 0) {
            averageRequestServiceTimeInTotal = millis;
        } else {
            averageRequestServiceTimeInTotal = ((averageRequestServiceTimeInTotal * (CompletedRequestsTakenInTotal - 1) + millis)) / CompletedRequestsTakenInTotal;
        }
    }

    public static void updateHighCriticalityAverageRequestServiceTime(long millis) {
        CompletedNumberOfHighCriticalityRequestsInInterval++;
        CompletedNumberOfHighCriticalityRequestsInTotal++;
        //System.out.println("millis is " + millis);
        if (CompletedNumberOfHighCriticalityRequestsInInterval == 0) {
            averageHighCriticalityRequestServiceTimeInInterval = millis;
        } else {
            averageHighCriticalityRequestServiceTimeInInterval = ((averageHighCriticalityRequestServiceTimeInInterval * (CompletedNumberOfHighCriticalityRequestsInInterval - 1)) + millis) / CompletedNumberOfHighCriticalityRequestsInInterval;
        }
        if (CompletedNumberOfHighCriticalityRequestsInTotal == 0) {
            averageHighCriticalityRequestServiceTimeInTotal = millis;
        } else {
            averageHighCriticalityRequestServiceTimeInTotal = ((averageHighCriticalityRequestServiceTimeInTotal * (CompletedNumberOfHighCriticalityRequestsInTotal - 1)) + millis) / CompletedNumberOfHighCriticalityRequestsInTotal;
        }
        updateAverageRequestServiceTime(millis);
    }

}
