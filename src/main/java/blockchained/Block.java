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
 * Representa um bloco numa blockchain
 * Cada bloco contém um conjunto de transações, um hash do bloco anterior,
 * uma Merkle root das transações, e um nonce obtido através de mineração
 * 
 * @author Acer
 */
public class Block implements Serializable {
    private int blockID; // Identificador único do bloco na cadeia
    private String previousHash; // Hash do bloco anterior (liga os blocos)
    private String merkleRoot; // Raiz da árvore de Merkle das transações
    private String currentHash; // Hash atual deste bloco
    private int nonce; // Número usado uma vez (proof-of-work)
    private List<Transaction> transactions; // Lista de transações neste bloco

    /**
     * Construtor que cria um novo bloco e calcula todos os seus campos
     * O bloco é automaticamente minerado durante a construção
     * 
     * @param blockID Identificador do bloco
     * @param previousHash Hash do bloco anterior na cadeia
     * @param transactions Lista de transações a incluir no bloco
     * @throws InterruptedException Se o processo de mineração for interrompido
     */
    public Block(int blockID, String previousHash, List<Transaction> transactions) throws InterruptedException {
        System.out.println("Making blocks");
        this.blockID = blockID;
        
        System.out.println("Making previous hash");
        this.previousHash = previousHash;
        
        System.out.println("Making Transacitions");
        // Cria uma cópia da lista para evitar modificações externas
        this.transactions = new ArrayList<>(transactions);
        
        System.out.println("Making merkle root");
        // Calcula a Merkle root (resumo criptográfico de todas as transações)
        this.merkleRoot = calculateMerkleRoot();
        
        System.out.println("Making nonce");
        // Minera o bloco para encontrar um nonce válido (proof-of-work)
        this.nonce = Miner.getNonce(previousHash + getMerkleRoot(), 3);
        
        System.out.println("Making current hash");
        // Calcula o hash final do bloco
        this.currentHash = calculateHash();
        
        System.out.println("Finished making blocks");
    }

    /**
     * Calcula o hash SHA-256 do bloco
     * Combina o ID do bloco, hash anterior, merkle root, nonce e transações
     * 
     * @return Hash do bloco em formato hexadecimal
     */
    public String calculateHash() {
        try {
            // Concatena todos os dados do bloco
            String data = blockID + previousHash + merkleRoot + nonce + transactionsToString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Calcula a raiz da árvore de Merkle das transações
     * A Merkle root permite verificar eficientemente se uma transação
     * está incluída no bloco sem ter que verificar todas as transações
     * 
     * @return Merkle root em formato hexadecimal
     */
    private String calculateMerkleRoot() {
        // Se não houver transações, retorna "0"
        if (transactions.isEmpty()) {
            return "0";
        }
        
        // Começa com os hashes de todas as transações
        List<String> hashes = new ArrayList<>();
        for (Transaction tx : transactions) {
            hashes.add(tx.getTransactionId());
        }
        
        // Constrói a árvore de Merkle combinando pares de hashes
        // até restar apenas um hash (a raiz)
        while (hashes.size() > 1) {
            List<String> newHashes = new ArrayList<>();
            // Percorre os hashes em pares
            for (int i = 0; i < hashes.size(); i += 2) {
                if (i + 1 < hashes.size()) {
                    // Combina dois hashes adjacentes
                    newHashes.add(hashPair(hashes.get(i), hashes.get(i + 1)));
                } else {
                    // Se houver número ímpar, o último hash sobe sozinho
                    newHashes.add(hashes.get(i));
                }
            }
            hashes = newHashes;
        }
        
        return hashes.get(0);
    }
    
    /**
     * Calcula o hash de um par de strings concatenadas
     * Usado na construção da árvore de Merkle
     * 
     * @param a Primeira string
     * @param b Segunda string
     * @return Hash SHA-256 da concatenação em formato hexadecimal
     */
    private String hashPair(String a, String b) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((a + b).getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Converte todas as transações numa string concatenada
     * 
     * @return String com todos os IDs de transações concatenados
     */
    private String transactionsToString() {
        StringBuilder sb = new StringBuilder();
        for (Transaction tx : transactions) {
            sb.append(tx.getTransactionId());
        }
        return sb.toString();
    }

    /**
     * Obtém o ID do bloco
     * 
     * @return ID do bloco
     */
    public int getBlockID() {
        return blockID;
    }

    /**
     * Obtém o hash do bloco anterior
     * 
     * @return Hash do bloco anterior
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /**
     * Obtém a raiz da árvore de Merkle
     * 
     * @return Merkle root
     */
    public String getMerkleRoot() {
        return merkleRoot;
    }

    /**
     * Obtém o hash atual do bloco
     * 
     * @return Hash do bloco
     */
    public String getCurrentHash() {
        return currentHash;
    }

    /**
     * Obtém a lista de transações do bloco
     * 
     * @return Lista de transações
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    /**
     * Representação em string do bloco com informação resumida
     * Mostra apenas os primeiros 8 caracteres dos hashes para legibilidade
     * 
     * @return String com informação do bloco
     */
    @Override
    public String toString() {
        // Mostra apenas os primeiros 8 caracteres dos hashes
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
    
    /**
     * Valida a integridade do bloco
     * Recalcula o hash e compara com o hash armazenado
     * 
     * @return true se o bloco for válido, false caso contrário
     */
    public boolean isValid() {
        return currentHash.equals(calculateHash());
    }
    
    /**
     * Converte um array de bytes para representação hexadecimal
     * Usado para converter hashes binários em strings legíveis
     * 
     * @param bytes Array de bytes a converter
     * @return String hexadecimal
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}