/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ControlUnit;

import DecisionMakingUnit.DecisionMakingUnit;
import Logging.MyLogger;
import Libraries.ThreadAffinity;
import Logging.DumpVariables;
import MonitoringUnit.MonitoredVariable;
import MonitoringUnit.MonitoringUnit;
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
import java.util.ConcurrentModificationException;
import util.AdvertismentConsumer;
import util.CustomException;

/**
 *
 * @author billaros
 */
public class Control {

    private DecisionMakingUnit dm;
    private final SharedMemory memory;

    Thread dropDaemon, populate;
    String uid = "";
    boolean debug;
    public ThreadAffinity threadAffinity;

    public InetAddress addr = null;// = getFirstNonLoopbackAddress(true, false);
    public String ip;// = addr.getHostAddress();
    ServiceProvisionUnit spu;
    int threadId = 0;

    public Control(boolean debug) {

        DumpVariables.init();
        //Prime memory unit
        memory = SharedMemory.<String, SharedMemory>get("SMU");
 
        System.out.println(memory.<String, String>get("registryUnitIP"));
        memory.<String, Control>set("MCU", this);
        SharedMemory.<String, Boolean>set("OverLoadStatus", false);
        dm = new DecisionMakingUnit();
        memory.<String, DecisionMakingUnit>set("DMU", dm);
        threadAffinity = new ThreadAffinity(this);
        memory.<String, Integer>set("AvailableCores", threadAffinity.cores().length);
        memory.<String, ThreadAffinity>set("Affinity", threadAffinity);

        memory.<String, ArrayList<CoreDefinition>>set("Cores", new ArrayList<CoreDefinition>());
        for (int i = 0; i < threadAffinity.cores().length; i++) {
            boolean pub, run;
            if (i == 0) {
                pub = false;
                //pub = true; //remove this line for raspi
                run = true;
            } else if (i == 1) {
                pub = true;
                run = true;
            } else {
                pub = true;
                run = false;
            }
            CoreDefinition core = new CoreDefinition(threadAffinity.cores()[i], run, 0, i, pub);
            memory.<String, ArrayList<CoreDefinition>>get("Cores").add(core);
            System.out.println("added " + core);
        }
        //System.out.println("WTF");
        System.out.println("Cores available = " + threadAffinity.cores().length);
        System.out.println("Cores in arrayList = " + memory.<String, ArrayList<CoreDefinition>>get("Cores").size());

        //System.out.println("WTF");
        SensorsCommunicationUnit.SensorsCommunicationUnit SCU = new SensorsCommunicationUnit.SensorsCommunicationUnit();
        memory.<String, SensorsCommunicationUnit.SensorsCommunicationUnit>set("SCU", SCU);
        memory.<String, Integer>set("CriticalityLevels", util.Statics.CriticallityLevels);
        memory.<String, String>set("ServingAlgorithm", "CAFIFO");
        //System.out.println("STEP 1");
        MonitoringUnit mon = new MonitoringUnit();
        memory.<String, MonitoringUnit>set("MON", mon);
        //System.out.println("STEP 1.0.1");
        for (int i = 1; i < memory.<String, Integer>get("CriticalityLevels") + 1; i++) {
            memory.<String, ArrayList>set("ThreadBucket" + i, new ArrayList<RequestExecutionThread>());
        }
        //System.out.println("STEP 1.0.2");
        memory.<String, ArrayList<PendingRequest>>set("RequestBucket", new ArrayList<PendingRequest>());
        try {
            //System.out.println("STEP 1.0.3");
            addr = getFirstNonLoopbackAddress(true, false);
            memory.<String, InetAddress>set("addr", addr);
        } catch (SocketException ex) {
            System.exit(1);
            //System.out.println("STEP 1.0.4");
        }
        try {
            ip = addr.getHostAddress();
            //System.out.println("STEP 1.0.5");

        } catch (Exception e) {
            ip = "127.0.0.1";
        }
        //System.out.println("STEP 1.0.6");
        memory.<String, String>set("ip", ip);
        System.out.println("Percieved ip is " + ip + " first non loopbak is " + addr);
        //memory.<String, String>set("registryUnitIP", "192.168.2.5");

        //find registry
        new AdvertismentConsumer(this);
        while (memory.<String, String>get("registryUnitIP") == null) {
            //wait for consumer
        }

        memory.<String, Integer>set("registryPort", 8383);
        memory.<String, Integer>set("myPort", 8181);
        spu = new ServiceProvisionUnit(this);
        memory.<String, ServiceProvisionUnit>set("SPU", spu);
        //System.out.println("STEP 1.1");
        spu.startServer();
        //System.out.println("STEP 1.2");
        System.out.println("Set ports");
        //System.out.println("STEP 2");
        this.debug = debug;
        String jsonReply = "";
        System.out.println("setAffinity");
        System.out.println("reached");
        try {
            //System.out.println("STEP 3");
            System.out.println("Tryint to https reg unit");
            jsonReply = "{result : \"success\", uid : \"1\"}";

            RequestObject ro = memory.<String, ServiceProvisionUnit>get("SPU").httpContact(
                    new RequestObject(
                            "http://" + memory.<String, String>get("registryUnitIP"),
                            memory.<String, Integer>get("registryPort").intValue(),
                            URLEncoder.encode("ip=" + memory.<String, String>get("ip") + "&port=" + memory.<String, Integer>get("myPort") + "&services={\"services\":[{\"uri\" : \"/sensors\", \"description\" : \"returns a list of sensors available\"}]}"),
                            "/register",
                            memory.<String, InetAddress>get("addr"),
                            "Post"));

            jsonReply = ro.getResponse();
            System.out.println("json reply is " + jsonReply);
            System.out.println("Done https reg unit");
            //registers itself to the registry unit
            MyLogger.log(ro.toString());
            if (debug) {
                MyLogger.log("reply is: " + jsonReply);
            }
            JSONObject obj;
            MyLogger.log("Error parsing this" + jsonReply);
            obj = new JSONObject(jsonReply);

            if (!obj.has("result") && !obj.get("result").equals("success")) {
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

            MyLogger.log(jsonReply);
            //e.printStackTrace();
        }
        //System.out.println("Step 2.01");
        SharedMemory.<String, ArrayList<MicazMote>>set("SensorsList", new ArrayList<MicazMote>());
        SharedMemory.<String, ArrayList<MicazMote>>set("BlackList", new ArrayList<MicazMote>());
        //System.out.println("Step 2.02");
        dropDaemon = createDropDaemon();
        //System.out.println("Step 2.03");
        dropDaemon.start();
        //System.out.println("Step 2.04");
        populate = createPollDaemon();
        //System.out.println("Step 2.05");
        populate.start();
        //System.out.println("Step 2.06");
        loadMonitoredVariables();
        //System.out.println("Step 2.07");
        requestServingDaemon();
        //System.out.println("Step 2.08");
        requestsTidier();
        //System.out.println("Step 2.09");
        //AggregatorStatusReport.init();
        //System.out.println("Step 2.10");
        return;

    }

    public DecisionMakingUnit getDm() {
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
                            MyLogger.log("Latest activity " + m.getLatestActivity());
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
                        MyLogger.log("CurrentTime " + Util.getTime());
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
                            SharedMemory.<String, SensorsCommunicationUnit.SensorsCommunicationUnit>get("SCU").sendPoll();
                            //System.out.println("polling");
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
        SharedMemory.<String, SensorsCommunicationUnit.SensorsCommunicationUnit>get("SCU").sendPoll();
    }

    public void getSwitchInfo(int id) {
        SharedMemory.<String, SensorsCommunicationUnit.SensorsCommunicationUnit>get("SCU").sendSwitchPoll(id);
    }

    public void toggleSwitch(int id) {
        SharedMemory.<String, SensorsCommunicationUnit.SensorsCommunicationUnit>get("SCU").sendSwitchToggle(id);
    }

    /*public void sendReadingRequest(int id, boolean cached, String ServiceURI) {
     for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
     if (m.getId() == id) {
     //messages.sendReadingRequest(id, type);
     m.RequestServiceReading(uid, cached);
     }
     }
     }*/
    public void reportReading(int id, int messageType, int[] Readings) {
        for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == id) {
                switch (messageType) {
                    case SensorsCommunicationUnit.Constants.TEMP: {
                        m.setTempReading(Util.median(Readings));
                        //System.out.println("tried to set reading to " + Util.median(Readings));
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

    public void addToBlackList(MicazMote mote) {
        SharedMemory.<String, ArrayList<MicazMote>>get("BlackList").add(mote);
    }

    public void emptyBlacklist() {
        SharedMemory.<String, ArrayList<MicazMote>>unset("BlackList");
        SharedMemory.<String, ArrayList<MicazMote>>set("BlackList", new ArrayList<MicazMote>());
    }

    public void UpdateRecordInSHM(MicazMote mote) {
        boolean found = false;

        if (SharedMemory.<String, Boolean>get("OverLoadStatus")) {
            for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("BlackList")) {
                if (m.getId() == mote.getId()) {
                    return;
                }
            }
        }

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
        //System.out.println("ADD URL IS " + url);
        threadId = (threadId + 1) % 10000;

        //System.out.println("DM HERE ID IS" + threadId);
        int criticality = getCriticalityLevelOfRequest(url);
        request.setCriticality(criticality);
        if(criticality >4){
            DumpVariables.newHighCriticalityRequest();
        }
        else{
            DumpVariables.newRequest();
        }
        RequestExecutionThread thread = new RequestExecutionThread(threadId, criticality, url);
        addRequestToBucket(request, threadId, thread);
        //System.out.println("CREATED THREAD");
        memory.<String, ArrayList<RequestExecutionThread>>get("ThreadBucket" + criticality).add(thread);

        //System.out.println("ADDED THREAD in bucket " + "ThreadBucket" + criticality);
        return threadId;
    }

    private void addRequestToBucket(DoComms request, int id, RequestExecutionThread ret) {
        PendingRequest pr = new PendingRequest(request, id);
        pr.setRet(ret);
        memory.<String, ArrayList<PendingRequest>>get("RequestBucket").add(pr);
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
        removeRequest(id);

    }

    private int getCriticalityLevelOfRequest(String url) {
        int criticality = 1;
        String[] parametersSplit = url.split("\\?");
        if (parametersSplit.length < 2) {
            criticality = 1;
            // System.out.println("parametersSplit <2 = " + parametersSplit.length);
            for (String s : parametersSplit) {
                //MyLogger.log(s);
            }
        } else {
            //for (String s : parametersSplit) {
            //   System.out.println("parametersSplit !<2 = " + s);
            //return 1;
            //}

            //System.out.println("Param split is " +parametersSplit);
            String[] parameters = parametersSplit[1].split("&");

            for (String parameter : parameters) {
                //System.out.println("Checking parm " +parameter);
                if (parameter.startsWith("crit")) {
                    String value = parameter.split("=")[1];
                    //System.out.println("crit val is "+value + "from "+parameter);
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
                return memory.<String, ArrayList<CoreDefinition>>get("Cores").get(i);
            } else {
                //System.out.println("current get id is " + memory.<String, ArrayList<CoreDefinition>>get("Cores").get(i).getId() + " while id given is " + id);
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

                    if (core == null) {
                        //System.out.println("No core available :(");
                        continue;
                    }
                    if (!hasNextRequestToServe()) {
                        continue;
                    }
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            RequestExecutionThread req = getNextRequestToServe();
                            //thread is removed form bucket from the call

                            if (req == null) {
                                return;
                            }

                            //System.out.println("Serving " + req.getId() + " of criticality " + req.getCriticality());
                            req.setWhatCore(core);
                            //the attachment happens during the running of the thread so
                            //this statement is now redundant core.attachTo(req);
                            req.run();

                        }
                    });

                    t.start();

                }
            }
        });
        daemon.start();
    }

    public void requestsTidier() {
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //System.out.println("Tidier ran");
                    try {
                        PendingRequest req = null;
                        for (PendingRequest request : SharedMemory.<String, ArrayList<PendingRequest>>get("RequestBucket")) {
                            if (request.getTimeOut() <= System.currentTimeMillis()) {
                                req = request;
                                request.setComplete(true);
                            }
                        }

                        if (req != null) {
                            //SharedMemory.<String, ArrayList<PendingRequest>>get("RequestBucket").remove(req);
                        }
                        Thread.sleep(150);
                    } catch (ConcurrentModificationException ex) {
                        Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        daemon.start();
    }

    private void removeRequest(int id) {
        System.out.println("Done with request " + id);
        PendingRequest removable = null;
        for (PendingRequest request : SharedMemory.<String, ArrayList<PendingRequest>>get("RequestBucket")) {
            if (request.getId() == id) {
                removable = request;
                request.setComplete(true);
            }
        }
        try {
            if (removable != null) {
                SharedMemory.<String, ArrayList<PendingRequest>>get("RequestBucket").remove(removable);
            }
            //Thread.sleep(500);
        } catch (ConcurrentModificationException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private RequestExecutionThread getNextRequestToServe() {
        switch (memory.<String, String>get("ServingAlgorithm")) {
            case "CAFIFO": {
                for (int i = memory.<String, Integer>get("CriticalityLevels"); i > 0; i--) {
                    if (!memory.<String, ArrayList>get("ThreadBucket" + i).isEmpty()) {
                        try {
                            return memory.<String, ArrayList<RequestExecutionThread>>get("ThreadBucket" + i).remove(0);
                        } catch (Exception e) {
                        }
                    }
                }
                break;
            }
        }
        return null;
    }

    private boolean hasNextRequestToServe() {
        switch (memory.<String, String>get("ServingAlgorithm")) {
            case "CAFIFO": {
                for (int i = memory.<String, Integer>get("CriticalityLevels"); i > 0; i--) {
                    if (!memory.<String, ArrayList>get("ThreadBucket" + i).isEmpty()) {
                        try {
                            return true;
                        } catch (Exception e) {
                        }
                    }
                }
                break;
            }
        }
        return false;
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

    public void setCoreMode(int id, int mode) {
        System.out.println("Setting core " + id + " to " + mode);
        for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
            if (core.getId() == id) {
                switch (mode) {
                    case -1: {
                        core.setUnderUtilized();
                        break;
                    }
                    case 0: {
                        core.setNormalLoad();
                        break;
                    }
                    case 1: {
                        core.setOverLoadLimit();
                        break;
                    }
                }
                break;
            }
        }
    }

    public void setCoreAvailability(int id, boolean mode) {
        for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
            if (core.getId() == id) {
                core.setRunning(mode);
                break;
            }
        }
    }

    public void changeToPush(int id) {
        for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == id) {
                m.setPush(true);

                //to apo katw na ginetai sto dmu basei exception kai 8a ylopoiei to nhma to control
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (m.isPush()) {
                            for (Service ser : m.getServices()) {
                                m.RequestServiceReading(ser.getURI(), false, 5);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(RequestExecutionThread.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                });
                Logging.MyLogger.log("Mote " + m.getId() + " turned to push protocol");
                t.start();
                break;
            }
        }
    }

    private void loadMonitoredVariables() {
        //this SHOULD be done dynamically from a file during the mon unit 
        //initialiation. this is bakalistikos way

        memory.<String, ArrayList<MonitoredVariable>>get("monitoredVariables").add(
                new MonitoredVariable("CpuLoad", 1, util.Statics.overloadLevel, new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (CoreDefinition core : memory.<String, ArrayList<CoreDefinition>>get("Cores")) {

                            if (core.getLoad() > util.Statics.overloadLevel && !core.isOverLoadLimit()) {
                                SharedMemory.<String, DecisionMakingUnit>get("DMU").reconfigure(new CustomException("woah", "woaaaaah", "CoreReconfiguration"));
                                break;
                            }
                            if (core.getLoad() < util.Statics.underUtilizedLevel && !core.isUnderUtilized()) {
                                SharedMemory.<String, DecisionMakingUnit>get("DMU").reconfigure(new CustomException("woah", "woaaaaah", "CoreReconfiguration"));
                                break;
                            }
                            if (core.getLoad() <= util.Statics.exitOverLoadLevel && core.isOverLoadLimit()) {
                                SharedMemory.<String, DecisionMakingUnit>get("DMU").reconfigure(new CustomException("woah", "woaaaaah", "CoreReconfiguration"));
                                break;
                            }
                            if (core.getLoad() > util.Statics.underUtilizedLevel && core.getLoad() < util.Statics.overloadLevel && !core.isNormalLoad()) {
                                SharedMemory.<String, DecisionMakingUnit>get("DMU").reconfigure(new CustomException("woah", "woaaaaah", "CoreReconfiguration"));
                                break;
                            }

                        }
                    }
                })));

        memory.<String, ArrayList<MonitoredVariable>>get("monitoredVariables").add(
                new MonitoredVariable("OverloadFlag", 1, 1, new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (SharedMemory.<String, Boolean>get("OverLoadStatus")) {
                            SharedMemory.<String, DecisionMakingUnit>get("DMU").reconfigure(new CustomException("woah", "woaaaaah", "OverLoaded"));
                        }
                    }
                })));
        memory.<String, ArrayList<MonitoredVariable>>get("monitoredVariables").add(
                new MonitoredVariable("SensorReseter", 1, 1, new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (MicazMote m : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
                            m.reset();
                        }
                    }
                })));
        memory.<String, ArrayList<MonitoredVariable>>get("monitoredVariables").add(
                new MonitoredVariable("BlacklistEmptier", 10, 1, new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SharedMemory.<String, Control>get("MCU").emptyBlacklist();
                    }
                })));
    }
}
