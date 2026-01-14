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
 * Implementação de uma blockchain para sistema de votação eletrónica
 * Gere a cadeia de blocos, transações pendentes, registo de votantes
 * e validação de votos para prevenir votação duplicada
 * 
 * @author Acer
 */
public class Blockchain {

    private List<Block> chain; // Cadeia de blocos (a blockchain propriamente dita)
    private List<Transaction> pendingTransactions; // Transações pendentes de serem mineradas
    private int difficulty; // Dificuldade de mineração (número de zeros no hash)
    private Map<String, Set<String>> voterRegistry; // Registo: chave pública -> eleições em que votou
    private Map<String, Election> elections; // Mapa de eleições disponíveis

    /**
     * Construtor que inicializa a blockchain com um bloco génesis
     * 
     * @param difficulty Dificuldade de mineração dos blocos
     * @throws InterruptedException Se a criação do bloco génesis for interrompida
     */
    public Blockchain(int difficulty) throws InterruptedException {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.difficulty = difficulty;
        this.voterRegistry = new HashMap<>();
        this.elections = new HashMap<>();

        // Cria o bloco génesis (primeiro bloco da cadeia)
        chain.add(createGenesisBlock());
    }

    /**
     * Cria o bloco génesis - o primeiro bloco da blockchain
     * Este bloco não tem hash anterior (usa "0") nem transações
     * 
     * @return Bloco génesis
     * @throws InterruptedException Se a mineração for interrompida
     */
    private Block createGenesisBlock() throws InterruptedException {
        return new Block(difficulty, "0", new ArrayList<>());
    }

    /**
     * Obtém os hashes de todos os blocos da blockchain
     * Útil para verificação e sincronização entre nós
     * 
     * @return Array com os hashes de todos os blocos
     */
    public String[] getBlockHashes() {
        String[] hashes = new String[chain.size()];
        for (int i = 0; i < chain.size(); i++) {
            hashes[i] = chain.get(i).getCurrentHash();
        }
        return hashes;
    }

    /**
     * Obtém o último bloco da cadeia
     * 
     * @return Último bloco adicionado à blockchain
     */
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    /**
     * Sincroniza a blockchain local com blocos recebidos de outro nó
     * Remove blocos divergentes e adiciona os novos blocos
     * 
     * @param newBlocks Lista de blocos para sincronizar
     * @throws IllegalStateException Se a blockchain ficar inválida após sincronização
     */
    public void sync(List<Block> newBlocks) {
        if (newBlocks.isEmpty()) {
            return;
        }

        Block start = newBlocks.get(0);
        int index = -1;

        // Encontra o índice do primeiro bloco novo comparando hashes
        // (não usa referência de objetos porque podem vir de máquinas diferentes)
        for (int i = 0; i < chain.size(); i++) {
            if (chain.get(i).getCurrentHash().equals(start.getCurrentHash())) {
                index = i;
                break;
            }
        }

        // Remove blocos antigos a partir do índice encontrado
        if (index != -1) {
            chain.subList(index, chain.size()).clear();
        }

        // Adiciona os novos blocos
        chain.addAll(newBlocks);

        // Valida a integridade da blockchain após sincronização
        if (!isChainValid()) {
            throw new IllegalStateException("Blockchain invalid after sync!");
        }
    }

    /**
     * Adiciona uma transação de voto à lista de transações pendentes
     * Valida a autenticidade, assinatura e previne votação duplicada
     * 
     * @param transaction Transação a adicionar
     * @param publicKey Chave pública do votante para validação
     * @throws Exception Se a transação for inválida ou o votante já tiver votado
     */
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
        if (!transaction.getPublicVoterKey().equals(sKeys.publicKey)) {
            throw new IllegalStateException("A chave do voto não corresponde ao utilizador autenticado.");
        }
        
        // Verificação duplicada (possível código legacy)
        if (!transaction.getPublicVoterKey().equals(sKeys.publicKey)) {
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
        if (hasVoterVoted(transaction.getPublicVoterKey().toString(), transaction.getElectionId())) {
            throw new IllegalArgumentException("Voter has already voted in this election");
        }
        
        // Verificação duplicada de voto duplicado
        if (hasVoterVoted(transaction.getPublicVoterKey().toString(), transaction.getElectionId())) {
            throw new IllegalArgumentException(
                    "O eleitor já votou nesta eleição."
            );
        }

        // 4) Adiciona a transação ao pool de transações pendentes
        // Estas transações serão incluídas num bloco após mineração
        pendingTransactions.add(transaction);

        // Log para debug/auditoria
        System.out.println(
                "Transação adicionada à lista de transações pendentes.: " + transaction
        );
    }

    /**
     * Minera todas as transações pendentes criando um novo bloco
     * Atualiza o registo de votantes e limpa a lista de transações pendentes
     * 
     * @throws InterruptedException Se a mineração for interrompida
     */
    public void minePendingTransactions() throws InterruptedException {
        // Se não há transações, não faz nada
        if (pendingTransactions.isEmpty()) {
            System.out.println("No transactions to mine");
            return;
        }

        // Cria um novo bloco com as transações pendentes
        Block block = new Block(chain.size(), getLatestBlock().getCurrentHash(), pendingTransactions);

        // Adiciona o bloco à cadeia
        chain.add(block);

        // Atualiza o registo de votantes
        // Marca que cada votante já votou nas respetivas eleições
        for (Transaction tx : pendingTransactions) {
            voterRegistry.computeIfAbsent(tx.getPublicVoterKey().toString(), k -> new HashSet<>())
                    .add(tx.getElectionId());
        }

        // Limpa a lista de transações pendentes
        pendingTransactions = new ArrayList<>();
    }

