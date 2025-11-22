# Fluxograma: Jantar dos Filósofos
## Solução por Hierarquia de Recursos

---

## Fluxograma Principal - Processo do Filósofo

```
┌─────────────────────────────────────────────────────────────┐
│                    INÍCIO (Filósofo ID)                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
                ┌─────────────────┐
                │ Estado = PENSANDO│
                └────────┬─────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │   Registrar Log:     │
              │  "Iniciou pensando"  │
              └──────────┬───────────┘
                         │
                         ▼
         ┌───────────────────────────────────┐
         │  Pensar por tempo aleatório      │
         │  (1000-3000 ms)                  │
         └───────────────┬───────────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │   Registrar Log:     │
              │ "Terminou de pensar" │
              └──────────┬───────────┘
                         │
                         ▼
                ┌─────────────────┐
                │ Estado = COM_FOME│
                └────────┬─────────┘
                         │
                         ▼
        ┌────────────────────────────────────────┐
        │  Determinar ordem de aquisição:       │
        │  esq = garfo_esquerda(ID)             │
        │  dir = garfo_direita(ID)              │
        │  primeiro = min(esq, dir)             │
        │  segundo = max(esq, dir)              │
        └────────────────┬───────────────────────┘
                         │
                         ▼
              ┌──────────────────────┐
              │   Registrar Log:     │
              │  "Tentando pegar     │
              │   garfo {primeiro}"  │
              └──────────┬───────────┘
                         │
                         ▼
         ┌───────────────────────────────────┐
         │  mutex_lock(garfos[primeiro])    │◄───┐
         │                                   │    │
         │  [BLOQUEIA se garfo ocupado]     │    │ Espera
         └───────────────┬───────────────────┘    │ (não retorna
                         │                        │  até conseguir)
                         ▼                        │
              ┌──────────────────────┐            │
              │   Registrar Log:     │            │
              │  "Pegou garfo        │            │
              │   {primeiro}"        │            │
              └──────────┬───────────┘            │
                         │                        │
                         ▼                        │
              ┌──────────────────────┐            │
              │   Registrar Log:     │            │
              │  "Tentando pegar     │            │
              │   garfo {segundo}"   │            │
              └──────────┬───────────┘            │
                         │                        │
                         ▼                        │
         ┌───────────────────────────────────┐    │
         │  mutex_lock(garfos[segundo])     │◄───┤
         │                                   │    │
         │  [BLOQUEIA se garfo ocupado]     │    │ Espera
         └───────────────┬───────────────────┘    │
                         │                        │
                         ▼                        │
              ┌──────────────────────┐            │
              │   Registrar Log:     │            │
              │  "Pegou garfo        │            │
              │   {segundo}"         │            │
              └──────────┬───────────┘            │
                         │                        │
                         ▼                        │
                ┌─────────────────┐               │
                │ Estado = COMENDO │               │
                └────────┬─────────┘               │
                         │                        │
                         ▼                        │
              ┌──────────────────────┐            │
              │   Registrar Log:     │            │
              │  "Iniciou comendo"   │            │
              └──────────┬───────────┘            │
                         │                        │
                         ▼                        │
         ┌───────────────────────────────────┐    │
         │  Comer por tempo aleatório       │    │
         │  (1000-2000 ms)                  │    │
         └───────────────┬───────────────────┘    │
                         │                        │
                         ▼                        │
              ┌──────────────────────┐            │
              │   Registrar Log:     │            │
              │ "Terminou de comer"  │            │
              └──────────┬───────────┘            │
                         │                        │
                         ▼                        │
         ┌───────────────────────────────────┐    │
         │ mutex_unlock(garfos[segundo])    │    │
         └───────────────┬───────────────────┘    │
                         │                        │
                         ▼                        │
              ┌──────────────────────┐            │
              │   Registrar Log:     │            │
              │ "Soltou garfo        │            │
              │  {segundo}"          │            │
              └──────────┬───────────┘            │
                         │                        │
                         ▼                        │
         ┌───────────────────────────────────┐    │
         │ mutex_unlock(garfos[primeiro])   │    │
         └───────────────┬───────────────────┘    │
                         │                        │
                         ▼                        │
              ┌──────────────────────┐            │
              │   Registrar Log:     │            │
              │ "Soltou garfo        │            │
              │  {primeiro}"         │            │
              └──────────┬───────────┘            │
                         │                        │
                         └────────────────────────┘
                         │
                         └──────┐
                                │ Loop
                                │ Infinito
                                └──────────┐
                                           │
                         ┌─────────────────▼─────┐
                         │ Retorna ao início     │
                         │ (PENSANDO novamente)  │
                         └───────────────────────┘
```

