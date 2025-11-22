import java.util.concurrent.*;

public class CorridaComSemaphore {
    
    // Variável compartilhada entre todas as threads
    static int count = 0;
    
    // Semáforo binário FIFO para exclusão mútua
    // - Parâmetro 1: Uma permissão inicial (binário)
    // - Parâmetro true: Modo justo/FIFO (garante ordem de aquisição)
    static final Semaphore sem = new Semaphore(1, true);
    
    public static void main(String[] args) throws Exception {
        // Configuração do experimento
        int T = 8;          // Número de threads concorrentes
        int M = 250_000;    // Número de incrementos por thread
        
        System.out.println("=== CORREÇÃO COM SEMÁFORO BINÁRIO ===");
        System.out.println("Threads: " + T);
        System.out.println("Incrementos por thread: " + M);
        System.out.println("Total esperado: " + (T * M));
        System.out.println("Modo: FIFO (fair = true)");
        System.out.println("====================================\n");
        
        // Criar pool de threads
        ExecutorService pool = Executors.newFixedThreadPool(T);
        
        // Task que cada thread executará
        Runnable incrementTask = () -> {
            for (int i = 0; i < M; i++) {
                try {
                    // ============================================
                    // ADQUIRIR PERMISSÃO (entrada da seção crítica)
                    // ============================================
                    // Se permissão disponível: decrementa e prossegue
                    // Se não disponível: thread BLOQUEIA até release()
                    sem.acquire();
                    
                    try {
                        // =====================================
                        // SEÇÃO CRÍTICA (protegida pelo semáforo)
                        // =====================================
                        // Apenas UMA thread executa isso por vez
                        // count++ agora é "atomizado" pela exclusão mútua
                        count++;
                        
                    } finally {
                        // ============================================
                        // LIBERAR PERMISSÃO (saída da seção crítica)
                        // ============================================
                        // SEMPRE executado, mesmo se houver exceção
                        // Incrementa permissões e desbloqueia próxima thread
                        sem.release();
                    }
                    
                } catch (InterruptedException e) {
                    // acquire() pode lançar InterruptedException
                    // se a thread for interrompida enquanto espera
                    
                    // Boa prática: restaurar flag de interrupção
                    Thread.currentThread().interrupt();
                    
                    // Sair do loop (não continuar incrementando)
                    break;
                }
            }
        };
        
        // Iniciar medição de tempo
        long startTime = System.nanoTime();
        
        // Submeter T tarefas ao pool
        for (int i = 0; i < T; i++) {
            pool.submit(incrementTask);
        }
        
        // Iniciar shutdown ordenado (não aceita novas tasks)
        pool.shutdown();
        
        // Aguardar conclusão de todas as tasks (máximo 1 minuto)
        pool.awaitTermination(1, TimeUnit.MINUTES);
        
        // Finalizar medição de tempo
        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;
        
        // Calcular estatísticas
        int expected = T * M;
        int obtained = count;
        double throughput = obtained / elapsedSeconds;
        
        // Exibir resultados
        System.out.println("=== RESULTADOS ===");
        System.out.printf("Esperado:   %,d%n", expected);
        System.out.printf("Obtido:     %,d%n", obtained);
        System.out.printf("Correção:   %s%n", (obtained == expected ? "✅ 100%" : "❌ ERRO"));
        System.out.printf("Tempo:      %.3f segundos%n", elapsedSeconds);
        System.out.printf("Throughput: %,.0f ops/s%n", throughput);
        System.out.println("==================\n");
        
        // Análise
        if (obtained == expected) {
            System.out.println("✅ SUCESSO! Todos os incrementos foram preservados.");
            System.out.println("O semáforo garantiu exclusão mútua corretamente.\n");
            
            System.out.println("📊 ANÁLISE:");
            System.out.println("  • Atomicidade: ✅ Garantida (um thread por vez)");
            System.out.println("  • Visibilidade: ✅ Happens-before entre release/acquire");
            System.out.println("  • Fairness: ✅ FIFO evita starvation");
            System.out.println("  • Custo: ⚠️  ~70x mais lento (serialização de acesso)\n");
            
            System.out.println("💡 OTIMIZAÇÃO POSSÍVEL:");
            System.out.println("Para contadores simples, considere usar:");
            System.out.println("  java.util.concurrent.atomic.AtomicInteger");
            System.out.println("(Operações lock-free, ~19x mais rápido que semáforo)\n");
            
        } else {
            System.out.println("❌ ERRO INESPERADO! O semáforo não funcionou corretamente.");
            System.out.println("Verifique se há algum bug na implementação.\n");
        }
    }
}
