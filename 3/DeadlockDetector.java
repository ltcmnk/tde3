import java.lang.management.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DeadlockDetector {
    
    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    static volatile boolean deadlockDetected = false;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║     DETECÇÃO DE DEADLOCK EM TEMPO DE EXECUÇÃO       ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Este programa irá:");
        System.out.println("  1. Criar um deadlock intencional");
        System.out.println("  2. Detectar o deadlock automaticamente");
        System.out.println("  3. Gerar relatório detalhado");
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println();
        
        // Iniciar monitor de deadlock em background
        Thread monitor = new Thread(() -> {
            ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
            
            while (!deadlockDetected) {
                try {
                    Thread.sleep(1000);  // Verificar a cada 1 segundo
                    
                    long[] deadlockedIds = tmx.findDeadlockedThreads();
                    
                    if (deadlockedIds != null && deadlockedIds.length > 0) {
                        deadlockDetected = true;
                        
                        System.out.println();
                        System.out.println("════════════════════════════════════════════════════════");
                        System.out.println();
                        System.out.println("🚨 DEADLOCK DETECTADO!");
                        System.out.println();
                        
                        gerarRelatorioDeadlock(tmx, deadlockedIds);
                        
                        System.out.println("════════════════════════════════════════════════════════");
                        System.out.println();
                        System.out.println("Encerrando programa em 2 segundos...");
                        Thread.sleep(2000);
                        System.exit(0);
                    }
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "DeadlockMonitor");
        
        monitor.setDaemon(true);
        monitor.start();
        
        log("MAIN", "Iniciando threads que causarão deadlock...");
        
        // Thread 1: LOCK_A → LOCK_B
        Thread thread1 = new Thread(() -> {
            log("Thread-1", "Iniciada");
            log("Thread-1", "Adquirindo LOCK_A...");
            
            synchronized (LOCK_A) {
                log("Thread-1", "✓ LOCK_A adquirido");
                dormir(100);  // Dar tempo para Thread-2 adquirir LOCK_B
                
                log("Thread-1", "Tentando LOCK_B...");
                synchronized (LOCK_B) {
                    log("Thread-1", "✓ LOCK_B adquirido");
                }
            }
            
        }, "Thread-1-Worker");
        
        // Thread 2: LOCK_B → LOCK_A (ordem invertida = deadlock)
        Thread thread2 = new Thread(() -> {
            log("Thread-2", "Iniciada");
            log("Thread-2", "Adquirindo LOCK_B...");
            
            synchronized (LOCK_B) {
                log("Thread-2", "✓ LOCK_B adquirido");
                dormir(100);  // Dar tempo para Thread-1 adquirir LOCK_A
                
                log("Thread-2", "Tentando LOCK_A...");
                synchronized (LOCK_A) {
                    log("Thread-2", "✓ LOCK_A adquirido");
                }
            }
            
        }, "Thread-2-Worker");
        
        thread1.start();
        thread2.start();
        
        // Aguardar threads (elas nunca terminarão devido ao deadlock)
        thread1.join();
        thread2.join();
    }
    
    /**
     * Gera relatório detalhado do deadlock detectado
     */
    static void gerarRelatorioDeadlock(ThreadMXBean tmx, long[] deadlockedIds) {
        System.out.println("📊 RELATÓRIO DE DEADLOCK");
        System.out.println();
        System.out.println("Número de threads em deadlock: " + deadlockedIds.length);
        System.out.println();
        
        // Obter informações detalhadas de cada thread
        ThreadInfo[] threadInfos = tmx.getThreadInfo(deadlockedIds, true, true);
        
        // Mapear threads e locks envolvidos
        Map<Long, String> threadNames = new HashMap<>();
        Map<Long, String> waitingFor = new HashMap<>();
        Map<Long, List<String>> holding = new HashMap<>();
        
        for (ThreadInfo info : threadInfos) {
            if (info != null) {
                long tid = info.getThreadId();
                threadNames.put(tid, info.getThreadName());
                
                // Lock que a thread está esperando
                LockInfo lockInfo = info.getLockInfo();
                if (lockInfo != null) {
                    waitingFor.put(tid, formatLock(lockInfo));
                }
                
                // Locks que a thread possui
                MonitorInfo[] monitors = info.getLockedMonitors();
                List<String> heldLocks = new ArrayList<>();
                for (MonitorInfo monitor : monitors) {
                    heldLocks.add(formatLock(monitor));
                }
                holding.put(tid, heldLocks);
            }
        }
        
        // Exibir informações de cada thread
        System.out.println("─────────────────────────────────────────────────────────");
        for (ThreadInfo info : threadInfos) {
            if (info != null) {
                System.out.println();
                System.out.println("Thread: " + info.getThreadName());
                System.out.println("  ID: " + info.getThreadId());
                System.out.println("  Estado: " + info.getThreadState());
                
                List<String> heldLocks = holding.get(info.getThreadId());
                if (heldLocks != null && !heldLocks.isEmpty()) {
                    System.out.println("  Possui locks:");
                    for (String lock : heldLocks) {
                        System.out.println("    ✓ " + lock);
                    }
                }
                
                String waiting = waitingFor.get(info.getThreadId());
                if (waiting != null) {
                    System.out.println("  Aguardando lock:");
                    System.out.println("    ⏳ " + waiting);
                    
                    // Encontrar qual thread possui esse lock
                    LockInfo lockInfo = info.getLockInfo();
                    if (lockInfo != null) {
                        long ownerTid = info.getLockOwnerId();
                        if (ownerTid >= 0) {
                            String ownerName = threadNames.get(ownerTid);
                            System.out.println("    Possuído por: " + ownerName);
                        }
                    }
                }
                
                System.out.println();
                System.out.println("  Stack Trace:");
                StackTraceElement[] stack = info.getStackTrace();
                for (int i = 0; i < Math.min(stack.length, 5); i++) {
                    System.out.println("    at " + stack[i]);
                }
                if (stack.length > 5) {
                    System.out.println("    ... (" + (stack.length - 5) + " more)");
                }
            }
        }
        
        // Exibir ciclo de deadlock
        System.out.println();
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println();
        System.out.println("🔄 CICLO DE DEADLOCK:");
        System.out.println();
        
        for (ThreadInfo info : threadInfos) {
            if (info != null) {
                String threadName = info.getThreadName();
                String waitLock = waitingFor.get(info.getThreadId());
                
                System.out.println("  " + threadName + " aguarda " + waitLock);
                
                // Encontrar quem possui esse lock
                long ownerTid = info.getLockOwnerId();
                if (ownerTid >= 0) {
                    String ownerName = threadNames.get(ownerTid);
                    System.out.println("    ↓");
                    System.out.println("  possuído por " + ownerName);
                    System.out.println("    ↓");
                }
            }
        }
        System.out.println("  (volta ao início = CICLO)");
        
        System.out.println();
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println();
        System.out.println("✅ CONDIÇÕES DE COFFMAN SATISFEITAS:");
        System.out.println("  1. Exclusão Mútua:  ✓ (synchronized)");
        System.out.println("  2. Hold-and-Wait:   ✓ (segura um, espera outro)");
        System.out.println("  3. Não Preempção:   ✓ (locks não podem ser roubados)");
        System.out.println("  4. Espera Circular: ✓ (ciclo detectado acima)");
        System.out.println();
    }
    
    /**
     * Formata informação de lock para exibição
     */
    static String formatLock(LockInfo lock) {
        String className = lock.getClassName();
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        return simpleName + "@" + Integer.toHexString(lock.getIdentityHashCode());
    }
    
    /**
     * Log formatado
     */
    static void log(String source, String message) {
        String time = LocalTime.now().format(TIME_FORMAT);
        System.out.printf("[%s] %-15s %s%n", time, source + ":", message);
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
