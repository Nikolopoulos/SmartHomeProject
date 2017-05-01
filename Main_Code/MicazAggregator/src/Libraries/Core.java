/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Libraries;

import ControlUnit.Control;

/**
 *
 * @author billaros
 */
public class Core {

    private final int sequence;

    private Control c;

    public Core(final int sequence, Control c) {
        this.c = c;

        this.sequence = sequence;
        if (sequence > Integer.SIZE) {
            throw new IllegalStateException("Too many cores (" + sequence + ") for integer mask");
        }
    }

    public Control getC() {
        return c;
    }

    public void setC(Control c) {
        this.c = c;
    }

    public int sequence() {
        return sequence;
    }

    public void attachTo() throws Exception {

        final long mask = mask();
        try {
            SharedMemory.SharedMemory.<String,ThreadAffinity>get("Affinity").setCurrentThreadAffinityMask(mask);
        } catch (NullPointerException E) {
            System.out.println("WHOS THAT POKEMON? ITS MASK " + mask);
             System.out.println("WHOS THAT POKEMON? ITS CONTROL " + c);
              System.out.println("WHOS THAT POKEMON? ITS THREAD AFFINITY " + SharedMemory.SharedMemory.<String,ThreadAffinity>get("Affinity"));
               System.out.println("WHOS THAT POKEMON?" );
        }
    }

    public void attach(final Thread thread) throws Exception {
        final long mask = mask();
        //fixme: it does not work for now!
        SharedMemory.SharedMemory.<String,ThreadAffinity>get("Affinity").setThreadAffinityMask(thread.getId(), mask);
    }

    private int mask() {
        return 1 << sequence;
    }

    @Override
    public String toString() {
        return String.format("Core[#%d]", sequence());
    }

}
