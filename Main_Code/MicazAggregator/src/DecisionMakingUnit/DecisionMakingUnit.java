/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DecisionMakingUnit;

import ControlUnit.Control;
import ControlUnit.CoreDefinition;
import SharedMemory.SharedMemory;
import java.util.ArrayList;
import util.CustomException;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class DecisionMakingUnit {

    public static void reconfigure(CustomException e) {
        if (e.getType().equalsIgnoreCase("CoreReconfiguration")) {
            int availableCores = SharedMemory.<String, Integer>get("AvailableCores");
            int runningCores = 0;
            int overloadedCores = 0;
            for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                if (core.isPublicResource() && core.getRunning()) {
                    runningCores++;
                    if (core.isOverLoadLimit()) {
                        overloadedCores++;
                    }
                }
            }
            for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                if (core.getLoad() > util.Statics.overloadLevel && !core.isOverLoadLimit()) {
                    SharedMemory.<String, Control>get("MCU").setCoreMode(core.getId(), 1);
                    if (runningCores > overloadedCores) {
                        for (CoreDefinition core2 : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                            if (core2.isPublicResource() && core2.getRunning() && !core2.isOverLoadLimit()) {
                                SharedMemory.<String, Control>get("MCU").setCoreAvailability(core2.getId(), true);
                                break;
                            }
                        }
                    }
                } else if (core.getLoad() < util.Statics.underUtilizedLevel && !core.isUnderUtilized()) {
                    SharedMemory.<String, Control>get("MCU").setCoreMode(core.getId(), -1);
                    if (runningCores > 1) {
                        SharedMemory.<String, Control>get("MCU").setCoreAvailability(core.getId(), false);
                    }
                } else if(!core.isNormalLoad()){
                     SharedMemory.<String, Control>get("MCU").setCoreMode(core.getId(), 0);
                }
            }
        }
    }
}
