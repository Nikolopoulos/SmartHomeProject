/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MonitoringUnit;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class MonitoredVariable {

    private String variableName;
    private float seconds;
    private float threshold;
    private Thread monitoringAction;

    public MonitoredVariable(String variableName, float seconds, float threshold, Thread action) {
        this.variableName = variableName;
        this.seconds = seconds;
        this.threshold = threshold;
        this.monitoringAction = action;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public float getSeconds() {
        return seconds;
    }

    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public Thread getAction() {
        return monitoringAction;
    }

    public void setAction(Thread action) {
        this.monitoringAction = action;
    }
    
    
}
