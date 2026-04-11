# Plataforma de Streaming — LP2/AED2 2025/26

## Autores
- **Diogo Vicente** — Managers (`UserManager`, `ArtistManager`, `ArtistContentManager`, `FollowManager`, `StreamingDatabase`, `StreamingGraph`) + modelos (`User`, `Artist`, `ArtistContent`, `UserFollow`)
- **Pedro** — Managers (`ContentManager`, `ContentBST`, `GenreManager`, `ContentFileManager`, `ContentSerializer`) + modelos (`Content`, `Movie`, `Series`, `Documentary`, `Genre`, `Interation`) + GUI (`StreamingGUI`)

---

## Estrutura do Projeto

```
src/
└── main/java/edu/ufp/streaming/rec/
    ├── enums/
    │   ├── ArtistRole.java          # ACTOR, DIRECTOR, PRODUCER, WRITER
    │   └── InterationType.java      # WATCH, RATE, BOOKMARK, SKIP
    ├── models/
    │   ├── User.java                # Utilizador da plataforma
    │   ├── Artist.java              # Artista (ator, realizador, etc.)
    │   ├── Content.java             # Classe base de conteúdo (Serializable)
    │   ├── Movie.java               # Filme (extends Content)
    │   ├── Series.java              # Série (extends Content)
    │   ├── Documentary.java         # Documentário (extends Content)
    │   ├── Genre.java               # Género (Serializable)
    │   ├── Interation.java          # Interação utilizador-conteúdo
    │   ├── ArtistContent.java       # Relação artista↔conteúdo
    │   └── UserFollow.java          # Relação follow entre utilizadores
    ├── managers/
    │   ├── UserManager.java         # Gere utilizadores (ST + 2 BSTs)
    │   ├── ArtistManager.java       # Gere artistas (ST + 2 BSTs)
    │   ├── ContentManager.java      # Gere conteúdos (ST)
    │   ├── ContentBST.java          # BST de conteúdos por data
    │   ├── GenreManager.java        # Gere géneros (ST)
    │   ├── ArtistContentManager.java# Gere relações artista↔conteúdo
    │   ├── FollowManager.java       # Gere relações de follow
    │   ├── ContentFileManager.java  # Import/export txt
    │   ├── ContentSerializer.java   # Serialização binária
    │   ├── StreamingDatabase.java   # Coordenador central (R4)
    │   └── StreamingGraph.java      # Grafo pesado direcionado (Fase 2)
    └── gui/
        └── StreamingGUI.java        # Interface gráfica (R9)

test/java/
    ├── TestUserArtist.java          # Testes Fase 1 — User e Artist
    ├── TestContent.java             # Testes Fase 1 — Content e Genre
    ├── TestArtistContent.java       # Testes Fase 1 — ArtistContent
    └── TestStreamingGraph.java      # Testes Fase 2 — StreamingGraph (R8a–R8g)
```

---

## Estruturas de Dados Utilizadas

### Symbol Tables (ST) — `edu.princeton.cs.algs4.ST`
Usadas como tabelas de hash primárias em todos os managers.  
**Complexidade:** inserção/pesquisa média O(1), pior caso O(n).

| Manager | Chave | Valor |
|---------|-------|-------|
| UserManager | userId | User |
| ArtistManager | artistId | Artist |
| ContentManager | contentId | Content |
| GenreManager | genreId | Genre |
| ArtistContentManager | "artistId:contentId:role" | ArtistContent |
| FollowManager | "followerId:followedId" | UserFollow |

### Red-Black BSTs — `edu.princeton.cs.algs4.RedBlackBST`
Usadas para pesquisas ordenadas e por intervalo.  
**Complexidade:** inserção/pesquisa/remoção O(log n) garantido.

| BST | Chave | Finalidade |
|-----|-------|-----------|
| UserManager.byDateBST | data registo (Long) | Pesquisa por data de registo |
| UserManager.byNameBST | nome (String) | Pesquisa por nome |
| ArtistManager.byBirthDateBST | data nascimento (Long) | Pesquisa por idade |
| ArtistManager.byNameBST | nome (String) | Pesquisa por nome |
| ArtistContentManager.byDateBST | data participação (Long) | Pesquisa filmografia por data |
| FollowManager.byDateBST | timestamp (Long) | Pesquisa follows por data |
| ContentBST | data lançamento (String) | Listagem ordenada de conteúdos |

### Grafo — `edu.princeton.cs.algs4.EdgeWeightedDigraph`
Grafo pesado direcionado heterogéneo com dois tipos de vértices (User e Content) e três tipos de arestas.  
**Mapeamento:** IDs de entidade → índices inteiros via ST.

