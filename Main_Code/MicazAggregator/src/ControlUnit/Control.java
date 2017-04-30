/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ControlUnit;

import DecisionMaking.DecisionMaking;
import Logging.MyLogger;
import Simulator.MoteLibSimulator;
import Simulator.Network;
import Simulator.SimulatedMessaging;
import Libraries.Core;
import Libraries.ThreadAffinity;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.json.JSONObject;
import SensorsCommunicationUnit.AssociatedHardware;
import SensorsCommunicationUnit.MicazMote;
import SensorsCommunicationUnit.Service;
import ServiceProvisionUnit.DoComms;
import ServiceProvisionUnit.RequestObject;
import util.Util;
import ServiceProvisionUnit.ServiceProvisionUnit;
import SharedMemory.SharedMemory;

/**
 *
 * @author billaros
 */
public class Control {

    private DecisionMaking dm;
    private final SharedMemory memory;
    public Network net;
    public MoteLibSimulator mlbs;

    private boolean simulation = true;
    public SimulatedMessaging messages;
    Thread dropDaemon, populate;
    String uid = "";
    boolean debug;
    public ThreadAffinity threadAffinity;

    /*public final Core criticalSensingCore;
     public final Core HTTPCore;
     public final Core sensingCore;
     public final Core cronCore;*/
    public InetAddress addr = null;// = getFirstNonLoopbackAddress(true, false);
    public String ip;// = addr.getHostAddress();
    ServiceProvisionUnit spu;
    int threadId = 0;

    public Control(boolean debug) {
        memory = SharedMemory.<String, SharedMemory>get("SMU");
        memory.<String, Control>set("MCU", this);
        dm = new DecisionMaking();
        memory.<String, DecisionMaking>set("DMU", dm);
        SensorsCommunicationUnit.SensorsCommunicationUnit SCU = new SensorsCommunicationUnit.SensorsCommunicationUnit();
        memory.<String, SensorsCommunicationUnit.SensorsCommunicationUnit>set("SCU", SCU);
        memory.<String, Integer>set("CriticalityLevels", util.Statics.CriticallityLevels);
        memory.<String, String>set("ServingAlgorithm", "CAFIFO");

        for (int i = 1; i < memory.<String, Integer>get("CriticalityLevels") + 1; i++) {
            memory.<String, ArrayList>set("ThreadBucket" + i, new ArrayList<RequestExecutionThread>());
        }
        memory.<String, ArrayList>set("RequestBucket", new ArrayList<PendingRequest>());
        try {
            addr = getFirstNonLoopbackAddress(true, false);
            memory.<String, InetAddress>set("addr", addr);
        } catch (SocketException ex) {
            System.exit(1);
        }
        try {
            ip = addr.getHostAddress();

        } catch (Exception e) {
            ip = "127.0.0.1";
        }
        memory.<String, String>set("ip", ip);
        System.out.println("Percieved ip is " + ip + " first non loopbak is " + addr);
        memory.<String, String>set("registryUnitIP", "192.168.2.5");
        memory.<String, Integer>set("registryPort", 8383);
        memory.<String, Integer>set("myPort", 8181);
        spu = new ServiceProvisionUnit(this);
        memory.<String, ServiceProvisionUnit>set("SPU", spu);
        spu.startServer();
        System.out.println("Set ports");
        threadAffinity = new ThreadAffinity(this);
        memory.<String, Integer>set("AvailableCores", threadAffinity.cores().length);
        memory.<String, ThreadAffinity>set("Affinity", threadAffinity);

        this.debug = debug;
        String jsonReply = "";
        System.out.println("setAffinity");

        memory.<String, ArrayList<CoreDefinition>>set("Cores", new ArrayList<CoreDefinition>());
        for (int i = 0; i < threadAffinity.cores().length; i++) {
            boolean pub, run;
            if (i == 0) {
                pub = false;
                run = true;
            } else {
                pub = true;
                run = false;
            }
            memory.<String, ArrayList<CoreDefinition>>get("Cores").add(new CoreDefinition(threadAffinity.cores()[i], run, 0, i, pub));
        }
        
        System.out.println("reached");
        try {
            //if(simulation == true){
            mlbs = new MoteLibSimulator();
            messages = new SimulatedMessaging(mlbs);
            net = new Network(messages);
            mlbs.setNet(net);
            //}
            System.out.println("Tryint to https reg unit");
            jsonReply = "{result : \"success\", uid : \"1\"}";
            //jsonReply = HTTPRequest.sendPost("http://" + registryUnitIP, registryPort, URLEncoder.encode("ip=" + ip + "&port=" + myPort + "&services={\"services\":[{\"uri\" : \"/sensors\", \"description\" : \"returns a list of sensors available\"}]}"), "/register", addr);
            System.out.println("Done https reg unit");
            //registers itself to the registry unit
            //MyLogger.log("http://" + registryUnitIP + ":" + registryPort + "/register" + URLEncoder.encode("ip=" + ip + "&port=" + myPort + "&services={\"services\":[{\"uri\" : \"/sensors\", \"description\" : \"returns a list of sensors available\"}]}"));
            if (debug) {
                MyLogger.log("reply is: " + jsonReply);
            }
            JSONObject obj;
            //MyLogger.log("Error parsing this" + jsonReply);
            obj = new JSONObject(jsonReply);

            if (!obj.get("result").equals("success")) {
                if (debug) {
                    MyLogger.log("jsonReply failed " + jsonReply);
                }
            } else {
                uid = obj.getString("uid");
                if (debug) {
                    MyLogger.log("myUID is " + uid);
                }
            }

        } catch (Exception e) {

//            MyLogger.log(jsonReply);
            e.printStackTrace();
        }

        SharedMemory.<String, ArrayList<MicazMote>>set("SensorsList", new ArrayList<MicazMote>());
        dropDaemon = createDropDaemon();
        dropDaemon.start();
        populate = createPollDaemon();
        populate.start();
        requestServingDaemon();
    }

