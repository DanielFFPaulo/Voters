/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package RemoteNodes;

import blockchained.Block;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Key;
import java.security.PublicKey;
import java.util.List;

/**
 *
 * @author Acer
 */
public interface RemoteVotingI extends Remote {

    /* =======================
       Voting
       ======================= */

    /**
     * Submit a vote transaction.
     *
     * @return status code (0 = success)
     */
    int vote(byte[] encryptedVote, byte[] voterSignature,PublicKey voterPublicKey
    ) throws RemoteException;

    /**
     * Get the hash of the latest block.
     */
    byte[] getLatestBlockHash() throws RemoteException;

    /**
     * Request blocks starting from a known hash.
     */
    List<Block> getBlocksFrom(byte[] fromBlockHash) throws RemoteException;
    
    
    void sync(List<Block> newBlocks)throws RemoteException;
    /**
     * Receive a block propagated by a peer.
     */
    void submitBlock(Block block) throws RemoteException;

    /* =======================
       Peer / Consensus
       ======================= */
    byte[] getAes(Key publicKey) throws RemoteException;
    /**
     * Notify peers of a new block.
     */
    void broadcastBlock(Block block) throws RemoteException;
    
    
    
    public String[] getBlockHashes() throws RemoteException;
    /**
     * Simple chain preference check.
     */
    boolean isMyChainShorter(int peerChainLength) throws RemoteException;

    /* =======================
       Security / Handshake
       ======================= */
    /**
     * Establish encrypted session (AES key encrypted with node public key).

    /**
     * Get blockchain height.
     */
    int getBlockchainHeight() throws RemoteException;
    
        //:::: N E T WO R K  :::::::::::
    public String getAdress() throws RemoteException;

    public void addNode(RemoteVotingI node) throws RemoteException;

    public List<RemoteVotingI> getNetwork() throws RemoteException;

    //::::::::::: T R A N S A C T I O N S  :::::::::::
    public void addTransaction(String data) throws RemoteException;

    public List<String> getTransactions() throws RemoteException;

    //::::::::::: M I N E R  :::::::::::
    public int mine(String message, int dificulty) throws RemoteException;

    public void stopMining(int nonce) throws RemoteException;

    public boolean isMining() throws RemoteException;

    public boolean isWinner() throws RemoteException;

    public int getNonce() throws RemoteException;
    
    public String getHash() throws RemoteException;
}