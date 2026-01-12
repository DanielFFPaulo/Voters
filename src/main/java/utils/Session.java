/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 *
 * @author Sofia Vedor
 */
public class Session {
    public static class Keys {
        public final String username;
        public final PublicKey publicKey;
        public final PrivateKey privateKey;
        public final SecretKey aesKey;

        public Keys(String username, PublicKey publicKey, PrivateKey privateKey, SecretKey aesKey) {
            this.username = username;
            this.publicKey = publicKey;
            this.privateKey = privateKey;
            this.aesKey = aesKey;
        }
    }

    private static Keys current;

    public static void set(Keys keys) { current = keys; }
    public static Keys get() { return current; }
    public static boolean isLoggedIn() { return current != null; }
    public static void logout() { current = null; }
}
