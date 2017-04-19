/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DecisionMaking;

import ControlUnit.RequestExecutionThread;
import Logging.MyLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import ControlUnit.Control;

import java.util.Random;

/**
 *
 * @author billaros
 */
public class DecisionMaking {

    private final List<RequestExecutionThread> threads = Collections.synchronizedList(new ArrayList<RequestExecutionThread>());
    private int id = 0;
    private Thread magic;
    private boolean run;

    public DecisionMaking() {
        run = true;
        //startDecisionDaemon();
    }

    /*private void startDecisionDaemon() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                while (run) {
                    synchronized (threads) {
                        for (Iterator<RequestExecutionThread> iter = threads.iterator(); iter.hasNext();) {
                            RequestExecutionThread thread = iter.next();
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
                                }
                            }
                        }
                    }
                }
            }
        }).start();

    }*/

    public String getResultOf(int id) {
        synchronized (threads) {
            RequestExecutionThread t = null;
            for (Iterator<RequestExecutionThread> iter = threads.iterator(); iter.hasNext();) {
                RequestExecutionThread thread = iter.next();
                if (thread.getId() == id) {
                    t = thread;
                    //MyLogger.log(thread + "got result");
                    break;
                }
            }

            //return (t == null || t.getReturnVal().equalsIgnoreCase("")) ? "-1" : t.getReturnVal();
            return (t == null ? "-1" : t.getReturnVal());
        }
    }

}
