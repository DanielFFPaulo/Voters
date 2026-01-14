/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import utils.SecurityUtils;
import utils.Session;

/**
 *
 * @author Acer
 */

public class Voter {
    private String name; // Nome do eleitor
    private KeyPair keyPair;// Par de chaves RSA (pública e privada)
    
    public Voter(String name) throws NoSuchAlgorithmException, Exception {
        this.name = name;
        this.keyPair = SecurityUtils.generateRSAKeyPair(2048);
    }
    
public Transaction castVote(String vote, String electionId) throws Exception {
    // Use the session key pair for both signing and public key
    Session.Keys sKeys = Session.get();
    return new Transaction(getPublicKey() , vote, electionId, sKeys.privateKey);
}

        /**
     * Devolve o nome do eleitor
     * 
     * @return Nome do eleitor
     */
    public String getName() { return name; }
        /**
     * Obtém a chave pública do eleitor
     * A chave pública é usada para verificar assinaturas digitais
     * e pode ser partilhada publicamente sem comprometer a segurança
     * 
     * @return Chave pública RSA do eleitor
     */
    public PublicKey getPublicKey() {Session.Keys sKeys = Session.get(); return sKeys.publicKey; }
    
}