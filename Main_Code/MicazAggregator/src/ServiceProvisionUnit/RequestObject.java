/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServiceProvisionUnit;

import java.net.InetAddress;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class RequestObject {
    private String url;
    private int port;
    private String parameters;
    private String service;
    private InetAddress myIP;
    private String method;
    private String response;

    public RequestObject(String url, int port, String parameters, String service, InetAddress myIP, String method) {
        this.url = url;
        this.port = port;
        this.parameters = parameters;
        this.service = service;
        this.myIP = myIP;
        this.method=method;
        response = "";
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public InetAddress getMyIP() {
        return myIP;
    }

    public void setMyIP(InetAddress myIP) {
        this.myIP = myIP;
    }

    @Override
    public String toString() {
        return "RequestObject{" + "url=" + url + ", port=" + port + ", parameters=" + parameters + ", service=" + service + ", myIP=" + myIP + ", method=" + method + ", response=" + response + '}';
    }
    
    
    
}
