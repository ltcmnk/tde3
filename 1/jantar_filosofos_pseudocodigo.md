# Pseudocódigo Detalhado: Jantar dos Filósofos
## Solução por Hierarquia de Recursos

---

## 1. Estruturas de Dados

```
CONSTANTES:
    N = 5                          // Número de filósofos
    TEMPO_PENSAR = [1000..3000]    // Tempo aleatório em ms
    TEMPO_COMER = [1000..2000]     // Tempo aleatório em ms

TIPOS:
    Estado = {PENSANDO, COM_FOME, COMENDO}
    Garfo = {LIVRE, OCUPADO}

VARIÁVEIS GLOBAIS:
    garfos[0..N-1]: Array de Mutex     // Cada garfo é um mutex
    estados[0..N-1]: Array de Estado   // Estado de cada filósofo
    log: Lista de Eventos              // Para análise e debug
```

---

## 2. Funções Auxiliares

### 2.1 Identificação de Garfos
```
FUNÇÃO garfo_esquerda(filosofo_id):
    RETORNA filosofo_id

FUNÇÃO garfo_direita(filosofo_id):
    RETORNA (filosofo_id + 1) MOD N
```

### 2.2 Ordem de Aquisição (Hierarquia)
```
FUNÇÃO determinar_ordem_aquisicao(filosofo_id):
    esq = garfo_esquerda(filosofo_id)
    dir = garfo_direita(filosofo_id)
    
    SE esq < dir ENTÃO
        primeiro = esq
        segundo = dir
    SENÃO
        primeiro = dir
        segundo = esq
    FIM SE
    
    RETORNA (primeiro, segundo)
```

### 2.3 Registro de Eventos
```
FUNÇÃO registrar_evento(filosofo_id, evento, detalhes):
    timestamp = obter_tempo_atual()
    log.adicionar({
        tempo: timestamp,
        filosofo: filosofo_id,
        evento: evento,
        detalhes: detalhes,
        estado_anterior: estados[filosofo_id]
    })
    IMPRIMIR "[{timestamp}] Filósofo {filosofo_id}: {evento} - {detalhes}"
```

---

## 3. Processo do Filósofo (Thread Principal)

```
PROCEDIMENTO filosofo(id):
    ENQUANTO VERDADEIRO FAÇA
        // ============================================
        // FASE 1: PENSAR
        // ============================================
        estados[id] = PENSANDO
        registrar_evento(id, "INICIOU", "pensando")
        
        tempo = aleatorio(TEMPO_PENSAR)
        pensar(tempo)
        
        registrar_evento(id, "TERMINOU", "de pensar")
        
        // ============================================
        // FASE 2: TENTAR COMER (AQUISIÇÃO DE RECURSOS)
        // ============================================
        estados[id] = COM_FOME
        registrar_evento(id, "MUDOU", "com fome - tentando pegar garfos")
        
        // Determinar ordem de aquisição (hierarquia)
        (primeiro_garfo, segundo_garfo) = determinar_ordem_aquisicao(id)
        
        // Adquirir primeiro garfo (menor índice)
        registrar_evento(id, "TENTANDO", "pegar garfo {primeiro_garfo}")
        mutex_lock(garfos[primeiro_garfo])    // Bloqueia se necessário
        registrar_evento(id, "PEGOU", "garfo {primeiro_garfo}")
        
        // Adquirir segundo garfo (maior índice)
        registrar_evento(id, "TENTANDO", "pegar garfo {segundo_garfo}")
        mutex_lock(garfos[segundo_garfo])     // Bloqueia se necessário
        registrar_evento(id, "PEGOU", "garfo {segundo_garfo}")
        
        // ============================================
        // FASE 3: COMER (SEÇÃO CRÍTICA)
        // ============================================
        estados[id] = COMENDO
        registrar_evento(id, "INICIOU", "comendo")
        
        tempo = aleatorio(TEMPO_COMER)
        comer(tempo)
        
        registrar_evento(id, "TERMINOU", "de comer")
        
        // ============================================
        // FASE 4: LIBERAR RECURSOS
        // ============================================
        // Liberar na ordem inversa (boa prática, mas não obrigatório)
        mutex_unlock(garfos[segundo_garfo])
        registrar_evento(id, "SOLTOU", "garfo {segundo_garfo}")
        
        mutex_unlock(garfos[primeiro_garfo])
        registrar_evento(id, "SOLTOU", "garfo {primeiro_garfo}")
        
        estados[id] = PENSANDO
        
    FIM ENQUANTO
FIM PROCEDIMENTO
```

