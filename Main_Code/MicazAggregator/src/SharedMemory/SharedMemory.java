/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SharedMemory;

import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.Statics;

/**
 *
 * @author Basil Nikolopoulos <nikolopoulosbasil.com>
 */
public class SharedMemory {

    private static HashMap memory;
    private static HashMap <Object,Semaphore>semaphores;



    public SharedMemory() {
        memory = new HashMap();
        semaphores = new HashMap<Object,Semaphore>();//new Semaphore(1,true);
    }

    private static <KEYTYPE,VALUETYPE> VALUETYPE internalGet(KEYTYPE key) throws InterruptedException {
        
        if (memory.containsKey(key)) {
            semaphores.get(key).acquire();
            VALUETYPE returningObject = (VALUETYPE)memory.get(key);
            semaphores.get(key).release();
            return returningObject;
        } else {
            return null;
        }
    }

    private static <KEYTYPE,VALUETYPE> void internalSet(KEYTYPE key,VALUETYPE value) throws InterruptedException {
        if(memory.containsKey(key)){
            semaphores.get(key).acquire();
            memory.put(key, value);
            semaphores.get(key).release();
        }
        else{
            semaphores.put(key, new Semaphore(1,true));
            semaphores.get(key).acquire();
            memory.put(key, value);
            semaphores.get(key).release();
        }
        
    }
    
    private static <KEYTYPE,VALUETYPE> void internalUnset(KEYTYPE key) throws InterruptedException {
        if(memory.containsKey(key)){
            semaphores.get(key).acquire();
            memory.remove(key);
            semaphores.get(key).release();
            semaphores.remove(key);
        }
        else{
            if(semaphores.containsKey(key)){
                semaphores.remove(key);
            }
        }
        
    }
    
    public static <KEYTYPE,VALUETYPE> VALUETYPE get(KEYTYPE key) {
        try {
            VALUETYPE returnObj = internalGet(key);
            return returnObj;
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedMemory.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }

    public static <KEYTYPE,VALUETYPE> void set(KEYTYPE key,VALUETYPE value) {
        try {
            internalSet(key, value);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static <KEYTYPE,VALUETYPE> void unset(KEYTYPE key) {
        try {
            internalUnset(key);
        } catch (InterruptedException ex) {
            Logger.getLogger(SharedMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
