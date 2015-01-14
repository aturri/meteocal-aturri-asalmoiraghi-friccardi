/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.utils;

import it.polimi.meteocal.boundary.RecoverPasswordBean;
import it.polimi.meteocal.entity.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  This class contains some utility like hashing function
 * @author Fabiuz
 */
public class Utility {
     /**
     * This method provide the hashed string of the input string (with SHA-256 algorithm)
     * @param string the string on which will be executed the hashing function
     * @return the string that represents the code
     */
    public static String getHashSHA256(String string){        
        MessageDigest md=null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(RecoverPasswordBean.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        md.update(string.getBytes());
        
        byte[] mdbytes = md.digest();
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mdbytes.length; i++) {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        //System.out.println("Hex format : " + sb.toString());

        return sb.toString();
    }
    
    /**
     * Return the boolean value into a string. If the conversion fails, it return false.
     * The accepted value for true are "true", "True","T","1" and "t"
     * @param string
     * @return the boolean value
     */
    public static Boolean stringToBoolean(String string){
        return "t".equals(string)||"true".equals(string)||"TRUE".equals(string)||"T".equals(string)||"1".equals(string);
    }
}