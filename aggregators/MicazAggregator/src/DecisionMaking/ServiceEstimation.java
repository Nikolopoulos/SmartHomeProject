/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DecisionMaking;

import Logging.MyLogger;

/**
 *
 * @author billaros
 */
public class ServiceEstimation {
    private String serviceArguements;
    private float clEstimation=0;
    private int Domain=0;
    private int QoSArguement=0;

    public ServiceEstimation(String serviceArguements) {
        this.serviceArguements = serviceArguements;
        computeCL();
    }

    public String getServiceArguements() {
        return serviceArguements;
    }

    public void setServiceArguements(String serviceArguements) {
        this.serviceArguements = serviceArguements;
    }

    public float getClEstimation() {
        return clEstimation;
    }

    public void setClEstimation(float clEstimation) {
        this.clEstimation = clEstimation;
    }

    public int getDomain() {
        return Domain;
    }

    public void setDomain(int Domain) {
        this.Domain = Domain;
    }

    public int getQoSArguement() {
        return QoSArguement;
    }

    public void setQoSArguement(int QoSArguement) {
        this.QoSArguement = QoSArguement;
    }
    
    private void computeCL(){
        String[] parametersSplit = serviceArguements.split("\\?");
        if(parametersSplit.length<2){
            clEstimation = 0;
            System.out.println("parametersSplit <2 = "+parametersSplit.length);
            for(String s : parametersSplit){
                //MyLogger.log(s);
            }
        }
        else{
            for(String s: parametersSplit)
                System.out.println("parametersSplit !<2 = "+s);
            
            String[] parameters = parametersSplit[1].split("&");
            
            for(String parameter : parameters){
                if(parameter.startsWith("qos")||parameter.startsWith("QOS")||parameter.startsWith("QoS")&&parameter.charAt(3)=='='){
                    String value = parameter.split("=")[1];
                    QoSArguement = Integer.parseInt(value);
                }
                if(parameter.startsWith("domain")&&parameter.charAt(6)=='='){
                    String value = parameter.split("=")[1];
                    Domain = Integer.parseInt(value);
                }
            }
            clEstimation = (QoSArguement / 10.0f) * Domain;
            System.out.println("CLESTIM= = "+clEstimation);
        }
    }
}
