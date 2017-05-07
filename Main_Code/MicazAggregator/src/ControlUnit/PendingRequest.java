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
    private long timeOut = System.currentTimeMillis() + 3000;
    private boolean complete = false;
    private RequestExecutionThread ret;

    public RequestExecutionThread getRet() {
        return ret;
    }

    public void setRet(RequestExecutionThread ret) {
        this.ret = ret;
    }

    
    public long getTimeOut() {
        return timeOut;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    
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
        //System.out.println("Got my reply set from control unit and the result is "+reply);
        if(this.ret!=null){
            if(ret.getWhatCore()!=null){
                ret.getWhatCore().remove();
            }
        }
        request.setResponse(reply);
    }
}
