/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author billaros
 */
public class UnitConfig {

    private static String uid = "";
    private static boolean debug=false;

    public static String getUid() {
        return uid;
    }

    public static void setUid(String uid) {
        UnitConfig.uid = uid;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        UnitConfig.debug = debug;
    }
}
