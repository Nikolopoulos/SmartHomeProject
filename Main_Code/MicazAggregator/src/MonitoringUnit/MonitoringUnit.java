/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MonitoringUnit;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class MonitoringUnit {

    ArrayList<MonitoredVariable> monitoredVariables;
    static int cycle;

    public MonitoringUnit() {
        SharedMemory.SharedMemory.<String, ArrayList<MonitoredVariable>>set("monitoredVariables", new ArrayList<MonitoredVariable>());
        initiate();
    }

    private void initiate() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    for (MonitoredVariable var : SharedMemory.SharedMemory.<String, ArrayList<MonitoredVariable>>get("monitoredVariables")) {
                        if (cycle % var.getSeconds() == 0) {
                            var.getAction().run();
                        }
                        cycle++;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MonitoringUnit.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        t.run();
    }

}
