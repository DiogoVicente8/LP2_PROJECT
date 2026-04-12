package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.DijkstraSP;
import edu.princeton.cs.algs4.KosarajuSharirSCC;
import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.enums.InterationType;
import edu.ufp.streaming.rec.models.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Representa o grafo pesado direcionado heterogéneo da plataforma (Fase 2 — R7/R8).
 *
 * <p>Os vértices representam entidades do tipo {@link User} e {@link Content}.
 * Cada ID de entidade é mapeado para um índice inteiro único através de uma {@link ST}.
 * As arestas representam relações:
 * <ul>
 *   <li>{@code User → User} (seguir) — peso = epoch seconds da data do follow</li>
 *   <li>{@code User → Content} (WATCH) — peso = progresso de visualização (0.0 a 1.0)</li>
 *   <li>{@code User → Content} (RATE) — peso = classificação (0.0 a 5.0)</li>
 * </ul>
 *
 * @author Diogo Vicente
 */
public class StreamingGraph {

    /** Mapeia ID da entidade (userId ou contentId) → índice inteiro do vértice. */
    private final ST<String, Integer> idParaIndice;

    /** Mapeia índice inteiro do vértice → ID da entidade. */
    private final ST<Integer, String> indiceParaId;

    /** Mapeia índice inteiro do vértice → tipo de entidade ("USER" ou "CONTENT"). */
    private final ST<Integer, String> indiceParaTipo;

    /** O grafo pesado direcionado subjacente da algs4. */
    private EdgeWeightedDigraph grafo;

    /** Número actual de vértices no grafo. */
    private int totalVertices;

    /** Capacidade máxima actual (duplica automaticamente quando necessário). */
    private int capacidade;

    /**
     * Constrói um StreamingGraph vazio com uma capacidade inicial.
     *
     * @param capacidadeInicial número máximo inicial de vértices
     */
    public StreamingGraph(int capacidadeInicial) {
        this.capacidade     = capacidadeInicial;
        this.idParaIndice   = new ST<>();
        this.indiceParaId   = new ST<>();
        this.indiceParaTipo = new ST<>();
        this.grafo          = new EdgeWeightedDigraph(capacidade);
        this.totalVertices  = 0;
    }

    // -------------------------------------------------------------------------
    // Gestão de vértices
    // -------------------------------------------------------------------------

    /**
     * Adiciona um {@link User} como vértice no grafo.
     *
     * @param user o {@link User} a adicionar
     */
    public void addUser(User user) {
        if (user == null || idParaIndice.contains(user.getId())) return;
        garantirCapacidade();
        int idx = totalVertices++;
        idParaIndice.put(user.getId(), idx);
        indiceParaId.put(idx, user.getId());
        indiceParaTipo.put(idx, "USER");
    }

    /**
     * Adiciona um {@link Content} como vértice no grafo.
     *
     * @param content o {@link Content} a adicionar
     */
    public void addContent(Content content) {
        if (content == null || idParaIndice.contains(content.getId())) return;
        garantirCapacidade();
        int idx = totalVertices++;
        idParaIndice.put(content.getId(), idx);
        indiceParaId.put(idx, content.getId());
        indiceParaTipo.put(idx, "CONTENT");
    }

    // -------------------------------------------------------------------------
    // Gestão de arestas — User → User (seguir)
    // -------------------------------------------------------------------------

    /**
     * Adiciona uma aresta direcionada de follow (User → User).
     *
     * @param follow o {@link UserFollow} a representar como aresta
     */
    public void addFollowEdge(UserFollow follow) {
        if (follow == null) return;
        String origemId  = follow.getFollower().getId();
        String destinoId = follow.getFollowed().getId();
        if (!idParaIndice.contains(origemId) || !idParaIndice.contains(destinoId)) return;

        double peso = follow.getDate().toEpochSecond(ZoneOffset.UTC);
        grafo.addEdge(new DirectedEdge(idParaIndice.get(origemId), idParaIndice.get(destinoId), peso));
    }

    /**
     * Remove todas as arestas de follow envolvendo um dado utilizador.
     *
     * @param userId o ID do utilizador cujas arestas de follow devem ser removidas
     */
    public void removeFollowEdges(String userId) {
        reconstruirGrafoExcluindo(userId, null);
    }

