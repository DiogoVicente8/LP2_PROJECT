package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.DirectedEdge;
import edu.princeton.cs.algs4.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.DijkstraSP;
import edu.princeton.cs.algs4.ST;
import edu.ufp.streaming.rec.enums.InterationType;
import edu.ufp.streaming.rec.models.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa o grafo pesado direcionado heterogéneo da plataforma (Fase 2 — R7).
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
 * <p>Utiliza {@link EdgeWeightedDigraph} da biblioteca algs4.
 * Como a algs4 requer vértices inteiros, todos os IDs das entidades são mapeados
 * para inteiros através de um índice {@code ST<String, Integer>}.
 *
 * @author  Diogo Vicente
 * 
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
     * Não faz nada se o utilizador já estiver presente.
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
     * Não faz nada se o conteúdo já estiver presente.
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
     * Adiciona uma aresta direcionada que representa uma relação de follow (User → User).
     * O peso da aresta é a data do follow expressa em epoch seconds.
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
     * Como a algs4 não suporta remoção de arestas nativamente, reconstrói o grafo
     * sem as arestas afectadas.
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
     * Adiciona uma aresta direcionada que representa uma interacção do utilizador
     * com um conteúdo (User → Content).
     * <ul>
     *   <li>Aresta WATCH — peso = progresso (0.0 a 1.0)</li>
     *   <li>Aresta RATE — peso = classificação (0.0 a 5.0)</li>
     * </ul>
     * Interacções do tipo BOOKMARK e SKIP não são adicionadas ao grafo.
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

    // -------------------------------------------------------------------------
    // R8 — Consultas sobre o grafo
    // -------------------------------------------------------------------------

    /**
     * Calcula o caminho mais curto (menor peso total) entre dois utilizadores
     * através das arestas de follow. Utiliza o algoritmo de Dijkstra.
     *
     * @param idOrigem  ID do utilizador de origem
     * @param idDestino ID do utilizador de destino
     * @return lista de IDs de entidades representando o caminho mais curto
     *         (incluindo origem e destino), ou lista vazia se não existir caminho
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

    /**
     * Retorna todos os seguidores de um utilizador que visualizaram um conteúdo
     * específico dentro de um intervalo de tempo (R8g).
     *
     * @param userId    ID do utilizador cujos seguidores se pretendem verificar
     * @param contentId ID do conteúdo
     * @param de        início do intervalo de tempo (inclusivo)
     * @param ate       fim do intervalo de tempo (inclusivo)
     * @param followMgr o {@link FollowManager} para obter os seguidores
     * @param userMgr   o {@link UserManager} para obter as interacções dos utilizadores
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

    /**
     * Verifica se o subgrafo formado pelas arestas User→User é fortemente conexo,
     * ou seja, se todos os utilizadores conseguem alcançar todos os outros
     * através das relações de follow.
     *
     * @return {@code true} se o subgrafo de utilizadores for fortemente conexo
     */
    public boolean isGrafoUtilizadoresConexo() {
        List<Integer> verticesUtilizadores = getVerticesUtilizadores();
        if (verticesUtilizadores.isEmpty()) return true;

        for (int src : verticesUtilizadores) {
            DijkstraSP sp = new DijkstraSP(grafo, src);
            for (int dest : verticesUtilizadores) {
                if (src != dest && !sp.hasPathTo(dest)) return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Utilitários
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

    /**
     * Retorna o número total de vértices no grafo.
     *
     * @return número de vértices
     */
    public int totalVertices() { return totalVertices; }

    /**
     * Retorna o número total de arestas no grafo.
     *
     * @return número de arestas
     */
    public int totalArestas() { return grafo.E(); }

    /**
     * Retorna o {@link EdgeWeightedDigraph} subjacente da algs4.
     * Útil para algoritmos de grafo adicionais na Fase 2.
     *
     * @return o grafo pesado direcionado da algs4
     */
    public EdgeWeightedDigraph getGrafo() { return grafo; }

    // -------------------------------------------------------------------------
    // Métodos privados auxiliares
    // -------------------------------------------------------------------------

    /**
     * Retorna os índices inteiros de todos os vértices do tipo USER.
     *
     * @return lista de índices de vértices de utilizadores
     */
    private List<Integer> getVerticesUtilizadores() {
        List<Integer> resultado = new ArrayList<>();
        for (Integer idx : indiceParaTipo.keys())
            if ("USER".equals(indiceParaTipo.get(idx))) resultado.add(idx);
        return resultado;
    }

    /**
     * Garante que o grafo tem capacidade suficiente, duplicando-a se necessário.
     */
    private void garantirCapacidade() {
        if (totalVertices < capacidade) return;
        capacidade *= 2;
        EdgeWeightedDigraph novoGrafo = new EdgeWeightedDigraph(capacidade);
        for (int v = 0; v < grafo.V(); v++)
            for (DirectedEdge e : grafo.adj(v))
                novoGrafo.addEdge(e);
        grafo = novoGrafo;
    }

    /**
     * Reconstrói o grafo excluindo todas as arestas que envolvem uma dada entidade.
     * Utilizado para a remoção em cascata quando um utilizador é eliminado.
     *
     * @param excludeId          ID da entidade cujas arestas devem ser excluídas
     * @param excludeContentId   ID opcional de conteúdo a excluir também (pode ser {@code null})
     */
    private void reconstruirGrafoExcluindo(String excludeId, String excludeContentId) {
        EdgeWeightedDigraph novoGrafo = new EdgeWeightedDigraph(capacidade);
        Integer idxExcluido        = idParaIndice.contains(excludeId) ? idParaIndice.get(excludeId) : -1;
        Integer idxConteudoExcluido = (excludeContentId != null && idParaIndice.contains(excludeContentId))
                ? idParaIndice.get(excludeContentId) : -1;

        for (int v = 0; v < grafo.V(); v++) {
            if (v == idxExcluido) continue;
            for (DirectedEdge e : grafo.adj(v)) {
                if (e.from() == idxExcluido || e.to() == idxExcluido) continue;
                if (e.from() == idxConteudoExcluido || e.to() == idxConteudoExcluido) continue;
                novoGrafo.addEdge(e);
            }
        }
        grafo = novoGrafo;
    }
}
