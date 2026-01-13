/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

/**
 *
 * @author Acer
 */

public class Voter {
    private String name;
    private KeyPair keyPair;
    private String publicKeyString;
    
    public Voter(String name) throws NoSuchAlgorithmException {
        this.name = name;
        this.keyPair = generateKeyPair();
        this.publicKeyString = Base64.getEncoder()
                                     .encodeToString(keyPair.getPublic().getEncoded());
    }
    
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }
    
    public Transaction castVote(String vote, String electionId) throws Exception {
        return new Transaction(publicKeyString, vote, electionId, keyPair.getPrivate());
    }
    
    public String getName() { return name; }
    public PublicKey getPublicKey() { return keyPair.getPublic(); }
    public String getPublicKeyString() { return publicKeyString; }
}