    // -------------------------------------------------------------------------
    // Gestão de arestas — User → Content (visualizar / classificar)
    // -------------------------------------------------------------------------

    /**
     * Adiciona uma aresta direcionada de interação User → Content.
     *
     * @param interacao a {@link Interation} a representar como aresta
     */
    public void addInteractionEdge(Interation interacao) {
        if (interacao == null) return;
        if (interacao.getType() != InterationType.WATCH
                && interacao.getType() != InterationType.RATE) return;

        String origemId  = interacao.getUser().getId();
        String destinoId = interacao.getContent().getId();
        if (!idParaIndice.contains(origemId) || !idParaIndice.contains(destinoId)) return;

        double peso = interacao.getType() == InterationType.WATCH
                ? interacao.getProgress()
                : interacao.getRating();

        grafo.addEdge(new DirectedEdge(idParaIndice.get(origemId), idParaIndice.get(destinoId), peso));
    }

    /**
     * Remove todas as arestas e o vértice de um conteúdo removido do sistema (R4).
     *
     * @param contentId o ID do conteúdo a remover do grafo
     */
    public void removeContentEdges(String contentId) {
        reconstruirGrafoExcluindo(null, contentId);
    }

    // -------------------------------------------------------------------------
    // R8a — Caminho mais curto entre utilizadores
    // -------------------------------------------------------------------------

    /**
     * Calcula o caminho mais curto entre dois utilizadores via Dijkstra.
     *
     * @param idOrigem  ID do utilizador de origem
     * @param idDestino ID do utilizador de destino
     * @return lista de IDs representando o caminho, ou lista vazia se não existir
     */
    public List<String> caminhoMaisCurtoBetweenUsers(String idOrigem, String idDestino) {
        List<String> caminho = new ArrayList<>();
        if (!idParaIndice.contains(idOrigem) || !idParaIndice.contains(idDestino)) return caminho;

        int src  = idParaIndice.get(idOrigem);
        int dest = idParaIndice.get(idDestino);

        DijkstraSP sp = new DijkstraSP(grafo, src);
        if (!sp.hasPathTo(dest)) return caminho;

        for (DirectedEdge e : sp.pathTo(dest)) {
            if (caminho.isEmpty()) caminho.add(indiceParaId.get(e.from()));
            caminho.add(indiceParaId.get(e.to()));
        }
        return caminho;
    }

    /**
     * Retorna o peso total do caminho mais curto entre dois utilizadores.
     *
     * @param idOrigem  ID do utilizador de origem
     * @param idDestino ID do utilizador de destino
     * @return o peso total, ou {@code Double.POSITIVE_INFINITY} se não existir caminho
     */
    public double pesoCaminhoMaisCurto(String idOrigem, String idDestino) {
        if (!idParaIndice.contains(idOrigem) || !idParaIndice.contains(idDestino))
            return Double.POSITIVE_INFINITY;

        int src  = idParaIndice.get(idOrigem);
        int dest = idParaIndice.get(idDestino);

        DijkstraSP sp = new DijkstraSP(grafo, src);
        return sp.hasPathTo(dest) ? sp.distTo(dest) : Double.POSITIVE_INFINITY;
    }

    // -------------------------------------------------------------------------
    // R8b — Extração de subgrafos
    // -------------------------------------------------------------------------

    /**
     * Extrai um subgrafo com utilizadores de uma determinada região e as suas arestas de follow.
     *
     * @param region  a região a filtrar (ex: "PT", "US")
     * @param userMgr o {@link UserManager} para obter os utilizadores
     * @return novo {@link EdgeWeightedDigraph} apenas com utilizadores dessa região
     */
    public EdgeWeightedDigraph subgrafoByRegion(String region, UserManager userMgr) {
        Set<Integer> idxRegiao = new HashSet<>();
        for (User u : userMgr.searchByRegion(region)) {
            if (idParaIndice.contains(u.getId()))
                idxRegiao.add(idParaIndice.get(u.getId()));
        }

        EdgeWeightedDigraph sub = new EdgeWeightedDigraph(capacidade);
        for (int v : idxRegiao) {
            for (DirectedEdge e : grafo.adj(v)) {
                if (idxRegiao.contains(e.to())) sub.addEdge(e);
            }
        }
        return sub;
    }

