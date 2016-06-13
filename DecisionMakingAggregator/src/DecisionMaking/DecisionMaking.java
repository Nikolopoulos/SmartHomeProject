/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DecisionMaking;

import Logging.MyLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import util.Control;

import java.util.Random;

/**
 *
 * @author billaros
 */
public class DecisionMaking {

    private final List<SuperSmahtThread> threads = Collections.synchronizedList(new ArrayList<SuperSmahtThread>());
    private Control myControl;
    private int id = 0;
    private Thread magic;
    private boolean run;

    public int add(String ServiceArguements) {
        System.out.println("ADD URL IS " + ServiceArguements);
        id = (id + 1) % 10000;
<<<<<<< HEAD
        System.out.println("DM HERE ID IS" + id);
        ServiceEstimation service = new ServiceEstimation(ServiceArguements);
        System.out.println("COMPUTED CL");
        SuperSmahtThread thread = new SuperSmahtThread(id, service, myControl, ServiceArguements);
        System.out.println("CREATED THREAD");
        synchronized (threads) {
            threads.add(thread);
        }
        System.out.println("ADDED THREAD");
=======
        MyLogger.log("Decision making received request of "+ServiceArguements +" and assigned id of "+id);
        
        ServiceEstimation service = new ServiceEstimation(ServiceArguements);
        MyLogger.log("Service with id "+id +" was assigned CL of "+service.getClEstimation()+"/3");
        SuperSmahtThread thread = new SuperSmahtThread(id, service, myControl,ServiceArguements);
        
        threads.add(thread);
>>>>>>> 82451c03364367e64fec0d55dd35693b6fa233bc
        return id;
    }

    public DecisionMaking(Control c) {
        this.myControl = c;
        run = true;
        startDecisionDaemon();
    }

    private void startDecisionDaemon() {
        new Thread(new Runnable() {

            @Override
            public void run() {
<<<<<<< HEAD

                while (run) {
                    synchronized (threads) {
                        for (Iterator<SuperSmahtThread> iter = threads.iterator(); iter.hasNext();) {
                            SuperSmahtThread thread = iter.next();
                            if (!thread.isRunning()) {
                                //MyLogger.log(thread + "not running");
                                if(!(thread.getWhatCore()==thread.getC().criticalSensingCore || thread.getWhatCore()==thread.getC().sensingCore))
                                if (thread.getTsoulou().getClEstimation() > 1.5) {
                                    //i should probably find a way to estimate per core load
                                    MyLogger.log("Attaching service of cl=" + thread.getTsoulou().getClEstimation() + " to critical core");
                                    thread.setWhatCore(thread.getC().criticalSensingCore);
                                } else {
                                    MyLogger.log("Attaching service of cl=" + thread.getTsoulou().getClEstimation() + " to non-critical core");
                                    thread.setWhatCore(thread.getC().sensingCore);
                                }

                                if (new Random().nextInt(10) > new Random().nextInt(5) + 5) {
                                    thread.run();
                                    //MyLogger.log(thread + "ran");
=======
                while(run){
                    for(Iterator<SuperSmahtThread> iter = threads.iterator(); iter.hasNext();){
                        SuperSmahtThread thread = iter.next();
                            if(!thread.isRunning()){                               
                                if(thread.getTsoulou().getClEstimation()>2){
                                    //i should probably find a way to estimate per core load
                                    MyLogger.log("Attaching service with id="+thread.getId() +" to critical core");
                                    thread.setWhatCore(thread.getC().criticalSensingCore);
                                }
                                else{
                                    MyLogger.log("Attaching service with id="+thread.getId() +" to non-critical core");
                                    thread.setWhatCore(thread.getC().sensingCore);
                                }
                                
                                if(new Random().nextInt(10)>new Random().nextInt(5)+5){
                                    thread.run();    
                                    MyLogger.log("Running service with id="+thread.getId());
>>>>>>> 82451c03364367e64fec0d55dd35693b6fa233bc
                                }
                            }
                        }
                    }
                }
            }
        }).start();

    }

    public String getResultOf(int id) {
<<<<<<< HEAD
        synchronized (threads) {
            SuperSmahtThread t = null;
            for (Iterator<SuperSmahtThread> iter = threads.iterator(); iter.hasNext();) {
                SuperSmahtThread thread = iter.next();
                if (thread.getId() == id) {
                    t = thread;
                    //MyLogger.log(thread + "got result");
                    break;
                }
=======
        SuperSmahtThread t = null;
        for (Iterator<SuperSmahtThread> iter = threads.iterator(); iter.hasNext();) {
            SuperSmahtThread thread = iter.next();
            if (thread.getId() == id) {
                t = thread;
                break;
>>>>>>> 82451c03364367e64fec0d55dd35693b6fa233bc
            }

            return (t == null || t.getReturnVal().equalsIgnoreCase("")) ? "" : t.getReturnVal();
        }
    }

}
