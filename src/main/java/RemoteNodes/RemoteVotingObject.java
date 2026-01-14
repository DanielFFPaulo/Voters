/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RemoteNodes;

import blockchained.Block;
import blockchained.Blockchain;
import blockchained.Election;
import blockchained.Transaction;
import blockchained.Voter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import utils.RMI;
import utils.SecurityUtils;

/**
 * Objeto remoto para sistema de votação distribuído usando blockchain
 * Implementa um nó numa rede P2P que permite votações seguras e sincronização
 * da blockchain entre nós
 * 
 * @author Acer
 */
public class RemoteVotingObject extends UnicastRemoteObject implements RemoteVotingI {

    public static String REMOTE_OBJECT_NAME = "remoteNode";
    static Election eleicao; // Eleição atual do sistema
    static Blockchain blockchain; // Blockchain partilhada
    String address; // Endereço RMI deste nó
    Set<RemoteVotingI> network; // Conjunto de nós conectados na rede P2P
    Set<String> transactions; // Conjunto de transações pendentes
    NodeListener listener; // Listener para eventos do nó
    public MinerDistributed miner = new MinerDistributed(); // Minerador para proof-of-work
    private Key aes; // Chave AES para encriptação simétrica
    private KeyPair rsa; // Par de chaves RSA para encriptação assimétrica

