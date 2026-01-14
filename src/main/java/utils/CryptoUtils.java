/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

/**
 *
 * @author Sofia Vedor
 */
public class CryptoUtils { // Declara a classe utilitária CryptoUtils (métodos estáticos de criptografia)

    public static KeyPair generateRSAKeyPair() throws GeneralSecurityException { // Método estático que gera um par de chaves RSA; pode lançar exceções de segurança
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA"); // Cria um gerador de pares de chaves para o algoritmo RSA
        kpg.initialize(2048); // Inicializa o gerador com tamanho de chave 2048 bits
        return kpg.generateKeyPair(); // Gera e devolve o par de chaves (pública + privada)
    } // Fim do método generateRSAKeyPair

    public static SecretKey generateAESKey() throws GeneralSecurityException { // Método estático que gera uma chave simétrica AES; pode lançar exceções
        KeyGenerator kg = KeyGenerator.getInstance("AES"); // Obtém um gerador de chaves para o algoritmo AES
        kg.init(256); // Inicializa o gerador com tamanho de chave 256 bits
        return kg.generateKey(); // Gera e devolve a chave secreta AES
    } // Fim do método generateAESKey

    public static byte[] randomBytes(int n) { // Método que devolve um array de n bytes aleatórios
        byte[] b = new byte[n]; // Cria um array de bytes com tamanho n
        new SecureRandom().nextBytes(b); // Preenche o array com bytes aleatórios criptograficamente seguros
        return b; // Devolve o array preenchido
    } // Fim do método randomBytes

