/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Final;

/**
 *
 * @author Acer
 */
import java.security.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.spec.*;
import javax.crypto.*;

class Transaction {
    private String transactionId;
    private String voterPublicKey;
    private String encryptedVote;
    private String electionId;
    private long timestamp;
    private byte[] signature;
    private int nonce;
    
    public Transaction(String voterPublicKey, String vote, String electionId, 
                       PrivateKey privateKey) throws Exception {
        this.voterPublicKey = voterPublicKey;
        this.electionId = electionId;
        this.timestamp = System.currentTimeMillis();
        this.nonce = new Random().nextInt(1000000);
        
        // Encrypt the vote
        this.encryptedVote = encryptVote(vote);
        
        // Generate transaction ID
        this.transactionId = calculateHash();
        
        // Sign the transaction
        this.signature = signTransaction(privateKey);
    }
    
    private String encryptVote(String vote) {
        // Simple encoding for demo - in production use real encryption
        return Base64.getEncoder().encodeToString(vote.getBytes());
    }
    
    public String calculateHash() {
        try {
            String data = voterPublicKey + encryptedVote + electionId + 
                         timestamp + nonce;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    private byte[] signTransaction(PrivateKey privateKey) throws Exception {
        String data = transactionId + encryptedVote + electionId;
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data.getBytes());
        return sig.sign();
    }
    
    public boolean verifySignature(PublicKey publicKey) throws Exception {
        String data = transactionId + encryptedVote + electionId;
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        return sig.verify(signature);
    }
    
    // Getters
    public String getTransactionId() { return transactionId; }
    public String getVoterPublicKey() { return voterPublicKey; }
    public String getEncryptedVote() { return encryptedVote; }
    public String getElectionId() { return electionId; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "Transaction{" +
               "id='" + transactionId.substring(0, 8) + "...', " +
               "voter='" + voterPublicKey.substring(0, 8) + "...', " +
               "election='" + electionId + "', " +
               "time=" + new Date(timestamp) +
               '}';
    }
    
    // Helper method to convert bytes to hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
