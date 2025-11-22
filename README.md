# Trabalho de Performance em Sistemas Ciberfísicos - Sincronização e Deadlock

**Instituição:** PUCPR 
**Disciplina:** Performance em Sistemas Ciberfísicos 
**Data:** 21 de Novembro de 2025  
**Aluna:** Letícia Miniuk

## 📹 Vídeo Explicativo

**Link do Vídeo:** [INSERIR LINK DO YOUTUBE/DRIVE AQUI]

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Parte 1 - Jantar dos Filósofos](#parte-1---jantar-dos-filósofos)
3. [Parte 2 - Threads e Semáforos](#parte-2---threads-e-semáforos)
4. [Parte 3 - Deadlock](#parte-3---deadlock)
5. [Como Compilar e Executar](#como-compilar-e-executar)
6. [Resultados e Análises](#resultados-e-análises)
7. [Referências](#referências)

---

## 🎯 Visão Geral

Este trabalho aborda três problemas clássicos de sincronização em sistemas operacionais:

1. **Jantar dos Filósofos**: Demonstração de deadlock e solução por hierarquia de recursos
2. **Condição de Corrida**: Race condition em contador concorrente e correção com semáforos
3. **Deadlock Clássico**: Reprodução, análise das Condições de Coffman e correção

### Objetivos de Aprendizado

- ✅ Compreender deadlock, starvation e race conditions
- ✅ Aplicar estratégias de prevenção de deadlock (hierarquia de recursos)
- ✅ Utilizar semáforos para exclusão mútua
- ✅ Analisar Condições de Coffman
- ✅ Garantir fairness e happens-before em sistemas concorrentes

---

## 📖 Parte 1 - Jantar dos Filósofos

### Problema

Cinco filósofos sentados em uma mesa circular alternando entre **pensar** e **comer**. Para comer, cada filósofo precisa de **dois garfos** (esquerdo e direito), compartilhados com vizinhos.

### Protocolo Ingênuo (com Deadlock)

```
Para cada filósofo:
    1. Pensar
    2. Pegar garfo da ESQUERDA
    3. Pegar garfo da DIREITA
    4. Comer
    5. Soltar garfos
```

**Problema:** Se todos pegarem o garfo esquerdo simultaneamente → **DEADLOCK**

### Solução: Hierarquia de Recursos

**Estratégia:** Impor ordem global de aquisição de garfos

```
Para cada filósofo p:
    left = min(garfo_esquerda(p), garfo_direita(p))
    right = max(garfo_esquerda(p), garfo_direita(p))
    
    Adquirir(left)   // Sempre menor primeiro
    Adquirir(right)  // Depois maior
    Comer()
    Liberar(right)
    Liberar(left)
```

**Por que funciona?** Elimina a **espera circular** (4ª condição de Coffman)

### Arquivos

- 📄 `jantar_filosofos_relatorio.md` - Relatório técnico completo
- 📄 `jantar_filosofos_pseudocodigo.md` - Pseudocódigo detalhado
- 📄 `jantar_filosofos_fluxograma.md` - Diagramas e fluxogramas

### Condições de Coffman Analisadas

| Condição | Status | Solução |
|----------|--------|---------|
| 1. Exclusão Mútua | ✅ Mantida | Necessária |
| 2. Hold-and-Wait | ✅ Mantida | Necessária |
| 3. Não Preempção | ✅ Mantida | Necessária |
| 4. **Espera Circular** | ❌ **ELIMINADA** | **Hierarquia de recursos** |

---

## 🔄 Parte 2 - Threads e Semáforos

### Problema: Contador Concorrente

Demonstrar **race condition** ao incrementar um contador compartilhado sem sincronização.

### Operação `count++` NÃO é Atômica

```
count++  →  LOAD (ler)
         →  ADD  (incrementar)
         →  STORE (gravar)
```

Múltiplas threads executando simultaneamente = **perda de incrementos**

### Implementação

#### 1. `CorridaSemControle.java` ❌
- 8 threads, 250.000 incrementos cada
- **Esperado:** 2.000.000
- **Obtido:** ~500.000 a 1.200.000 (50-65% de perda)
- **Tempo:** ~0.04s (rápido, mas incorreto)

#### 2. `CorridaComSemaphore.java` ✅
```java
static Semaphore sem = new Semaphore(1, true);  // Binário, FIFO

sem.acquire();
try {
    count++;  // Seção crítica
} finally {
    sem.release();
}
```

- **Obtido:** 2.000.000 (100% correto)
- **Tempo:** ~2.86s (70x mais lento, mas correto)

#### 3. `ComparacaoSincronizacao.java` 📊
Compara 7 mecanismos:
- Sem sincronização
- Semaphore (fair/unfair)
- **AtomicInteger** ⭐ (recomendado - 19x mais rápido que semáforo)
- synchronized
- ReentrantLock (fair/unfair)

### Resultados

| Mecanismo | Tempo (s) | Correção | Throughput |
|-----------|-----------|----------|------------|
| Sem sinc. | 0.042 | ❌ 52% | 47.6M/s |
| **Semaphore** | **2.863** | **✅ 100%** | **698K/s** |
| AtomicInteger | 0.150 | ✅ 100% | 13.3M/s |
| synchronized | 2.500 | ✅ 100% | 800K/s |

### Conceitos Demonstrados

1. **Race Condition**: Múltiplas threads + dado compartilhado + escrita não sincronizada
2. **Exclusão Mútua**: Semáforo binário garante que apenas uma thread acesse a seção crítica
3. **Fairness**: Modo FIFO evita starvation
4. **Happens-Before**: `release()` de T1 happens-before `acquire()` de T2
5. **Visibilidade**: Mudanças feitas antes de `release()` são visíveis após `acquire()`

### Arquivos

- 📄 `parte2_threads_semaforos.md` - Relatório técnico
- ☕ `CorridaSemControle.java` - Demonstra race condition
- ☕ `CorridaComSemaphore.java` - Correção com semáforo
- ☕ `ComparacaoSincronizacao.java` - Comparação de mecanismos

---

## 🔒 Parte 3 - Deadlock

### Problema: Duas Threads, Dois Locks

```
Thread-1: LOCK_A → LOCK_B
Thread-2: LOCK_B → LOCK_A  ❌ (ordem invertida)
```

**Resultado:** Deadlock (ambas ficam bloqueadas indefinidamente)

### Condições de Coffman (Todas Presentes = Deadlock)

```
✅ 1. Exclusão Mútua:  synchronized garante acesso exclusivo
✅ 2. Hold-and-Wait:   T1 segura LOCK_A, aguarda LOCK_B
✅ 3. Não Preempção:   Locks não podem ser forçadamente liberados
✅ 4. Espera Circular: T1 → T2 → T1 (ciclo)
```

### Implementação

#### 1. `DeadlockDemo.java` ❌ (Reproduz Deadlock)
```java
Thread t1 = new Thread(() -> {
    synchronized (LOCK_A) {
        dormir(50);
        synchronized (LOCK_B) {  // Bloqueia aqui
            System.out.println("T1 concluiu");
        }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (LOCK_B) {
        dormir(50);
        synchronized (LOCK_A) {  // Bloqueia aqui
            System.out.println("T2 concluiu");
        }
    }
});
```

**Resultado:** Programa trava, nunca exibe "concluiu"

#### 2. `DeadlockCorrigido.java` ✅ (Hierarquia de Recursos)
```java
// Ambas threads seguem a MESMA ordem: A → B

Thread t1 = new Thread(() -> {
    synchronized (LOCK_A) {
        synchronized (LOCK_B) {  // ✅
            System.out.println("T1 concluiu");
        }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (LOCK_A) {  // ✅ Mudou de B para A
        synchronized (LOCK_B) {
            System.out.println("T2 concluiu");
        }
    }
});
```

**Resultado:** Ambas threads terminam com sucesso

#### 3. `DeadlockDetector.java` 🔍 (Detecção Automática)
- Usa `ThreadMXBean.findDeadlockedThreads()`
- Detecta deadlock em tempo de execução
- Gera relatório completo:
  - Threads envolvidas
  - Locks possuídos vs. aguardados
  - Stack traces
  - Ciclo de espera

### Por Que a Correção Funciona?

**Hierarquia de Recursos:** LOCK_A < LOCK_B

Se todas as threads seguem a mesma ordem, é **impossível formar ciclo**:

```
Prova por Contradição:

Para existir ciclo: T1 → Lock_i → T2 → Lock_j → T1

Pela hierarquia:
  T1 possui Lock_a onde Lock_a < Lock_i
  T2 possui Lock_i onde Lock_i < Lock_j

Para fechar ciclo: T1 deveria possuir Lock_j
Mas Lock_a < Lock_i < Lock_j → CONTRADIÇÃO!

∴ Ciclo é impossível → Sem deadlock ∎
```

### Arquivos

- 📄 `parte3_deadlock_relatorio.md` - Análise teórica completa
- ☕ `DeadlockDemo.java` - Reproduz deadlock
- ☕ `DeadlockCorrigido.java` - Correção por hierarquia
- ☕ `DeadlockDetector.java` - Detecção automática

### Relação com Jantar dos Filósofos

Ambos usam **hierarquia de recursos** para negar **espera circular**:

| Aspecto | Filósofos | Deadlock 2 Threads |
|---------|-----------|-------------------|
| Recursos | 5 garfos | 2 locks |
| Protocolo Ingênuo | Pega esq→dir | T1: A→B, T2: B→A |
| Deadlock | Todos pegam esquerdo | Cada um pega um lock |
| **Solução** | **min→max nos garfos** | **A→B em ambas** |
| Condição Negada | Espera Circular | Espera Circular |

---

## 🚀 Como Compilar e Executar

### Pré-requisitos

- Java JDK 8 ou superior
- Terminal/CMD

### Parte 2 - Threads e Semáforos

```bash
# Race condition (incorreto)
javac CorridaSemControle.java
java CorridaSemControle

# Correção com semáforo
javac CorridaComSemaphore.java
java CorridaComSemaphore

# Comparação de mecanismos
javac ComparacaoSincronizacao.java
java ComparacaoSincronizacao
```

### Parte 3 - Deadlock

```bash
# Reproduzir deadlock (pressione Ctrl+C para parar)
javac DeadlockDemo.java
java DeadlockDemo

# Correção do deadlock
javac DeadlockCorrigido.java
java DeadlockCorrigido

# Detecção automática
javac DeadlockDetector.java
java DeadlockDetector
```

### Detectar Deadlock com jstack

```bash
# Em um terminal, execute o programa com deadlock
java DeadlockDemo

# Em outro terminal
jps                    # Encontrar PID do processo
jstack <PID>           # Gerar thread dump

# Output mostrará: "Found one Java-level deadlock"
```

---

## 📊 Resultados e Análises

### Parte 1 - Jantar dos Filósofos

**Protocolo Ingênuo:**
- ❌ Deadlock garantido quando todos pegam garfo esquerdo
- ❌ Todas 4 condições de Coffman satisfeitas

**Solução por Hierarquia:**
- ✅ Deadlock impossível (espera circular eliminada)
- ✅ Fairness eventual (com scheduler justo)
- ✅ Progresso garantido (sempre pelo menos um filósofo pode comer)

### Parte 2 - Threads e Semáforos

**Race Condition:**
- Perda de 40-75% dos incrementos
- Resultado não determinístico
- Execução rápida (~0.04s), mas incorreta

**Semáforo Binário:**
- 100% de correção
- 70x mais lento (~2.86s)
- Garantias: exclusão mútua, happens-before, visibilidade

**Trade-off:** Correção vs. Desempenho

**Melhor solução:** `AtomicInteger` (correto + 19x mais rápido que semáforo)

### Parte 3 - Deadlock

**Deadlock Clássico:**
- Programa trava indefinidamente
- Todas 4 condições de Coffman presentes
- Detectável via `jstack` ou `ThreadMXBean`

**Correção por Hierarquia:**
- 100% eficaz
- Baixo overhead
- Simples de implementar
- Padrão universal aplicável a diversos problemas

---

## 🎓 Conceitos Aprendidos

### 1. Condições de Coffman

Para deadlock ocorrer, **TODAS** devem estar presentes:

1. **Exclusão Mútua**: Recurso não compartilhável
2. **Hold-and-Wait**: Processo segura recurso enquanto espera outro
3. **Não Preempção**: Recurso só pode ser liberado voluntariamente
4. **Espera Circular**: Existe ciclo de espera entre processos

**Solução:** Negar pelo menos UMA condição

### 2. Hierarquia de Recursos

**Princípio:** Impor ordem total global sobre recursos

**Aplicação:**
- Jantar dos Filósofos: garfo_min → garfo_max
- Deadlock 2 locks: LOCK_A → LOCK_B

**Resultado:** Impossível formar ciclo de espera

### 3. Semáforos

**Operações:**
- `acquire()`: Decrementa contador; bloqueia se zero
- `release()`: Incrementa contador; libera thread

**Tipos:**
- **Binário (1)**: Mutex (exclusão mútua)
- **Contador (N)**: Pool de recursos

**Modos:**
- **Fair (true)**: FIFO, evita starvation
- **Unfair (false)**: Maior throughput, possível starvation

### 4. Happens-Before

**Garantia Java Memory Model:**
```
release() de Thread A  happens-before  acquire() de Thread B
```

**Implicações:**
- Escritas antes de `release()` são visíveis após `acquire()`
- Ordem de operações preservada
- Consistência de memória garantida

### 5. Race Condition

**Definição:**
```
Race Condition = Múltiplas threads + 
                 Dado compartilhado + 
                 ≥1 escrita + 
                 Sem sincronização
```

**Soluções:**
1. Semáforos/Locks (sincronização)
2. Tipos atômicos (`AtomicInteger`)
3. Thread-local storage
4. Imutabilidade

---

## 📚 Referências

### Artigos e Documentação

1. [Dining Philosophers Problem - Wikipedia](https://en.wikipedia.org/wiki/Dining_philosophers_problem)
2. [Java Semaphore Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Semaphore.html)
3. [Deadlock - Wikipedia](https://en.wikipedia.org/wiki/Deadlock_(computer_science))
4. [GeeksforGeeks - Deadlock in OS](https://www.geeksforgeeks.org/operating-systems/introduction-of-deadlock-in-operating-system/)
5. [GeeksforGeeks - Dining Philosophers](https://www.geeksforgeeks.org/operating-systems/dining-philosophers-problem/)

### Vídeos Educacionais

6. [Dining Philosophers - YouTube](https://www.youtube.com/watch?v=NbwbQQB7xNQ)
7. [Deadlock Explanation - YouTube](https://www.youtube.com/watch?v=FYUi-u7UWgw)

### Tutoriais e Guias

8. [Scaler - Dining Philosophers in OS](https://www.scaler.com/topics/operating-system/dining-philosophers-problem-in-os/)
9. [TechVidvan - Semaphore in Java](https://techvidvan.com/tutorials/semaphore-in-java/)
10. [David Vlijmincx - Java Semaphore](https://davidvlijmincx.com/posts/how-to-use-java-semaphore/)

### Livros e Papers

11. Java Concurrency in Practice (Goetz et al.)
12. Modern Operating Systems (Tanenbaum & Bos)
13. Operating System Concepts (Silberschatz, Galvin, Gagne)

---

## 📧 Contato

**Aluna:** Letícia Miniuk
**Disciplina:** Performance em Sistemas Ciberfísicos
**Professor:** Andrey Meira Cabral
**Instituição:** PUCPR

---

## 📄 Licença

Este trabalho é de propriedade da autora e foi desenvolvido para fins educacionais.

---

**Data de Entrega:** 21 de Novembro de 2025
**Última Atualização:** 21 de Novembro de 2025
