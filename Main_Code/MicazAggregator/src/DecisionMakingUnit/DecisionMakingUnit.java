/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DecisionMakingUnit;

import ControlUnit.Control;
import ControlUnit.CoreDefinition;
import SensorsCommunicationUnit.MicazMote;
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

            System.out.println("*************************");
            for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                runningCores = 0;
                overloadedCores = 0;
                for (CoreDefinition coreinside : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                    if (coreinside.isPublicResource() && coreinside.getRunning()) {
                        runningCores++;
                        if (coreinside.isOverLoadLimit()) {
                            overloadedCores++;
                        }
                    }
                }

                System.out.println("Got in reconf for core " + core.getId());
                System.out.println("Core load is " + core.getLoad());
                System.out.println("Core overload func is " + (core.getLoad() > util.Statics.overloadLevel));
                System.out.println("Core overload bool is  " + core.isOverLoadLimit());
                if (core.getLoad() > util.Statics.overloadLevel && !core.isOverLoadLimit()) {

                    SharedMemory.<String, Control>get("MCU").setCoreMode(core.getId(), 1);
                    overloadedCores++;
                    if (runningCores == overloadedCores) {
                        for (CoreDefinition core2 : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                            if (core2.isPublicResource() && !core2.getRunning() && !core2.isOverLoadLimit()) {
                                SharedMemory.<String, Control>get("MCU").setCoreAvailability(core2.getId(), true);
                                break;
                            }
                        }
                    }
                }
                if (core.getLoad() < util.Statics.underUtilizedLevel && !core.isUnderUtilized()) {
                    SharedMemory.<String, Control>get("MCU").setCoreMode(core.getId(), -1);
                    if (runningCores > 1) //change to 2 for more than 2 cores
                    {
                        System.out.println("Running are " + runningCores);
                        SharedMemory.<String, Control>get("MCU").setCoreAvailability(core.getId(), false);
                    }
                }
                if (core.getLoad() <= util.Statics.exitOverLoadLevel && core.isOverLoadLimit()) {
                    SharedMemory.<String, Control>get("MCU").setCoreMode(core.getId(), 0);
                }
                if (core.getLoad() > util.Statics.underUtilizedLevel && core.getLoad() < util.Statics.overloadLevel && !core.isNormalLoad()) {
                    SharedMemory.<String, Control>get("MCU").setCoreMode(core.getId(), 0);
                }
            }

            int publicCores = 0;
            overloadedCores = 0;
            publicCores = 0;

            for (CoreDefinition core : SharedMemory.<String, ArrayList<CoreDefinition>>get("Cores")) {
                if (core.isOverLoadLimit()) {
                    overloadedCores++;
                }
                if (core.isPublicResource()) {
                    publicCores++;
                }
            }

            if ((overloadedCores == publicCores) && !SharedMemory.<String, Boolean>get("OverLoadStatus")) {
                SharedMemory.<String, Boolean>set("OverLoadStatus", true);
            } else if ((overloadedCores != publicCores) && SharedMemory.<String, Boolean>get("OverLoadStatus")) {
                SharedMemory.<String, Boolean>set("OverLoadStatus", false);
            }

        } else if (e.getType().equalsIgnoreCase("OverLoaded")) {
            MicazMote candidate = null;
            for (MicazMote mote : SharedMemory.<String, ArrayList<MicazMote>>get("SensorsList")) {
                if (candidate == null) {
                    candidate = mote;
                    continue;
                }
                try{
                if ((candidate.getCallsSinceLastMonitoring() / candidate.getHighestCritSinceLastMonitoring()) < (mote.getCallsSinceLastMonitoring() / mote.getHighestCritSinceLastMonitoring())) {
                    candidate = mote;
                }}
                catch(ArithmeticException ex){
                    candidate = mote;
                }
            }
            SharedMemory.<String, Control>get("MCU").addToBlackList(candidate);
            Logging.MyLogger.log("Candidate for migration is mote with id " + candidate.getId());
        } else if (e.getType().equalsIgnoreCase("PushCondition")) {
            SharedMemory.<String, Control>get("MCU").changeToPush(Integer.parseInt(e.getDescription()));
        }
    }
}
