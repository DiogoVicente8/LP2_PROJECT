# Plataforma de Streaming — LP2/AED2 2025/26

## Autores
- **Diogo Vicente- 2024115283**  
- **Pedro- 2024114929** 
---

## Estrutura do Projeto

```
untitled/src/
├── main/java/edu/ufp/streaming/rec/
│   ├── enums/
│   │   ├── ArtistRole.java              # ACTOR, DIRECTOR, PRODUCER, WRITER
│   │   └── InterationType.java          # WATCH, RATE, BOOKMARK, SKIP
│   ├── models/
│   │   ├── User.java                    # Utilizador (password SHA-256+salt, isAdmin)
│   │   ├── Artist.java                  # Artista (ator, realizador, produtor)
│   │   ├── Content.java                 # Classe base de conteúdo (Serializable)
│   │   ├── Movie.java                   # Filme (extends Content)
│   │   ├── Series.java                  # Série (extends Content)
│   │   ├── Documentary.java             # Documentário (extends Content)
│   │   ├── Genre.java                   # Género (Serializable)
│   │   ├── Interation.java              # Interação utilizador-conteúdo
│   │   ├── ArtistContent.java           # Relação artista ↔ conteúdo
│   │   └── UserFollow.java              # Relação de follow entre utilizadores
│   ├── managers/
│   │   ├── UserManager.java             # Gere utilizadores (ST + 2 BSTs)
│   │   ├── ArtistManager.java           # Gere artistas (ST + 2 BSTs)
│   │   ├── ContentManager.java          # Gere conteúdos (ST + ContentBST)
│   │   ├── ContentBST.java              # BST de conteúdos por data de lançamento
│   │   ├── GenreManager.java            # Gere géneros (ST)
│   │   ├── ArtistContentManager.java    # Gere relações artista ↔ conteúdo
│   │   ├── FollowManager.java           # Gere relações de follow (índices bidirecionais)
│   │   ├── ContentFileManager.java      # Import/export ficheiros texto
│   │   ├── ContentSerializer.java       # Serialização binária de conteúdos
│   │   ├── AppStateSerializer.java      # Persistência completa do estado da app
│   │   ├── SeedData.java               # Dados de exemplo (géneros, artistas, conteúdos, utilizadores)
│   │   ├── StreamingDatabase.java       # Fachada central — coordena managers + garante R4
│   │   └── StreamingGraph.java          # Grafo pesado direcionado (Dijkstra + Kosaraju-Sharir)
│   └── gui/
│       ├── Launcher.java                # Ponto de entrada JavaFX
│       ├── StreamingAppFX.java          # Setup inicial da aplicação
│       ├── LoginScreenFX.java           # Ecrã de login e registo
│       ├── StreamingDashboardFX.java    # Dashboard do utilizador normal
│       └── AdminDashboardFX.java        # Painel de administração (acesso restrito)
└── test/java/
    ├── TestUserArtist.java              # Testes Fase 1 — User e Artist
    ├── TestContent.java                 # Testes Fase 1 — Content e Genre
    ├── TestArtistContent.java           # Testes Fase 1 — ArtistContent e cascata R4
    ├── TestStreamingGraph.java          # Testes Fase 2 — StreamingGraph (R8a–R8g)
    └── TestImportacao.java              # Testes de import/export de ficheiros
```

---

## Funcionalidades

### Utilizador Normal
- Navegar e filtrar conteúdos (Filmes, Séries, Documentários) por tipo e título
- Marcar conteúdos como **Visto**, **Guardado** ou **Skip** (estado persistente entre sessões)
- Avaliar conteúdos com rating de 1 a 5 estrelas
- Seguir e deixar de seguir outros utilizadores
- Ver histórico de interações, recomendações e lista de follows
- Editar perfil (nome, email, região, password)
- Explorar artistas filtrados por função (Actor, Director, Producer)
- Usar as ferramentas de grafo (R8a, R8c, R8g)

### Administrador
- **Gestão de Utilizadores** — criar, editar, remover, promover/revogar admin
- **Gestão de Conteúdos** — criar (Filme/Série/Documentário), editar, remover
- **Gestão de Artistas** — criar, editar nacionalidade/nome, remover
- **Gestão de Géneros** — criar, remover (com proteção de cascata)
- Acesso via login com ID `admin` (dashboard exclusivo com badge dourado)

### Persistência
- Toda a informação é guardada automaticamente em `app_state.dat` ao fechar ou fazer logout
- Formato binário proprietário (imune a alterações de classe ao contrário de `ObjectOutputStream`)
- Dados guardados: géneros, conteúdos, artistas, utilizadores, participações artista↔conteúdo, follows, interações

