/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServiceProvisionUnit;

import java.net.Socket;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class ResponseObject {

    private Socket server;    
    private String reply;

    public Socket getServer() {
        return server;
    }

    public void setServer(Socket server) {
        this.server = server;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public ResponseObject(Socket server) {
        this.server = server;
    }
    
}
