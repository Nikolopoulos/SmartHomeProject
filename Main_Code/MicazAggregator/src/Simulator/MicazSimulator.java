/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulator;

import java.util.Random;
import lib.Constants;
import sensorPlatforms.MicazMote;
import util.Statics;

/**
 *
 * @author billaros
 */
public class MicazSimulator {

    private Network net;
    public int id;
    private MicazMote mote;

    public void processRequest(SimulatedRequest req) {
        final String instance;
        final int[] readings = new int[10];
        final int type;
        switch (req.getRequestType()) {
            case Constants.SWITCHCHANGE: {
                if (mote.isSwitchService()) {
                    if (mote.getSwitchState() == 1) {
                        mote.setSwitchState(0);
                    } else {
                        mote.setSwitchState(1);
                    }
                    instance = "SWITCH_ANSWER";
                    type = Constants.READINGANSWER;
                } else {
                    instance = "";
                    type = -1;
                }
                break;
            }
            case Constants.READINGREQUEST: {
                if (req.getReadingType() == Constants.TEMP && mote.isTempService()) {
                    for (int i = 0; i < Constants.NREADINGS; i++) {
                        readings[i] = Statics.ROOM_TEMPERATURE;

                    }
                    type = Constants.READINGANSWER;
                    instance = "READING_ANSWER";
                } else if (req.getReadingType() == Constants.PHOTO && mote.isPhotoService()) {
                    for (int i = 0; i < Constants.NREADINGS; i++) {
                        readings[i] = Statics.PHOTO;

                    }
                    type = Constants.READINGANSWER;
                    instance = "READING_ANSWER";

                } else {
                    instance = "";
                    type =-1;
                }

                break;
            }
            case Constants.POLL: {
                instance = "Poll_Answer";
                type = Constants.ACK;
                break;
            }
            default: {
                type = -1;
                instance = "";

            }
        }
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                SimulatedMessage an = new SimulatedMessage(instance, mote.getId(), mote.getServicesInt(), type, readings, mote.getSwitchState());
                net.receiveMessageFromNode(an);
            }
        });
        t.run();
    }

    public MicazSimulator(Network net) {
        this.net = net;
        this.mote = new MicazMote(new Random().nextInt(), new Random().nextInt() % 7, System.currentTimeMillis());
        this.id = mote.getId();
    }

}
