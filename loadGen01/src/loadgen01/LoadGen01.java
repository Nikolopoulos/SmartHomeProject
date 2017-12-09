/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadgen01;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
    public static void main(String[] args) throws InterruptedException, Exception {
        //new GUIServices.Temperature();
        //new GUIServices.FireWatchdog();

        //new GUIServices.Whereabouts();
        //DOSAttack ddos = new DOSAttack(1, 200);
        //Thread.sleep(20000);
        //ddos.stop();
        //System.out.println("Ending phase " + phase);
        String fileName = "clientOutput.csv";
        initFile(fileName);

        long start, receive, total;
        int crit = 0;
        int id =0;
        long diff = 0;
        
        //start
        id++;
        diff = 0;
        crit = 1;
        start = System.currentTimeMillis();
        String url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 1;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 1;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request

        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 1;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 4;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 1;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 4;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 4;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 5;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //Start request
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 5;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 5;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 5;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 4;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 4;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 1;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 5;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
        
        //start
        id++;
        diff = System.currentTimeMillis() - receive;
        crit = 4;
        start = System.currentTimeMillis();
        url = "http://192.168.2.10:8181/sensor/2/temp?crit="+crit;
        HTTPRequest.sendGet(url);
        receive = System.currentTimeMillis();
        total = receive - start;
        logToFile(fileName,id,crit,diff,start,receive,total);
        //prime unit with data from sensor 2
        //this data will remain for 10 sec in cache.
        Thread.sleep(500);
        //End Request
    }

    public static void initFile(String name) {
        try {
            PrintWriter pw = new PrintWriter(new File(name));
            pw.write("");
            pw.close();
            BufferedWriter bw = null;
            bw = new BufferedWriter(new FileWriter(new File(name), true));
            bw.write("Request ID;"
                    + "Request Criticality (1-5);"
                    + "Time since last request in ms;"
                    + "Time Sent in ms since epoch;"
                    + "Time Received anaswer in ms since epoch;"
                    + "Total Time in ms");
            bw.newLine();
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(LoadGen01.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void logToFile(String name,int id,int crit,long diff, long sent, long received, long total) {
        try {
            BufferedWriter bw = null;
            bw = new BufferedWriter(new FileWriter(new File(name), true));
            bw.write(id+";"
                    + crit+";"
                    + diff +";"
                    + sent + ";"
                    + received +";"
                    + total);
            bw.newLine();
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(LoadGen01.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