    public static SecretKey deriveAESKeyFromPassword(char[] password, byte[] salt, int iterations)
            throws GeneralSecurityException { // Deriva uma chave AES a partir de uma password usando PBKDF2; pode lançar exceções
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); // Obtém a fábrica PBKDF2 com HMAC-SHA256
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256); // Define a especificação: password + salt + nº iterações + tamanho (256 bits)
        byte[] keyBytes = factory.generateSecret(spec).getEncoded(); // Executa PBKDF2 e obtém os bytes da chave derivada
        return new SecretKeySpec(keyBytes, "AES"); // Envolve os bytes numa SecretKey AES e devolve
    } // Fim do método deriveAESKeyFromPassword

    public static class AesGcmBlob { // Classe interna para transportar (IV + ciphertext) de uma cifra AES-GCM
        public final byte[] iv; // Vetor de inicialização (nonce) usado no GCM
        public final byte[] ciphertext; // Texto cifrado (inclui também a tag de autenticação no final, no formato do Java)
        public AesGcmBlob(byte[] iv, byte[] ciphertext) { // Construtor que recebe IV e ciphertext
            this.iv = iv; // Guarda o IV
            this.ciphertext = ciphertext; // Guarda o ciphertext
        } // Fim do construtor
    } // Fim da classe AesGcmBlob

    public static AesGcmBlob encryptAesGcm(SecretKey key, byte[] plaintext) throws GeneralSecurityException { // Cifra plaintext com AES-GCM e devolve IV + ciphertext
        byte[] iv = randomBytes(12); // Gera um IV/nonce aleatório de 12 bytes (96 bits), tamanho recomendado para GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); // Obtém a implementação de cifra AES em modo GCM sem padding
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv)); // Inicializa para cifrar com a chave e parâmetros GCM (tag 128 bits + IV)
        byte[] ct = cipher.doFinal(plaintext); // Cifra os dados; o resultado inclui ciphertext + tag GCM (autenticação)
        return new AesGcmBlob(iv, ct); // Devolve um “blob” com IV e ciphertext
    } // Fim do método encryptAesGcm

    public static byte[] decryptAesGcm(SecretKey key, byte[] iv, byte[] ciphertext) throws GeneralSecurityException { // Decifra AES-GCM; valida a tag; pode lançar exceção se falhar
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); // Obtém a implementação AES/GCM
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv)); // Inicializa para decifrar com tag 128 bits e o IV fornecido
        return cipher.doFinal(ciphertext); // Decifra e valida integridade; se tag inválida lança AEADBadTagException
    } // Fim do método decryptAesGcm

    public static byte[] rsaEncryptOaep(PublicKey publicKey, byte[] data) throws GeneralSecurityException { // Cifra dados com RSA usando OAEP(SHA-256)
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); // Obtém RSA com OAEP SHA-256 (o "ECB" é só convenção de nome)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey); // Inicializa para cifrar usando a chave pública
        return cipher.doFinal(data); // Cifra e devolve o resultado
    } // Fim do método rsaEncryptOaep

    public static byte[] rsaDecryptOaep(PrivateKey privateKey, byte[] data) throws GeneralSecurityException { // Decifra dados RSA OAEP com a chave privada
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"); // Obtém RSA/OAEP SHA-256
        cipher.init(Cipher.DECRYPT_MODE, privateKey); // Inicializa para decifrar usando a chave privada
        return cipher.doFinal(data); // Decifra e devolve o plaintext
    } // Fim do método rsaDecryptOaep

    public static byte[] sign(PrivateKey privateKey, byte[] payload) throws GeneralSecurityException { // Assina um payload com RSA+SHA-256
        Signature s = Signature.getInstance("SHA256withRSA"); // Obtém o algoritmo de assinatura SHA-256 com RSA (PKCS#1 v1.5)
        s.initSign(privateKey); // Inicializa o objeto Signature para assinar com a chave privada
        s.update(payload); // Alimenta o objeto com os bytes a assinar
        return s.sign(); // Gera e devolve a assinatura
    } // Fim do método sign

    public static boolean verify(PublicKey publicKey, byte[] payload, byte[] signature) throws GeneralSecurityException { // Verifica se uma assinatura é válida para um payload
        Signature s = Signature.getInstance("SHA256withRSA"); // Obtém o algoritmo de verificação SHA-256 com RSA
        s.initVerify(publicKey); // Inicializa para verificação com a chave pública
        s.update(payload); // Fornece o payload original
        return s.verify(signature); // Verifica a assinatura e devolve true/false
    } // Fim do método verify

    public static PublicKey bytesToPublicKey(byte[] encoded) throws GeneralSecurityException { // Converte bytes (X.509) para PublicKey RSA
        X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded); // Cria uma KeySpec no formato X.509 (tipicamente usado para chave pública)
        return KeyFactory.getInstance("RSA").generatePublic(spec); // Reconstrói a chave pública RSA a partir do spec e devolve
    } // Fim do método bytesToPublicKey

    public static PrivateKey bytesToPrivateKey(byte[] encoded) throws GeneralSecurityException { // Converte bytes (PKCS#8) para PrivateKey RSA
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded); // Cria uma KeySpec no formato PKCS#8 (tipicamente usado para chave privada)
        return KeyFactory.getInstance("RSA").generatePrivate(spec); // Reconstrói a chave privada RSA a partir do spec e devolve
    } // Fim do método bytesToPrivateKey

    public static byte[] concat(byte[]... parts) { // Concatena vários arrays de bytes num só
        int len = 0; // Acumulador do tamanho total
        for (byte[] p : parts) len += p.length; // Soma o comprimento de cada parte
        byte[] out = new byte[len]; // Cria um array de saída com o tamanho total
        int pos = 0; // Posição atual onde vamos copiar no array de saída
        for (byte[] p : parts) { // Percorre cada parte
            System.arraycopy(p, 0, out, pos, p.length); // Copia p para out a partir de 'pos'
            pos += p.length; // Avança a posição
        }
        return out; // Devolve o array concatenado
    } // Fim do método concat

    public static byte[] slice(byte[] a, int from, int to) { // Extrai uma fatia do array a: [from, to)
        return Arrays.copyOfRange(a, from, to); // Devolve uma cópia do intervalo (from inclusive, to exclusivo)
    } // Fim do método slice
} // Fim da classe CryptoUtils
