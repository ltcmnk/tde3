# Parte 3 — Deadlock
## Análise, Reprodução e Correção de Deadlock

**Data:** 21 de Novembro de 2025  
**Tema:** Demonstração de Deadlock e Estratégias de Prevenção

---

## 1. Fundamentos de Deadlock

### 1.1 Definição
**Deadlock** é uma situação em que um conjunto de processos ou threads não pode progredir porque cada um aguarda uma ação (tipicamente a liberação de um recurso) de outro membro do conjunto, formando um ciclo de espera que nunca se resolve.

### 1.2 Condições de Coffman (Necessárias e Suficientes)

Para que um deadlock ocorra, **TODAS as quatro condições** devem estar presentes simultaneamente:

#### 1. Exclusão Mútua (Mutual Exclusion)
- Um recurso só pode ser usado por um processo/thread por vez
- Outros processos devem aguardar até que o recurso seja liberado
- **Exemplo:** Um lock `synchronized` em Java

#### 2. Manter e Esperar (Hold and Wait)
- Um processo que já possui recursos pode solicitar recursos adicionais
- Ele mantém os recursos atuais enquanto aguarda os novos
- **Exemplo:** Thread segura LOCK_A e aguarda por LOCK_B

#### 3. Não Preempção (No Preemption)
- Recursos não podem ser forçadamente retirados de um processo
- Apenas o processo que os possui pode liberá-los voluntariamente
- **Exemplo:** `synchronized` não pode ser "roubado" por outra thread

#### 4. Espera Circular (Circular Wait)
- Existe uma cadeia circular de processos onde cada um aguarda um recurso do próximo
- **Exemplo:** T1 → aguarda recurso de T2 → aguarda recurso de T1

---

## 2. Estratégias de Tratamento de Deadlock

### 2.1 Taxonomia de Abordagens

```
┌─────────────────────────────────────────────────────────────┐
│                 ESTRATÉGIAS DE DEADLOCK                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. IGNORAR (Ostrich Algorithm)                            │
│     └─ Assumir que deadlock é raro e não tratar           │
│     └─ Usado em sistemas não-críticos                     │
│                                                             │
│  2. DETECTAR E RECUPERAR                                   │
│     └─ Permitir deadlock, detectar e quebrar ciclo        │
│     └─ Resource Allocation Graph, timeout                 │
│                                                             │
│  3. PREVENIR (quebrar uma condição necessária)            │
│     └─ Negar Exclusão Mútua: usar recursos compartilháveis│
│     └─ Negar Hold-and-Wait: alocação atômica              │
│     └─ Negar Não-Preempção: permitir preempção            │
│     └─ Negar Espera Circular: HIERARQUIA DE RECURSOS ✓    │
│                                                             │
│  4. EVITAR (análise preditiva)                             │
│     └─ Banker's Algorithm (Dijkstra)                      │
│     └─ Avaliar se alocação leva a estado seguro           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Foco: Prevenção por Hierarquia de Recursos

**Estratégia escolhida:** Negar a condição de **Espera Circular** impondo uma ordem global de aquisição de recursos.

**Mecanismo:**
1. Atribuir ordem total aos recursos (ex.: LOCK_A < LOCK_B)
2. Todos os processos devem adquirir recursos na mesma ordem
3. Impossível formar ciclo de espera

---

## 3. Cenário do Deadlock: Duas Threads, Dois Locks

### 3.1 Diagrama do Problema

```
Estado Inicial (T=0ms):
═══════════════════════
LOCK_A: [LIVRE]
LOCK_B: [LIVRE]
Thread-1: Iniciando
Thread-2: Iniciando


Tempo T=10ms: Ambas threads adquirem primeiro lock
════════════════════════════════════════════════════
Thread-1: synchronized(LOCK_A) ✓ → aguarda 50ms
Thread-2: synchronized(LOCK_B) ✓ → aguarda 50ms

LOCK_A: [OCUPADO por Thread-1]
LOCK_B: [OCUPADO por Thread-2]


