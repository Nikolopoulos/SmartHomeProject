/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Services;

import java.util.Random;
import loadgen01.HTTPRequest;

/**
 *
 * @author billaros
 */
public class DOSAttack {

    public DOSAttack(int mode) {
        while (true) {
            Thread daemon = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Random rng = new Random();
                        String url = "";
                        if(mode == 1){
                        url = "http://localhost:8181/sensor/" + (rng.nextInt(2) + 1) * 2 + "/temp?crit=" + (rng.nextInt(3) + 3);
                        }
                        else{
                            url = "http://localhost:8181/sensor/"+(rng.nextInt(2)+1)*2+"/temp?crit=" + 4;
                        }
                        System.out.println(url);
                        HTTPRequest.sendGet(url);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            });
            try {
                Thread.sleep(new Random().nextInt(50));
            } catch (InterruptedException ex) {
                //Logger.getLogger(LoadGen01.class.getName()).log(Level.SEVERE, null, ex);
            }
            daemon.start();
        }
    }
}
