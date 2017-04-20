/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ControlUnit;

import Libraries.Core;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class CoreDefinition {
    Core core;
    Boolean running;
    int load;
    int id;
    boolean publicResource;

    public CoreDefinition(Core core, Boolean running, int load, int id, boolean publicResource) {
        this.core = core;
        this.running = running;
        this.load = load;
        this.id = id;
        this.publicResource = publicResource;
    }

    public boolean isPublicResource() {
        return publicResource;
    }

    public void setPublicResource(boolean publicResource) {
        this.publicResource = publicResource;
    }
    
    public void attachTo(RequestExecutionThread req){
        req.setWhatCore(this);
        this.load++;
    }

    public Core getCore() {
        return core;
    }

    public void setCore(Core core) {
        this.core = core;
    }

    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    
}
