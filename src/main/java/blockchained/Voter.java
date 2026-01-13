/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import utils.SecurityUtils;
import utils.Session;

/**
 *
 * @author Acer
 */

public class Voter {
    private String name;
    private KeyPair keyPair;
    
    public Voter(String name) throws NoSuchAlgorithmException, Exception {
        this.name = name;
        this.keyPair = SecurityUtils.generateRSAKeyPair(2048);
    }
    
public Transaction castVote(String vote, String electionId) throws Exception {
    // Use the session key pair for both signing and public key
    Session.Keys sKeys = Session.get();
    return new Transaction(sKeys.publicKey, vote, electionId, sKeys.privateKey);
}

    
    public String getName() { return name; }
    public PublicKey getPublicKey() { return keyPair.getPublic(); }
    
}