| Aresta | Peso |
|--------|------|
| User → User (follow) | epoch seconds da data do follow |
| User → Content (WATCH) | progresso (0.0 a 1.0) |
| User → Content (RATE) | rating (0.0 a 5.0) |

---

## Requisitos Implementados

### Fase 1
| Req. | Descrição | Implementado |
|------|-----------|:---:|
| R1 | Diagrama UML | ✅ |
| R2 | CRUD de Géneros, Conteúdos, Utilizadores, Artistas | ✅ |
| R3 | Pesquisas (substring, data, região, género, etc.) | ✅ |
| R4 | Consistência em cascata na remoção | ✅ |
| R5 | Interações utilizador-conteúdo | ✅ |
| R10 | Import/Export ficheiros texto | ✅ |
| R11 | Serialização binária | ✅ |

### Fase 2
| Req. | Descrição | Implementado |
|------|-----------|:---:|
| R7 | Grafo pesado direcionado | ✅ |
| R8a | Caminho mais curto entre utilizadores (Dijkstra) | ✅ |
| R8b | Extração de subgrafos por região e por género | ✅ |
| R8c | Verificar se o grafo de utilizadores é fortemente conexo | ✅ |
| R8d | Recomendações baseadas em proximidade (follows) | ✅ |
| R8e | Estatísticas de visualização entre duas datas | ✅ |
| R8f | Utilizadores que viram séries de um género num período | ✅ |
| R8g | Seguidores que viram o mesmo conteúdo num intervalo | ✅ |
| R9 | GUI (Java Swing) | ✅ |

---

## Algoritmos e Complexidade

### Dijkstra (R8a — caminho mais curto)
- **Algoritmo:** `DijkstraSP` da biblioteca algs4 (Dijkstra com heap binária)
- **Complexidade:** O((V + E) log V), onde V = vértices, E = arestas
- **Limitação:** requer pesos não-negativos (garantido: epoch seconds e ratings são sempre ≥ 0)

### Subgrafos (R8b)
- **Subgrafo por região:** O(U + E), onde U = utilizadores da região
- **Subgrafo por género:** O(V + E), iteração sobre todas as arestas

### Verificação de conectividade (R8c)
- **Algoritmo:** Dijkstra para cada vértice USER
- **Complexidade:** O(U × (V + E) log V) — adequado para grafos de dimensão razoável

### Recomendações (R8d)
- **Algoritmo:** BFS de 1 hop (utilizadores seguidos)
- **Complexidade:** O(F × I), onde F = follows, I = interações por utilizador

---

## Como Compilar e Executar

### Compilar
```bash
javac -cp libs/algs4.jar -d out \
  src/main/java/edu/ufp/streaming/rec/enums/*.java \
  src/main/java/edu/ufp/streaming/rec/models/*.java \
  src/main/java/edu/ufp/streaming/rec/managers/*.java \
  src/main/java/edu/ufp/streaming/rec/gui/*.java \
  test/java/*.java
```

### Executar testes
```bash
# Testes Fase 1
java -cp out:libs/algs4.jar -ea TestUserArtist
java -cp out:libs/algs4.jar -ea TestContent
java -cp out:libs/algs4.jar -ea TestArtistContent

# Testes Fase 2
java -cp out:libs/algs4.jar -ea TestStreamingGraph
```

### Executar GUI
```bash
java -cp out:libs/algs4.jar edu.ufp.streaming.rec.gui.StreamingGUI
```

> **Nota:** a flag `-ea` ativa os `assert` nos testes.

---

## Decisões de Design

1. **`StreamingDatabase` como fachada central** — centraliza todas as operações e garante a consistência R4 (remoção em cascata). Os managers individuais não se conhecem entre si.

2. **Índices múltiplos nos managers** — cada manager mantém uma ST primária (pesquisa por ID) e BSTs secundárias (pesquisa ordenada). Isto triplica o espaço mas reduz a complexidade de pesquisa de O(n) para O(log n).

3. **Grafo com expansão dinâmica** — o `StreamingGraph` duplica a capacidade automaticamente quando necessário, evitando erros de índice sem desperdício excessivo de memória.

4. **Aresta de follow com peso = epoch seconds** — permite ordenar follows cronologicamente e usar Dijkstra para encontrar o "caminho mais recente" entre utilizadores.

5. **Separação Watch/Rate no grafo** — interações BOOKMARK e SKIP não geram arestas no grafo, pois não representam consumo ou avaliação efetiva de conteúdo.# LP2_PROJECT