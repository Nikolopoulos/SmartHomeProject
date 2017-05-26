/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Services;

import loadgen01.HTTPRequest;

/**
 *
 * @author billaros
 */
public class Temperature {

    static float temp = 0;
    static float wantedTemp = 29;

    public Temperature() {
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String url = "http://localhost:8181/sensor/2/temp?crit=2";

                        System.out.println(url);
                        String resp = HTTPRequest.sendGet(url);

                        int start = "{\"sensor\":{\"ID\":\"2\", \"temp\":\"".length();
                        resp = resp.substring(start, start + 4);
                        if (Double.parseDouble(resp) > temp) {
                            turnOn();
                        }
                        if (Double.parseDouble(resp) < temp + 2) {
                            turnOff();
                        }

                    } catch (Exception ex) {

                    }
                }

            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //Logger.getLogger(LoadGen01.class.getName()).log(Level.SEVERE, null, ex);
        }
        daemon.start();

    }

    private void turnOn() throws Exception {
        String url = "http://localhost:8181/sensor/4/switch?crit=4";

        System.out.println(url);
        HTTPRequest.sendGet(url);
    }

    private void turnOff() throws Exception {
        String url = "http://localhost:8181/sensor/4/switch?crit=4";

        System.out.println(url);
        HTTPRequest.sendGet(url);
    }
}
