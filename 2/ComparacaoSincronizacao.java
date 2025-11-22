import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ComparacaoSincronizacao {
    
    // Configuração do experimento
    static final int T = 8;          // Threads
    static final int M = 250_000;    // Incrementos por thread
    static final int EXPECTED = T * M;
    
    public static void main(String[] args) throws Exception {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  COMPARAÇÃO DE MECANISMOS DE SINCRONIZAÇÃO            ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.printf("Threads: %d | Incrementos/thread: %,d | Total: %,d%n%n", 
                          T, M, EXPECTED);
        
        // Executar cada teste
        test1_SemSincronizacao();
        test2_SemaphoreFair();
        test3_SemaphoreUnfair();
        test4_AtomicInteger();
        test5_Synchronized();
        test6_ReentrantLockFair();
        test7_ReentrantLockUnfair();
        
        // Resumo final
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║  RESUMO E RECOMENDAÇÕES                               ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("🏆 MELHOR ESCOLHA: AtomicInteger");
        System.out.println("   → Correto, rápido (~19x mais que Semaphore), lock-free\n");
        System.out.println("📋 QUANDO USAR CADA UM:");
        System.out.println("   • AtomicInteger:     Operações atômicas simples");
        System.out.println("   • synchronized:      Seções críticas pequenas e simples");
        System.out.println("   • ReentrantLock:     Necessita tryLock, interruptible, etc.");
        System.out.println("   • Semaphore:         Controlar pool de recursos (N > 1)");
        System.out.println("   • Sem sincronização: ❌ NUNCA em produção!\n");
    }
    
    // ========================================================================
    // TESTE 1: SEM SINCRONIZAÇÃO (Race Condition)
    // ========================================================================
    static void test1_SemSincronizacao() throws Exception {
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("TEST 1: Sem Sincronização (Race Condition)");
        System.out.println("─────────────────────────────────────────────────────────");
        
        int[] count = {0};  // Array para permitir modificação em lambda
        
        long time = runTest(() -> {
            for (int i = 0; i < M; i++) {
                count[0]++;  // RACE CONDITION!
            }
        });
        
        printResults("Sem Sincronização", count[0], time, false);
    }
    
    // ========================================================================
    // TESTE 2: SEMAPHORE BINÁRIO (FAIR)
    // ========================================================================
    static void test2_SemaphoreFair() throws Exception {
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("TEST 2: Semaphore(1, true) - Fair/FIFO");
        System.out.println("─────────────────────────────────────────────────────────");
        
        int[] count = {0};
        Semaphore sem = new Semaphore(1, true);
        
        long time = runTest(() -> {
            for (int i = 0; i < M; i++) {
                try {
                    sem.acquire();
                    try {
                        count[0]++;
                    } finally {
                        sem.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        printResults("Semaphore Fair", count[0], time, true);
    }
    
    // ========================================================================
    // TESTE 3: SEMAPHORE BINÁRIO (UNFAIR)
    // ========================================================================
    static void test3_SemaphoreUnfair() throws Exception {
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("TEST 3: Semaphore(1, false) - Unfair");
        System.out.println("─────────────────────────────────────────────────────────");
        
        int[] count = {0};
        Semaphore sem = new Semaphore(1, false);
        
        long time = runTest(() -> {
            for (int i = 0; i < M; i++) {
                try {
                    sem.acquire();
                    try {
                        count[0]++;
                    } finally {
                        sem.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        printResults("Semaphore Unfair", count[0], time, true);
    }
    
    // ========================================================================
    // TESTE 4: ATOMICINTEGER (Recomendado)
    // ========================================================================
    static void test4_AtomicInteger() throws Exception {
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("TEST 4: AtomicInteger (Lock-Free)");
        System.out.println("─────────────────────────────────────────────────────────");
        
        AtomicInteger count = new AtomicInteger(0);
        
        long time = runTest(() -> {
            for (int i = 0; i < M; i++) {
                count.incrementAndGet();  // Operação atômica via CAS
            }
        });
        
        printResults("AtomicInteger", count.get(), time, true);
        System.out.println("💡 Usa Compare-And-Swap (CAS) - instruções atômicas de hardware");
    }
    
    // ========================================================================
    // TESTE 5: SYNCHRONIZED
    // ========================================================================
    static void test5_Synchronized() throws Exception {
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("TEST 5: synchronized block");
        System.out.println("─────────────────────────────────────────────────────────");
        
        int[] count = {0};
        Object lock = new Object();
        
        long time = runTest(() -> {
            for (int i = 0; i < M; i++) {
                synchronized (lock) {
                    count[0]++;
                }
            }
        });
        
        printResults("synchronized", count[0], time, true);
    }
    
    // ========================================================================
    // TESTE 6: REENTRANTLOCK (FAIR)
    // ========================================================================
    static void test6_ReentrantLockFair() throws Exception {
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("TEST 6: ReentrantLock(true) - Fair");
        System.out.println("─────────────────────────────────────────────────────────");
        
        int[] count = {0};
        ReentrantLock lock = new ReentrantLock(true);
        
        long time = runTest(() -> {
            for (int i = 0; i < M; i++) {
                lock.lock();
                try {
                    count[0]++;
                } finally {
                    lock.unlock();
                }
            }
        });
        
        printResults("ReentrantLock Fair", count[0], time, true);
    }
    
    // ========================================================================
    // TESTE 7: REENTRANTLOCK (UNFAIR)
    // ========================================================================
    static void test7_ReentrantLockUnfair() throws Exception {
        System.out.println("─────────────────────────────────────────────────────────");
        System.out.println("TEST 7: ReentrantLock(false) - Unfair");
        System.out.println("─────────────────────────────────────────────────────────");
        
        int[] count = {0};
        ReentrantLock lock = new ReentrantLock(false);
        
        long time = runTest(() -> {
            for (int i = 0; i < M; i++) {
                lock.lock();
                try {
                    count[0]++;
                } finally {
                    lock.unlock();
                }
            }
        });
        
        printResults("ReentrantLock Unfair", count[0], time, true);
    }
    
    // ========================================================================
    // FUNÇÕES AUXILIARES
    // ========================================================================
    
    /**
     * Executa um teste com T threads executando a task fornecida
     */
    static long runTest(Runnable task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(T);
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < T; i++) {
            pool.submit(task);
        }
        
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
        
        long endTime = System.nanoTime();
        return endTime - startTime;
    }
    
    /**
     * Exibe resultados formatados
     */
    static void printResults(String name, int obtained, long nanoTime, boolean shouldBeCorrect) {
        double seconds = nanoTime / 1_000_000_000.0;
        double throughput = obtained / seconds;
        boolean isCorrect = (obtained == EXPECTED);
        
        System.out.printf("Esperado:   %,10d%n", EXPECTED);
        System.out.printf("Obtido:     %,10d ", obtained);
        
        if (shouldBeCorrect) {
            System.out.println(isCorrect ? "✅" : "❌");
        } else {
            double correctness = (obtained * 100.0) / EXPECTED;
            System.out.printf("(%.1f%% correto) ❌%n", correctness);
        }
        
        System.out.printf("Tempo:      %10.3f s%n", seconds);
        System.out.printf("Throughput: %,10.0f ops/s%n", throughput);
        
        if (!isCorrect && shouldBeCorrect) {
            System.out.println("⚠️  AVISO: Resultado incorreto!");
        }
        
        System.out.println();
    }
}
