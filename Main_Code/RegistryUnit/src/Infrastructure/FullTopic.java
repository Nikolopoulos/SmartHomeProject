/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Infrastructure;

import java.util.HashSet;

/**
 *
 * @author billaros
 */
public class FullTopic extends BasicTopic {
    int id;
    HashSet<Aggregator> participants;

    public FullTopic(int id, String description, String machineDescription, HashSet<Aggregator> participants, String name) {
        super(description,machineDescription, name);
        this.id = id;
        this.participants = participants;
    }
    
    public FullTopic(int id, String description, String machineDescription, String name) {
        super(description,machineDescription, name);
        this.id = id;
        this.participants = new HashSet<>();
    }
    
    public FullTopic(BasicTopic base, int id){
        super(base);
        this.id = id;
        this.participants = new HashSet<>();
    }
}
