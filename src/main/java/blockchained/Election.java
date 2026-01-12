/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

/**
 *
 * @author Acer
 */
public class Election {
        private String electionId;
    private String title;
    private long startTime;
    private long endTime;
    private boolean active;
    
    public Election(String electionId, String title){
        this.electionId = electionId;
        this.title = title;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (24 * 60 * 60 * 1000); // 24 horas
        this.active = true;
        System.out.println(this.active);
    }
    
    public boolean isActive() {
        long now = System.currentTimeMillis();
        return active && now >= startTime && now <= endTime;
    }
    
    public void close() {
        this.active = false;
        System.out.println("CLOSED ELECTION "+ title);
    }
    
    
    public String getElectionId() { return electionId; }
    public String getTitle() { return title; }
}
