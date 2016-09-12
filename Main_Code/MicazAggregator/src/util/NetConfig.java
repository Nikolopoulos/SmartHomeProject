/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 *
 * @author billaros
 */
public class NetConfig {

    private static InetAddress addr = null;// = getFirstNonLoopbackAddress(true, false);
    private static String ip;// = addr.getHostAddress();
    private static  String registryUnitIP;// = "127.0.0.1";
    private static  int registryPort;// = 8383;
    private static  int myPort;// = 8282;

    public static InetAddress getAddr() {
        return addr;
    }

    public static void setAddr(InetAddress addr) {
        NetConfig.addr = addr;
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        NetConfig.ip = ip;
    }

    public static String getRegistryUnitIP() {
        return registryUnitIP;
    }

    public static void setRegistryUnitIP(String registryUnitIP) {
        NetConfig.registryUnitIP = registryUnitIP;
    }

    public static int getRegistryPort() {
        return registryPort;
    }

    public static void setRegistryPort(int registryPort) {
        NetConfig.registryPort = registryPort;
    }

    public static int getMyPort() {
        return myPort;
    }

    public static void setMyPort(int myPort) {
        NetConfig.myPort = myPort;
    }
    
        //courtesy of How to get the ip of the computer on linux through Java? -> http://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
    public static void setFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                InetAddress tempAddr = (InetAddress) en2.nextElement();
                if (!tempAddr.isLoopbackAddress()) {
                    if (tempAddr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        NetConfig.addr=tempAddr;
                    }
                    if (tempAddr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        NetConfig.addr=tempAddr;
                    }
                }
            }
        }
       NetConfig.addr=null;
    }
}
