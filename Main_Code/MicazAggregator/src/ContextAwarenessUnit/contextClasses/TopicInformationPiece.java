/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContextAwarenessUnit.contextClasses;

/**
 *
 * @author billaros
 */
public class TopicInformationPiece {
    String informationType;
    String object;

    public TopicInformationPiece(String informationType, String object) {
        this.informationType = informationType;
        this.object = object;
    }

    public String getInformationType() {
        return informationType;
    }

    public void setInformationType(String informationType) {
        this.informationType = informationType;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }
    
    
}
