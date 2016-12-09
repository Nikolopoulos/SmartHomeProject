/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulator;

import lib.*;
import sensorPlatforms.MicazMote;
import util.Control;
/**
 *
 * @author billaros
 */
public class SimulatedMessaging{

    Control c;
    MoteLibSimulator mote;
    
    public SimulatedMessaging(MoteLibSimulator moteLib) {
        this.mote = moteLib;
    }
    /* Broadcast a version+interval message. */

    public void sendPoll() {
        try{
            mote.send(-1, Constants.POLL);//using -1 as broadcast addr on simulation
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendReadingRequest(int id,int type) {
        try{
            mote.send(id, Constants.READINGREQUEST,type);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    public void sendSwitchPoll(int id) {
        try{
            mote.send(id, Constants.SWITCHPOLL);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void sendSwitchToggle(int id) {
        try{
            mote.send(id, Constants.SWITCHCHANGE);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    public synchronized void messageReceived(SimulatedMessage msg) {
        if (msg.instanceOf().equalsIgnoreCase("Poll_Answer")) {
            //System.out.println("POLL ANSWER" + msg);
            MicazMote cmote = new MicazMote(msg.get_id(), msg.get_services(),util.Util.getTime());
            c.reportPollAck(cmote);
        }
        else if(msg.instanceOf().equalsIgnoreCase("ReadingMsgAnswer")) {
            System.out.println("READING ANSWER" + msg);
             c.reportReading(msg.get_id(),msg.get_type(),msg.get_readings());
        }
        else if(msg.instanceOf().equalsIgnoreCase("Switch_Answer")) {
            System.out.println("SWITCH ANSWER" + msg);
             c.reportSwitch(msg.get_id(),msg.get_state());
        }
        else{
            System.out.println("DUMP" + msg);
        }
    }
    

}
