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
 *
 * @author Acer
 */
public class RemoteVotingObject extends UnicastRemoteObject implements RemoteVotingI {

    public static String REMOTE_OBJECT_NAME = "remoteNode";
    static Election eleicao;
    static Blockchain blockchain;
    String address;
    Set<RemoteVotingI> network;
    Set<String> transactions;
    NodeListener listener;
    public MinerDistributed miner = new MinerDistributed();
    private Key aes;
    private KeyPair rsa;

    public RemoteVotingObject(int port, NodeListener listener) throws RemoteException, InterruptedException, Exception {
        super(port);
        
        
        
        
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.address = RMI.getRemoteName(host, port, REMOTE_OBJECT_NAME);
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
        
        blockchain = new Blockchain(3);
        eleicao = new Election("election001", "Class president");
        blockchain.createElection(eleicao);
        
        aes = SecurityUtils.generateAESKey(128);
        rsa = SecurityUtils.generateRSAKeyPair(2048);
        
        
        
        

    }

    @Override
    public int vote(byte[] voter, byte[] partido, PublicKey voterPublicKey) throws RemoteException {
        try {
            System.out.println("Decrypting");
            String username  = new String(SecurityUtils.decrypt(voter, aes));
            Voter user = new Voter(username);
            System.out.println("Decrypting too");
            String truePartido = new String(SecurityUtils.decrypt(partido, aes));
            
            System.out.println("Voting");
            Transaction voto = user.castVote(truePartido, eleicao.getElectionId());
            blockchain.addTransaction(voto, voterPublicKey);
            blockchain.minePendingTransactions();
            System.out.println(blockchain.getPendingTransactionCount());
            
            
            
        } catch (IllegalArgumentException ex) {
            System.out.println("aaaaaa");
            return 400;} catch (Exception ex) {
            System.getLogger(RemoteVotingObject.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return 500;
        }
            
        broadcastBlock(blockchain.getLatestBlock());
            
        
        
        return 200;
    }

    @Override
    public byte[] getLatestBlockHash() throws RemoteException {
        return blockchain.getLatestBlock().getCurrentHash().getBytes();
    }

    @Override
    public List<Block> getBlocksFrom(byte[] fromBlockHash) throws RemoteException {
        return blockchain.getBlocksFrom(new String(fromBlockHash));
    }

    @Override
    public void submitBlock(Block block) throws RemoteException {
        if(block.getCurrentHash().equals(blockchain.getLatestBlock().getCurrentHash())){
            return;
        }
        
        
        
        blockchain.addBlock(block);
        
        
    }

    @Override
    public void broadcastBlock(Block block) throws RemoteException {
        for (RemoteVotingI node : network) {
            node.submitBlock(block);
        }
    }

    @Override
    public boolean isMyChainShorter(int peerChainLength) throws RemoteException {
        return this.getBlockchainHeight() < peerChainLength;
    }

    @Override
    public int getBlockchainHeight() throws RemoteException {
        return blockchain.getHeight();
    }

    
    
    
    
    
    
    
    
    
    //already Done 
    @Override
    public String getAdress() throws RemoteException {
        return address;
    }

    @Override
    public void addNode(RemoteVotingI node) throws RemoteException {
        if (network.contains(node)) {
            return;
        }

        network.add(node);
        //this.transactions.addAll(node.getTransactions());
        if(this.isMyChainShorter(node.getBlockchainHeight())){
            
            blockchain.sync(node.getBlocksFrom(getLatestBlockHash()));
        }else{
            byte[] toFind = node.getLatestBlockHash();
            node.sync(this.getBlocksFrom(toFind));
        }
        
        
        
        
        node.addNode(this);
        for (RemoteVotingI neighbors : network) {
            neighbors.addNode(node);
        }

        if (listener != null) {
            listener.onConnect(node.getAdress());
        } else {
            System.out.println("Connected to " + node.getAdress());
        }

        System.out.println("Rede p2p");
        for (RemoteVotingI remoteVotingI : network) {
            System.out.println(remoteVotingI.getAdress());
        }

    }

    @Override
    public List<RemoteVotingI> getNetwork() throws RemoteException {
        return new ArrayList<>(network);
    }
    
    @Override
    public String[] getBlockHashes() throws RemoteException{
        return blockchain.getBlockHashes();
    }
    
    

    @Override
    public void addTransaction(String data) throws RemoteException {
        if (this.transactions.contains(data)) {
            return;
        }
        this.transactions.add(data);
    }

    @Override
    public List<String> getTransactions() throws RemoteException {
        return new ArrayList<>(transactions);
    }

    private String getRemoteHost() {
        try {
            return RemoteServer.getClientHost();
        } catch (ServerNotActiveException ex) {
            return "unknown";
        }
    }

    @Override
    public int mine(String message, int dificulty) throws RemoteException {
        //        se estiver a minar
        if (miner.isMining()) {
            return 0; // não faz nada
        }
        miner.isWorking.set(true);
        for (RemoteVotingI node : network) {
            node.mine(message, dificulty);
        }
        return miner.mine(message, dificulty);
    }

    @Override
    public void stopMining(int nonce) throws RemoteException {
                //se não estiver a minar
        if (!miner.isMining()) {
            return ; //nao faz nada
        }
        miner.stopMining(nonce);
        for (RemoteVotingI node : network) {
            node.stopMining(nonce);
        }         
    
    }
    @Override
    public boolean isMining() throws RemoteException {
        return miner.isMining();
    }

    @Override
    public boolean isWinner() throws RemoteException {
        return miner.isWinner();
    }

    @Override
    public int getNonce() throws RemoteException {
        return miner.getNonce();
    }

    @Override
    public String getHash() throws RemoteException {
        return MinerDistributed.getHash(miner.message+miner.getNonce());
    }

    @Override
    public byte[] getAes(Key publicKey) throws RemoteException {
        try {
            return SecurityUtils.encrypt(aes.getEncoded(), publicKey);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public void sync(List<Block> newBlocks) throws RemoteException {
        blockchain.sync(newBlocks);
    }

}