---

## 4. Funções de Simulação

### 4.1 Pensar
```
PROCEDIMENTO pensar(duracao):
    dormir(duracao)
FIM PROCEDIMENTO
```

### 4.2 Comer
```
PROCEDIMENTO comer(duracao):
    dormir(duracao)
FIM PROCEDIMENTO
```

---

## 5. Programa Principal

```
PROCEDIMENTO principal():
    // ============================================
    // INICIALIZAÇÃO
    // ============================================
    IMPRIMIR "=== JANTAR DOS FILÓSOFOS ==="
    IMPRIMIR "Número de filósofos: {N}"
    IMPRIMIR "Estratégia: Hierarquia de Recursos"
    IMPRIMIR "====================================\n"
    
    // Inicializar garfos
    PARA i DE 0 ATÉ N-1 FAÇA
        garfos[i] = criar_mutex()
        IMPRIMIR "Garfo {i} inicializado"
    FIM PARA
    
    // Inicializar estados
    PARA i DE 0 ATÉ N-1 FAÇA
        estados[i] = PENSANDO
    FIM PARA
    
    IMPRIMIR "\n--- Tabela de Ordem de Aquisição ---"
    PARA i DE 0 ATÉ N-1 FAÇA
        (primeiro, segundo) = determinar_ordem_aquisicao(i)
        IMPRIMIR "Filósofo {i}: Primeiro garfo={primeiro}, Segundo garfo={segundo}"
    FIM PARA
    IMPRIMIR "------------------------------------\n"
    
    // ============================================
    // CRIAR E INICIAR THREADS
    // ============================================
    threads[N]: Array de Thread
    
    PARA i DE 0 ATÉ N-1 FAÇA
        threads[i] = criar_thread(filosofo, i)
        IMPRIMIR "Thread do Filósofo {i} criada"
    FIM PARA
    
    IMPRIMIR "\n=== SIMULAÇÃO INICIADA ===\n"
    
    // ============================================
    // AGUARDAR THREADS (execução infinita)
    // ============================================
    PARA i DE 0 ATÉ N-1 FAÇA
        aguardar_thread(threads[i])
    FIM PARA
    
    // Na prática, pode-se executar por tempo limitado:
    // dormir(60000)  // Executar por 60 segundos
    // PARA cada thread FAÇA
    //     cancelar_thread(thread)
    // FIM PARA
    
FIM PROCEDIMENTO
```

---

## 6. Exemplo de Execução Trace

```
[T=0ms] Sistema: Inicialização completa
[T=0ms] Filósofo 0: INICIOU - pensando
[T=0ms] Filósofo 1: INICIOU - pensando
[T=0ms] Filósofo 2: INICIOU - pensando
[T=0ms] Filósofo 3: INICIOU - pensando
[T=0ms] Filósofo 4: INICIOU - pensando

[T=1500ms] Filósofo 2: TERMINOU - de pensar
[T=1500ms] Filósofo 2: MUDOU - com fome
[T=1500ms] Filósofo 2: TENTANDO - pegar garfo 1
[T=1500ms] Filósofo 2: PEGOU - garfo 1
[T=1500ms] Filósofo 2: TENTANDO - pegar garfo 2
[T=1500ms] Filósofo 2: PEGOU - garfo 2
[T=1500ms] Filósofo 2: INICIOU - comendo

[T=2000ms] Filósofo 0: TERMINOU - de pensar
[T=2000ms] Filósofo 0: MUDOU - com fome
[T=2000ms] Filósofo 0: TENTANDO - pegar garfo 0
[T=2000ms] Filósofo 0: PEGOU - garfo 0
[T=2000ms] Filósofo 0: TENTANDO - pegar garfo 4
[T=2000ms] Filósofo 0: BLOQUEADO - aguardando garfo 4 (ocupado por Filósofo 4)

[T=2200ms] Filósofo 4: TERMINOU - de pensar
[T=2200ms] Filósofo 4: MUDOU - com fome
[T=2200ms] Filósofo 4: TENTANDO - pegar garfo 3
[T=2200ms] Filósofo 4: PEGOU - garfo 3
[T=2200ms] Filósofo 4: TENTANDO - pegar garfo 4
[T=2200ms] Filósofo 4: BLOQUEADO - aguardando garfo 4 (ocupado por Filósofo 0)

[T=3000ms] Filósofo 2: TERMINOU - de comer
[T=3000ms] Filósofo 2: SOLTOU - garfo 2
[T=3000ms] Filósofo 2: SOLTOU - garfo 1
[T=3000ms] Filósofo 2: INICIOU - pensando

... (continua indefinidamente sem deadlock)
```

