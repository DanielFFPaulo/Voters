/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;

/**
 *
 * @author Acer
 */
public class Transaction implements Serializable {

    private String transactionId; // Hash único que identifica a transação
    private Key publicVoterKey; // Chave pública do votante (identificação anónima)
    private String encryptedVote; // Voto encriptado em Base64
    private String electionId; // ID da eleição a que pertence o voto
    private byte[] signature; // Assinatura digital da transação
    private int nonce; // Nonce para proof-of-work da transação

    /**
     * Construtor que cria uma nova transação de voto Encripta o voto, calcula o
     * hash, minera um nonce e assina digitalmente
     *
     * @param voterPublicKey Chave pública do votante
     * @param vote Voto em texto plano
     * @param electionId ID da eleição
     * @param privateKey Chave privada do votante para assinar
     * @throws Exception Se houver erro na encriptação ou assinatura
     */
    public Transaction(Key voterPublicKey, String vote, String electionId, PrivateKey privateKey) throws Exception {
        this.publicVoterKey = voterPublicKey;
        this.electionId = electionId;
        // Minera um nonce para a transação (proof-of-work)
        this.nonce = Miner.getNonce(publicVoterKey.toString() + encryptedVote + electionId, 3);

        // Encripta o voto antes de armazenar
        this.encryptedVote = encryptVote(vote);

        // Calcula o hash único da transação
        this.transactionId = calculateHash();

        // Assina digitalmente a transação com a chave privada
        this.signature = signTransaction(privateKey);
    }

    /**
     * Encripta um voto usando codificação Base64 Nota: Esta é uma implementação
     * simplificada para demonstração Em produção, deveria usar encriptação
     * assimétrica ou simétrica real
     *
     * @param vote Voto em texto plano
     * @return Voto encriptado em Base64
     */
    public String encryptVote(String vote) {
        return Base64.getEncoder().encodeToString(vote.getBytes());
    }

    /**
     * Assina digitalmente a transação usando a chave privada do votante Usa o
     * algoritmo SHA256withRSA para criar uma assinatura verificável
     *
     * @param privateKey Chave privada do votante
     * @return Assinatura digital em bytes
     * @throws Exception Se houver erro no processo de assinatura
     */
    private byte[] signTransaction(PrivateKey privateKey) throws Exception {
        // Concatena os dados da transação
        String data = publicVoterKey.toString() + encryptedVote + electionId;

        // Cria e inicializa o objeto de assinatura
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data.getBytes());

        // Retorna a assinatura digital
        return sig.sign();
    }

    /**
     * Verifica se a assinatura digital da transação é válida Usa a chave
     * pública para validar que a transação foi assinada pela chave privada
     * correspondente
     *
     * @param publicKey Chave pública para verificar a assinatura
     * @return true se a assinatura for válida, false caso contrário
     * @throws Exception Se houver erro na verificação
     */
    public boolean verifySignature(PublicKey publicKey) throws Exception {
        // Reconstrói os dados originais que foram assinados
        String data = publicVoterKey.toString() + encryptedVote + electionId;

        // Inicializa o verificador de assinatura
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());

        // Verifica se a assinatura corresponde aos dados
        return sig.verify(signature);
    }

    /**
     * Calcula o hash SHA-256 único da transação Este hash serve como ID da
     * transação e inclui todos os dados relevantes
     *
     * @return Hash da transação em formato hexadecimal
     */
    private String calculateHash() {
        try {
            // Concatena todos os dados da transação incluindo o nonce
            String data = publicVoterKey.toString() + encryptedVote + electionId + nonce;

            // Calcula o hash SHA-256
            MessageDigest digest;
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            // Converte para hexadecimal
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * Converte um array de bytes para representação hexadecimal
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

    /**
     * Obtém o ID único da transação
     *
     * @return Hash da transação
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Obtém a chave pública do votante
     *
     * @return Chave pública
     */
    public Key getPublicVoterKey() {
        return publicVoterKey;
    }

    /**
     * Obtém o voto encriptado
     *
     * @return Voto em Base64
     */
    public String getEncryptedVote() {
        return encryptedVote;
    }

    /**
     * Obtém o ID da eleição
     *
     * @return ID da eleição
     */
    public String getElectionId() {
        return electionId;
    }

    /**
     * Representação em string da transação com informação resumida Mostra
     * apenas os primeiros 8 caracteres dos hashes longos
     *
     * @return String com informação da transação
     */
    @Override
    public String toString() {
        return "Transaction{"
                + "id='" + transactionId.substring(0, 8) + "...', "
                + "voter='" + publicVoterKey.toString().substring(0, 8) + "...', "
                + "election='" + electionId + "', "
                + '}';
    }

}
