/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Final;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

/**
 *
 * @author Acer
 */
class Election {
    private String electionId;
    private String title;
    private long startTime;
    private long endTime;
    private boolean active;
    
    public Election(String electionId, String title) {
        this.electionId = electionId;
        this.title = title;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (24 * 60 * 60 * 1000); // 24 hours
        this.active = true;
    }
    
    public boolean isActive() {
        long now = System.currentTimeMillis();
        return active && now >= startTime && now <= endTime;
    }
    
    public void close() {
        this.active = false;
    }
    
    public String getElectionId() { return electionId; }
    public String getTitle() { return title; }
}

// ============================================
// PART 5: Voter Class (Key Management)
// ============================================

class Voter {
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