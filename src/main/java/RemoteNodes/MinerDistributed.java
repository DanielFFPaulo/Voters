/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RemoteNodes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Classe para mineração distribuída usando múltiplos threads
 * Implementa um sistema de prova de trabalho (Proof of Work) para encontrar um nonce
 * que gera um hash com um número específico de zeros no início
 * 
 * @author Acer
 */
public class MinerDistributed {
    
    // Método principal para testar a funcionalidade do minerador
    public static void main(String[] args) {
        String msg = "Transaction 7";
        MinerDistributed miner = new MinerDistributed();

        // Thread separada para simular a paragem da mineração após 1 segundo
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("STOP");
                System.out.println("Mining " + miner.isMining());
                miner.stopMining(20);
            } catch (InterruptedException ex) {
                System.getLogger(MinerDistributed.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

        }).start();
        
        // Inicia a mineração com dificuldade 4 (4 zeros no início do hash)
        int n = miner.mine(msg, 4);
        System.out.println("Message = " + msg);
        System.out.println("Nonce = " + miner.nonce);
        System.out.println("Hash = " + getHash(msg + n));
    }

    // Objetos partilhados entre threads
    AtomicInteger nonce = new AtomicInteger(0); // Nonce da mensagem (número que satisfaz a prova de trabalho)
    AtomicBoolean isWorking = new AtomicBoolean(); // Indica se o minerador está a trabalhar
    static AtomicBoolean isChampion = new AtomicBoolean(); // Indica se este minerador encontrou o nonce
    static MinerListener listener; // Listener para eventos de mineração
    String message; // Mensagem a ser minerada

    /**
     * Adiciona um listener para receber notificações sobre eventos de mineração
     */
    public void addListener(MinerListener listener) {
        this.listener = listener;
    }

    /**
     * Para a mineração e define o nonce manualmente
     * @param number Nonce a ser definido
     */
    public void stopMining(int number) {
        isWorking.set(false);
        nonce.set(number);
        if(listener != null){
            listener.onStopMining(number);
        } else {
            System.out.println("Stoping miner .. " + nonce);
        }
    }

    /**
     * Obtém o valor atual do nonce
     * @return Valor do nonce
     */
    public int getNonce() {
        return nonce.get();
    }

    /**
     * Verifica se o minerador está atualmente a minerar
     * @return true se estiver a minerar, false caso contrário
     */
    public boolean isMining() {
        return isWorking.get();
    }

    /**
     * Verifica se este minerador foi o vencedor (encontrou o nonce)
     * @return true se for o vencedor, false caso contrário
     */
    public boolean isWinner() {
        return isChampion.get();
    }

    /**
     * Inicia o processo de mineração usando múltiplos threads
     * Cada thread testa números diferentes até encontrar um hash válido
     * 
     * @param msg Mensagem a ser minerada
     * @param dificulty Dificuldade (número de zeros no início do hash)
     * @return Nonce que satisfaz a dificuldade especificada
     */
    public int mine(String msg, int dificulty) {
        this.message = msg;
        try {
            // Notifica o início da mineração
            if (listener != null) {
                listener.onStartMining(msg, dificulty);
            } else {
                System.out.println("Start Mining " + dificulty + "\t" + msg);
            }
            
            // Inicializa os objetos partilhados
            isWorking.set(true);
            isChampion.set(false);
            nonce = new AtomicInteger(0); // Reinicia o nonce
            
            // Gera um número aleatório inicial para distribuir o trabalho
            Random rnd = new Random();
            AtomicInteger ticket = new AtomicInteger(Math.abs(rnd.nextInt()));
            
            // Cria uma thread por cada processador disponível
            MinerThr thr[] = new MinerThr[Runtime.getRuntime().availableProcessors()];
            for (int i = 0; i < thr.length; i++) {
                thr[i] = new MinerThr(nonce, ticket, dificulty, msg);
                thr[i].start();
            }
            
            // Aguarda que a primeira thread termine (quando o nonce for encontrado)
            thr[0].join();
            return nonce.get();
        } catch (InterruptedException ex) {
            return 0;
        }
    }

    /**
     * Thread individual que procura o nonce válido
     * Cada thread pega números sequenciais e testa se geram um hash válido
     */
    private static class MinerThr extends Thread {

        AtomicInteger trueNonce; // Nonce encontrado (partilhado entre threads)
        AtomicInteger numberTicket; // Próximo número a testar (partilhado entre threads)
        int dificulty; // Número de zeros necessários no início do hash
        String message; // Mensagem a ser minerada

        public MinerThr(AtomicInteger nonce, AtomicInteger ticket, int dificulty, String msg) {
            this.trueNonce = nonce;
            this.numberTicket = ticket;
            this.dificulty = dificulty;
            this.message = msg;
        }

        @Override
        public void run() {
            // Cria a string de zeros a procurar no início do hash
            String zeros = String.format("%0" + dificulty + "d", 0);
            
            // Continua a procurar enquanto nenhuma thread encontrou o nonce
            while (trueNonce.get() == 0) {
                // Obtém o próximo número a testar de forma atómica
                int n = numberTicket.getAndIncrement();
                
                // Calcula o hash da mensagem concatenada com o número
                String hash = getHash(message + n);
                
                // Verifica se o hash começa com o número necessário de zeros
                if (hash.startsWith(zeros)) {
                    isChampion.set(true);
                    // Atualiza o nonce verdadeiro (isto para todas as threads)
                    trueNonce.set(n);
                    
                    // Notifica o listener ou imprime no console
                    if (listener != null) {
                        listener.onNonceFound(n);
                    } else {
                        System.out.println(Thread.currentThread().getName() + " found nonce " + n);
                        System.out.println("Hash " + hash);
                    }
                }
            }
            // Thread termina quando o nonce é encontrado
            System.out.println(Thread.currentThread().getName() + " stop ");
        }
    }

    /**
     * Calcula o hash SHA3-256 de uma mensagem e retorna em Base64
     * 
     * @param msg Mensagem a ser transformada em hash
     * @return Hash em formato Base64 ou "ERROR" em caso de falha
     */
    public static String getHash(String msg) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA3-256");
            md.update(msg.getBytes());
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException ex) {
            return "ERROR";
        }
    }
}