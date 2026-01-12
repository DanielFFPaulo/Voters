/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package RemoteNodes;

/**
 *
 * @author Acer
 */
public interface MinerListener {
    public void onStartMining(String message, int difficulty);
    public void onStopMining(int nonce);
    public void onNonceFound(int nonce);
}
