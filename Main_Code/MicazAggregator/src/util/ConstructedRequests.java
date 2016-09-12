/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import Logging.MyLogger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author billaros
 */
public class ConstructedRequests {

    public static JSONObject RegisterToRegistryUnit() {
        try {
            String jsonReply = HTTPRequest.sendPost("http://" + NetConfig.getRegistryUnitIP(), NetConfig.getRegistryPort(), URLEncoder.encode("ip=" + NetConfig.getIp() + "&port=" + NetConfig.getMyPort() + "&services={\"services\":[{\"uri\" : \"/sensors\", \"description\" : \"returns a list of sensors available\"}]}"), "/register", NetConfig.getAddr());
            //registers itself to the registry unit
            //MyLogger.log("http://" + registryUnitIP + ":" + registryPort + "/register" + URLEncoder.encode("ip=" + ip + "&port=" + myPort + "&services={\"services\":[{\"uri\" : \"/sensors\", \"description\" : \"returns a list of sensors available\"}]}"));
            if (UnitConfig.isDebug()) {
                MyLogger.log("reply is: " + jsonReply);
            }
            JSONObject obj;
            //MyLogger.log("Error parsing this" + jsonReply);
            obj = new JSONObject(jsonReply);
            return obj;
        } catch (Exception ex) {
            MyLogger.log("Contacting the RU failed " + ex);
        }
        return new JSONObject("");
    }

    public static JSONObject UpdateServices(String services) {
        try {
            String jsonReply = HTTPRequest.sendPost("http://" + NetConfig.getRegistryUnitIP(), NetConfig.getRegistryPort(), URLEncoder.encode("uid=" + UnitConfig.getUid() + "&services=" + services), "/update", NetConfig.getAddr());
            JSONObject obj;
            //MyLogger.log("Error parsing this" + jsonReply);
            obj = new JSONObject(jsonReply);
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(ConstructedRequests.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONObject("");
    }

    public static JSONObject DeleteServices(String services) {
        try {
            String jsonReply = HTTPRequest.sendPost("http://" + NetConfig.getRegistryUnitIP(), NetConfig.getRegistryPort(), URLEncoder.encode("uid=" + UnitConfig.getUid() + "&services=" + services), "/delete", NetConfig.getAddr());
            JSONObject obj;
            //MyLogger.log("Error parsing this" + jsonReply);
            obj = new JSONObject(jsonReply);
            return obj;
        } catch (Exception ex) {
            Logger.getLogger(ConstructedRequests.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONObject("");
    }
}
