import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DeadlockDemo {
    
    // Dois locks que serão adquiridos em ordens diferentes
    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();
    
    // Formatador para timestamps nos logs
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║     DEMONSTRAÇÃO DE DEADLOCK CLÁSSICO               ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("⚠️  AVISO: Este programa IRÁ TRAVAR (deadlock)");
        System.out.println("    Pressione Ctrl+C para encerrar quando observar o travamento.");
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println();
        
        // Thread 1: Tenta adquirir A → B
        Thread thread1 = new Thread(() -> {
            log("Thread-1", "INICIADA");
            
            log("Thread-1", "Tentando adquirir LOCK_A...");
            synchronized (LOCK_A) {
                log("Thread-1", "✓ LOCK_A ADQUIRIDO");
                
                // Aguarda para dar tempo da Thread-2 adquirir LOCK_B
                log("Thread-1", "Aguardando 50ms (para sincronizar com Thread-2)...");
                dormir(50);
                
                log("Thread-1", "Tentando adquirir LOCK_B...");
                log("Thread-1", "⏳ BLOQUEADA aguardando LOCK_B (ocupado por Thread-2)");
                
                // ❌ DEADLOCK ACONTECE AQUI!
                // Thread-1 fica bloqueada indefinidamente porque Thread-2 possui LOCK_B
                // e está aguardando LOCK_A (que Thread-1 possui)
                synchronized (LOCK_B) {
                    log("Thread-1", "✓ LOCK_B ADQUIRIDO");
                    log("Thread-1", "🎉 CONCLUIU COM SUCESSO");
                }
                
                log("Thread-1", "Liberando LOCK_B");
            }
            log("Thread-1", "Liberando LOCK_A");
            
        }, "Thread-1");
        
        // Thread 2: Tenta adquirir B → A (ordem invertida!)
        Thread thread2 = new Thread(() -> {
            log("Thread-2", "INICIADA");
            
            log("Thread-2", "Tentando adquirir LOCK_B...");
            synchronized (LOCK_B) {
                log("Thread-2", "✓ LOCK_B ADQUIRIDO");
                
                // Aguarda para dar tempo da Thread-1 adquirir LOCK_A
                log("Thread-2", "Aguardando 50ms (para sincronizar com Thread-1)...");
                dormir(50);
                
                log("Thread-2", "Tentando adquirir LOCK_A...");
                log("Thread-2", "⏳ BLOQUEADA aguardando LOCK_A (ocupado por Thread-1)");
                
                // ❌ DEADLOCK ACONTECE AQUI!
                // Thread-2 fica bloqueada indefinidamente porque Thread-1 possui LOCK_A
                // e está aguardando LOCK_B (que Thread-2 possui)
                synchronized (LOCK_A) {
                    log("Thread-2", "✓ LOCK_A ADQUIRIDO");
                    log("Thread-2", "🎉 CONCLUIU COM SUCESSO");
                }
                
                log("Thread-2", "Liberando LOCK_A");
            }
            log("Thread-2", "Liberando LOCK_B");
            
        }, "Thread-2");
        
        // Iniciar ambas as threads
        thread1.start();
        thread2.start();
        
        // Aguardar 3 segundos e verificar se threads ainda estão vivas
        dormir(3000);
        
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println();
        
        if (thread1.isAlive() && thread2.isAlive()) {
            System.out.println("❌ DEADLOCK DETECTADO!");
            System.out.println();
            System.out.println("📊 ANÁLISE:");
            System.out.println("  • Thread-1: Possui LOCK_A, aguarda LOCK_B");
            System.out.println("  • Thread-2: Possui LOCK_B, aguarda LOCK_A");
            System.out.println();
            System.out.println("🔄 CICLO DE ESPERA:");
            System.out.println("  Thread-1 → LOCK_B → Thread-2 → LOCK_A → Thread-1");
            System.out.println();
            System.out.println("✅ CONDIÇÕES DE COFFMAN SATISFEITAS:");
            System.out.println("  1. Exclusão Mútua:    ✓ synchronized");
            System.out.println("  2. Hold-and-Wait:     ✓ Segura um, espera outro");
            System.out.println("  3. Não Preempção:     ✓ Locks não podem ser roubados");
            System.out.println("  4. Espera Circular:   ✓ T1 → T2 → T1");
            System.out.println();
            System.out.println("💡 SOLUÇÃO:");
            System.out.println("  Veja DeadlockCorrigido.java para a versão corrigida");
            System.out.println("  usando Hierarquia de Recursos (A < B).");
            System.out.println();
            System.out.println("🔍 PARA INSPECIONAR:");
            System.out.println("  Em outro terminal:");
            System.out.println("    jps                    # Encontrar PID");
            System.out.println("    jstack <PID>           # Thread dump");
            System.out.println();
            System.out.println("⚠️  Pressione Ctrl+C para encerrar o programa.");
            
        } else {
            System.out.println("⚠️  INESPERADO: Uma ou ambas threads terminaram.");
            System.out.println("    O deadlock deveria ter ocorrido.");
            System.out.println("    Execute novamente ou ajuste os timings.");
        }
        
        // Aguardar indefinidamente (deadlock mantém threads vivas)
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
