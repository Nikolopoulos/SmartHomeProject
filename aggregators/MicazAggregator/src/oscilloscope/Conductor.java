/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oscilloscope;

import affinitySupport.ThreadAffinity;
import util.Control;
import webServer.Server;

/**
 *
 * @author billaros
 */
public class Conductor {
    
    
    
    //Note to people reading this shit
    //you don't want to read this shit
    //Sincerely, Basil Nikolopoulos
    
    public static void main(String args[]) {
        
        Control c = new Control(true);
        Logging.MyLogger.init();
        Server myServer = new Server(c);
        myServer.startServer();
    }
}