---

## Estruturas de Dados Utilizadas

### Symbol Tables (ST) — `edu.princeton.cs.algs4.ST`
Tabelas de hash primárias em todos os managers.
**Complexidade:** inserção/pesquisa média O(1), pior caso O(n).

| Manager | Chave | Valor |
|---------|-------|-------|
| UserManager | userId | User |
| ArtistManager | artistId | Artist |
| ContentManager | contentId | Content |
| GenreManager | genreId | Genre |
| ArtistContentManager | `"artistId:contentId:role"` | ArtistContent |
| FollowManager | `"followerId:followedId"` | UserFollow |

### Red-Black BSTs — `edu.princeton.cs.algs4.RedBlackBST`
Pesquisas ordenadas e por intervalo.
**Complexidade:** O(log n) garantido em inserção, pesquisa e remoção.

| BST | Chave | Finalidade |
|-----|-------|------------|
| UserManager.byNameBST | nome (String) | Pesquisa por nome |
| UserManager.byDateBST | data registo (Long) | Pesquisa por data |
| ArtistManager.byNameBST | nome (String) | Pesquisa por nome |
| ArtistManager.byBirthDateBST | data nascimento (Long) | Pesquisa por idade |
| ArtistContentManager.byDateBST | data participação (Long) | Filmografia por data |
| FollowManager.byDateBST | timestamp (Long) | Follows por data |
| ContentBST | data lançamento (String) | Conteúdos ordenados por data |

### Grafo — `edu.princeton.cs.algs4.EdgeWeightedDigraph`
Grafo pesado direcionado heterogéneo com dois tipos de vértices (USER e CONTENT) e três tipos de arestas. Expansão dinâmica de capacidade por duplicação.

| Aresta | Peso |
|--------|------|
| User → User (follow) | epoch seconds da data do follow |
| User → Content (WATCH) | progresso (0.0 a 1.0) |
| User → Content (RATE) | rating (0.0 a 5.0) |

---

## Requisitos Implementados

### Fase 1
| Req. | Descrição | Estado |
|------|-----------|:------:|
| R1 | Diagrama UML | ✅ |
| R2 | CRUD de Géneros, Conteúdos, Utilizadores, Artistas | ✅ |
| R3 | Pesquisas (substring, data, região, género, etc.) | ✅ |
| R4 | Consistência em cascata na remoção | ✅ |
| R5 | Interações utilizador-conteúdo (Watch, Rate, Bookmark, Skip) | ✅ |
| R10 | Import/Export ficheiros texto | ✅ |
| R11 | Serialização binária | ✅ |

### Fase 2
| Req. | Descrição | Estado |
|------|-----------|:------:|
| R7 | Grafo pesado direcionado | ✅ |
| R8a | Caminho mais curto entre utilizadores (Dijkstra) | ✅ |
| R8b | Extração de subgrafos por região e por género | ✅ |
| R8c | Verificar se o grafo de utilizadores é fortemente conexo (Kosaraju-Sharir) | ✅ |
| R8d | Recomendações baseadas em proximidade (follows) | ✅ |
| R8e | Estatísticas de visualização entre duas datas | ✅ |
| R8f | Utilizadores que viram séries de um género num período | ✅ |
| R8g | Seguidores que viram o mesmo conteúdo num intervalo | ✅ |
| R9 | GUI JavaFX (Login, Dashboard, Admin) | ✅ |

---

## Algoritmos e Complexidade

### Dijkstra (R8a — caminho mais curto)
- **Algoritmo:** `DijkstraSP` da biblioteca algs4 (heap binária)
- **Complexidade:** O((V + E) log V)
- **Subgrafo:** apenas vértices USER, evitando atalhos via conteúdos

### Kosaraju-Sharir (R8c — componentes fortemente conexas)
- **Algoritmo:** `KosarajuSharirSCC` da biblioteca algs4
- **Complexidade:** O(V + E)
- **Remapeamento:** IDs para índices contíguos antes de correr o algoritmo

### Recomendações (R8d)
- **Algoritmo:** BFS de 1 hop (utilizadores seguidos pelo loggedUser)
- **Complexidade:** O(F × I), onde F = follows, I = interações por utilizador

---

### Credenciais de exemplo
| ID | Password | Papel |
|----|----------|-------|
| `u1` | `alice123` | Utilizador normal |
| `u2` | `bruno123` | Utilizador normal |
| `admin` | `admin123` | Administrador |

> **Nota:** ao correr pela primeira vez, apagar o ficheiro `app_state.dat` se existir, para garantir que os dados de seed são recriados corretamente.

---