---

## Fluxograma - Determinação da Ordem de Aquisição

```
┌──────────────────────────────────┐
│  Entrada: filosofo_id            │
└────────────┬─────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│  esq = filosofo_id                     │
│  dir = (filosofo_id + 1) MOD 5         │
└────────────┬───────────────────────────┘
             │
             ▼
        ┌─────────┐
        │ esq < dir? │
        └─────┬──────┘
              │
       ┌──────┴──────┐
       │             │
     SIM            NÃO
       │             │
       ▼             ▼
┌─────────────┐  ┌─────────────┐
│primeiro = esq│  │primeiro = dir│
│segundo = dir │  │segundo = esq │
└──────┬───────┘  └──────┬───────┘
       │                 │
       └────────┬────────┘
                │
                ▼
   ┌────────────────────────────┐
   │ Retornar (primeiro, segundo)│
   └────────────────────────────┘
```

---

## Fluxograma - Inicialização do Sistema

```
┌──────────────────────────────┐
│      INÍCIO DO SISTEMA       │
└──────────────┬───────────────┘
               │
               ▼
    ┌──────────────────────┐
    │  Imprimir cabeçalho  │
    └──────────┬───────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  PARA i = 0 até N-1 FAÇA     │
    │    garfos[i] = criar_mutex() │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  PARA i = 0 até N-1 FAÇA     │
    │    estados[i] = PENSANDO     │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Imprimir tabela de ordem    │
    │  de aquisição para cada      │
    │  filósofo                    │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  PARA i = 0 até N-1 FAÇA     │
    │    threads[i] =              │
    │      criar_thread(           │
    │        filosofo, i)          │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  Imprimir "Simulação         │
    │  iniciada"                   │
    └──────────┬───────────────────┘
               │
               ▼
    ┌──────────────────────────────┐
    │  PARA cada thread FAÇA       │
    │    aguardar_thread(thread)   │
    │                              │
    │  [Execução infinita ou       │
    │   até cancelamento manual]   │
    └──────────────────────────────┘
```

---

## Diagrama de Estados do Filósofo

```
                    ┌─────────────┐
         ┌─────────►│  PENSANDO   │◄──────────┐
         │          └──────┬──────┘           │
         │                 │                  │
         │          Terminou de pensar       │
         │                 │                  │
         │                 ▼                  │
         │          ┌─────────────┐           │
         │          │  COM FOME   │           │
         │          └──────┬──────┘           │
         │                 │                  │
         │    Conseguiu ambos garfos         │
         │                 │                  │
         │                 ▼                  │
         │          ┌─────────────┐           │
         │          │   COMENDO   │           │
         │          └──────┬──────┘           │
         │                 │                  │
         │          Terminou de comer        │
         │          Liberou garfos           │
         │                 │                  │
         └─────────────────┴──────────────────┘

Transições:
• PENSANDO → COM_FOME: Automática após tempo de pensar
• COM_FOME → COMENDO: Após adquirir ambos garfos (pode bloquear)
• COMENDO → PENSANDO: Automática após tempo de comer e liberação de garfos
```

---

## Diagrama de Bloqueio/Desbloqueio de Garfos

