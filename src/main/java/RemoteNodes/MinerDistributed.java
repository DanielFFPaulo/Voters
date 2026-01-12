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
 *
 * @author Acer
 */
public class MinerDistributed {
        public static void main(String[] args) {
        String msg = "Transaction 7";
        MinerDistributed miner = new MinerDistributed();

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
        int n = miner.mine(msg, 4);
        System.out.println("Message = " + msg);
        System.out.println("Nonce = " + miner.nonce);
        System.out.println("Hash = " + getHash(msg + n));

    }

    //shared objects
    AtomicInteger nonce = new AtomicInteger(0); //nonce of message
    AtomicBoolean isWorking = new AtomicBoolean();
    static AtomicBoolean isChampion = new AtomicBoolean();
    static MinerListener listener;
    String message;

    public void addListener(MinerListener listener) {
        this.listener = listener;
    }

    public void stopMining(int number) {
        isWorking.set(false);
        nonce.set(number);
        if(listener !=null){
            listener.onStopMining(number);
        }else{
            System.out.println("Stoping miner .. " + nonce);
        }
    }

    public int getNonce() {
        return nonce.get();
    }

    public boolean isMining() {
        return isWorking.get();
    }

    public boolean isWinner() {
        return isChampion.get();
    }

    public int mine(String msg, int dificulty) {
        this.message = msg;
        try {
            if (listener != null) {
                listener.onStartMining(msg, dificulty);
            } else {
                System.out.println("Start Mining " + dificulty + "\t" + msg);
            }
            //shared objects
            isWorking.set(true);
            isChampion.set(false);
            nonce = new AtomicInteger(0); //nonce of message
            Random rnd = new Random();
            AtomicInteger ticket = new AtomicInteger(Math.abs(rnd.nextInt())); // tickets to numbers
            MinerThr thr[] = new MinerThr[Runtime.getRuntime().availableProcessors()];
            for (int i = 0; i < thr.length; i++) {
                thr[i] = new MinerThr(nonce, ticket, dificulty, msg);
                thr[i].start();
            }
            thr[0].join();
            return nonce.get();
        } catch (InterruptedException ex) {
            return 0;
        }

    }

    private static class MinerThr extends Thread {

        AtomicInteger trueNonce; //nounce found
        AtomicInteger numberTicket; // number to test
        int dificulty; // number of zeros
        String message; // message to mine

        public MinerThr(AtomicInteger nonce, AtomicInteger ticket, int dificulty, String msg) {
            this.trueNonce = nonce;
            this.numberTicket = ticket;
            this.dificulty = dificulty;
            this.message = msg;
        }

        @Override
        public void run() {
            //zeros to find in hash
            String zeros = String.format("%0" + dificulty + "d", 0);
            //nounce not found
            while (trueNonce.get() == 0) {
                //get a number to test
                int n = numberTicket.getAndIncrement();
                //calculate hash with nonce
                String hash = getHash(message + n);
                //starts with zeros
                if (hash.startsWith(zeros)) {
                    isChampion.set(true);
                    //update true nonce
                    trueNonce.set(n);
                    if (listener != null) {
                        listener.onNonceFound(n);
                    } else {
                        System.out.println(Thread.currentThread().getName() + " found nonce " + n);
                        System.out.println("Hash " + hash);
                    }

                }
            }
            System.out.println(Thread.currentThread().getName() + " stop ");
        }

    }

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
