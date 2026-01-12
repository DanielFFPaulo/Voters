/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

/**
 *
 * @author Sofia Vedor
 */
public class CryptoUtils {

    public static KeyPair generateRSAKeyPair() throws GeneralSecurityException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    public static SecretKey generateAESKey() throws GeneralSecurityException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        return kg.generateKey();
    }

    public static byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        new SecureRandom().nextBytes(b);
        return b;
    }

    public static SecretKey deriveAESKeyFromPassword(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static class AesGcmBlob {
        public final byte[] iv;
        public final byte[] ciphertext;
        public AesGcmBlob(byte[] iv, byte[] ciphertext) {
            this.iv = iv;
            this.ciphertext = ciphertext;
        }
    }

    public static AesGcmBlob encryptAesGcm(SecretKey key, byte[] plaintext) throws GeneralSecurityException {
        byte[] iv = randomBytes(12);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] ct = cipher.doFinal(plaintext);
        return new AesGcmBlob(iv, ct);
    }

    public static byte[] decryptAesGcm(SecretKey key, byte[] iv, byte[] ciphertext) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return cipher.doFinal(ciphertext);
    }

    public static byte[] rsaEncryptOaep(PublicKey publicKey, byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] rsaDecryptOaep(PrivateKey privateKey, byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    public static byte[] sign(PrivateKey privateKey, byte[] payload) throws GeneralSecurityException {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(privateKey);
        s.update(payload);
        return s.sign();
    }

    public static boolean verify(PublicKey publicKey, byte[] payload, byte[] signature) throws GeneralSecurityException {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initVerify(publicKey);
        s.update(payload);
        return s.verify(signature);
    }

    public static PublicKey bytesToPublicKey(byte[] encoded) throws GeneralSecurityException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    public static PrivateKey bytesToPrivateKey(byte[] encoded) throws GeneralSecurityException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    public static byte[] concat(byte[]... parts) {
        int len = 0;
        for (byte[] p : parts) len += p.length;
        byte[] out = new byte[len];
        int pos = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, pos, p.length);
            pos += p.length;
        }
        return out;
    }

    public static byte[] slice(byte[] a, int from, int to) {
        return Arrays.copyOfRange(a, from, to);
    }
}
