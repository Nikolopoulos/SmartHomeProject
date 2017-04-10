/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kickstart;

import ControlUnit.Control;
import SharedMemory.SharedMemory;

/**
 *
 * @author billaros
 */
public class Conductor {
    
    
    
    //Note to people reading this shit
    //you don't want to read this shit
    //Sincerely, Basil Nikolopoulos
    
    public static void main(String args[]) {
        
        Logging.MyLogger.init();
        
        SharedMemory memory = new SharedMemory();
        memory.<String,SharedMemory>set("SMU",memory);
        Control c = new Control(false);

       
    }
}
