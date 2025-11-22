# Problema do Jantar dos Filósofos
## Relatório Técnico

**Data:** 21 de Novembro de 2025  
**Tema:** Análise de Deadlock e Solução por Hierarquia de Recursos

---

## 1. Dinâmica do Problema

### 1.1 Descrição do Cenário
O Problema do Jantar dos Filósofos modela um sistema concorrente com **5 filósofos** sentados em uma mesa circular. Cada filósofo alterna entre três estados:

- **Pensando**: estado inativo, não requer recursos
- **Com fome**: deseja comer, precisa adquirir recursos
- **Comendo**: consumindo recursos (dois garfos adjacentes)

### 1.2 Compartilhamento de Recursos
- Existem **5 garfos** na mesa (um entre cada par de filósofos)
- Cada filósofo compartilha o garfo à sua esquerda com o vizinho esquerdo
- Cada filósofo compartilha o garfo à sua direita com o vizinho direito
- Para comer, um filósofo precisa de **ambos os garfos** simultaneamente

---

## 2. Protocolo Ingênuo e Ocorrência de Impasse

### 2.1 Protocolo Ingênuo
```
Loop infinito:
    1. Pensar
    2. Pegar garfo da esquerda
    3. Pegar garfo da direita
    4. Comer
    5. Soltar garfo da direita
    6. Soltar garfo da esquerda
```

### 2.2 Cenário de Deadlock
O impasse ocorre quando:
1. **Tempo T₀**: Todos os 5 filósofos ficam com fome simultaneamente
2. **Tempo T₁**: Cada filósofo pega o garfo à sua esquerda
3. **Tempo T₂**: Todos tentam pegar o garfo à sua direita
4. **Resultado**: Nenhum garfo direito está disponível (todos foram pegos como "garfo esquerdo" por outro filósofo)
5. **Conclusão**: Todos aguardam indefinidamente → **DEADLOCK**

---

## 3. Condições de Coffman para Deadlock

Para que um deadlock ocorra, **todas as quatro** condições devem ser satisfeitas simultaneamente:

### 3.1 Exclusão Mútua
✅ **Presente**: Um garfo só pode ser usado por um filósofo por vez.

### 3.2 Manter e Esperar (Hold and Wait)
✅ **Presente**: Um filósofo segura o garfo esquerdo enquanto espera pelo direito.

### 3.3 Não Preempção
✅ **Presente**: Garfos não podem ser forçadamente retirados; apenas o filósofo que os possui pode liberá-los.

### 3.4 Espera Circular
✅ **Presente**: Existe um ciclo de espera:
- Filósofo 0 espera por Filósofo 1 (seu garfo direito)
- Filósofo 1 espera por Filósofo 2 (seu garfo direito)
- Filósofo 2 espera por Filósofo 3 (seu garfo direito)
- Filósofo 3 espera por Filósofo 4 (seu garfo direito)
- Filósofo 4 espera por Filósofo 0 (seu garfo direito)
- **Ciclo completo**: 0 → 1 → 2 → 3 → 4 → 0

---

## 4. Solução Proposta: Hierarquia de Recursos

### 4.1 Estratégia
**Negar a condição de Espera Circular** através da imposição de uma ordem global de aquisição de recursos.

### 4.2 Mecanismo
1. Atribuir um **índice único** a cada garfo: `0, 1, 2, 3, 4`
2. Ordenar os garfos de cada filósofo: `left = min(garfo_esq, garfo_dir)` e `right = max(garfo_esq, garfo_dir)`
3. **Regra fundamental**: Sempre adquirir primeiro o garfo de **menor índice**, depois o de **maior índice**

### 4.3 Por que Funciona?
- A ordem parcial fixa impede a formação de ciclos de espera
- Todos os filósofos seguem a mesma hierarquia global de recursos
- Um filósofo nunca pode esperar por um recurso de índice menor enquanto possui um de índice maior
- **Impossível formar o ciclo**: 0 → 1 → 2 → 3 → 4 → 0

### 4.4 Análise das Condições de Coffman
| Condição | Status | Justificativa |
|----------|--------|---------------|
| Exclusão Mútua | ✅ Mantida | Necessária para integridade dos dados |
| Manter e Esperar | ✅ Mantida | Filósofos ainda seguram um garfo enquanto esperam outro |
| Não Preempção | ✅ Mantida | Garfos são liberados voluntariamente |
| **Espera Circular** | ❌ **ELIMINADA** | Ordem global impede ciclos |

### 4.5 Garantias
- **Livre de Deadlock**: Impossível formar ciclo de espera
- **Progresso**: Sempre haverá pelo menos um filósofo que pode prosseguir
- **Justiça Eventual**: Todos os filósofos terão oportunidade de comer (starvation-free com schedulers justos)

---

## 5. Mapeamento de Garfos

### 5.1 Configuração da Mesa
```
        Filósofo 0
       /          \
   Garfo 4      Garfo 0
     /              \
Filósofo 4      Filósofo 1
     |              |
   Garfo 3      Garfo 1
     \              /
  Filósofo 3  Filósofo 2
       \          /
        Garfo 2
```

### 5.2 Tabela de Aquisição
| Filósofo | Garfo Esquerdo | Garfo Direito | Primeiro a Pegar | Segundo a Pegar |
|----------|----------------|---------------|------------------|-----------------|
| 0        | 4              | 0             | **0** (menor)    | 4 (maior)       |
| 1        | 0              | 1             | **0** (menor)    | 1 (maior)       |
| 2        | 1              | 2             | **1** (menor)    | 2 (maior)       |
| 3        | 2              | 3             | **2** (menor)    | 3 (maior)       |
| 4        | 3              | 4             | **3** (menor)    | 4 (maior)       |

### 5.3 Observação Crítica
O **Filósofo 0** quebra a simetria ao pegar primeiro o garfo direito (índice 0) em vez do esquerdo (índice 4), eliminando o ciclo circular.

---

## 6. Conclusão

A solução por **hierarquia de recursos** é elegante, simples de implementar e matematicamente correta. Ao negar a condição de **espera circular**, garantimos que o sistema está livre de deadlock sem necessidade de:
- Timeouts complexos
- Mecanismos de detecção e recuperação
- Árbitros centralizados (que poderiam ser gargalos)

A técnica é aplicável a muitos problemas de sincronização e representa um padrão fundamental em sistemas concorrentes.

---

## Referências
[1] Condições de Coffman para Deadlock  
[2] Problema do Jantar dos Filósofos (Dijkstra, 1965)  
[3] Técnicas de Prevenção de Deadlock por Ordenação de Recursos
