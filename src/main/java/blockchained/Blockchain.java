/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import utils.Session;

/**
 *
 * @author Acer
 */
public class Blockchain {
    private List<Block> chain;
    private List<Transaction> pendingTransactions;
    private int difficulty;
    private Map<String, Set<String>> voterRegistry; // publicKey -> set of electionIds voted in
    private Map<String, Election> elections;
    
    public Blockchain(int difficulty) throws InterruptedException {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.difficulty = difficulty;
        this.voterRegistry = new HashMap<>();
        this.elections = new HashMap<>();
        
        
        chain.add(createGenesisBlock());
    }
    
    private Block createGenesisBlock() throws InterruptedException {
        return new Block(0, "0", new ArrayList<>());
        
    }
    
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }
    
    
        public void addTransaction(Transaction transaction, PublicKey publicKey) throws Exception {

        // 1) Garantir que o voto pertence ao utilizador autenticado
        // Obtém as chaves associadas à sessão atual
        Session.Keys sKeys = Session.get();

        // Converte a chave pública da sessão para Base64
        // para poder comparar com a chave presente na transação
        String sessionPub = Base64.getEncoder()
                .encodeToString(sKeys.publicKey.getEncoded());

        // Verifica se a chave pública do voto corresponde
        // à chave pública do utilizador autenticado
        if (!transaction.getPublicVoterKey().equals(sessionPub)) {
            throw new IllegalStateException(
                    "A chave do voto não corresponde ao utilizador autenticado."
            );
        }

        // 2) Validação da transação
        // Confirma assinatura digital, integridade dos dados
        // e outros critérios definidos em isTransactionValid
        if (!isTransactionValid(transaction, publicKey)) {
            throw new IllegalArgumentException("Transação inválida");
        }

        // 3) Prevenção de voto duplicado
        // Verifica se este eleitor já votou nesta eleição
        if (hasVoterVoted(
                transaction.getPublicVoterKey(),
                transaction.getElectionId())) {
            throw new IllegalArgumentException(
                    "O eleitor já votou nesta eleição."
            );
        }

        // 4) Adiciona a transação ao pool de transações pendentes
        // Estas transações poderão depois ser incluídas num bloco
        pendingTransactions.add(transaction);

        // Log para debug/auditoria
        System.out.println(
                "Transação adicionada à lista de transações pendentes.: " + transaction
        );
    }
    
    
    public void minePendingTransactions() throws InterruptedException {
        if (pendingTransactions.isEmpty()) {
            System.out.println("No transactions to mine");
            return;
        }
        
        Block block = new Block(chain.size(), getLatestBlock().getCurrentHash(), pendingTransactions);
        
        chain.add(block);
        
        // Update voter registry
        for (Transaction tx : pendingTransactions) {
            voterRegistry.computeIfAbsent(tx.getPublicVoterKey(), k -> new HashSet<>())
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
            if (!currentBlock.getCurrentHash().equals(currentBlock.calculateHash())) {
                System.out.println("Block " + i + " hash is invalid");
                return false;
            }
            
            // Check if blocks are properly linked
            if (!currentBlock.getPreviousHash().equals(previousBlock.getCurrentHash())) {
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
    
    public int getHeight(){
        return chain.size();
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
