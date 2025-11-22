import java.util.concurrent.*;

public class CorridaSemControle {
    
    // Variável compartilhada entre todas as threads
    // PROBLEMA: Sem sincronização, sujeita a race condition
    static int count = 0;
    
    public static void main(String[] args) throws Exception {
        // Configuração do experimento
        int T = 8;          // Número de threads concorrentes
        int M = 250_000;    // Número de incrementos por thread
        
        System.out.println("=== DEMONSTRAÇÃO DE RACE CONDITION ===");
        System.out.println("Threads: " + T);
        System.out.println("Incrementos por thread: " + M);
        System.out.println("Total esperado: " + (T * M));
        System.out.println("=====================================\n");
        
        // Criar pool de threads
        ExecutorService pool = Executors.newFixedThreadPool(T);
        
        // Task que cada thread executará
        Runnable incrementTask = () -> {
            for (int i = 0; i < M; i++) {
                // CONDIÇÃO DE CORRIDA AQUI!
                // count++ não é atômico, múltiplas threads podem
                // ler o mesmo valor e sobrescrever incrementos
                count++;
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
        int lost = expected - obtained;
        double lossPercentage = (lost * 100.0) / expected;
        
        // Exibir resultados
        System.out.println("=== RESULTADOS ===");
        System.out.printf("Esperado: %,d%n", expected);
        System.out.printf("Obtido:   %,d%n", obtained);
        System.out.printf("Perdidos: %,d (%.1f%%)%n", lost, lossPercentage);
        System.out.printf("Tempo:    %.3f segundos%n", elapsedSeconds);
        System.out.println("==================\n");
        
        // Análise
        if (obtained < expected) {
            System.out.println("❌ RACE CONDITION DETECTADA!");
            System.out.println("Incrementos foram perdidos devido à falta de sincronização.");
            System.out.println("Múltiplas threads leram o mesmo valor antes de incrementar,");
            System.out.println("causando sobrescrita e perda de atualizações.\n");
        } else {
            System.out.println("⚠️  Por acaso obteve o valor correto desta vez,");
            System.out.println("mas o resultado ainda é não-determinístico.");
            System.out.println("Execute novamente e provavelmente verá valores diferentes.\n");
        }
        
        System.out.println("SOLUÇÃO: Use sincronização (Semaphore, synchronized, ou AtomicInteger)");
        System.out.println("Veja: CorridaComSemaphore.java para a versão corrigida.\n");
    }
}
