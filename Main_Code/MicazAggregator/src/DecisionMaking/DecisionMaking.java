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

        System.out.println("DM HERE ID IS" + id);
        ServiceEstimation service = new ServiceEstimation(ServiceArguements);
        System.out.println("COMPUTED CL");
        SuperSmahtThread thread = new SuperSmahtThread(id, service, myControl, ServiceArguements);
        System.out.println("CREATED THREAD");
        synchronized (threads) {
            threads.add(thread);
        }
        System.out.println("ADDED THREAD");
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

                while (run) {
                    synchronized (threads) {
                        for (Iterator<SuperSmahtThread> iter = threads.iterator(); iter.hasNext();) {
                            SuperSmahtThread thread = iter.next();
                            if (!thread.isRunning()) {
                                //MyLogger.log(thread + "not running");
                                /*if (!(thread.getWhatCore() == thread.getC().criticalSensingCore || thread.getWhatCore() == thread.getC().sensingCore)) {
                                 if (thread.getTsoulou().getClEstimation() > 1.5) {
                                 //i should probably find a way to estimate per core load
                                 MyLogger.log("Attaching service of cl=" + thread.getTsoulou().getClEstimation() + " to critical core");
                                        
                                 } else {
                                 MyLogger.log("Attaching service of cl=" + thread.getTsoulou().getClEstimation() + " to non-critical core");
                                 thread.setWhatCore(thread.getC().sensingCore);
                                 }
                                 }*/
                                thread.setWhatCore(util.CoreManagement.getLeastBusyCore().getCore());
                            }
                        }
                    }
                }
            }
        }).start();

    }

    public String getResultOf(int id) {
        synchronized (threads) {
            SuperSmahtThread t = null;
            for (Iterator<SuperSmahtThread> iter = threads.iterator(); iter.hasNext();) {
                SuperSmahtThread thread = iter.next();
                if (thread.getId() == id) {
                    t = thread;
                    //MyLogger.log(thread + "got result");
                    break;
                }
            }

            return (t == null || t.getReturnVal().equalsIgnoreCase("")) ? "" : t.getReturnVal();
        }
    }

}
