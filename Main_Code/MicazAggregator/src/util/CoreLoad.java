/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import affinitySupport.Core;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author billaros
 */
public class CoreLoad {

    private final Core core;
    private int load;
    private ArrayList<Thread> threads;

    public CoreLoad(Core core) {
        this.core = core;
        load = 0;
        monitor();
    }

    public Core getCore() {
        return core;
    }

    public int getLoad() {
        return load;
    }
    

    public void addThread(Thread t) {
        core.attach(t);
        threads.add(t);
        load++;
    }

    private void monitor() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                for (Thread t : threads) {
                    if (t.getState() == State.TERMINATED) {
                        threads.remove(t);
                        load--;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CoreLoad.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();

    }

}