    public DecisionMaking getDm() {
        return dm;
    }

    private synchronized Thread createDropDaemon() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    findCoreById(0).getCore().attachTo();
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
                ArrayList<MicazMote> toRemove = new ArrayList<MicazMote>();
                if (debug) {
                    MyLogger.log("Drop daemon started");
                }
                while (true) {
                    for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {

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
                                MyLogger.log("My uid at update is " + uid);
                            }
                            RequestObject regReq = new RequestObject("http://" + SharedMemory.<String, String>get("registryUnitIP"), SharedMemory.<String, Integer>get("registryPort"), URLEncoder.encode("uid=" + uid + "&services=" + services), "/delete", addr, "POST");
                            regReq = spu.httpContact(regReq);
                            String jsonReply = regReq.getResponse();
                            if (debug) {
                                MyLogger.log("reply is: " + jsonReply);
                            }
                            JSONObject obj;

                            obj = new JSONObject(jsonReply);
                        } catch (Exception ex) {
                            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList").remove(m);
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
        return t;
    }

    public void reportReadingOfSensor(final MicazMote sensor) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    findCoreById(0).getCore().attachTo();

                    boolean found = false;
                    MicazMote foundSensor = null;
                    for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
                        if (m.getId() == sensor.getId()) {
                            foundSensor = m;
                            m.setLatestActivity(Util.getTime());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList").add(sensor);
                        String jsonReply = "";
                        if (uid.length() > 0) {
                            try {
                                String services = "{\"services\":[";

                                services += "{\"uri\" : \"/sensor/" + sensor.getId() + "\", \"description\" : \"returns data of specific sensor with id  " + sensor.getId() + "\"}";
                                for (Service s : sensor.getServices()) {
                                    services += ",{\"uri\" : \"/sensor/" + sensor.getId() + s.getURI() + "\", \"description\" : \"" + s.getDescription() + "  " + sensor.getId() + "\"}";
                                }
                                services += "]}";
                                if (debug) {
                                    MyLogger.log("My uid at update is " + uid);
                                }
                                RequestObject regReq = new RequestObject("http://" + SharedMemory.<String, String>get("registryUnitIP"), SharedMemory.<String, Integer>get("registryPort"), URLEncoder.encode("uid=" + uid + "&services=" + services), "/update", addr, "POST");
                                regReq = spu.httpContact(regReq);
                                jsonReply = regReq.getResponse();
                                if (debug) {
                                    MyLogger.log("reply is: " + jsonReply);
                                }
                                JSONObject obj;

                                //MyLogger.log("Error parsing this" + jsonReply);
                                obj = new JSONObject(jsonReply);
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
        t.start();
    }

    private Thread createPollDaemon() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    findCoreById(0).getCore().attachTo();

                } catch (Exception ex) {
                    Logger.getLogger(Control.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
                Thread serial = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            //System.out.println("polling");
                            messages.sendPoll();
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

    //courtesy of How to get the ip of the computer on linux through Java? -> http://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
    private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }

    public void sendPoll() {
        messages.sendPoll();
    }

    public void getSwitchInfo(int id) {
        messages.sendSwitchPoll(id);
    }

    public void toggleSwitch(int id) {
        messages.sendSwitchToggle(id);
    }

    public void sendReadingRequest(int id, boolean cached, String ServiceURI) {
        for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == id) {
                //messages.sendReadingRequest(id, type);
                m.RequestServiceReading(uid, cached);
            }
        }
    }

