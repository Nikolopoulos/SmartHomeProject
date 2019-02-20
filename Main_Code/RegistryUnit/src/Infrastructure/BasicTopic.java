/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Infrastructure;

/**
 *
 * @author billaros
 */
public class BasicTopic {
    String description;
    String machineDescription;
    String name;

    public BasicTopic(String description, String machineDescription, String name) {
        this.description = description;
        this.machineDescription = machineDescription;
        this.name = name;
    }

    public BasicTopic(BasicTopic base) {
        this.description = base.description;
        this.machineDescription = base.machineDescription;
        this.name = base.name;
    }
    
    
}
