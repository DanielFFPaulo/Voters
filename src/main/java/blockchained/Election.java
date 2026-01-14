/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package blockchained;

/**
 * Representa uma eleição no sistema de votação blockchain
 * Gere o estado, período de tempo e validação de uma eleição
 * 
 * @author Acer
 */
public class Election {
    private String electionId; // Identificador único da eleição
    private String title; // Título/descrição da eleição
    private long startTime; // Timestamp de início da eleição (em milissegundos)
    private long endTime; // Timestamp de fim da eleição (em milissegundos)
    private boolean active; // Indica se a eleição está ativa
    
    /**
     * Construtor que cria uma nova eleição
     * A eleição é automaticamente ativada e tem duração de 24 horas
     * 
     * @param electionId Identificador único da eleição
     * @param title Título ou descrição da eleição
     */
    public Election(String electionId, String title) {
        this.electionId = electionId;
        this.title = title;
        // Define o tempo de início como o momento atual
        this.startTime = System.currentTimeMillis();
        // Define o tempo de fim como 24 horas após o início
        this.endTime = startTime + (24 * 60 * 60 * 1000); // 24 horas em milissegundos
        // Ativa a eleição por padrão
        this.active = true;
        System.out.println(this.active);
    }
    
    /**
     * Verifica se a eleição está ativa e dentro do período de votação
     * Uma eleição é considerada ativa se:
     * - O flag active estiver true
     * - O tempo atual estiver entre startTime e endTime
     * 
     * @return true se a eleição estiver ativa e dentro do período, false caso contrário
     */
    public boolean isActive() {
        long now = System.currentTimeMillis();
        // Verifica se está ativa E dentro do período de tempo válido
        return active && now >= startTime && now <= endTime;
    }
    
    /**
     * Fecha a eleição manualmente antes do tempo de fim
     * Após fechar, a eleição não aceita mais votos
     */
    public void close() {
        this.active = false;
        System.out.println("CLOSED ELECTION " + title);
    }
    
    /**
     * Obtém o identificador único da eleição
     * 
     * @return ID da eleição
     */
    public String getElectionId() { 
        return electionId; 
    }
    
    /**
     * Obtém o título da eleição
     * 
     * @return Título da eleição
     */
    public String getTitle() { 
        return title; 
    }
}