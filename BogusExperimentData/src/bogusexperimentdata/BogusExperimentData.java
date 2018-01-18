/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bogusexperimentdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author billaros
 */
public class BogusExperimentData {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        int second = 0;
        int requestID = 0;
        boolean isPushingSensor1 = false;
        boolean isPushingSensor2 = false;

        int NO_LOAD = 0;
        int LOW_LOAD = 1;
        int MEDIUM_LOAD = 2;
        int HEAVY_LOAD = 3;

        int NO_LOAD_PEAK = 0;
        int LOW_LOAD_PEAK = 40;
        int MEDIUM_LOAD_PEAK = 90;
        int HEAVY_LOAD_PEAK = 130;

        int NO_LOAD_MIN = 0;
        int LOW_LOAD_MIN = 30;
        int MEDIUM_LOAD_MIN = 75;
        int HEAVY_LOAD_MIN = 115;

        int CURRENT_STAGE_MIN = 0;
        int CURRENT_STAGE_MAX = 0;

        int previousRequests = 0;
        int requestsInLowQueue = 0;
        int requestsInHighQueue = 0;
        int requestsReceivedInInterval = 0;
        int requestsReceivedInTotal = 0;
        int requestsCompletedInInterval = 0;
        int requestsCompletedInTotal = 0;
        int highCriticalityRequestsReceivedInInterval = 0;
        int highCriticalityRequestsReceivedInTotal = 0;
        int highCriticalityRequestsCompletedInInterval = 0;
        int highCriticalityRequestsCompletedInTotal = 0;

        int numberOfActiveCores = 0;
        int numberOfOverloadedCores = 0;
        boolean overloaded = false;

        double averageHCRtimeInInterval = 0;
        double averageHCRtimeInTotal = 0;
        double averageResponsetimeInInterval = 0;
        double averageResponsetimeInTotal = 0;

        int currentStage = 0;
        File logfile = new File("logfile" + System.currentTimeMillis() + ".csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(logfile, true));
        bw.write("Request ID;"
                + "Request Criticality (1-5);"
                + "Time Sent in ms since epoch;"
                + "Time Received answer in ms since epoch;"
                + "Total Time in ms");
        bw.newLine();
        //bw.close();
        currentStage++;
        for (int i = 0; i < 20 * 60; i++) {

            second++;
            if (second == 0 || second == 10 || second == 139 || second == 150 || second == 269 || second == 399 || second == 528 || second == 539 || second == 668 || second == 797 || second == 926 || second == 1055 || second == 1184) {
                currentStage++;
            }
            switch (currentStage) {
                case 1: {
                    CURRENT_STAGE_MAX = NO_LOAD_PEAK;
                    CURRENT_STAGE_MIN = NO_LOAD_MIN;
                    break;
                }
                case 2: {
                    CURRENT_STAGE_MAX = LOW_LOAD_PEAK;
                    CURRENT_STAGE_MIN = LOW_LOAD_MIN;
                    break;
                }
                case 3: {
                    CURRENT_STAGE_MAX = NO_LOAD_PEAK;
                    CURRENT_STAGE_MIN = NO_LOAD_MIN;
                    break;
                }
                case 4: {
                    CURRENT_STAGE_MAX = LOW_LOAD_PEAK;
                    CURRENT_STAGE_MIN = LOW_LOAD_MIN;
                    break;
                }
                case 5: {
                    CURRENT_STAGE_MAX = MEDIUM_LOAD_PEAK;
                    CURRENT_STAGE_MIN = MEDIUM_LOAD_MIN;
                    break;
                }
                case 6: {
                    CURRENT_STAGE_MAX = LOW_LOAD_PEAK;
                    CURRENT_STAGE_MIN = LOW_LOAD_MIN;
                    break;
                }
                case 7: {
                    CURRENT_STAGE_MAX = NO_LOAD_PEAK;
                    CURRENT_STAGE_MIN = NO_LOAD_MIN;
                    break;
                }
                case 8: {
                    CURRENT_STAGE_MAX = LOW_LOAD_PEAK;
                    CURRENT_STAGE_MIN = LOW_LOAD_MIN;
                    break;
                }
                case 9: {
                    CURRENT_STAGE_MAX = MEDIUM_LOAD_PEAK;
                    CURRENT_STAGE_MIN = MEDIUM_LOAD_MIN;
                    break;
                }
                case 10: {
                    CURRENT_STAGE_MAX = HEAVY_LOAD_PEAK;
                    CURRENT_STAGE_MIN = HEAVY_LOAD_MIN;
                    break;
                }
                case 11: {
                    CURRENT_STAGE_MAX = MEDIUM_LOAD_PEAK;
                    CURRENT_STAGE_MIN = MEDIUM_LOAD_MIN;
                    break;
                }
                case 12: {
                    CURRENT_STAGE_MAX = LOW_LOAD_PEAK;
                    CURRENT_STAGE_MIN = LOW_LOAD_MIN;
                    break;
                }
                case 13: {
                    CURRENT_STAGE_MAX = NO_LOAD_PEAK;
                    CURRENT_STAGE_MIN = NO_LOAD_MIN;
                    break;
                }

            }

            if (CURRENT_STAGE_MAX == 0) {
                requestsReceivedInInterval = 0;
            } else {
                requestsReceivedInInterval = new Random().nextInt((CURRENT_STAGE_MAX - CURRENT_STAGE_MIN)) + CURRENT_STAGE_MIN;
            }
            System.out.println(currentStage);
            for (int j = 0; j < requestsReceivedInInterval; j++) {
                requestID++;
                int criticalityRand = new Random().nextInt(100);
                int criticality = 0;
                int weight = 0;
                if (criticalityRand < 43) {
                    criticality = 1;
                    weight = 5;
                } else if (criticalityRand > 42 && criticalityRand < 83) {
                    criticality = 2;
                    weight = 4;
                } else if (criticalityRand > 82 && criticalityRand < 89) {
                    criticality = 3;
                    weight = 3;
                } else if (criticalityRand > 88 && criticalityRand < 93) {
                    criticality = 4;
                    weight = 2;
                } else if (criticalityRand > 92) {
                    criticality = 5;
                    weight = 1;
                }
                int randomizer = new Random().nextInt(1000);
                long timeSent = System.currentTimeMillis() + 1000 * second+randomizer;
                long timeReceivedAnswer = System.currentTimeMillis() + 1000 * second + new Random().nextInt(weight * 20) + new Random().nextInt(25)+randomizer;
                long traversalTime = timeReceivedAnswer - timeSent;
                bw.write(requestID + ";"
                        + criticality + ";"
                        + timeSent + ";"
                        + timeReceivedAnswer + ";"
                        + traversalTime);
                bw.newLine();
            }
        }

        bw.close();
    }

}
