import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DeadlockCorrigido {
    
    // Mesmos locks que a versão com deadlock
    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();
    
    // Formatador para timestamps
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║     CORREÇÃO DE DEADLOCK - HIERARQUIA DE RECURSOS   ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📏 ORDEM GLOBAL DEFINIDA: LOCK_A < LOCK_B");
        System.out.println("📋 REGRA: Todas as threads adquirem LOCK_A antes de LOCK_B");
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println();
        
        // Thread 1: Adquire A → B (já estava correto)
        Thread thread1 = new Thread(() -> {
            log("Thread-1", "INICIADA");
            log("Thread-1", "Ordem de aquisição: LOCK_A → LOCK_B (conforme hierarquia)");
            
            log("Thread-1", "Tentando adquirir LOCK_A...");
            synchronized (LOCK_A) {
                log("Thread-1", "✓ LOCK_A ADQUIRIDO");
                
                log("Thread-1", "Aguardando 50ms...");
                dormir(50);
                
                log("Thread-1", "Tentando adquirir LOCK_B...");
                synchronized (LOCK_B) {
                    log("Thread-1", "✓ LOCK_B ADQUIRIDO");
                    
                    // Trabalho na seção crítica
                    log("Thread-1", "Executando trabalho crítico...");
                    dormir(100);
                    
                    log("Thread-1", "🎉 CONCLUIU COM SUCESSO");
                }
                log("Thread-1", "Liberou LOCK_B");
            }
            log("Thread-1", "Liberou LOCK_A");
            
        }, "Thread-1");
        
        // Thread 2: Agora também adquire A → B (CORRIGIDO!)
        Thread thread2 = new Thread(() -> {
            log("Thread-2", "INICIADA");
            log("Thread-2", "Ordem de aquisição: LOCK_A → LOCK_B (conforme hierarquia)");
            
            log("Thread-2", "Tentando adquirir LOCK_A...");
            // ✅ MUDANÇA CRÍTICA: Agora tenta LOCK_A primeiro (não LOCK_B)
            synchronized (LOCK_A) {
                log("Thread-2", "✓ LOCK_A ADQUIRIDO");
                
                log("Thread-2", "Aguardando 50ms...");
                dormir(50);
                
                log("Thread-2", "Tentando adquirir LOCK_B...");
                synchronized (LOCK_B) {
                    log("Thread-2", "✓ LOCK_B ADQUIRIDO");
                    
                    // Trabalho na seção crítica
                    log("Thread-2", "Executando trabalho crítico...");
                    dormir(100);
                    
                    log("Thread-2", "🎉 CONCLUIU COM SUCESSO");
                }
                log("Thread-2", "Liberou LOCK_B");
            }
            log("Thread-2", "Liberou LOCK_A");
            
        }, "Thread-2");
        
        // Medir tempo de execução
        long startTime = System.currentTimeMillis();
        
        // Iniciar ambas as threads
        thread1.start();
        thread2.start();
        
        // Aguardar conclusão
        thread1.join();
        thread2.join();
        
        long endTime = System.currentTimeMillis();
        double elapsed = (endTime - startTime) / 1000.0;
        
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("✅ SUCESSO! Ambas threads concluíram sem deadlock.");
        System.out.printf("⏱️  Tempo total: %.3f segundos%n", elapsed);
        System.out.println();
        System.out.println("📊 ANÁLISE DA CORREÇÃO:");
        System.out.println();
        System.out.println("ANTES (Deadlock):");
        System.out.println("  Thread-1: LOCK_A → LOCK_B");
        System.out.println("  Thread-2: LOCK_B → LOCK_A  ❌ (ordem invertida)");
        System.out.println("  Resultado: Espera circular → Deadlock");
        System.out.println();
        System.out.println("DEPOIS (Corrigido):");
        System.out.println("  Thread-1: LOCK_A → LOCK_B  ✅");
        System.out.println("  Thread-2: LOCK_A → LOCK_B  ✅ (mesma ordem)");
        System.out.println("  Resultado: Sem ciclo → Sem deadlock");
        System.out.println();
        System.out.println("🔍 EXECUÇÃO REAL:");
        System.out.println("  1. Thread-1 adquire LOCK_A primeiro");
        System.out.println("  2. Thread-2 tenta LOCK_A → BLOQUEIA (Thread-1 tem)");
        System.out.println("  3. Thread-1 adquire LOCK_B → trabalha → libera B e A");
        System.out.println("  4. Thread-2 desbloqueia → adquire LOCK_A");
        System.out.println("  5. Thread-2 adquire LOCK_B → trabalha → libera B e A");
        System.out.println("  6. ✅ Ambas terminam com sucesso!");
        System.out.println();
        System.out.println("✅ CONDIÇÕES DE COFFMAN (Análise):");
        System.out.println("  1. Exclusão Mútua:    ✓ Presente (necessária)");
        System.out.println("  2. Hold-and-Wait:     ✓ Presente (necessária)");
        System.out.println("  3. Não Preempção:     ✓ Presente (necessária)");
        System.out.println("  4. Espera Circular:   ❌ ELIMINADA (hierarquia)");
        System.out.println();
        System.out.println("💡 LIÇÃO:");
        System.out.println("  Negar UMA condição de Coffman é suficiente para prevenir deadlock.");
        System.out.println("  Hierarquia de recursos é elegante, eficiente e confiável.");
        System.out.println();
        System.out.println("🔗 RELAÇÃO COM JANTAR DOS FILÓSOFOS:");
        System.out.println("  Mesma solução! Garfos ordenados (min → max)");
        System.out.println("  Princípio geral aplicável a muitos problemas de deadlock.");
        System.out.println();
    }
    
    /**
     * Log formatado com timestamp e nome da thread
     */
    static void log(String threadName, String message) {
        String time = LocalTime.now().format(TIME_FORMAT);
        System.out.printf("[%s] %-10s %s%n", time, threadName + ":", message);
    }
    
    /**
     * Dormir sem propagar InterruptedException
     */
    static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
