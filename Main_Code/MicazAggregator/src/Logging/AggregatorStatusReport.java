/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logging;

import ControlUnit.CoreDefinition;
import ControlUnit.PendingRequest;
import SharedMemory.SharedMemory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author billaros
 */
public class AggregatorStatusReport {

    public static File logfile;
    private static PrintWriter pw;

    public static void init() {
        logfile = new File("status.html");
        if (logfile.exists()) {
            try {
                pw = new PrintWriter(logfile);
                pw.write("");
                pw.close();
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        updateDaemon();

    }

    public static void updateDaemon() {
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    log();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AggregatorStatusReport.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        daemon.start();
    }

    public static void log() {
        PrintWriter pw;
        try {
            pw = new PrintWriter(logfile);
            pw.write("");
            pw.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AggregatorStatusReport.class.getName()).log(Level.SEVERE, null, ex);
        }

        String toWrite
                = "<html>"
                + "<head>"
                + "<title>Aggregator status</title>"
                + "<script type=\"text/javascript\">"
                + "var frequency = 5000;"
                + "function reloadPage(){location.reload();}"
                + "setInterval(\"reloadPage()\",frequency);"
                + "</script>"
                + "</head>"
                + "<body>";
        toWrite += "<div>";
        toWrite += "<table style=\"border:solid 2px #888;\">";
        toWrite += "<tr>";
        toWrite += "<th>";
        toWrite += "CPU id";
        toWrite += "</th>";
        toWrite += "<th>";
        toWrite += "Core Load";
        toWrite += "</th>";
        toWrite += "<th>";
        toWrite += "Running";
        toWrite += "</th>";
        toWrite += "<th>";
        toWrite += "Public Resource";
        toWrite += "</th>";
        toWrite += "<th>";
        toWrite += "Status";
        toWrite += "</th>";
        toWrite += "</tr>";
        for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {

            toWrite += "<tr>";
            toWrite += "<td>";
            toWrite += core.getId() + "";
            toWrite += "</td>";
            toWrite += "<td>";
            toWrite += ((core.getLoad() / util.Statics.maxThreads) * 100) + "%";
            toWrite += "</td>";
            toWrite += "<td>";
            toWrite += core.getRunning() ? "Running" : "Idle";
            toWrite += "</td>";
            toWrite += "<td>";
            toWrite += core.isPublicResource() ? "Public" : "Reserved";
            toWrite += "</td>";
            toWrite += "<td>";
            toWrite += core.isNormalLoad() ? "Normal" : "";
            toWrite += core.isUnderUtilized() ? "Low" : "";
            toWrite += core.isOverLoadLimit() ? "High" : "";
            toWrite += "</td>";
            toWrite += "</tr>";

        }
        toWrite += "</table>";
        toWrite += "</div>";

        toWrite += "<div>";
        toWrite += "<table style=\"border:solid 2px #888;\">";
        toWrite += "<tr>";
        toWrite += "<th>";
        toWrite += "Request Id";
        toWrite += "</th>";
        toWrite += "<th>";
        toWrite += "Criticality";
        toWrite += "</th>";
        toWrite += "<th>";
        toWrite += "IP";
        toWrite += "</th>";
        toWrite += "<th>";
        toWrite += "Complete";
        toWrite += "</th>";
        toWrite += "</tr>";

        for (PendingRequest request : SharedMemory.<String, ArrayList<PendingRequest>>get("RequestBucket")) {
            toWrite += "<tr>";
            toWrite += "<td>";
            toWrite += request.getId() + "";
            toWrite += "</td>";
            toWrite += "<td>";
            toWrite += request.getRequest().getCriticality();
            toWrite += "</td>";
            toWrite += "<td>";
            toWrite += request.getRequest().getServer().getInetAddress();
            toWrite += "</td>";
            toWrite += "<td>";
            toWrite += request.isComplete()?"Complete":"Pending";
            toWrite += "</td>";
            toWrite += "</tr>";

        }
        toWrite += "</table>";
        toWrite += "</div>";

        toWrite += "</body>";
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logfile, true));
            bw.write(toWrite);
            bw.newLine();
            bw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String readLog() {
        String toReturn = "<html><body>";
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(logfile)));
            String temp = "";
            toReturn += "<table>";
            while ((temp = bf.readLine()) != null) {
                toReturn += "<tr>";
                toReturn += "<td>" + temp;
                toReturn += "<tr>";
            }
            toReturn += "</table>";
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        toReturn += "</body></html>";
        return toReturn;
    }
}