Tempo T=60ms: Ambas threads tentam segundo lock
════════════════════════════════════════════════
Thread-1: possui LOCK_A, tenta synchronized(LOCK_B)
Thread-2: possui LOCK_B, tenta synchronized(LOCK_A)

┌───────────┐                    ┌───────────┐
│ Thread-1  │                    │ Thread-2  │
│           │                    │           │
│ possui: A │───── aguarda ─────>│ possui: B │
│ quer: B   │<───── aguarda ─────│ quer: A   │
└───────────┘                    └───────────┘
     ▲                                 │
     │                                 │
     └─────────── CICLO ───────────────┘

❌ DEADLOCK DETECTADO!
Ambas threads ficam bloqueadas indefinidamente.
```

### 3.2 Análise das Condições de Coffman

| Condição | Presente? | Justificativa |
|----------|-----------|---------------|
| **1. Exclusão Mútua** | ✅ SIM | `synchronized` garante que apenas uma thread possua cada lock |
| **2. Hold-and-Wait** | ✅ SIM | Thread-1 segura LOCK_A e espera LOCK_B; Thread-2 segura LOCK_B e espera LOCK_A |
| **3. Não Preempção** | ✅ SIM | Locks não podem ser forçadamente liberados; apenas liberação voluntária |
| **4. Espera Circular** | ✅ SIM | Thread-1 → Thread-2 → Thread-1 (ciclo completo) |

**Conclusão:** Todas as 4 condições satisfeitas → **DEADLOCK INEVITÁVEL**

---

## 4. Análise de Código: Versão com Deadlock

### 4.1 Trace de Execução Detalhado

```java
// Thread-1                          // Thread-2
synchronized (LOCK_A) {              synchronized (LOCK_B) {
  // ✓ Adquire LOCK_A                  // ✓ Adquire LOCK_B
  
  dormir(50);                          dormir(50);
  // Aguarda 50ms                      // Aguarda 50ms
  
  synchronized (LOCK_B) {              synchronized (LOCK_A) {
    // ❌ BLOQUEIA!                      // ❌ BLOQUEIA!
    // LOCK_B ocupado por T2            // LOCK_A ocupado por T1
    // Aguarda indefinidamente...       // Aguarda indefinidamente...
```

### 4.2 Resource Allocation Graph (RAG)

```
Legenda:
  □ = Thread
  ○ = Recurso
  → = Solicitação (request edge)
  ← = Alocação (assignment edge)

Estado de Deadlock:

    ┌──────┐
    │ T1   │
    └───┬──┘
        │
      possui
        │
        ↓
    ┌───────┐        solicita        ┌───────┐
    │LOCK_A │ <──────────────────────┤ T2    │
    └───────┘                        └───┬───┘
                                         │
                                      possui
                                         │
                                         ↓
    ┌───────┐        solicita        ┌───────┐
    │LOCK_B │ <──────────────────────┤ T1    │
    └───────┘                        └───────┘
        │
      possui
        │
        ↓
    ┌──────┐
    │ T2   │
    └──────┘

CICLO DETECTADO: T1 → LOCK_B → T2 → LOCK_A → T1
```

---

## 5. Estratégia de Correção: Hierarquia de Recursos

### 5.1 Princípio
Impor uma **ordem total global** sobre todos os recursos e exigir que todos os processos adquiram recursos **sempre na mesma ordem**.

### 5.2 Por Que Funciona?
Se todos seguem a mesma ordem, é **impossível formar um ciclo** de espera:

```
Prova por Contradição:

Suponha que existe um ciclo:
  P₁ → R_i → P₂ → R_j → P₃ → R_k → ... → Pₙ → R_m → P₁

Onde Pₓ → R_y significa "Pₓ aguarda R_y"

Para que Pₓ aguarde R_y, Pₓ já deve possuir algum recurso R_z.

Pela hierarquia: R_z < R_y (Pₓ adquiriu primeiro o de menor índice)

Aplicando ao ciclo:
  P₁ possui R_a onde R_a < R_i
  P₂ possui R_i onde R_i < R_j
  P₃ possui R_j onde R_j < R_k
  ...
  Pₙ possui R_k onde R_k < R_m

Para fechar o ciclo, Pₙ aguarda R_m e P₁ deve possuir R_m.
Mas P₁ só possui R_a onde R_a < R_i < R_j < ... < R_m

CONTRADIÇÃO: P₁ não pode possuir R_m pois R_a < R_m

∴ Ciclo é impossível → Não pode haver deadlock ∎
```

### 5.3 Aplicação ao Problema

**Ordem Global:** LOCK_A < LOCK_B

**Regra:** Todas as threads devem adquirir LOCK_A antes de LOCK_B

```
Thread-1 (CORRETO):           Thread-2 (CORRETO):
synchronized (LOCK_A) {       synchronized (LOCK_A) {  ← Mudou!
  synchronized (LOCK_B) {       synchronized (LOCK_B) {
    // Trabalho                   // Trabalho
  }                             }
}                             }

Ambas respeitam: A < B
```

### 5.4 Análise da Correção

| Condição | Status na Solução | Comentário |
|----------|-------------------|------------|
| **1. Exclusão Mútua** | ✅ Mantida | Ainda necessária para correção |
| **2. Hold-and-Wait** | ✅ Mantida | Threads ainda seguram um lock e pedem outro |
| **3. Não Preempção** | ✅ Mantida | Locks ainda não podem ser preemptados |
| **4. Espera Circular** | ❌ **ELIMINADA** | Ordem global impede formação de ciclo |

**Conclusão:** Ao negar uma condição necessária (Espera Circular), tornamos deadlock **impossível**.

---

## 6. Comparação: Problema dos Filósofos

### 6.1 Analogia Estrutural

| Aspecto | Filósofos | Duas Threads |
|---------|-----------|--------------|
| **Processos** | 5 filósofos | 2 threads |
| **Recursos** | 5 garfos | 2 locks |
| **Protocolo Ingênuo** | Pega esq → dir | T1: A→B, T2: B→A |
| **Deadlock** | Todos pegam esquerdo | Cada um pega um lock |
| **Solução** | Ordem min→max nos garfos | Ordem A→B em ambas |
| **Condição Negada** | Espera Circular | Espera Circular |

### 6.2 Padrão Comum
Ambas as soluções aplicam **Hierarquia de Recursos** para quebrar a espera circular, demonstrando que é uma técnica geral e poderosa.

---

## 7. Soluções Alternativas

### 7.1 Eliminar Hold-and-Wait (Alocação Atômica)

```java
// Adquirir AMBOS os locks atomicamente
synchronized (GLOBAL_LOCK) {
    synchronized (LOCK_A) {
        synchronized (LOCK_B) {
            // Trabalho
        }
    }
}
```

**Problema:** Serializa todo o acesso, elimina paralelismo.

### 7.2 Timeout com Retry

```java
ReentrantLock lockA = new ReentrantLock();
ReentrantLock lockB = new ReentrantLock();

while (true) {
    if (lockA.tryLock(100, TimeUnit.MILLISECONDS)) {
        try {
            if (lockB.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // Trabalho
                    break;  // Sucesso!
                } finally {
                    lockB.unlock();
                }
            }
        } finally {
            lockA.unlock();
        }
    }
    // Backoff exponencial
    Thread.sleep(random(10, 50));
}
```

**Vantagens:** Detecta e recupera de deadlock  
**Desvantagens:** Overhead, possível livelock, não determinístico

### 7.3 Semáforo Único

```java
Semaphore sem = new Semaphore(1);

// Thread 1 e 2:
sem.acquire();
try {
    // Trabalho que antes usaria LOCK_A e LOCK_B
} finally {
    sem.release();
}
```

**Problema:** Perde granularidade, serializa tudo.

---

## 8. Detecção de Deadlock em Produção

### 8.1 Thread Dump (Java)
```bash
# Enviar SIGQUIT para processo Java
kill -3 <PID>

# Ou usar jstack
jstack <PID>
```

**Output típico:**
```
Found one Java-level deadlock:
=============================
"Thread-2":
  waiting to lock monitor 0x00007f8b1c004e00 (object 0x000000076ab0d7a0, a java.lang.Object),
  which is held by "Thread-1"
"Thread-1":
  waiting to lock monitor 0x00007f8b1c004f00 (object 0x000000076ab0d7b0, a java.lang.Object),
  which is held by "Thread-2"
```

### 8.2 JConsole / VisualVM
- GUI para monitoramento de threads
- Detecta deadlocks automaticamente
- Mostra RAG (Resource Allocation Graph)

### 8.3 ThreadMXBean (Programático)
```java
ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
long[] deadlockedThreads = tmx.findDeadlockedThreads();

if (deadlockedThreads != null) {
    System.out.println("DEADLOCK DETECTADO!");
    for (long threadId : deadlockedThreads) {
        ThreadInfo info = tmx.getThreadInfo(threadId);
        System.out.println(info.getThreadName());
    }
}
```

---

## 9. Boas Práticas para Evitar Deadlock

### 9.1 Design
1. ✅ **Minimize compartilhamento:** Use estruturas thread-local quando possível
2. ✅ **Evite locks aninhados:** Um lock por operação
3. ✅ **Documente ordem de aquisição:** Hierarquia explícita
4. ✅ **Use abstrações de alto nível:** `ConcurrentHashMap`, `BlockingQueue`

### 9.2 Implementação
1. ✅ **Sempre a mesma ordem:** Estabeleça e siga hierarquia
2. ✅ **Timeouts em locks:** `tryLock()` com timeout
3. ✅ **Limite duração de locks:** Minimize seção crítica
4. ✅ **Evite operações bloqueantes:** Não faça I/O dentro de locks

### 9.3 Testing
1. ✅ **Stress tests:** Execute com alta concorrência
2. ✅ **Thread sanitizers:** Ferramentas como ThreadSanitizer (C++), FindBugs (Java)
3. ✅ **Monitoramento:** Habilite detecção de deadlock em produção
4. ✅ **Code review:** Verificar ordem de aquisição

---

## 10. Comparação de Estratégias

| Estratégia | Overhead | Complexidade | Garantia | Aplicabilidade |
|------------|----------|--------------|----------|----------------|
| **Hierarquia** | Baixo | Baixa | 100% prevenção | Alta |
| Alocação Atômica | Médio | Baixa | 100% prevenção | Baixa |
| Timeout/Retry | Alto | Média | Recuperação | Média |
| Banker's | Alto | Alta | 100% evitar | Baixa |
| Ignorar | Zero | Zero | Nenhuma | Tolerante |

**Recomendação:** **Hierarquia de Recursos** é a melhor escolha geral.

---

## 11. Conclusões

### 11.1 Principais Aprendizados
1. **Deadlock requer 4 condições simultâneas** (Coffman)
2. **Negar qualquer uma** das condições previne deadlock
3. **Hierarquia de recursos** é elegante, eficiente e confiável
4. **Detecção é possível**, mas prevenção é melhor
5. **Padrão universal** aplicável a diversos problemas (Filósofos, Banqueiros, etc.)

### 11.2 Relação com Jantar dos Filósofos
Ambos os problemas:
- Apresentam potencial de deadlock por espera circular
- São resolvidos pela mesma técnica (hierarquia)
- Demonstram que deadlock é um problema fundamental em concorrência
- Requerem raciocínio cuidadoso sobre ordem de aquisição

### 11.3 Aplicação Prática
Em sistemas reais:
- **Bancos de dados:** Lock ordering em transações
- **Sistemas operacionais:** Alocação de recursos (memória, I/O)
- **Redes:** Prevenção de ciclos em protocolos de roteamento
- **Aplicações concorrentes:** Qualquer cenário com múltiplos locks

---

## 12. Referências

[1] Dijkstra, E. W. (1965). "Solution of a Problem in Concurrent Programming Control"  
[2] Coffman, E. G., Elphick, M., & Shoshani, A. (1971). "System Deadlocks"  
[3] Tanenbaum & Bos. "Modern Operating Systems" (Deadlock Chapter)  
[4] Java Concurrency in Practice (Goetz et al.) - Chapter on Deadlock  

---

**Fim do Relatório**
