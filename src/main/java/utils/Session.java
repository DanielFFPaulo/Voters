/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 *
 * @author Sofia Vedor
 */
public class Session { // Classe Session: mantém o estado da sessão atual (quem está autenticado)

    public static class Keys { // Classe interna que agrupa todas as chaves associadas a um utilizador autenticado
        public final String username; // Nome do utilizador autenticado
        public final PublicKey publicKey; // Chave pública RSA do utilizador
        public final PrivateKey privateKey; // Chave privada RSA do utilizador (já decifrada)
        public final SecretKey aesKey; // Chave simétrica AES associada ao utilizador (ex.: para cifrar dados)

        public Keys(String username, PublicKey publicKey, PrivateKey privateKey, SecretKey aesKey) { // Construtor da classe Keys
            this.username = username; // Guarda o nome do utilizador
            this.publicKey = publicKey; // Guarda a chave pública
            this.privateKey = privateKey; // Guarda a chave privada
            this.aesKey = aesKey; // Guarda a chave AES
        } // Fim do construtor
    } // Fim da classe interna Keys

    private static Keys current; // Referência estática para as chaves da sessão atual (null se ninguém estiver autenticado)

    public static void set(Keys keys) { current = keys; } // Define a sessão atual (login)
    public static Keys get() {
    if (current == null) {
        throw new IllegalStateException("Sem sessão ativa.");
    }
    return current;
}
    public static boolean isLoggedIn() { return current != null; } // Indica se existe um utilizador autenticado
    public static void logout() { current = null; } // Termina a sessão (remove as chaves da memória)
} // Fim da classe Session
