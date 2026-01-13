/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

import java.security.MessageDigest;
import java.util.Base64;

/**
 *
 * @author Acer
 */
public class Hash {
    
    
    public static byte[] calculateHash(byte[] data)
            throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(data);
        return md.digest();
    }
    
    public static String calculateHash(String data )
            throws Exception {
        return Base64.getEncoder().encodeToString(
                calculateHash(data.getBytes())
        );
    }
}
