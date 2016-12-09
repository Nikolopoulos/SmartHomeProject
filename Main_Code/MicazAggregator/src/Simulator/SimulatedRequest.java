/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Simulator;

/**
 *
 * @author billaros
 */
public class SimulatedRequest {
    private int id;
    private int requestType;
    private int readingType;

    public SimulatedRequest(int id, int requestType, int readingType) {
        this.id = id;
        this.requestType = requestType;
        this.readingType = readingType;
    }
    public SimulatedRequest(int id, int requestType) {
        this.id = id;
        this.requestType = requestType;
        this.readingType = -1;
    }

    public int getId() {
        return id;
    }

    public int getRequestType() {
        return requestType;
    }

    public int getReadingType() {
        return readingType;
    }
    
    
}
