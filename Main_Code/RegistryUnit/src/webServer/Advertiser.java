/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class Advertiser {

    public Advertiser() {
        Thread advertiseIpThread = new Thread(new Runnable() {

            @Override
            public void run() {

                String message="";
                try {
                    message = getFirstNonLoopbackAddress(true, false).toString();
                } catch (SocketException ex) {
                    Logger.getLogger(Advertiser.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    // Which port should we send to
                    int port = 5000;
                    // Which address
                    String group = "225.255.255.6";
                    // Which ttl
                    int ttl = 1;

                    // Create the socket but we don't bind it as we are only going to send data
                    MulticastSocket s = new MulticastSocket();

                    // Note that we don't have to join the multicast group if we are only
                    // sending data and not receiving
                    // Fill the buffer with some data
                    byte buf[] = new byte[message.length()];
                    for (int i = 0; i < buf.length; i++) {
                        buf[i] = (byte) message.charAt(i);
                    }
                    // Create a DatagramPacket
                    DatagramPacket pack = new DatagramPacket(buf, buf.length,
                            InetAddress.getByName(group), port);
                    // Do a send. Note that send takes a byte for the ttl and not an int.
                    while (true) {
                        s.send(pack, (byte) ttl);
                        //System.out.println("Sent message to broadcast group");
                        Thread.sleep(1000);
                    }
                    // And when we have finished sending data close the socket

                } catch (IOException ex) {
                    Logger.getLogger(Advertiser.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Advertiser.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        advertiseIpThread.start();
    }

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

}
