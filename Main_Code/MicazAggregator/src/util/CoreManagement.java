/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import Logging.MyLogger;
import affinitySupport.Core;
import affinitySupport.ThreadAffinity;
import java.util.ArrayList;
import oscilloscope.Messaging;

/**
 *
 * @author billaros
 */
public class CoreManagement {

    private static ThreadAffinity threadAffinity;

    public static ArrayList<Core> cores = new ArrayList<Core>();
    public static ArrayList<CoreLoad> loads = new ArrayList<CoreLoad>();

    public static CoreLoad getLeastBusyCore() {
        CoreLoad min = loads.get(0);
        for (CoreLoad cl : loads) {
            if (cl.getLoad() < min.getLoad()) {
                min = cl;
            }
        }
        return min;
    }

    public static void init() {
        CoreManagement.threadAffinity = new ThreadAffinity();
        MyLogger.log("Available cores: " + threadAffinity.cores().length);

        for (Core c : CoreManagement.threadAffinity.getCores()) {
            CoreManagement.cores.add(c);
            CoreManagement.loads.add(new CoreLoad(c));
        }
    }

}
