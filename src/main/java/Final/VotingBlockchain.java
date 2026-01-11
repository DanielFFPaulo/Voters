/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Final;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Acer
 */
class VotingBlockchain {
    private List<Block> chain;
    private List<Transaction> pendingTransactions;
    private int difficulty;
    private Map<String, Set<String>> voterRegistry; // publicKey -> set of electionIds voted in
    private Map<String, Election> elections;
    
    public VotingBlockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.difficulty = difficulty;
        this.voterRegistry = new HashMap<>();
        this.elections = new HashMap<>();
        
        // Create genesis block
        chain.add(createGenesisBlock());
    }
    
    private Block createGenesisBlock() {
        return new Block(0, "0", new ArrayList<>());
    }
    
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }
    
    public void addTransaction(Transaction transaction, PublicKey publicKey) throws Exception {
        // Validate transaction
        if (!isTransactionValid(transaction, publicKey)) {
            throw new IllegalArgumentException("Invalid transaction");
        }
        
        // Check for double voting
        if (hasVoterVoted(transaction.getVoterPublicKey(), transaction.getElectionId())) {
            throw new IllegalArgumentException("Voter has already voted in this election");
        }
        
        // Add to pending pool
        pendingTransactions.add(transaction);
        System.out.println("Transaction added to pending pool: " + transaction);
    }
    
    public void minePendingTransactions() {
        if (pendingTransactions.isEmpty()) {
            System.out.println("No transactions to mine");
            return;
        }
        
        Block block = new Block(chain.size(), getLatestBlock().getHash(), pendingTransactions);
        block.mineBlock(difficulty);
        
        chain.add(block);
        
        // Update voter registry
        for (Transaction tx : pendingTransactions) {
            voterRegistry.computeIfAbsent(tx.getVoterPublicKey(), k -> new HashSet<>())
                        .add(tx.getElectionId());
        }
        
        pendingTransactions = new ArrayList<>();
    }
    
    private boolean isTransactionValid(Transaction transaction, PublicKey publicKey) 
            throws Exception {
        // Verify signature
        if (!transaction.verifySignature(publicKey)) {
            System.out.println("Invalid signature");
            return false;
        }
        
        // Check if election exists and is active
        Election election = elections.get(transaction.getElectionId());
        if (election == null || !election.isActive()) {
            System.out.println("Election not active");
            return false;
        }
        
        return true;
    }
    
    private boolean hasVoterVoted(String voterPublicKey, String electionId) {
        Set<String> votedElections = voterRegistry.get(voterPublicKey);
        return votedElections != null && votedElections.contains(electionId);
    }
    
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);
            
            // Check if current block hash is valid
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Block " + i + " hash is invalid");
                return false;
            }
            
            // Check if blocks are properly linked
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                System.out.println("Block " + i + " is not properly linked");
                return false;
            }
        }
        return true;
    }
    
    public Map<String, Integer> tallyVotes(String electionId) {
        Map<String, Integer> results = new HashMap<>();
        
        for (Block block : chain) {
            for (Transaction tx : block.getTransactions()) {
                if (tx.getElectionId().equals(electionId)) {
                    // Decrypt vote (simplified for demo)
                    String vote = decryptVote(tx.getEncryptedVote());
                    results.put(vote, results.getOrDefault(vote, 0) + 1);
                }
            }
        }
        
        return results;
    }
    
    private String decryptVote(String encryptedVote) {
        // Simple decoding for demo - in production use real decryption
        return new String(Base64.getDecoder().decode(encryptedVote));
    }
    
    public void createElection(String electionId, String title) {
        elections.put(electionId, new Election(electionId, title));
        System.out.println("Election created: " + title);
    }
    
    public void printBlockchain() {
        System.out.println("\n=== BLOCKCHAIN ===");
        for (Block block : chain) {
            System.out.println(block);
            for (Transaction tx : block.getTransactions()) {
                System.out.println("  └─ " + tx);
            }
        }
        System.out.println("==================\n");
    }
    
    public int getPendingTransactionCount() {
        return pendingTransactions.size();
    }
}