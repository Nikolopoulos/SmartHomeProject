/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SensorsCommunicationUnit;

import net.tinyos.message.Message;
import net.tinyos.message.MessageListener;
import net.tinyos.message.MoteIF;
import net.tinyos.util.PrintStreamMessenger;
import ControlUnit.Control;
/**
 *
 * @author billaros
 */
public class SensorsCommunicationUnit implements MessageListener {

    static MoteIF mote;
    Control c;
    
    public SensorsCommunicationUnit() {
        this.c=SharedMemory.SharedMemory.<String,Control>get("MCU");
        mote = new MoteIF(PrintStreamMessenger.err);
        mote.registerListener(new Poll_Answer(), this);
        mote.registerListener(new ReadingMsgAnswer(), this);
        mote.registerListener(new Switch_Answer(), this);
        
    }
    /* Broadcast a version+interval message. */

    public static void sendPoll() {
        Poll_Request pr = new Poll_Request();
        pr.set_messageType(Constants.POLL);
        
        try{
            mote.send(MoteIF.TOS_BCAST_ADDR, pr);
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void sendReadingRequest(int id,int type) {
        ReadingMsgRequest req = new ReadingMsgRequest();
        req.set_id(id);
        req.set_messageType(Constants.READINGREQUEST);
        req.set_type(type);
        
        try{
            mote.send(id, req);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
    public static void sendSwitchPoll(int id) {
        Switch_Poll req = new Switch_Poll();
        req.set_id(id);
        req.set_messageType(Constants.SWITCHPOLL);        
        
        try{
            mote.send(id, req);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void sendSwitchToggle(int id) {
        Switch_Toggle req = new Switch_Toggle();
        req.set_id(id);
        req.set_messageType(Constants.SWITCHCHANGE);        
        
        try{
            mote.send(id, req);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @Override
    public synchronized void messageReceived(int to, Message msg) {
        if (msg instanceof Poll_Answer) {
            //System.out.println("POLL ANSWER" + msg);
            Poll_Answer cmsg = (Poll_Answer) msg;
            MicazMote mote = new MicazMote(cmsg.get_id(), cmsg.get_services(),util.Util.getTime());
            c.UpdateRecordInSHM(mote);
        }
        else if(msg instanceof ReadingMsgAnswer) {
            System.out.println("READING ANSWER" + msg);
             ReadingMsgAnswer cmsg = (ReadingMsgAnswer) msg;
             c.reportReading(cmsg.get_id(),cmsg.get_type(),cmsg.get_readings());
        }
        else if(msg instanceof Switch_Answer) {
            System.out.println("SWITCH ANSWER" + msg);
             Switch_Answer cmsg = (Switch_Answer) msg;
             c.reportSwitch(cmsg.get_id(),cmsg.get_state());
        }
        else{
            System.out.println("DUMP" + msg);
        }
    }
    

}