    /**
     * Extrai um subgrafo com conteúdos de um determinado género e as arestas User→Content.
     *
     * @param genreId    ID do género a filtrar
     * @param contentMgr o {@link ContentManager} para obter os conteúdos
     * @return novo {@link EdgeWeightedDigraph} apenas com conteúdos desse género
     */
    public EdgeWeightedDigraph subgrafoByGenre(String genreId, ContentManager contentMgr) {
        Set<Integer> idxGenero = new HashSet<>();
        for (Content c : contentMgr.searchByGenre(genreId)) {
            if (idParaIndice.contains(c.getId()))
                idxGenero.add(idParaIndice.get(c.getId()));
        }

        EdgeWeightedDigraph sub = new EdgeWeightedDigraph(capacidade);
        for (int v = 0; v < grafo.V(); v++) {
            for (DirectedEdge e : grafo.adj(v)) {
                if (idxGenero.contains(e.to())) sub.addEdge(e);
            }
        }
        return sub;
    }

    /**
     * Extrai um subgrafo apenas com utilizadores que classificaram conteúdos acima de um rating mínimo.
     * Inclui as arestas de follow entre esses utilizadores.
     *
     * @param minRating  rating mínimo (inclusive)
     * @param userMgr    o {@link UserManager} para obter as interações
     * @return novo {@link EdgeWeightedDigraph} com utilizadores que cumprem o critério
     */
    public EdgeWeightedDigraph subgrafoByMinRating(double minRating, UserManager userMgr) {
        Set<Integer> idxUtilizadores = new HashSet<>();
        for (User u : userMgr.listAll()) {
            boolean qualifica = false;
            for (var i : u.getInteractions()) {
                if (i.getType() == InterationType.RATE && i.getRating() >= minRating) {
                    qualifica = true;
                    break;
                }
            }
            if (qualifica && idParaIndice.contains(u.getId()))
                idxUtilizadores.add(idParaIndice.get(u.getId()));
        }

        EdgeWeightedDigraph sub = new EdgeWeightedDigraph(capacidade);
        for (int v : idxUtilizadores) {
            for (DirectedEdge e : grafo.adj(v)) {
                if (idxUtilizadores.contains(e.to())) sub.addEdge(e);
            }
        }
        return sub;
    }

    // -------------------------------------------------------------------------
    // R8b (cont.) — Caminho mais curto entre dois artistas via conteúdos partilhados
    // -------------------------------------------------------------------------

    /**
     * Calcula o caminho mais curto entre dois artistas com base nos conteúdos
     * em que participaram em conjunto (grafo artista → artista via conteúdos partilhados).
     *
     * <p>Dois artistas estão ligados se participaram no mesmo conteúdo.
     * O peso da aresta é o {@code toEpochDay()} da data de participação mais antiga
     * no conteúdo partilhado. O algoritmo constrói um grafo temporário
     * artista→artista e aplica Dijkstra.
     *
     * @param artistIdOrigem  ID do artista de origem
     * @param artistIdDestino ID do artista de destino
     * @param acMgr           o {@link ArtistContentManager} para obter as participações
     * @return lista de IDs de artistas representando o caminho, ou lista vazia se não existir
     */
    public List<String> caminhoMaisCurtoEntreArtistas(String artistIdOrigem,
                                                      String artistIdDestino,
                                                      ArtistContentManager acMgr) {
        // Recolher todos os artistas presentes nas participações
        List<ArtistContent> todasParticipacoes = acMgr.listAll();
        if (todasParticipacoes.isEmpty()) return new ArrayList<>();

        // Mapear artistId → índice local para o grafo temporário
        ST<String, Integer> artistIdx = new ST<>();
        ST<Integer, String> idxArtist = new ST<>();
        int count = 0;
        for (ArtistContent ac : todasParticipacoes) {
            String aid = ac.getArtist().getId();
            if (!artistIdx.contains(aid)) {
                artistIdx.put(aid, count);
                idxArtist.put(count, aid);
                count++;
            }
        }

        if (!artistIdx.contains(artistIdOrigem) || !artistIdx.contains(artistIdDestino))
            return new ArrayList<>();

        // Construir grafo temporário artista → artista via conteúdos partilhados
        // Agrupar participações por contentId para encontrar pares de artistas no mesmo conteúdo
        ST<String, List<ArtistContent>> porConteudo = new ST<>();
        for (ArtistContent ac : todasParticipacoes) {
            String cid = ac.getContent().getId();
            List<ArtistContent> lista = porConteudo.contains(cid)
                    ? porConteudo.get(cid) : new ArrayList<>();
            lista.add(ac);
            porConteudo.put(cid, lista);
        }

        EdgeWeightedDigraph grafoArtistas = new EdgeWeightedDigraph(count);
        for (String cid : porConteudo.keys()) {
            List<ArtistContent> participantes = porConteudo.get(cid);
            for (int i = 0; i < participantes.size(); i++) {
                for (int j = 0; j < participantes.size(); j++) {
                    if (i == j) continue;
                    ArtistContent a = participantes.get(i);
                    ArtistContent b = participantes.get(j);
                    int idxA = artistIdx.get(a.getArtist().getId());
                    int idxB = artistIdx.get(b.getArtist().getId());
                    // Peso = dia da época da participação (data mais antiga do par)
                    double peso = Math.min(
                            a.getDate().toEpochDay(),
                            b.getDate().toEpochDay());
                    grafoArtistas.addEdge(new DirectedEdge(idxA, idxB, peso));
                }
            }
        }

        int src  = artistIdx.get(artistIdOrigem);
        int dest = artistIdx.get(artistIdDestino);

        DijkstraSP sp = new DijkstraSP(grafoArtistas, src);
        List<String> caminho = new ArrayList<>();
        if (!sp.hasPathTo(dest)) return caminho;

        for (DirectedEdge e : sp.pathTo(dest)) {
            if (caminho.isEmpty()) caminho.add(idxArtist.get(e.from()));
            caminho.add(idxArtist.get(e.to()));
        }
        return caminho;
    }