```
Filósofo tentando adquirir Garfo K:

┌──────────────────────┐
│  mutex_lock(garfo[k]) │
└──────────┬───────────┘
           │
           ▼
      ┌─────────┐
      │ Garfo K  │
      │ livre?   │
      └────┬─────┘
           │
    ┌──────┴──────┐
    │             │
  SIM            NÃO
    │             │
    ▼             ▼
┌────────────┐  ┌──────────────────┐
│ Adquire    │  │ Thread BLOQUEIA  │
│ imediata-  │  │ (entra em fila   │
│ mente      │  │  de espera do    │
└─────┬──────┘  │  mutex)          │
      │         └────────┬─────────┘
      │                  │
      │         ┌────────▼─────────┐
      │         │ Aguarda até      │
      │         │ outro filósofo   │
      │         │ fazer unlock     │
      │         └────────┬─────────┘
      │                  │
      │         ┌────────▼─────────┐
      │         │ Scheduler do SO  │
      │         │ acorda thread e  │
      │         │ tenta novamente  │
      │         └────────┬─────────┘
      │                  │
      └──────────────────┘
      │
      ▼
┌────────────────┐
│ Filósofo possui│
│ o garfo K      │
└────────────────┘
```

---

## Exemplo Visual: Cenário sem Deadlock

```
Tempo T0: Todos pensando
═══════════════════════════
        F0 (pensando)
       /              \
   G4 [livre]      G0 [livre]
     /                  \
F4 (pensando)      F1 (pensando)
     |                  |
   G3 [livre]      G1 [livre]
     \                  /
  F3 (pensando)  F2 (pensando)
       \              /
        G2 [livre]


Tempo T1: F0 e F2 ficam com fome simultaneamente
════════════════════════════════════════════════
        F0 (com fome)
       /              \
   G4 [livre]      G0 [livre]
     /                  \
F4 (pensando)      F1 (pensando)
     |                  |
   G3 [livre]      G1 [livre]
     \                  /
  F3 (pensando)  F2 (com fome)
       \              /
        G2 [livre]


Tempo T2: F0 pega G0 (menor), F2 pega G1 (menor)
═════════════════════════════════════════════════
        F0 (com fome)
       /              \
   G4 [livre]      G0 [OCUPADO por F0]
     /                  \
F4 (pensando)      F1 (pensando)
     |                  |
   G3 [livre]      G1 [OCUPADO por F2]
     \                  /
  F3 (pensando)  F2 (com fome)
       \              /
        G2 [livre]


Tempo T3: F0 tenta G4, F2 pega G2 (ambos conseguem!)
═══════════════════════════════════════════════════
        F0 (comendo!)
       /              \
   G4 [OCUPADO por F0] G0 [OCUPADO por F0]
     /                  \
F4 (pensando)      F1 (pensando)
     |                  |
   G3 [livre]      G1 [OCUPADO por F2]
     \                  /
  F3 (pensando)  F2 (comendo!)
       \              /
        G2 [OCUPADO por F2]

✓ NÃO HÁ DEADLOCK!
✓ F0 e F2 comem simultaneamente
✓ F1, F3 e F4 podem pegar seus garfos quando disponíveis
```

---

## Exemplo Visual: Por que não há ciclo de espera?

```
Suponha todos com fome ao mesmo tempo:

Ordem de aquisição (sempre menor → maior):
• F0: G0 → G4
• F1: G0 → G1
• F2: G1 → G2
• F3: G2 → G3
• F4: G3 → G4

Cenário de contenção máxima:
════════════════════════════

F0 pega G0 ✓ → espera G4
F1 espera G0 (F0 tem) → bloqueado
F2 pega G1 ✓ → pega G2 ✓ → COME!
F3 espera G2 (F2 tem) → bloqueado
F4 pega G3 ✓ → espera G4

Análise de possível ciclo:
• F0 espera G4
• F4 possui G3, espera G4
• Ambos esperam G4, mas nenhum possui G4!
• Não há ciclo: F0 e F4 disputam o mesmo recurso livre

Assim que F2 terminar:
• Libera G1 e G2
• F1 pode pegar G0 (quando F0 liberar) e depois G1
• F3 pode pegar G2

✓ SEMPRE HÁ PROGRESSO
✓ IMPOSSÍVEL FORMAR CICLO COMPLETO
```

---

**FIM DOS FLUXOGRAMAS**
