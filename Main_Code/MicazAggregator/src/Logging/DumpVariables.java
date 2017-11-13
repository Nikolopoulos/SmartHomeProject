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
        init = true;
        logfile = new File(new Date().toGMTString() + ".csv");
        if (logfile.exists()) {
            try {
                pw = new PrintWriter(logfile);
                pw.write("");
                pw.write("Number of requests arrived in interval,"
                        + "Number of requests arrived in total,"
                        + "Number of High criticality requets arrived in interval,"
                        + "Number of High criticality requets arrived in total,"
                        + "Number of active cores in interval,"
                        + "Number of sensors pushing data,"
                        + "Number of sensors we pull data,"
                        + "Overloaded system,"
                        + "Average High Criticality requests service time in interval,"
                        + "Average High Criticality requests service time in total,"
                        + "Average request service time in interval,"
                        + "Average request service time in total");
                pw.close();
                resetIntervalCounters();
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public static void dump() {
        numberOfCoresInInterval = 0;
        if(SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores") == null || SharedMemory.<String, Boolean>get("OverLoadStatus") == null){
            return;
        }
        for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
            if (core.isOverLoadLimit()) {
                numberOfCoresInInterval++;
            }
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(logfile, true));
            bw.write("" + requestsTakenInInterval + ","
                    + requestsTakenInTotal + ","
                    + numberOfHighCriticalityRequestsInInterval + ","
                    + numberOfHighCriticalityRequestsInTotal + ","
                    + numberOfCoresInInterval + ","
                    + numberOfPushingSensors + ","
                    + numberOfPullingSensors + ","
                    + SharedMemory.<String, Boolean>get("OverLoadStatus") + ","
                    + averageHighCriticalityRequestServiceTimeInInterval + ","
                    + averageHighCriticalityRequestServiceTimeInTotal + ","
                    + averageRequestServiceTimeInInterval + ","
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
        numberOfCoresInInterval = 0;
        if (SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores") != null) {
            for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                if (core.isOverLoadLimit()) {
                    numberOfCoresInInterval++;
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
        if (CompletedRequestsTakenInInterval == 0) {
            averageRequestServiceTimeInInterval = millis;
        } else {
            averageRequestServiceTimeInInterval = ((averageRequestServiceTimeInInterval * CompletedRequestsTakenInInterval) + millis) / CompletedRequestsTakenInInterval;
        }
        if (CompletedRequestsTakenInTotal == 0) {
            averageRequestServiceTimeInTotal = millis;
        } else {
            averageRequestServiceTimeInTotal = ((averageRequestServiceTimeInTotal * CompletedRequestsTakenInTotal) + millis) / CompletedRequestsTakenInTotal;
        }
    }

    public static void updateHighCriticalityAverageRequestServiceTime(long millis) {
        CompletedNumberOfHighCriticalityRequestsInInterval++;
        CompletedNumberOfHighCriticalityRequestsInTotal++;
        if (CompletedNumberOfHighCriticalityRequestsInInterval == 0) {
            averageHighCriticalityRequestServiceTimeInInterval = millis;
        } else {
            averageHighCriticalityRequestServiceTimeInInterval = ((averageHighCriticalityRequestServiceTimeInInterval * CompletedNumberOfHighCriticalityRequestsInInterval) + millis) / CompletedNumberOfHighCriticalityRequestsInInterval;
        }
        if (CompletedNumberOfHighCriticalityRequestsInTotal == 0) {
            averageHighCriticalityRequestServiceTimeInTotal = millis;
        } else {
            averageHighCriticalityRequestServiceTimeInTotal = ((averageHighCriticalityRequestServiceTimeInTotal * CompletedNumberOfHighCriticalityRequestsInTotal) + millis) / CompletedNumberOfHighCriticalityRequestsInTotal;
        }
        updateAverageRequestServiceTime(millis);
    }

}