---

## 7. Prova de Correção

### 7.1 Invariante Chave
**Invariante I**: Se um filósofo `P` possui o garfo `k` e está esperando pelo garfo `m`, então `k < m`.

### 7.2 Demonstração de Ausência de Ciclo

Suponha por contradição que existe um ciclo de espera:
```
P₀ → P₁ → P₂ → ... → Pₙ → P₀
```

Onde `Pᵢ → Pᵢ₊₁` significa "Pᵢ espera por um garfo que Pᵢ₊₁ possui".

Seja `gᵢ` o garfo que `Pᵢ` espera e que `Pᵢ₊₁` possui.

Pelo invariante I:
- `P₀` possui algum garfo `g₀'` onde `g₀' < g₀`
- `P₁` possui `g₀` e espera `g₁`, então `g₀ < g₁`
- `P₂` possui `g₁` e espera `g₂`, então `g₁ < g₂`
- ...
- `Pₙ` possui `gₙ₋₁` e espera `gₙ`, então `gₙ₋₁ < gₙ`

No ciclo, `P₀` espera por um garfo que pertence à sequência:
```
g₀ < g₁ < g₂ < ... < gₙ
```

Mas para fechar o ciclo, `Pₙ` deve estar esperando por um garfo que `P₀` possui, implicando que esse garfo deve ser menor que `gₙ`. No entanto, `P₀` pode possuir no máximo `g₀'` onde `g₀' < g₀ < gₙ`.

Isso cria uma cadeia estritamente crescente infinita de números finitos, o que é uma **contradição**.

Portanto, **não pode existir ciclo de espera** → **Sistema livre de deadlock**.

---

## 8. Análise de Justiça

### 8.1 Progresso Garantido
- Em qualquer configuração, pelo menos um filósofo pode adquirir ambos os garfos
- Quando um filósofo termina de comer e libera os garfos, outros filósofos progridem

### 8.2 Ausência de Starvation (sob scheduler justo)
- Com um scheduler de mutex justo (FIFO), cada filósofo eventualmente conseguirá ambos os garfos
- Não há favorecimento estrutural de nenhum filósofo específico
- A hierarquia não cria prioridades permanentes

### 8.3 Throughput
- Em média, 2-3 filósofos podem comer simultaneamente
- Máximo teórico: 2 filósofos (devido à configuração circular)
- Configuração típica: Filósofos não-adjacentes podem comer simultaneamente

---

## 9. Extensões Possíveis

### 9.1 Limite de Filósofos Simultâneos (Semáforo)
```
semaforo_mesa = criar_semaforo(N-1)  // Máximo N-1 filósofos simultâneos

PROCEDIMENTO filosofo_com_limitacao(id):
    ENQUANTO VERDADEIRO FAÇA
        pensar()
        semaforo_wait(semaforo_mesa)     // Garante no máximo N-1 ativos
        // ... tentar pegar garfos ...
        comer()
        // ... soltar garfos ...
        semaforo_signal(semaforo_mesa)
    FIM ENQUANTO
```

### 9.2 Timeout (Detecção de Possível Livelock)
```
CONSTANTE TIMEOUT = 5000  // 5 segundos

SE NÃO mutex_trylock_with_timeout(garfos[primeiro], TIMEOUT) ENTÃO
    registrar_evento(id, "TIMEOUT", "desistindo e retornando a pensar")
    continuar  // Volta a pensar
FIM SE
```

### 9.3 Prioridades (Filósofos VIP)
```
prioridades[N] = [1, 1, 2, 1, 1]  // Filósofo 2 tem prioridade

// Modificar mutexes para considerar prioridades
mutex_lock_priority(garfos[k], prioridades[id])
```

---

**FIM DO PSEUDOCÓDIGO**
