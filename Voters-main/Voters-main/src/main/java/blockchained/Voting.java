/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package blockchained;

import blockchained.Blockchain;
import blockchained.Transaction;
import blockchained.Voter;
import java.util.Map;

/**
 *
 * @author Acer
 */
public class Voting {

        public static void main(String[] args) {
        try {
            System.out.println("🗳️  BLOCKCHAIN VOTING SYSTEM DEMO 🗳️\n");
            
            // Create blockchain with difficulty 2
            Blockchain blockchain = new Blockchain(2);
            System.out.println("creating election");
            // Create an election
            blockchain.createElection("election-001", "Class President 2024");
            System.out.println("creating users");
            // Create voters
            Voter alice = new Voter("Alice");
            Voter bob = new Voter("Bob");
            Voter charlie = new Voter("Charlie");
            Voter diana = new Voter("Diana");
            
            System.out.println("Voters registered:");
            System.out.println("  - " + alice.getName());
            System.out.println("  - " + bob.getName());
            System.out.println("  - " + charlie.getName());
            System.out.println("  - " + diana.getName());
            System.out.println();
            
            // Cast votes
            System.out.println("Casting votes...\n");
            
            Transaction vote1 = alice.castVote("Candidate A", "election-001");
            blockchain.addTransaction(vote1, alice.getPublicKey());
            
            Transaction vote2 = bob.castVote("Candidate B", "election-001");
            blockchain.addTransaction(vote2, bob.getPublicKey());
            
            Transaction vote3 = charlie.castVote("Candidate A", "election-001");
            blockchain.addTransaction(vote3, charlie.getPublicKey());
            
            Transaction vote4 = diana.castVote("Candidate B", "election-001");
            blockchain.addTransaction(vote4, diana.getPublicKey());
            
            System.out.println("Pending transactions: " + 
                             blockchain.getPendingTransactionCount());
            System.out.println();
            
            // Mine block
            System.out.println("Mining block...\n");
            blockchain.minePendingTransactions();
            
            // Try double voting (should fail)
            System.out.println("\nAttempting double vote by Alice...");
            try {
                Transaction doubleVote = alice.castVote("Candidate B", "election-001");
                blockchain.addTransaction(doubleVote, alice.getPublicKey());
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Double vote prevented: " + e.getMessage());
            }
            
            // Verify blockchain
            System.out.println("\nVerifying blockchain integrity...");
            boolean isValid = blockchain.isChainValid();
            System.out.println("Blockchain is " + (isValid ? "✅ VALID" : "❌ INVALID"));
            
            // Display blockchain
            blockchain.printBlockchain();
            
            // Tally votes
            System.out.println("📊 ELECTION RESULTS:");
            Map<String, Integer> results = blockchain.tallyVotes("election-001");
            for (Map.Entry<String, Integer> entry : results.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " votes");
            }
            
            // Demonstrate tampering detection
            System.out.println("\n🔨 Simulating tampering attempt...");
            System.out.println("(This would require accessing internal chain structure)");
            System.out.println("After tampering, isChainValid() would return false");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
