/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import ControlUnit.Control;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class AdvertismentConsumer {

    public AdvertismentConsumer(Control c) {
        Thread consumer = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    // Which port should we listen to
                    int port = 5000;
                    // Which address
                    String group = "225.255.255.6";

                    // Create the socket and bind it to port 'port'.
                    MulticastSocket s = new MulticastSocket(port);

                    // join the multicast group
                    InetAddress add = InetAddress.getByName(group);
                    s.joinGroup(InetAddress.getByName(group));
                    // Now the socket is set up and we are ready to receive packets

                    // Create a DatagramPacket and do a receive
                    byte buf[] = new byte[1024];
                    DatagramPacket pack = new DatagramPacket(buf, buf.length);

                    while (true) {
                        s.receive(pack);
                        SharedMemory.SharedMemory.set("registryUnitIP", new String(buf).substring(1).trim());
                        //System.out.println("Received data from: " + pack.getAddress().toString()
                        //        + ":" + pack.getPort() + " with length: "
                        //        + pack.getLength() + " and data " + new String(buf).trim());
                        Thread.sleep(1000);
                    }
                    // Finally, let us do something useful with the data we just received,
                    // like print it on stdout :-)

                    // And when we have finished receiving data leave the multicast group and
                    // close the socket
                    //s.leaveGroup(InetAddress.getByName(group));
                    //s.close();
                } catch (IOException ex) {
                    Logger.getLogger(AdvertismentConsumer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AdvertismentConsumer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        consumer.start();
    }

}
