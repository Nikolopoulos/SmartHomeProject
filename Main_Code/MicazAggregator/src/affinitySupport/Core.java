/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package affinitySupport;

import java.util.logging.Level;
import java.util.logging.Logger;
import util.Control;

/**
 *
 * @author billaros
 */
public class Core {

    private final int sequence;
    private String name;
    private ThreadAffinity threadAffinity;
    

    public Core(final int sequence,ThreadAffinity threadAffinity , String name) {
        
        this.name=name;
        this.threadAffinity=threadAffinity;
        this.sequence = sequence;
        if (sequence > Integer.SIZE) {
            throw new IllegalStateException("Too many cores (" + sequence + ") for integer mask");
        }
    }

    public int sequence() {
        return sequence;
    }

    public void attachTo() throws Exception {

        final long mask = mask();
        try {
            threadAffinity.setCurrentThreadAffinityMask(mask);
        } catch (NullPointerException E) {
            System.out.println("WHOS THAT POKEMON? ITS MASK " + mask);
            System.out.println("WHOS THAT POKEMON? ITS THREAD AFFINITY " + threadAffinity);
            System.out.println("WHOS THAT POKEMON?");
        }
    }

    public void attach(final Thread thread) {
        final long mask = mask();
        try {
            //fixme: it does not work for now!
            threadAffinity.setThreadAffinityMask(thread.getId(), mask);
        } catch (Exception ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int mask() {
        return 1 << sequence;
    }

    @Override
    public String toString() {
        return String.format("Core[#%d]", sequence());
    }

}
