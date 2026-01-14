/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.*;
import java.security.*;

/**
 *
 * @author Sofia Vedor
 */
public class KeyStoreService { // Declara a classe KeyStoreService, responsável por criar “utilizadores” e fazer login via ficheiros

    private static final int SALT_LEN = 16; // Tamanho do salt (em bytes) usado no PBKDF2 (16 bytes = 128 bits)
    private static final int IV_LEN = 12; // Tamanho do IV/nonce para AES-GCM (12 bytes = 96 bits, recomendado)
    private static final int PBKDF2_ITERS = 65536; // Número de iterações do PBKDF2 (custo para dificultar brute-force)

    public static void createUser(Path folder, String username, char[] password) throws Exception { // Cria os ficheiros do utilizador (pub/sim/priv)
        Files.createDirectories(folder); // Garante que a pasta existe (cria se não existir)

        KeyPair kp = CryptoUtils.generateRSAKeyPair(); // Gera par de chaves RSA (pública + privada) para o utilizador
        SecretKey aes = CryptoUtils.generateAESKey(); // Gera uma chave AES aleatória (simétrica) para uso futuro (ex.: cifrar dados)

        // c) AES cifrada com pública => .sim
        byte[] sim = CryptoUtils.rsaEncryptOaep(kp.getPublic(), aes.getEncoded()); // Cifra os bytes da chave AES com a chave pública RSA (OAEP)

        // d) privada cifrada com password => .priv (salt|iv|ciphertext)
        byte[] salt = CryptoUtils.randomBytes(SALT_LEN); // Gera um salt aleatório para derivar a chave a partir da password
        SecretKey pwKey = CryptoUtils.deriveAESKeyFromPassword(password, salt, PBKDF2_ITERS); // Deriva uma chave AES (pwKey) via PBKDF2(password,salt,iterações)

        CryptoUtils.AesGcmBlob blob = CryptoUtils.encryptAesGcm(pwKey, kp.getPrivate().getEncoded()); // Cifra (AES-GCM) a chave privada (formato PKCS#8) com pwKey
        byte[] privFile = CryptoUtils.concat(salt, blob.iv, blob.ciphertext); // Constrói o conteúdo do ficheiro .priv: salt || iv || ciphertext

        // e) pública => .pub
        byte[] pub = kp.getPublic().getEncoded(); // Obtém a chave pública em formato X.509 (bytes)

        Files.write(folder.resolve(username + ".pub"), pub); // Escreve a chave pública no ficheiro username.pub
        Files.write(folder.resolve(username + ".sim"), sim); // Escreve a chave AES cifrada com RSA no ficheiro username.sim
        Files.write(folder.resolve(username + ".priv"), privFile); // Escreve salt|iv|ciphertext (privada cifrada) no ficheiro username.priv
    } // Fim do método createUser

    public static Session.Keys login(Path folder, String username, char[] password) throws Exception { // Faz login: recupera as chaves a partir dos ficheiros + password
        byte[] pubBytes = Files.readAllBytes(folder.resolve(username + ".pub")); // Lê bytes do ficheiro da chave pública
        byte[] simBytes = Files.readAllBytes(folder.resolve(username + ".sim")); // Lê bytes do ficheiro com a chave AES cifrada com RSA
        byte[] privBytes = Files.readAllBytes(folder.resolve(username + ".priv")); // Lê bytes do ficheiro com a privada cifrada (salt|iv|ciphertext)

        PublicKey publicKey = CryptoUtils.bytesToPublicKey(pubBytes); // Converte os bytes X.509 numa PublicKey RSA

        if (privBytes.length < SALT_LEN + IV_LEN + 16) { // Valida tamanho mínimo: salt + iv + pelo menos 16 bytes (tag/ct mínimo) para GCM
            throw new IOException("Invalid .priv file"); // Se for demasiado pequeno, o formato não é válido
        }

        byte[] salt = CryptoUtils.slice(privBytes, 0, SALT_LEN); // Extrai o salt: primeiros 16 bytes
        byte[] iv = CryptoUtils.slice(privBytes, SALT_LEN, SALT_LEN + IV_LEN); // Extrai o IV: bytes [16, 28)
        byte[] ciphertext = CryptoUtils.slice(privBytes, SALT_LEN + IV_LEN, privBytes.length); // Extrai o resto: ciphertext (inclui tag GCM)

        SecretKey pwKey = CryptoUtils.deriveAESKeyFromPassword(password, salt, PBKDF2_ITERS); // Re-deriva a mesma chave (pwKey) a partir da password + salt

        // se password errada => exceção (AEADBadTagException)
        byte[] privateEncoded = CryptoUtils.decryptAesGcm(pwKey, iv, ciphertext); // Tenta decifrar a privada; se a tag falhar, lança exceção
        PrivateKey privateKey = CryptoUtils.bytesToPrivateKey(privateEncoded); // Converte os bytes PKCS#8 numa PrivateKey RSA

        // abrir .sim com a privada => AES key
        byte[] aesRaw = CryptoUtils.rsaDecryptOaep(privateKey, simBytes); // Usa a chave privada RSA para decifrar a chave AES guardada em .sim
        SecretKey aesKey = new SecretKeySpec(aesRaw, "AES"); // Constrói a SecretKey AES a partir dos bytes decifrados

        Session.Keys keys = new Session.Keys(username, publicKey, privateKey, aesKey); // Cria um objeto com as chaves do utilizador (inclui AES)
        Session.set(keys); // Guarda estas chaves na “sessão” global/atual da aplicação
        return keys; // Devolve as chaves para uso pelo chamador
    } // Fim do método login
} // Fim da classe KeyStoreService
