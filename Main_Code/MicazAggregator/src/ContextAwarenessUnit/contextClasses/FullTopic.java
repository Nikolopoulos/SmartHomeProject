/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContextAwarenessUnit.contextClasses;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author billaros
 */
public class FullTopic extends BasicTopic {
    int id;
    ArrayList<Aggregator> participants;

    public FullTopic(int id, String description, String machineDescription, ArrayList<Aggregator> participants, String name) {
        super(description,machineDescription, name);
        this.id = id;
        this.participants = participants;
    }
    
    public FullTopic(int id, String description, String machineDescription, String name) {
        super(description,machineDescription, name);
        this.id = id;
        this.participants = new ArrayList<>();
    }
    
    public FullTopic(BasicTopic base, int id){
        super(base);
        this.id = id;
        this.participants = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Aggregator> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Aggregator> participants) {
        this.participants = participants;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMachineDescription() {
        return machineDescription;
    }

    public void setMachineDescription(String machineDescription) {
        this.machineDescription = machineDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
}
