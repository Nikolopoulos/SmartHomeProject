/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ControlUnit;

import ServiceProvisionUnit.DoComms;
/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class PendingRequest {
    private DoComms request;
    private int id;

    public PendingRequest(DoComms request, int id) {
        this.request = request;
        this.id = id;
    }

    public DoComms getRequest() {
        return request;
    }

    public void setRequest(DoComms request) {
        this.request = request;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void setReply(String reply){
        System.out.println("Got my reply set from control unit and the result is "+reply);
        request.setResponse(reply);
    }
}
