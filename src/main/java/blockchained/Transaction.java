/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;

/**
 *
 * @author Acer
 */
public class Transaction {
    private String transactionId;
    private  String publicVoterKey;
    private  String encryptedVote;
    private String electionId;
    private long timestamp;
    private byte[] signature;
    private int nonce;

    public Transaction(String voterPublicKey, String vote, String electionId, PrivateKey  privateKey) throws Exception {
        this.publicVoterKey = voterPublicKey;
        this.electionId = electionId;
        this.timestamp = System.currentTimeMillis();
        this.nonce = Miner.getNonce(timestamp+publicVoterKey +encryptedVote +electionId, 3);
        
        this.encryptedVote = encryptVote(vote);
        this.transactionId = calculateHash();
        this.signature = signTransaction(privateKey);
    }
    
    
    public String encryptVote(String vote){
        return Base64.getEncoder().encodeToString(vote.getBytes());
    }

    private byte[] signTransaction(PrivateKey privateKey) throws Exception {
        String data = timestamp+publicVoterKey +encryptedVote +electionId;
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data.getBytes());
        return sig.sign();
    }

    
    public boolean verifySignature(PublicKey publicKey)throws Exception{
        String data = timestamp+publicVoterKey +encryptedVote +electionId;
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        return sig.verify(signature);
    }
    
    
    private String calculateHash() {
        try {
            String data = timestamp+publicVoterKey +encryptedVote +electionId+nonce;
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getPublicVoterKey() {
        return publicVoterKey;
    }

    public String getEncryptedVote() {
        return encryptedVote;
    }

    public String getElectionId() {
        return electionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
                return "Transaction{" +
               "id='" + transactionId.substring(0, 8) + "...', " +
               "voter='" + publicVoterKey.substring(0, 8) + "...', " +
               "election='" + electionId + "', " +
               "time=" + new Date(timestamp) +
               '}';
    }
    
    
    
}
