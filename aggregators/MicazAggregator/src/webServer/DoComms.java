/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webServer;

import DecisionMaking.DecisionMaking;
import Logging.MyLogger;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import sensorPlatforms.MicazMote;
import util.Control;
import static java.lang.Thread.sleep;

/**
 *
 * @author billaros
 */
class DoComms implements Runnable {

    private Socket server;
    private String line, input, requestedURL, noBreakInput;
    private final Control con;
    private DecisionMaking dm;

    DoComms(Socket server, Control c) {
        this.server = server;
        this.con = c;
        this.dm = new DecisionMaking(c);
    }

    //So server listens for clients, and this class is runnable and executes the communication requests.
    public void run() {

        try {
            con.HTTPCore.attachTo();
        } catch (Exception ex) {
            Logger.getLogger(DoComms.class.getName()).log(Level.SEVERE, null, ex);
        }
        input = "";
        noBreakInput = "";
        requestedURL = "";
        try {
            // Get input from the client
            DataInputStream in = new DataInputStream(server.getInputStream());
            PrintStream out = new PrintStream(server.getOutputStream());
            int lineNumber = 1;
            while ((line = in.readLine()) != null && line.length() > 0) {
                if (lineNumber == 1) {
                    String[] parts = line.split(" ");
                    requestedURL = parts[1];
                }
                input = input + line + "\n";
                noBreakInput = noBreakInput + line;
                lineNumber++;
            }
            String reply = "";
            if (requestedURL.equals("/")) {
                reply = con.ip + ":" + con.myPort + "/sensors -> returns a list of sensors available\n"
                        + con.ip + ":" + con.myPort + "/sensor/ID -> returns data of specific sensor with id = ID\n"
                        + con.ip + ":" + con.myPort + "/sensor/ID/ServiceName -> returns data from ServiceName runnign on Sensor ID\n";

            } else if (requestedURL.startsWith("/sensors")) {
                reply = "{\"sensors\":{[";
                for (MicazMote m : con.getMotesList()) {
                    reply += m.JSONDescription() + ",";
                }
                if (reply.length() > 10) {
                    reply = reply.substring(0, reply.length() - 1);
                }
                reply += "]}";
            } else if (requestedURL.startsWith("/log")) {
                reply = MyLogger.readLog();

            } else if (requestedURL.startsWith("/sensor/") && ((requestedURL.contains("photo")) || (requestedURL.contains("temp")) || (requestedURL.contains("switch")))) {
                if (requestedURL.length() < 9) {
                    reply = con.ip + ":" + con.myPort + "/sensors -> returns a list of sensors available\n"
                            + con.ip + ":" + con.myPort + "/sensor/ID -> returns data of specific sensor with id = ID\n"
                            + con.ip + ":" + con.myPort + "/sensor/ID/ServiceName -> returns data from ServiceName runnign on Sensor ID\n";

                } else if (requestedURL.startsWith("/sensor/")) {

                    reply = "{\"sensor\":{";
                    int threadID = dm.add(requestedURL);
                    String returnVal = "";
                    while (returnVal.equalsIgnoreCase("")) {
                        returnVal = dm.getResultOf(threadID);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(DoComms.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    reply += returnVal + "}}";

                }
            }
            out.println(reply);
            out.flush();
            out.close();
            server.close();
        } catch (IOException ioe) {
            System.out.println("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }
}
