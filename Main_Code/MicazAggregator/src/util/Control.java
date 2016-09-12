/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import DecisionMaking.DecisionMaking;
import Logging.MyLogger;
import java.lang.management.ManagementFactory;
import java.net.SocketException;
import java.util.ArrayList;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.json.JSONObject;
import sensorPlatforms.MicazMote;

/**
 *
 * @author billaros
 */
public class Control {

    private DecisionMaking dm;   


    public Control() {

        dm = new DecisionMaking(this);
        SensorManager.setSensorsList(new ArrayList<MicazMote>());

        //Maybe find a way to search for the registryIP?
        NetConfig.setRegistryUnitIP("192.168.2.5");
        NetConfig.setRegistryPort(80);
        NetConfig.setMyPort(80);

        //try to find my inetAddress
        try {
            NetConfig.setFirstNonLoopbackAddress(true, false);
        } catch (SocketException ex) {
            System.exit(1);
        }
        try {
            NetConfig.setIp(NetConfig.getAddr().getHostAddress());
        } catch (Exception e) {
            NetConfig.setIp("127.0.0.1");
        }

        //register to registry unit
        JSONObject jsonReply = ConstructedRequests.RegisterToRegistryUnit();

        if (!JsonDataExtractor.isSuccess(jsonReply)) {
            if (UnitConfig.isDebug()) {
                MyLogger.log("jsonReply failed " + jsonReply);
            }
        } else {
            UnitConfig.setUid(JsonDataExtractor.getUID(jsonReply));
            if (UnitConfig.isDebug()) {
                MyLogger.log("myUID is " + UnitConfig.getUid());
            }
        }

        SensorManager.init();
        CoreManagement.init();
        
        
    }

    public DecisionMaking getDm() {
        return dm;
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
