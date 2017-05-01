/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServiceProvisionUnit;

import DecisionMaking.DecisionMaking;
import Logging.MyLogger;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import SensorsCommunicationUnit.MicazMote;
import ControlUnit.Control;
import Libraries.Core;
import SharedMemory.SharedMemory;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author billaros
 */
public class DoComms implements Runnable {

    private Socket server;
    private String line, input, requestedURL, noBreakInput;
    private final Control con;
    private Core core;
    private String reply;
    private Semaphore sema;
    private ResponseObject response;

    DoComms(Socket server, Control c, Core core) {
        this.server = server;
        this.con = c;
        this.core = core;
        this.sema = new Semaphore(0, true);
        response = new ResponseObject(this.server);
    }

    //So server listens for clients, and this class is runnable and executes the communication requests.
    public void run() {

        try {
            core.attachTo();
        } catch (Exception ex) {
            Logger.getLogger(DoComms.class.getName()).log(Level.SEVERE, null, ex);
        }
        input = "";
        noBreakInput = "";
        requestedURL = "";

        Thread replyThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    replyToClient();
                } catch (IOException ex) {
                    Logger.getLogger(DoComms.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        replyThread.start();
        try {
            //this is to be migrated to control probably
            // Get input from the client
            String localinput = getInput();

            int requestType = getRequestType(localinput);
            System.out.println("checking request");
            switch (requestType) {
                case 0: {
                    getGenericResponse();
                    break;
                }
                case 1: {
                    getSensorsList();
                    break;
                }
                case 2: {
                    MyLogger.readLog();
                    break;
                }
                case 3: {
                    getGenericResponse();
                    break;
                }
                case 4: {
                    getSpecificSensor(localinput);
                    break;
                }
                case -1: {
                    getGenericResponse();
                    break;
                }
            }

        } catch (IOException ioe) {
            System.out.println("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }

    private String getInput() throws IOException {

        DataInputStream in = new DataInputStream(server.getInputStream());
        String url = "";
        int lineNumber = 1;
        while ((line = in.readLine()) != null && line.length() > 0) {
            if (lineNumber == 1) {
                String[] parts = line.split(" ");
                requestedURL = parts[1];
                url = parts[1];
                for (int i = 0; i < parts.length; i++) {
                    System.out.println("****************REQUESTED URL" + i + " IS " + parts[i]);
                }
            }
            input = input + line + "\n";
            noBreakInput = noBreakInput + line;
            lineNumber++;
        }
        return url;
    }

    private int getRequestType(String Url) {
        if (Url.equals("/")) {
            System.out.println("is pure /");
            return 0;

        } else if (Url.startsWith("/sensors")) {
            return 1;
        } else if (Url.startsWith("/log")) {
            return 2;
        } else if (Url.startsWith("/sensor/") /*&& !((Url.contains("photo")) || (Url.contains("temp")) || (Url.contains("switch")))*/) {
            System.out.println("starts with sensor but doesnt have required service name");
            if (Url.length() < 9) {
                return 3;
            } else if (Url.startsWith("/sensor/")) {
                return 4;
            }
        }
        return -1;
    }

    private void getGenericResponse() {

        reply = con.ip + ":" + SharedMemory.<String, Integer>get("myPort") + "/sensors -> returns a list of sensors available\n"
                + con.ip + ":" + SharedMemory.<String, Integer>get("myPort") + "/sensor/ID -> returns data of specific sensor with id = ID\n"
                + con.ip + ":" + SharedMemory.<String, Integer>get("myPort") + "/sensor/ID/ServiceName -> returns data from ServiceName running on Sensor ID\n";
        sema.release();
    }

    private void getSensorsList() {
        System.out.println("GetSensorsList");
        if (SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList").isEmpty()) {
            reply = "{\"sensors\":{}}";
            System.out.println("1. set reply to "+reply);
            sema.release();
        } else {
            reply = "{\"sensors\":{[";
            for (MicazMote m : SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList")) {
                reply += m.JSONDescription() + ",";
            }
            reply = reply.substring(0, reply.length()-1);
            reply+="]}}";
            System.out.println("2. set reply to "+reply);
            sema.release();
        }
        System.out.println("3. set reply to "+reply);
    }

    private void getSpecificSensor(String url) {
        System.out.println("starts with sensor and has both length and name");
        reply = "";
        System.out.println("adding to dm");
        int threadID = con.add(url,this);
        System.out.println("added to bucket");
        String returnVal = "";

        while (reply.length() == "".length()) {
            System.out.println("return val not acceptable");
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(DoComms.class.getName()).log(Level.SEVERE, null, ex);
            }           
        }
        reply = "{\"sensor\":{"+reply;
        System.out.println("Returnval = " + reply);
        reply += returnVal + "}}";
        sema.release();
    }

    private void replyToClient() throws IOException {
        try {
            sema.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(DoComms.class.getName()).log(Level.SEVERE, null, ex);
        }
        PrintStream out = new PrintStream(server.getOutputStream());
        out.println(reply);
        out.flush();
        out.close();
        server.close();
        sema.release();
    }

    public void setResponse(String response) {
        System.out.println("SetResponse from 1");
        this.response.setReply(response);
        this.reply = response;
        //this.sema.release();
    }

    public void setResponse(ResponseObject response) {
        System.out.println("SetResponse from 2");
        this.response = response;
        this.sema.release();
    }

}
