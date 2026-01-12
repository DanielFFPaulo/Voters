/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package RemoteNodes;

/**
 *
 * @author Acer
 */
public interface NodeListener {
    public void onStart(String message);
    public void onConnect(String address);
    public void onException(Exception e, String title);
    public void onTransaction(String transaction);
}
