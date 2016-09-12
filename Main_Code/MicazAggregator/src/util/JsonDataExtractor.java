/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import org.json.JSONObject;

/**
 *
 * @author billaros
 */
public class JsonDataExtractor {
    public static boolean isSuccess(JSONObject obj){
        return obj.has("result")?
               obj.getString("result").equals("success"):
               false;
    }
    public static String getUID(JSONObject obj){
        return obj.has("uid")?
               obj.getString("uid"):
               "-1";
    }
}
