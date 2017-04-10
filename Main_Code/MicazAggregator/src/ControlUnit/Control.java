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
    //public Messaging messages;
    private boolean simulation = true;
    public SimulatedMessaging messages;
    Thread dropDaemon, populate;
    String uid = "";
    boolean debug;
    public ThreadAffinity threadAffinity;

    public final Core criticalSensingCore;
    public final Core HTTPCore;
    public final Core sensingCore;
    public final Core cronCore;
    public InetAddress addr = null;// = getFirstNonLoopbackAddress(true, false);
    public String ip;// = addr.getHostAddress();
    ServiceProvisionUnit spu;

    public Control(boolean debug) {
        memory = SharedMemory.<String,SharedMemory>get("SMU");
        memory.<String,Control>set("MCU", this);
        dm = new DecisionMaking(this);
        memory.<String,DecisionMaking>set("DMU", dm);
        
        try {
            addr = getFirstNonLoopbackAddress(true, false);
            memory.<String,InetAddress>set("addr", addr);
        } catch (SocketException ex) {
            System.exit(1);
        }
        try {
            ip = addr.getHostAddress();
            
        } catch (Exception e) {
            ip = "127.0.0.1";
        }
        memory.<String,String>set("ip", ip);
        System.out.println("Percieved ip is " + ip + " first non loopbak is " + addr);
        memory.<String,String>set("registryUnitIP", "192.168.2.5");
        memory.<String,Integer>set("registryPort", 8383);
        memory.<String,Integer>set("myPort", 8181);
        spu = new ServiceProvisionUnit(this);
        memory.<String,ServiceProvisionUnit>set("SPU", spu);
        spu.startServer();
        System.out.println("Set ports");
        threadAffinity = new ThreadAffinity(this);
        memory.<String,Integer>set("AvailableCores", threadAffinity.cores().length);
        memory.<String,ThreadAffinity>set("Affinity", threadAffinity);
        this.debug = debug;
        String jsonReply = "";
        System.out.println("setAffinity");

        if (threadAffinity.cores().length == 4) {
            criticalSensingCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[1];
            sensingCore = threadAffinity.cores()[2];
            cronCore = threadAffinity.cores()[3];
        } else if (threadAffinity.cores().length == 2) {
            criticalSensingCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[1];
            sensingCore = threadAffinity.cores()[1];
            cronCore = threadAffinity.cores()[0];
        } else if (threadAffinity.cores().length == 1) {
            criticalSensingCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[0];
            sensingCore = threadAffinity.cores()[0];
            cronCore = threadAffinity.cores()[0];
        } else {
            criticalSensingCore = threadAffinity.cores()[0];
            HTTPCore = threadAffinity.cores()[0];
            sensingCore = threadAffinity.cores()[0];
            cronCore = threadAffinity.cores()[0];
        }

        MyLogger.log("Available cores: " + threadAffinity.cores().length);
        MyLogger.log("criticalCore: " + criticalSensingCore);
        MyLogger.log("HTTPCore: " + HTTPCore);
        MyLogger.log("sensingCore: " + sensingCore);
        MyLogger.log("cronCore: " + cronCore);
        criticalSensingCore.setC(this);
        HTTPCore.setC(this);
        sensingCore.setC(this);
        cronCore.setC(this);
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
        
        SharedMemory.<String,ArrayList<MicazMote>>set("SensorsList",new ArrayList<MicazMote>());
        dropDaemon = createDropDaemon();
        dropDaemon.start();
        populate = createPollDaemon();
        populate.start();
    }

    public DecisionMaking getDm() {
        return dm;
    }

    private synchronized Thread createDropDaemon() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    cronCore.attachTo();
                } catch (Exception ex) {
                    Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
                }
                ArrayList<MicazMote> toRemove = new ArrayList<MicazMote>();
                if (debug) {
                    MyLogger.log("Drop daemon started");
                }
                while (true) {
                    for (MicazMote m : SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList")) {

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
                            RequestObject regReq = new RequestObject("http://" + SharedMemory.<String,String>get("registryUnitIP"), SharedMemory.<String,Integer>get("registryPort"), URLEncoder.encode("uid=" + uid + "&services=" + services), "/delete", addr, "POST");
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
                        SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList").remove(m);
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
                    sensingCore.attachTo();

                    boolean found = false;
                    MicazMote foundSensor = null;
                    for (MicazMote m : SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList")) {
                        if (m.getId() == sensor.getId()) {
                            foundSensor = m;
                            m.setLatestActivity(Util.getTime());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList").add(sensor);
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
                                RequestObject regReq = new RequestObject("http://" + SharedMemory.<String,String>get("registryUnitIP"), SharedMemory.<String,Integer>get("registryPort"), URLEncoder.encode("uid=" + uid + "&services=" + services), "/update", addr, "POST");
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
                    cronCore.attachTo();

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

    public void sendReadingRequest(int id, int type, String ServiceURI) {
        for (MicazMote m : SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == id) {
                //messages.sendReadingRequest(id, type);
                m.RequestServiceReading(uid, debug, messages);
            }
        }
    }

    public void reportReading(int id, int messageType, int[] Readings) {
        for (MicazMote m : SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == id) {
                switch (messageType) {
                    case SensorsCommunicationUnit.lib.Constants.TEMP: {
                        m.setTempReading(Util.median(Readings));
                        System.out.println("tried to set reading to " + Util.median(Readings));
                        break;
                    }
                    case SensorsCommunicationUnit.lib.Constants.PHOTO: {
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
        for (MicazMote m : SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == id) {
                m.setLatestActivity(Util.getTime());
                m.setSwitchState(state);
                System.out.println("State changed to " + state);
            }
        }
    }

    public void UpdateRecordInSHM(MicazMote mote) {
        boolean found = false;
        for (MicazMote m : SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList")) {
            if (m.getId() == mote.getId()) {
                m.setLatestActivity(Util.getTime());
                found = true;
                break;
            }
        }
        if (!found) {
            SharedMemory.<String,ArrayList<MicazMote>>get("SensorsList").add(mote);
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

                    RequestObject regReq = new RequestObject("http://" + SharedMemory.<String,String>get("registryUnitIP"), SharedMemory.<String,Integer>get("registryPort"), URLEncoder.encode("uid=" + uid + "&services=" + services), "/update", addr, "POST");
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
}
