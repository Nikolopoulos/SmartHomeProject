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
    public static void main(String[] args) throws InterruptedException {
        //new GUIServices.Temperature();
        //new GUIServices.FireWatchdog();
       
        //new GUIServices.Whereabouts();
        //start phase 1
        int phase =0;
        phase++;
        System.out.println("Starting phase "+phase);
        DOSAttack ddos = new DOSAttack(1,2);
        Thread.sleep(20000);
        ddos.stop();
        ddos = new DOSAttack(1,50);
        Thread.sleep(1000);
        ddos.stop();
        ddos = new DOSAttack(1,100);
        Thread.sleep(1000);
        ddos.stop();
        ddos = new DOSAttack(1,200);
        Thread.sleep(20000);
        ddos.stop();
        ddos = new DOSAttack(1,100);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,50);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,2);
        Thread.sleep(20000);
        ddos.stop();
        System.out.println("Ending phase "+phase);
        //end phase 1
        
        //start phase 2
        phase++;
        System.out.println("Starting phase "+phase);
        ddos = new DOSAttack(1,50);
        Thread.sleep(1000);
        ddos.stop();
        ddos = new DOSAttack(1,100);
        Thread.sleep(1000);
        ddos.stop();
        ddos = new DOSAttack(1,200);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,300);
        Thread.sleep(20000);
        ddos.stop();
        ddos = new DOSAttack(1,200);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,100);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,50);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,2);
        Thread.sleep(20000);
        ddos.stop();
        System.out.println("Ending phase "+phase);
        //end phase 2
        
        //start phase 3
        phase++;
        System.out.println("Starting phase "+phase);
        ddos = new DOSAttack(1,50);
        Thread.sleep(1000);
        ddos.stop();
        ddos = new DOSAttack(1,100);
        Thread.sleep(1000);
        ddos.stop();
        ddos = new DOSAttack(1,200);
        Thread.sleep(5000);
        ddos.stop();
        ddos = new DOSAttack(1,300);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,450);
        Thread.sleep(20000);
        ddos.stop();
        ddos = new DOSAttack(1,200);
        Thread.sleep(5000);
        ddos.stop();
        ddos = new DOSAttack(1,100);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,50);
        Thread.sleep(10000);
        ddos.stop();
        ddos = new DOSAttack(1,2);
        Thread.sleep(20000);
        ddos.stop();
        System.out.println("Ending phase "+phase);
        //end phase 3
        
    }

}
