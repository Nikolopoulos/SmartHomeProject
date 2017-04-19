/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulator;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import SensorsCommunicationUnit.Constants;
import util.Statics;

/**
 *
 * @author billaros
 */
public class Network {

    public ArrayList<MicazSimulator> nodes;
    public Network myself;
    public int networkThroughput;
    private SimulatedMessaging messaging;
    private Thread networkActivity;
    private Thread requestConsumer;
    private Thread answerConsumer;
    private ArrayList<SimulatedMessage> nodeMessages;
    private ArrayList<SimulatedRequest> requests;

    public void receiveMessageFromAgg(SimulatedRequest msg) {
        requests.add(msg);
    }

    public void receiveMessageFromNode(SimulatedMessage msg) {
        nodeMessages.add(msg);
    }

    public Network(SimulatedMessaging messaging) {
        myself = this;
        this.nodes = new ArrayList<MicazSimulator>();
        this.networkThroughput = 5;
        this.messaging = messaging;
        this.networkActivity = this.createNetActivity();
        this.requestConsumer = this.createConsumer();
        this.answerConsumer = this.createResponder();
        this.nodeMessages = new ArrayList<SimulatedMessage>();
        this.requests = new ArrayList<SimulatedRequest>();

        this.answerConsumer.start();
        this.requestConsumer.start();
        this.networkActivity.start();
    }

    private MicazSimulator findNodeById(int id) {
        for (MicazSimulator m : nodes) {
            if (m.id == id) {
                return m;
            }
        }
        return null;
    }

    private Thread createConsumer() {
        return new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (!requests.isEmpty()) {
                        SimulatedRequest req = requests.remove(0);
                        if (req.getRequestType() != Constants.POLL) {
                            MicazSimulator mica = findNodeById(req.getId());
                            if (mica != null) {
                                mica.processRequest(req);
                            }
                        } else {
                            for (MicazSimulator m : nodes) {
                                m.processRequest(req);
                            }
                        }
                    }
                    try {
                        Thread.sleep((int) ((1 / networkThroughput) * 1000));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private Thread createResponder() {
        return new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (!nodeMessages.isEmpty()) {

                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                if (nodeMessages.size() > 0) {
                                    messaging.messageReceived(nodeMessages.remove(0));
                                }
                            }
                        }).start();

                    }
                    try {
                        Thread.sleep((int) ((1 / networkThroughput) * 1000));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private Thread createNetActivity() {
        return new Thread(new Runnable() {

            @Override
            public void run() {
                Random rng = new Random();
                rng.setSeed(System.currentTimeMillis());
                while (true) {
                    if (rng.nextInt() % Statics.ERROR_PROBABILITY == 0) {
                        if (nodes.size() > 0) {
                            nodes.remove(0);
                        }
                        continue;
                    }
                    if (rng.nextInt() % Statics.TURNOFF_PROBABILITY == 0) {
                        if (nodes.size() > 0) {
                            nodes.remove(0);
                        }
                        continue;
                    }
                    if (rng.nextInt() % Statics.NEWNODE_PROBABILITY == 0) {
                        nodes.add(new MicazSimulator(myself));
                        continue;
                    }

                }
            }
        });

    }
}