    /**
     * Construtor que inicializa o objeto remoto de votação
     * 
     * @param port Porta para o serviço RMI
     * @param listener Listener para notificações de eventos
     * @throws RemoteException Em caso de erro RMI
     * @throws InterruptedException Se a thread for interrompida
     * @throws Exception Para outros erros de inicialização
     */
    public RemoteVotingObject(int port, NodeListener listener) throws RemoteException, InterruptedException, Exception {
        super(port);
        
        try {
            // Obtém o endereço IP local
            String host = InetAddress.getLocalHost().getHostAddress();
            this.address = RMI.getRemoteName(host, port, REMOTE_OBJECT_NAME);
            
            // Inicializa conjuntos thread-safe para rede e transações
            this.network = new CopyOnWriteArraySet<>();
            this.transactions = new CopyOnWriteArraySet<>();

            this.listener = listener;
            if (listener != null) {
                listener.onStart("Object " + address + "is listening");
            } else {
                System.err.println("Object " + address + "is listening");
            }
        } catch (UnknownHostException ex) {
            System.getLogger(RemoteVotingObject.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            if (listener != null) {
                listener.onException(ex, "Start remote Object");
            }
        }
        
        // Inicializa a blockchain com dificuldade 3
        blockchain = new Blockchain(3);
        
        // Cria uma eleição inicial
        eleicao = new Election("election001", "Class president");
        blockchain.createElection(eleicao);
        
        // Gera chaves de encriptação
        aes = SecurityUtils.generateAESKey(128);
        rsa = SecurityUtils.generateRSAKeyPair(2048);
    }

    /**
     * Processa um voto encriptado e adiciona-o à blockchain
     * 
     * @param voter Dados do votante encriptados
     * @param partido Partido/candidato encriptado
     * @param voterPublicKey Chave pública do votante para verificação
     * @return 200 se bem-sucedido, 400 para argumentos inválidos, 500 para erros
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public int vote(byte[] voter, byte[] partido, PublicKey voterPublicKey) throws RemoteException {
        try {
            // Desencripta os dados do votante
            System.out.println("Decrypting");
            String username = new String(SecurityUtils.decrypt(voter, aes));
            Voter user = new Voter(username);
            
            // Desencripta o partido/candidato
            System.out.println("Decrypting too");
            String truePartido = new String(SecurityUtils.decrypt(partido, aes));
            
            // Cria e adiciona o voto à blockchain
            System.out.println("Voting");
            Transaction voto = user.castVote(truePartido, eleicao.getElectionId());
            blockchain.addTransaction(voto, voterPublicKey);
            blockchain.minePendingTransactions();
            System.out.println(blockchain.getPendingTransactionCount());
            
        } catch (IllegalArgumentException ex) {
            System.out.println("aaaaaa");
            return 400; // Argumentos inválidos
        } catch (Exception ex) {
            System.getLogger(RemoteVotingObject.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return 500; // Erro interno
        }
        
        // Propaga o novo bloco para todos os nós da rede
        broadcastBlock(blockchain.getLatestBlock());
        
        return 200; // Sucesso
    }

    /**
     * Obtém o hash do último bloco da blockchain
     * 
     * @return Hash do último bloco em bytes
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public byte[] getLatestBlockHash() throws RemoteException {
        return blockchain.getLatestBlock().getCurrentHash().getBytes();
    }

    /**
     * Obtém todos os blocos a partir de um hash específico
     * 
     * @param fromBlockHash Hash do bloco a partir do qual obter blocos
     * @return Lista de blocos subsequentes
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public List<Block> getBlocksFrom(byte[] fromBlockHash) throws RemoteException {
        return blockchain.getBlocksFrom(new String(fromBlockHash));
    }

    /**
     * Submete um novo bloco recebido de outro nó
     * 
     * @param block Bloco a ser adicionado
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public void submitBlock(Block block) throws RemoteException {
        // Evita adicionar blocos duplicados
        if(block.getCurrentHash().equals(blockchain.getLatestBlock().getCurrentHash())){
            return;
        }
        
        blockchain.addBlock(block);
    }

    /**
     * Propaga um bloco para todos os nós da rede
     * 
     * @param block Bloco a ser propagado
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public void broadcastBlock(Block block) throws RemoteException {
        for (RemoteVotingI node : network) {
            node.submitBlock(block);
        }
    }

    /**
     * Verifica se a blockchain local é mais curta que a de outro nó
     * 
     * @param peerChainLength Comprimento da blockchain do outro nó
     * @return true se a blockchain local for mais curta
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public boolean isMyChainShorter(int peerChainLength) throws RemoteException {
        return this.getBlockchainHeight() < peerChainLength;
    }

    /**
     * Obtém a altura (número de blocos) da blockchain local
     * 
     * @return Altura da blockchain
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public int getBlockchainHeight() throws RemoteException {
        return blockchain.getHeight();
    }

    /**
     * Obtém o endereço RMI deste nó
     * 
     * @return Endereço do nó
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public String getAdress() throws RemoteException {
        return address;
    }

    /**
     * Adiciona um novo nó à rede P2P e sincroniza a blockchain
     * Propaga a conexão para todos os vizinhos
     * 
     * @param node Nó a ser adicionado
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public void addNode(RemoteVotingI node) throws RemoteException {
        // Evita adicionar nós duplicados
        if (network.contains(node)) {
            return;
        }

        network.add(node);
        
        // Sincroniza blockchain com o novo nó
        if(this.isMyChainShorter(node.getBlockchainHeight())){
            // Este nó tem blockchain mais curta, recebe blocos do outro
            blockchain.sync(node.getBlocksFrom(getLatestBlockHash()));
        } else {
            // Este nó tem blockchain mais longa, envia blocos ao outro
            byte[] toFind = node.getLatestBlockHash();
            node.sync(this.getBlocksFrom(toFind));
        }
        
        // Adiciona este nó à rede do novo nó (conexão bidirecional)
        node.addNode(this);
        
        // Propaga o novo nó para todos os vizinhos
        for (RemoteVotingI neighbors : network) {
            neighbors.addNode(node);
        }

        if (listener != null) {
            listener.onConnect(node.getAdress());
        } else {
            System.out.println("Connected to " + node.getAdress());
        }

        // Imprime a rede P2P atual
        System.out.println("Rede p2p");
        for (RemoteVotingI remoteVotingI : network) {
            System.out.println(remoteVotingI.getAdress());
        }
    }

    /**
     * Obtém a lista de nós conectados na rede
     * 
     * @return Lista de nós da rede
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public List<RemoteVotingI> getNetwork() throws RemoteException {
        return new ArrayList<>(network);
    }
    
    /**
     * Obtém os hashes de todos os blocos da blockchain
     * 
     * @return Array de hashes dos blocos
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public String[] getBlockHashes() throws RemoteException{
        return blockchain.getBlockHashes();
    }

    /**
     * Adiciona uma transação ao conjunto de transações pendentes
     * 
     * @param data Dados da transação
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public void addTransaction(String data) throws RemoteException {
        // Evita adicionar transações duplicadas
        if (this.transactions.contains(data)) {
            return;
        }
        this.transactions.add(data);
    }

    /**
     * Obtém a lista de transações pendentes
     * 
     * @return Lista de transações
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public List<String> getTransactions() throws RemoteException {
        return new ArrayList<>(transactions);
    }

    /**
     * Obtém o endereço do cliente remoto que está a fazer a chamada
     * 
     * @return Endereço do cliente ou "unknown" se não disponível
     */
    private String getRemoteHost() {
        try {
            return RemoteServer.getClientHost();
        } catch (ServerNotActiveException ex) {
            return "unknown";
        }
    }

    /**
     * Inicia o processo de mineração distribuída
     * Propaga o pedido de mineração para todos os nós da rede
     * 
     * @param message Mensagem a ser minerada
     * @param dificulty Dificuldade (número de zeros necessários)
     * @return Nonce encontrado ou 0 se já estiver a minerar
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public int mine(String message, int dificulty) throws RemoteException {
        // Se já estiver a minerar, não faz nada
        if (miner.isMining()) {
            return 0;
        }
        
        miner.isWorking.set(true);
        
        // Propaga o pedido de mineração para todos os nós
        for (RemoteVotingI node : network) {
            node.mine(message, dificulty);
        }
        
        return miner.mine(message, dificulty);
    }

    /**
     * Para o processo de mineração em todos os nós
     * 
     * @param nonce Nonce encontrado
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public void stopMining(int nonce) throws RemoteException {
        // Se não estiver a minerar, não faz nada
        if (!miner.isMining()) {
            return;
        }
        
        miner.stopMining(nonce);
        
        // Propaga o comando de paragem para todos os nós
        for (RemoteVotingI node : network) {
            node.stopMining(nonce);
        }         
    }
    
    /**
     * Verifica se o nó está atualmente a minerar
     * 
     * @return true se estiver a minerar
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public boolean isMining() throws RemoteException {
        return miner.isMining();
    }

    /**
     * Verifica se este nó foi o vencedor da mineração
     * 
     * @return true se foi o vencedor
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public boolean isWinner() throws RemoteException {
        return miner.isWinner();
    }

    /**
     * Obtém o nonce atual do minerador
     * 
     * @return Valor do nonce
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public int getNonce() throws RemoteException {
        return miner.getNonce();
    }

    /**
     * Obtém o hash da mensagem minerada com o nonce atual
     * 
     * @return Hash em formato Base64
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public String getHash() throws RemoteException {
        return MinerDistributed.getHash(miner.message + miner.getNonce());
    }

    /**
     * Obtém a chave AES encriptada com uma chave pública
     * Permite troca segura de chaves entre nós
     * 
     * @param publicKey Chave pública para encriptar a chave AES
     * @return Chave AES encriptada ou null em caso de erro
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public byte[] getAes(Key publicKey) throws RemoteException {
        try {
            return SecurityUtils.encrypt(aes.getEncoded(), publicKey);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Sincroniza a blockchain local com uma lista de novos blocos
     * 
     * @param newBlocks Lista de blocos para sincronização
     * @throws RemoteException Em caso de erro RMI
     */
    @Override
    public void sync(List<Block> newBlocks) throws RemoteException {
        blockchain.sync(newBlocks);
    }
}