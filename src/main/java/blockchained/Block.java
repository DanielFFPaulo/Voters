/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Acer
 */
public class Block implements Serializable{
    private int blockID;
    private String previousHash;
    private String merkleRoot;
    private String currentHash;
    private int nonce;
    private List<Transaction> transactions;

    public Block(int blockID, String previousHash, List<Transaction> transactions) throws InterruptedException {
        System.out.println("Making blocks");
        this.blockID = blockID;
        System.out.println("Making previous hash");
        this.previousHash = previousHash;
        System.out.println("Making Transacitions");
        this.transactions = new ArrayList<>(transactions);
        System.out.println("Making merkle root");
        this.merkleRoot = calculateMerkleRoot();
        System.out.println("Making nonce");
        this.nonce = Miner.getNonce(previousHash + getMerkleRoot(), 3);
        System.out.println("Making current hash");
        this.currentHash = calculateHash();
        System.out.println("Finished making blocks");
        
    }


    public String calculateHash() {
        try {
            String data = blockID + previousHash + merkleRoot + nonce +transactionsToString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private String calculateMerkleRoot() {
        if (transactions.isEmpty()) {
            return "0";
        }
        
        List<String> hashes = new ArrayList<>();
        for (Transaction tx : transactions) {
            hashes.add(tx.getTransactionId());
        }
        
        // Build merkle tree
        while (hashes.size() > 1) {
            List<String> newHashes = new ArrayList<>();
            for (int i = 0; i < hashes.size(); i += 2) {
                if (i + 1 < hashes.size()) {
                    newHashes.add(hashPair(hashes.get(i), hashes.get(i + 1)));
                } else {
                    newHashes.add(hashes.get(i));
                }
            }
            hashes = newHashes;
        }
        
        return hashes.get(0);
    }
    
    private String hashPair(String a, String b) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((a + b).getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String transactionsToString() {
        StringBuilder sb = new StringBuilder();
        for (Transaction tx : transactions) {
            sb.append(tx.getTransactionId());
        }
        return sb.toString();
    }

    public int getBlockID() {
        return blockID;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    
    
    
         @Override
    public String toString() {
        String hashPreview = currentHash.length() >= 8 ? currentHash.substring(0, 8) + "..." : currentHash;
        String prevHashPreview = previousHash.length() >= 8 ? 
                                 previousHash.substring(0, 8) + "..." : previousHash;
        return "Block{" +
               "number=" + blockID +
               ", hash='" + hashPreview + "', " +
               "previousHash='" + prevHashPreview + "', " +
               "transactions=" + transactions.size() +
               '}';
    }
    
    public boolean isValid() {
        return currentHash.equals(calculateHash());
    }
    
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
