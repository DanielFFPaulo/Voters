/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Final;

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
class Block {
    private int blockNumber;
    private long timestamp;
    private String previousHash;
    private String merkleRoot;
    private int nonce;
    private String hash;
    private List<Transaction> transactions;
    
    public Block(int blockNumber, String previousHash, List<Transaction> transactions) {
        this.blockNumber = blockNumber;
        this.timestamp = System.currentTimeMillis();
        this.previousHash = previousHash;
        this.transactions = new ArrayList<>(transactions);
        this.nonce = 0;
        this.merkleRoot = calculateMerkleRoot();
        this.hash = calculateHash();
    }
    
    public String calculateHash() {
        try {
            String data = blockNumber + timestamp + previousHash + merkleRoot + nonce +
                         transactionsToString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
    
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        
        System.out.println("Block mined! Hash: " + hash);
    }
    
    private String transactionsToString() {
        StringBuilder sb = new StringBuilder();
        for (Transaction tx : transactions) {
            sb.append(tx.getTransactionId());
        }
        return sb.toString();
    }
    
    // Getters
    public int getBlockNumber() { return blockNumber; }
    public String getHash() { return hash; }
    public String getPreviousHash() { return previousHash; }
    public List<Transaction> getTransactions() { return new ArrayList<>(transactions); }
    public String getMerkleRoot() { return merkleRoot; }
    
     @Override
    public String toString() {
        String hashPreview = hash.length() >= 8 ? hash.substring(0, 8) + "..." : hash;
        String prevHashPreview = previousHash.length() >= 8 ? 
                                 previousHash.substring(0, 8) + "..." : previousHash;
        return "Block{" +
               "number=" + blockNumber +
               ", hash='" + hashPreview + "', " +
               "previousHash='" + prevHashPreview + "', " +
               "transactions=" + transactions.size() +
               ", time=" + new Date(timestamp) +
               '}';
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}