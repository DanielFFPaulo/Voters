package blockchained;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Acer
 */
public class Miner {
    static AtomicInteger ticket = new AtomicInteger(0);
    static AtomicInteger trueNonce = new AtomicInteger(-1);
    static AtomicBoolean wasFound = new AtomicBoolean(false);
    
    public static int MAX_NONCE = (int)1E9;
    
    public static class Thrd extends Thread{
        
        String data, zeros;
        int nonce;

        public Thrd(String data, int difficulty) {
            this.data = data;
            this.zeros = String.format("%0" + difficulty + "d", 0);
        }
        
        @Override
        public void run(){
            while(!wasFound.get()){
                try {
                    nonce = ticket.getAndIncrement();
                    
                    if(nonce >= MAX_NONCE){
                        return;
                    }

                    String hash = Hash.calculateHash(nonce + data);
                    
                    if(hash.startsWith(zeros)){
                        if(wasFound.compareAndSet(false, true)){
                            trueNonce.set(nonce);
                        }
                        return;
                    }
                } catch (Exception ex) {
                    System.getLogger(Miner.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }
    }
    
    public static int getNonce(String data, int difficulty) throws InterruptedException{
        // CRITICAL FIX: Reset static variables before each mining operation
        ticket.set(0);
        trueNonce.set(-1);
        wasFound.set(false);
        
        int procs = Runtime.getRuntime().availableProcessors();
        Thrd[] threads = new Thrd[procs];
        
        for (int i = 0; i < procs; i++) {
            threads[i] = new Thrd(data, difficulty);
        }
        
        for (Thrd thread : threads) {
            thread.start();
        }
        
        for (Thrd thread : threads) {
            thread.join();
        }
        
        return trueNonce.get();
    }
}