    // -------------------------------------------------------------------------
    // R8c — Verificar se o grafo de utilizadores é fortemente conexo
    // -------------------------------------------------------------------------

    /**
     * Verifica se o subgrafo de utilizadores é fortemente conexo, recorrendo ao
     * algoritmo de Kosaraju-Sharir (KosarajuSharirSCC da algs4).
     *
     * <p>Um grafo dirigido é fortemente conexo se todos os pares de vértices
     * se alcançam mutuamente. Kosaraju-Sharir determina as componentes fortemente
     * conexas (SCCs) em O(V+E) — correto e eficiente para este fim.
     *
     * @return {@code true} se todos os utilizadores se alcançam mutuamente via follow
     */
    public boolean isGrafoUtilizadoresConexo() {
        List<Integer> verticesUtilizadores = getVerticesUtilizadores();
        if (verticesUtilizadores.size() <= 1) return true;

        // Construir um Digraph (não pesado) apenas com os vértices de utilizadores
        // KosarajuSharirSCC da algs4 opera sobre Digraph, não EdgeWeightedDigraph
        int n = capacidade;
        Digraph digraphUtilizadores = new Digraph(n);

        for (int v : verticesUtilizadores) {
            for (DirectedEdge e : grafo.adj(v)) {
                if (verticesUtilizadores.contains(e.to())) {
                    digraphUtilizadores.addEdge(e.from(), e.to());
                }
            }
        }

        KosarajuSharirSCC scc = new KosarajuSharirSCC(digraphUtilizadores);

        // Verificar que todos os vértices de utilizadores pertencem à mesma SCC
        int componenteReferencia = scc.id(verticesUtilizadores.get(0));
        for (int v : verticesUtilizadores) {
            if (scc.id(v) != componenteReferencia) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // R8d — Recomendações baseadas em proximidade
    // -------------------------------------------------------------------------

    /**
     * Recomenda conteúdos a um utilizador com base no que os utilizadores seguidos viram.
     *
     * @param userId    ID do utilizador que recebe recomendações
     * @param followMgr o {@link FollowManager} para obter os utilizadores seguidos
     * @param userMgr   o {@link UserManager} para obter as interações
     * @return lista de {@link Content} recomendados (sem duplicados, sem os já vistos)
     */
    public List<Content> recomendarConteudosPorProximidade(String userId,
                                                           FollowManager followMgr,
                                                           UserManager userMgr) {
        User user = userMgr.get(userId);
        if (user == null) return new ArrayList<>();

        Set<String> jaViu = new HashSet<>();
        for (Interation i : user.getInteractions()) {
            if (i.getType() == InterationType.WATCH) jaViu.add(i.getContent().getId());
        }

        LinkedHashMap<String, Content> recomendacoes = new LinkedHashMap<>();
        for (User seguido : followMgr.getFollowing(userId)) {
            User u = userMgr.get(seguido.getId());
            if (u == null) continue;
            for (Interation i : u.getInteractions()) {
                if (i.getType() == InterationType.WATCH && !jaViu.contains(i.getContent().getId())) {
                    recomendacoes.put(i.getContent().getId(), i.getContent());
                }
            }
        }
        return new ArrayList<>(recomendacoes.values());
    }

    // -------------------------------------------------------------------------
    // R8e — Estatísticas de visualização de um conteúdo entre duas datas
    // -------------------------------------------------------------------------

    /**
     * Retorna estatísticas de visualização de um conteúdo num intervalo de datas.
     * Calcula: número de visualizações, progresso médio e rating médio.
     *
     * @param contentId ID do conteúdo
     * @param de        início do intervalo (inclusivo)
     * @param ate       fim do intervalo (inclusivo)
     * @param userMgr   o {@link UserManager} para aceder às interações
     * @return mapa com chaves "visualizacoes", "progressoMedio", "ratingMedio"
     */
    public Map<String, Double> estatisticasVisualizacao(String contentId,
                                                        LocalDateTime de,
                                                        LocalDateTime ate,
                                                        UserManager userMgr) {
        Map<String, Double> stats = new HashMap<>();
        int visualizacoes = 0;
        double somaProgresso = 0;
        double somaRating = 0;
        int countRating = 0;

        for (User u : userMgr.listAll()) {
            for (Interation i : u.getInteractions()) {
                if (!i.getContent().getId().equals(contentId)) continue;
                LocalDateTime data = i.getWatchDate();
                if (data.isBefore(de) || data.isAfter(ate)) continue;

                if (i.getType() == InterationType.WATCH) {
                    visualizacoes++;
                    somaProgresso += i.getProgress();
                } else if (i.getType() == InterationType.RATE) {
                    somaRating += i.getRating();
                    countRating++;
                }
            }
        }

        stats.put("visualizacoes", (double) visualizacoes);
        stats.put("progressoMedio", visualizacoes > 0 ? somaProgresso / visualizacoes : 0.0);
        stats.put("ratingMedio", countRating > 0 ? somaRating / countRating : 0.0);
        return stats;
    }

    // -------------------------------------------------------------------------
    // R8f — Utilizadores que viram séries de um género num período
    // -------------------------------------------------------------------------

    /**
     * Retorna os utilizadores que visualizaram séries de um determinado género
     * dentro de um intervalo de datas.
     *
     * @param genreId    ID do género
     * @param de         início do intervalo (inclusivo)
     * @param ate        fim do intervalo (inclusivo)
     * @param userMgr    o {@link UserManager} para aceder às interações
     * @param contentMgr o {@link ContentManager} para verificar o tipo de conteúdo
     * @return lista de {@link User} que viram séries do género no período
     */
    public List<User> utilizadoresQueViramSeriesDeGenero(String genreId,
                                                         LocalDateTime de,
                                                         LocalDateTime ate,
                                                         UserManager userMgr,
                                                         ContentManager contentMgr) {
        List<User> resultado = new ArrayList<>();
        for (User u : userMgr.listAll()) {
            boolean viu = false;
            for (Interation i : u.getInteractions()) {
                if (i.getType() != InterationType.WATCH) continue;
                Content c = i.getContent();
                if (!(c instanceof Series)) continue;
                if (!c.getGenre().getId().equals(genreId)) continue;
                LocalDateTime data = i.getWatchDate();
                if (!data.isBefore(de) && !data.isAfter(ate)) {
                    viu = true;
                    break;
                }
            }
            if (viu) resultado.add(u);
        }
        return resultado;
    }

    // -------------------------------------------------------------------------
    // R8g — Seguidores que viram o mesmo conteúdo num intervalo
    // -------------------------------------------------------------------------

    /**
     * Retorna todos os seguidores de um utilizador que visualizaram um conteúdo
     * específico dentro de um intervalo de tempo.
     *
     * @param userId    ID do utilizador cujos seguidores se pretendem verificar
     * @param contentId ID do conteúdo
     * @param de        início do intervalo de tempo (inclusivo)
     * @param ate       fim do intervalo de tempo (inclusivo)
     * @param followMgr o {@link FollowManager} para obter os seguidores
     * @param userMgr   o {@link UserManager} para obter as interações dos utilizadores
     * @return lista de {@link User} seguidores que viram o conteúdo no intervalo
     */
    public List<User> seguidoresQueViramConteudo(String userId, String contentId,
                                                 LocalDateTime de, LocalDateTime ate,
                                                 FollowManager followMgr,
                                                 UserManager userMgr) {
        List<User> resultado = new ArrayList<>();
        List<User> seguidores = followMgr.getFollowers(userId);

        for (User seguidor : seguidores) {
            User u = userMgr.get(seguidor.getId());
            if (u == null) continue;
            for (Interation interacao : u.getInteractions()) {
                if (!interacao.getContent().getId().equals(contentId)) continue;
                if (interacao.getType() != InterationType.WATCH) continue;
                LocalDateTime dataVisualizacao = interacao.getWatchDate();
                if (!dataVisualizacao.isBefore(de) && !dataVisualizacao.isAfter(ate)) {
                    resultado.add(seguidor);
                    break;
                }
            }
        }
        return resultado;
    }

    // -------------------------------------------------------------------------
    // Utilitários públicos
    // -------------------------------------------------------------------------

    /**
     * Retorna o índice inteiro de um dado ID de entidade.
     *
     * @param id o ID da entidade
     * @return o índice inteiro, ou {@code -1} se não encontrado
     */
    public int indiceDe(String id) {
        return idParaIndice.contains(id) ? idParaIndice.get(id) : -1;
    }

    /**
     * Retorna o ID da entidade de um dado índice inteiro.
     *
     * @param indice o índice inteiro
     * @return o ID da entidade, ou {@code null} se não encontrado
     */
    public String idDe(int indice) {
        return indiceParaId.contains(indice) ? indiceParaId.get(indice) : null;
    }

    /**
     * Retorna o tipo de uma dada entidade ("USER" ou "CONTENT").
     *
     * @param id o ID da entidade
     * @return string de tipo, ou {@code null} se não encontrado
     */
    public String tipoDe(String id) {
        if (!idParaIndice.contains(id)) return null;
        return indiceParaTipo.get(idParaIndice.get(id));
    }

    /** @return número total de vértices no grafo */
    public int totalVertices() { return totalVertices; }

    /** @return número total de arestas no grafo */
    public int totalArestas() { return grafo.E(); }

    /**
     * Retorna o {@link EdgeWeightedDigraph} subjacente.
     *
     * @return o grafo pesado direcionado da algs4
     */
    public EdgeWeightedDigraph getGrafo() { return grafo; }

    // -------------------------------------------------------------------------
    // Métodos privados auxiliares
    // -------------------------------------------------------------------------

    private List<Integer> getVerticesUtilizadores() {
        List<Integer> resultado = new ArrayList<>();
        for (Integer idx : indiceParaTipo.keys())
            if ("USER".equals(indiceParaTipo.get(idx))) resultado.add(idx);
        return resultado;
    }

    private void garantirCapacidade() {
        if (totalVertices < capacidade) return;
        capacidade *= 2;
        EdgeWeightedDigraph novoGrafo = new EdgeWeightedDigraph(capacidade);
        for (int v = 0; v < grafo.V(); v++)
            for (DirectedEdge e : grafo.adj(v))
                novoGrafo.addEdge(e);
        grafo = novoGrafo;
    }

    private void reconstruirGrafoExcluindo(String excludeUserId, String excludeContentId) {
        EdgeWeightedDigraph novoGrafo = new EdgeWeightedDigraph(capacidade);
        Integer idxUser    = (excludeUserId    != null && idParaIndice.contains(excludeUserId))
                ? idParaIndice.get(excludeUserId)    : -1;
        Integer idxContent = (excludeContentId != null && idParaIndice.contains(excludeContentId))
                ? idParaIndice.get(excludeContentId) : -1;

        for (int v = 0; v < grafo.V(); v++) {
            if (v == idxUser || v == idxContent) continue;
            for (DirectedEdge e : grafo.adj(v)) {
                if (e.from() == idxUser    || e.to() == idxUser)    continue;
                if (e.from() == idxContent || e.to() == idxContent) continue;
                novoGrafo.addEdge(e);
            }
        }
        grafo = novoGrafo;
    }
}