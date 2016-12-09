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
public class SimulatedMessage {
    private String instance;
    private int id;
    private int services;
    private int type;
    private int[] readings;
    private int state;

    public SimulatedMessage(String instance, int id, int services, int type, int[] readings, int state) {
        this.instance = instance;
        this.id = id;
        this.services = services;
        this.type = type;
        this.readings = readings;
        this.state = state;
    }
    
    
    
    public String instanceOf(){
        return instance;
    }
    
    public int get_id(){
        return this.id;
    }
    
    public int get_services(){
        return this.services;
    }
    
    public int get_type(){
        return this.type;
    }
    
    public int[] get_readings(){
        return this.readings;
    }
    
    public int get_state(){
        return this.state;
    }
}
