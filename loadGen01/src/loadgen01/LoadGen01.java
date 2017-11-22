/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadgen01;

import Services.DOSAttack;
import Services.Temperature;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author billaros
 */
public class LoadGen01 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //new GUIServices.Temperature();
        //new GUIServices.FireWatchdog();
       
        //new GUIServices.Whereabouts();
        new DOSAttack(1);
    }

}
