/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.*;
import java.security.*;

/**
 *
 * @author Sofia Vedor
 */
public class KeyStoreService {

    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int PBKDF2_ITERS = 65536;

    public static void createUser(Path folder, String username, char[] password) throws Exception {
        Files.createDirectories(folder);

        KeyPair kp = CryptoUtils.generateRSAKeyPair();
        SecretKey aes = CryptoUtils.generateAESKey();

        // c) AES cifrada com pública => .sim
        byte[] sim = CryptoUtils.rsaEncryptOaep(kp.getPublic(), aes.getEncoded());

        // d) privada cifrada com password => .priv (salt|iv|ciphertext)
        byte[] salt = CryptoUtils.randomBytes(SALT_LEN);
        SecretKey pwKey = CryptoUtils.deriveAESKeyFromPassword(password, salt, PBKDF2_ITERS);

        CryptoUtils.AesGcmBlob blob = CryptoUtils.encryptAesGcm(pwKey, kp.getPrivate().getEncoded());
        byte[] privFile = CryptoUtils.concat(salt, blob.iv, blob.ciphertext);

        // e) pública => .pub
        byte[] pub = kp.getPublic().getEncoded();

        Files.write(folder.resolve(username + ".pub"), pub);
        Files.write(folder.resolve(username + ".sim"), sim);
        Files.write(folder.resolve(username + ".priv"), privFile);
    }

    public static Session.Keys login(Path folder, String username, char[] password) throws Exception {
        byte[] pubBytes = Files.readAllBytes(folder.resolve(username + ".pub"));
        byte[] simBytes = Files.readAllBytes(folder.resolve(username + ".sim"));
        byte[] privBytes = Files.readAllBytes(folder.resolve(username + ".priv"));

        PublicKey publicKey = CryptoUtils.bytesToPublicKey(pubBytes);

        if (privBytes.length < SALT_LEN + IV_LEN + 16) {
            throw new IOException("Invalid .priv file");
        }

        byte[] salt = CryptoUtils.slice(privBytes, 0, SALT_LEN);
        byte[] iv = CryptoUtils.slice(privBytes, SALT_LEN, SALT_LEN + IV_LEN);
        byte[] ciphertext = CryptoUtils.slice(privBytes, SALT_LEN + IV_LEN, privBytes.length);

        SecretKey pwKey = CryptoUtils.deriveAESKeyFromPassword(password, salt, PBKDF2_ITERS);

        // se password errada => exceção (AEADBadTagException)
        byte[] privateEncoded = CryptoUtils.decryptAesGcm(pwKey, iv, ciphertext);
        PrivateKey privateKey = CryptoUtils.bytesToPrivateKey(privateEncoded);

        // abrir .sim com a privada => AES key
        byte[] aesRaw = CryptoUtils.rsaDecryptOaep(privateKey, simBytes);
        SecretKey aesKey = new SecretKeySpec(aesRaw, "AES");

        Session.Keys keys = new Session.Keys(username, publicKey, privateKey, aesKey);
        Session.set(keys);
        return keys;
    }
}
