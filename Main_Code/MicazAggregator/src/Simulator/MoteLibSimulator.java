/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulator;

import SensorsCommunicationUnit.Constants;
import ControlUnit.Control;

/**
 *
 * @author billaros
 */
public class MoteLibSimulator {

    Network net;
    

    public MoteLibSimulator(Network net) {
        this.net = net;
    }

    public MoteLibSimulator() {
        System.out.println("NOTICE: MOTELIBSIMULATOR is constructed WITHOUT network, please set it");
    }

    public void setNet(Network net) {
        this.net = net;
    }
    
    public void send(int id, int requestType, int request) throws Exception {
                net.receiveMessageFromAgg(new SimulatedRequest(id, requestType, request));
    }

    public void send(int id, int requestType) throws Exception {
        switch (requestType) {
            case Constants.POLL: {
                if (id == -1) {
                    net.receiveMessageFromAgg(new SimulatedRequest(-1, Constants.POLL));
                } else {
                    throw (new Exception());
                }
            }
            case Constants.SWITCHCHANGE: {
                    net.receiveMessageFromAgg(new SimulatedRequest(id, Constants.SWITCHCHANGE));
            }
            case Constants.SWITCHPOLL: {
                    net.receiveMessageFromAgg(new SimulatedRequest(id, Constants.SWITCHPOLL));
            }
        }
    }
}
