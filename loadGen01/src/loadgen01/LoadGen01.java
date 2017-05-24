/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadgen01;

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
        while (true) {
            Thread daemon = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Random rng = new Random();
                        String url = "http://localhost:8181/sensor/"+(rng.nextInt(2)+1)*2+"/temp?crit=" + (rng.nextInt(3) + 3);
                        //String url = "http://localhost:8181/sensor/"+(rng.nextInt(2)+1)*2+"/temp?crit=" + 4;
                        System.out.println(url);
                        HTTPRequest.sendGet(url);
                            
                    } catch (Exception ex) {
                        ex.printStackTrace();;
                    }
                    
                }
            });
            try {
                Thread.sleep(new Random().nextInt(25));
            } catch (InterruptedException ex) {
                //Logger.getLogger(LoadGen01.class.getName()).log(Level.SEVERE, null, ex);
            }
            daemon.start();

        }
    }

}
