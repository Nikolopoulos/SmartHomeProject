/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServiceProvisionUnit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import ControlUnit.Control;
import java.util.concurrent.Semaphore;

/**
 *
 * @author billaros
 */
public class ServiceProvisionUnit {

    private static int port = 8181,
                       maxConnections = 0;
    // Listen for incoming connections and handle them
    final Control finalControl;

    public ServiceProvisionUnit(Control c) {
        
        this.finalControl = c;

    }

    public void startServer() {
        Thread serverThread = new Thread(new Runnable() {
            //so very difficult, listens for requests then executes them on another thread
            @Override
            public void run() {
                try {
                    try {
                        //System.out.println(finalControl);
                        //System.out.println(finalControl.findCoreById(0));
                            //System.out.println(finalControl.findCoreById(0).getCore());
                        finalControl.findCoreById(0).getCore().attachTo();
                         //System.out.println("Server attached!");
                    } catch (Exception ex) {
                        Logger.getLogger(ServiceProvisionUnit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    int i = 0;
                    ServerSocket listener = new ServerSocket(port);
                    Socket server;
                    while ((i++ < maxConnections) || (maxConnections == 0)) {
                        server = listener.accept();
                        
                         //System.out.println("Server Accepted!!");
                        DoComms conn_c = new DoComms(server, finalControl,finalControl.findCoreById(0).getCore());
                        Thread clientConnectionThread = new Thread(conn_c);
                        clientConnectionThread.setName("clientConnectionThread"+i);
                        clientConnectionThread.start();
                    }

                } catch (IOException ioe) {
                    //System.out.println("IOException on socket listen: " + ioe);
                    ioe.printStackTrace();
                }
            }
        });
        serverThread.start();
        //System.out.println("Server started!");
    }
    
    public RequestObject httpContact(RequestObject request){
        Semaphore reply = new Semaphore(0);
        
        if(request.getMethod().equalsIgnoreCase("POST")){
            Thread requestThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    request.setResponse(HTTPRequest.sendPost(request));
                    reply.release();
                }
            });
            requestThread.start();
        }
        
        else if (request.getMethod().equalsIgnoreCase("GET")){
            Thread requestThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        HTTPRequest.sendGet(request);
                        request.setResponse("");
                        reply.release();
                    } catch (Exception ex) {
                        Logger.getLogger(ServiceProvisionUnit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    reply.release();
                }
            });
            requestThread.start();
        }
        try {
            reply.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(ServiceProvisionUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return request;
    }

}
