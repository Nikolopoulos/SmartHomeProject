/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContextAwarenessUnit.contextClasses;

import java.util.ArrayList;

/**
 *
 * @author billaros
 */
public class Aggregator {
    String IP;    
    int Port;
    transient String uid;
    
    
   

    public Aggregator(String IP, int Port) {
        this.IP = IP;
        this.Port = Port;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }   

    public int getPort() {
        return Port;
    }

    public void setPort(int Port) {
        this.Port = Port;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }    
}