    public void reportReading(int id, int messageType, int[] Readings) {
        for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == id) {
                switch (messageType) {
                    case SensorsCommunicationUnit.Constants.TEMP: {
                        m.setTempReading(Util.median(Readings));
                        System.out.println("tried to set reading to " + Util.median(Readings));
                        break;
                    }
                    case SensorsCommunicationUnit.Constants.PHOTO: {
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

    public void reportSwitch(int id, int state) {
        for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == id) {
                m.setLatestActivity(Util.getTime());
                m.setSwitchState(state);
                System.out.println("State changed to " + state);
            }
        }
    }

    public void UpdateRecordInSHM(MicazMote mote) {
        boolean found = false;
        for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == mote.getId()) {
                m.setLatestActivity(Util.getTime());
                found = true;
                break;
            }
        }
        if (!found) {
            SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList").add(mote);
            String jsonReply;
            if (uid.length() > 0) {
                try {
                    String services = "{\"services\":[";

                    services += "{\"uri\" : \"/sensor/" + mote.getId() + "\", \"description\" : \"returns data of specific sensor with id  " + mote.getId() + "\"}";

                    for (Service s : mote.getServices()) {
                        services += ",{\"uri\" : \"/sensor/" + mote.getId() + s.getURI() + "\", \"description\" : \"" + s.getDescription() + "\"}";
                    }

                    services += "]}";
                    System.out.println("My uid at update is " + uid);

                    RequestObject regReq = new RequestObject("http://" + SharedMemory.<String, String>get("registryUnitIP"), SharedMemory.<String, Integer>get("registryPort"), URLEncoder.encode("uid=" + uid + "&services=" + services), "/update", addr, "POST");
                    regReq = spu.httpContact(regReq);
                    jsonReply = regReq.getResponse();

                    System.out.println("reply is: " + jsonReply);
                    JSONObject obj;

                    obj = new JSONObject(jsonReply);
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    public int add(String url, DoComms request) {
        System.out.println("ADD URL IS " + url);
        threadId = (threadId + 1) % 10000;

        addRequestToBucket(request, threadId);
        System.out.println("DM HERE ID IS" + threadId);
        int criticality = getCriticalityLevelOfRequest(url);
        RequestExecutionThread thread = new RequestExecutionThread(threadId, criticality, url);
        System.out.println("CREATED THREAD");
        memory.<String, ArrayList<RequestExecutionThread>>get("ThreadBucket" + criticality).add(thread);

        System.out.println("ADDED THREAD");
        return threadId;
    }

    private void addRequestToBucket(DoComms request, int id) {
        memory.<String, ArrayList<PendingRequest>>get("RequestBucket").add(new PendingRequest(request, id));
    }

    private PendingRequest findRequestFromBucket(int id) {
        for (PendingRequest p : memory.<String, ArrayList<PendingRequest>>get("RequestBucket")) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public void setResponseOfRequest(int id, String reply) {
        findRequestFromBucket(id).setReply(reply);
    }

    private int getCriticalityLevelOfRequest(String url) {
        int criticality = 1;
        String[] parametersSplit = url.split("\\?");
        if (parametersSplit.length < 1) {
            criticality = 1;
            System.out.println("parametersSplit <2 = " + parametersSplit.length);
            for (String s : parametersSplit) {
                //MyLogger.log(s);
            }
        } else {
            for (String s : parametersSplit) {
                System.out.println("parametersSplit !<2 = " + s);
            }

            String[] parameters = parametersSplit[1].split("&");

            for (String parameter : parameters) {
                if (parameter.startsWith("crit")) {
                    String value = parameter.split("=")[1];
                    criticality = Integer.parseInt(value);
                    break;
                }
            }
        }
        return criticality;
    }

    public static double getProcessCpuLoad() throws Exception {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

        if (list.isEmpty()) {
            return Double.NaN;
        }

        Attribute att = (Attribute) list.get(0);
        Double value = (Double) att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0) {
            return Double.NaN;
        }
        // returns a percentage value with 1 decimal point precision
        return ((int) (value * 1000) / 10.0);
    }

    public CoreDefinition findCoreById(int id) {

        for (int i = 0; i < memory.<String, ArrayList<CoreDefinition>>get("Cores").size(); i++) {
            if (memory.<String, ArrayList<CoreDefinition>>get("Cores").get(i).getId() == id) {
                memory.<String, ArrayList<CoreDefinition>>get("Cores").get(i);
            }
        }
        return null;
    }

    public void requestServingDaemon() {
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    CoreDefinition core = getServingCore();
                    if(core == null){                        
                        //raise dmu exception
                        continue;
                    }
                    RequestExecutionThread req = getNextRequestToServe();
                    if(req == null){
                        continue;
                    }                    
                    //the attachment happens during the running of the thread so
                    //this statement is now redundant core.attachTo(req);
                    req.run();
                }
            }
        });
        daemon.start();
    }

    private RequestExecutionThread getNextRequestToServe() {
        switch (memory.<String, String>get("ServingAlgorithm")) {
            case "CAFIFO": {
                for (int i = memory.<String, Integer>get("AvailableCores"); i > 0; i--) {
                    if (memory.<String, ArrayList>get("ThreadBucket" + i).size() > 0) {
                        return memory.<String, ArrayList<RequestExecutionThread>>get("ThreadBucket" + i).remove(0);
                    }
                }
                break;
            }
        }
        return null;
    }

    private CoreDefinition getServingCore() {
        CoreDefinition currentCandidate = null;
        for (CoreDefinition core : memory.<String, ArrayList<CoreDefinition>>get("Cores")) {
            if (core.publicResource
                    && core.running
                    && core.getLoad() < util.Statics.maxThreads) {
                if (currentCandidate == null) {
                    currentCandidate = core;
                } else if (currentCandidate.getLoad() > core.getLoad()) {
                    currentCandidate = core;
                }
            }
        }
        return currentCandidate;
    }
}