    /**
     * Adiciona um bloco já minerado à blockchain
     * Usado durante sincronização com outros nós
     * 
     * @param block Bloco a adicionar
     */
    public void addBlock(Block block) {
        chain.add(block);
    }

    /**
     * Valida uma transação verificando assinatura e estado da eleição
     * 
     * @param transaction Transação a validar
     * @param publicKey Chave pública para verificar a assinatura
     * @return true se a transação for válida, false caso contrário
     * @throws Exception Se houver erro na verificação da assinatura
     */
    private boolean isTransactionValid(Transaction transaction, PublicKey publicKey)
            throws Exception {
        // Verifica a assinatura digital da transação
        if (!transaction.verifySignature(publicKey)) {
            System.out.println("Invalid signature");
            return false;
        }

        // Verifica se a eleição existe e está ativa
        Election election = elections.get(transaction.getElectionId());
        if (election == null || !election.isActive()) {
            System.out.println("Election not active");
            return false;
        }

        return true;
    }

    /**
     * Verifica se um votante já votou numa eleição específica
     * 
     * @param voterPublicKey Chave pública do votante
     * @param electionId ID da eleição
     * @return true se já tiver votado, false caso contrário
     */
    private boolean hasVoterVoted(String voterPublicKey, String electionId) {
        Set<String> votedElections = voterRegistry.get(voterPublicKey);
        boolean hasVoted = votedElections != null && votedElections.contains(electionId);
        if (hasVoted) {
            System.out.println("already voted");
        }
        return hasVoted;
    }

    /**
     * Valida a integridade de toda a blockchain
     * Verifica se todos os blocos têm hashes corretos e estão bem ligados
     * 
     * @return true se a cadeia for válida, false caso contrário
     */
    public boolean isChainValid() {
        // Percorre todos os blocos (começando do segundo)
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Verifica se o hash do bloco atual está correto
            if (!currentBlock.getCurrentHash().equals(currentBlock.calculateHash())) {
                System.out.println("Block " + i + " hash is invalid");
                return false;
            }

            // Verifica se os blocos estão corretamente ligados
            // (hash anterior do bloco atual = hash do bloco anterior)
            if (!currentBlock.getPreviousHash().equals(previousBlock.getCurrentHash())) {
                System.out.println("Block " + i + " is not properly linked");
                return false;
            }
        }
        return true;
    }

    /**
     * Conta os votos de uma eleição específica
     * Percorre toda a blockchain e desencripta os votos
     * 
     * @param electionId ID da eleição a contar
     * @return Mapa com os resultados: candidato -> número de votos
     */
    public Map<String, Integer> tallyVotes(String electionId) {
        Map<String, Integer> results = new HashMap<>();

        // Percorre todos os blocos da cadeia
        for (Block block : chain) {
            // Percorre todas as transações de cada bloco
            for (Transaction tx : block.getTransactions()) {
                // Se a transação pertence à eleição pretendida
                if (tx.getElectionId().equals(electionId)) {
                    // Desencripta o voto
                    String vote = decryptVote(tx.getEncryptedVote());
                    // Incrementa o contador para esse candidato
                    results.put(vote, results.getOrDefault(vote, 0) + 1);
                }
            }
        }

        return results;
    }

    /**
     * Desencripta um voto (versão simplificada para demonstração)
     * Em produção, deveria usar desencriptação real com chaves apropriadas
     * 
     * @param encryptedVote Voto encriptado em Base64
     * @return Voto desencriptado
     */
    private String decryptVote(String encryptedVote) {
        // Descodificação simples para demonstração
        // Em produção usar desencriptação real
        return new String(Base64.getDecoder().decode(encryptedVote));
    }

    /**
     * Cria uma nova eleição a partir de um objeto Election
     * 
     * @param eleicao Objeto eleição a criar
     */
    public void createElection(Election eleicao) {
        createElection(eleicao.getElectionId(), eleicao.getTitle());
    }

    /**
     * Cria uma nova eleição com ID e título
     * 
     * @param electionId ID único da eleição
     * @param title Título/descrição da eleição
     */
    public void createElection(String electionId, String title) {
        elections.put(electionId, new Election(electionId, title));
        System.out.println("Election created: " + title);
    }

    /**
     * Obtém a altura da blockchain (número de blocos)
     * 
     * @return Número de blocos na cadeia
     */
    public int getHeight() {
        return chain.size();
    }

    /**
     * Imprime toda a blockchain no console
     * Mostra cada bloco e as suas transações de forma hierárquica
     */
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

    /**
     * Obtém o número de transações pendentes (ainda não mineradas)
     * 
     * @return Número de transações pendentes
     */
    public int getPendingTransactionCount() {
        return pendingTransactions.size();
    }

    /**
     * Obtém todos os blocos a partir de um hash específico
     * Usado para sincronização entre nós
     * 
     * @param desiredHash Hash do bloco a partir do qual obter blocos
     * @return Lista de blocos desde o hash especificado até ao fim
     */
    public List<Block> getBlocksFrom(String desiredHash) {
        List<Block> list = new ArrayList<>();

        // Procura o bloco com o hash pretendido
        for (int i = 0; i < chain.size(); i++) {
            if (chain.get(i).getCurrentHash().equals(desiredHash)) {
                // Cria uma nova ArrayList a partir da sublista
                // (todos os blocos desde este até ao fim)
                list = new ArrayList<>(chain.subList(i, chain.size()));
                break;
            }
        }

        return list;
    }
}