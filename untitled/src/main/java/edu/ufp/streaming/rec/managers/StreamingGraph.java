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
 * Representa o grafo pesado direcionado heterogéneo da plataforma (Fase 2).
 * * @author Diogo Vicente
 */
public class StreamingGraph {

    private final ST<String, Integer> idParaIndice;
    private final ST<Integer, String> indiceParaId;
    private final ST<Integer, String> indiceParaTipo;
    private EdgeWeightedDigraph grafo;
    private int totalVertices;
    private int capacidade;

    public StreamingGraph(int capacidadeInicial) {
        this.capacidade     = capacidadeInicial;
        this.idParaIndice   = new ST<>();
        this.indiceParaId   = new ST<>();
        this.indiceParaTipo = new ST<>();
        this.grafo          = new EdgeWeightedDigraph(capacidade);
        this.totalVertices  = 0;
    }

    // -------------------------------------------------------------------------
    // Gestão de Vértices e Arestas
    // -------------------------------------------------------------------------

    public void addUser(User user) {
        if (user == null || idParaIndice.contains(user.getId())) return;
        garantirCapacidade();
        int idx = totalVertices++;
        idParaIndice.put(user.getId(), idx);
        indiceParaId.put(idx, user.getId());
        indiceParaTipo.put(idx, "USER");
    }

    public void addContent(Content content) {
        if (content == null || idParaIndice.contains(content.getId())) return;
        garantirCapacidade();
        int idx = totalVertices++;
        idParaIndice.put(content.getId(), idx);
        indiceParaId.put(idx, content.getId());
        indiceParaTipo.put(idx, "CONTENT");
    }

    public void addFollowEdge(UserFollow follow) {
        if (follow == null) return;
        String origemId  = follow.getFollower().getId();
        String destinoId = follow.getFollowed().getId();
        if (!idParaIndice.contains(origemId) || !idParaIndice.contains(destinoId)) return;

        long dataAtual = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        double peso = (double) (dataAtual - follow.getDate().toEpochSecond(ZoneOffset.UTC));
        grafo.addEdge(new DirectedEdge(idParaIndice.get(origemId), idParaIndice.get(destinoId), peso));
    }

    public void removeUserEdges(String userId) {
        reconstruirGrafoExcluindo(userId, null);
    }

    public void addInteractionEdge(Interation interacao) {
        if (interacao == null) return;
        if (interacao.type() != InterationType.WATCH && interacao.type() != InterationType.RATE) return;

        String origemId  = interacao.user().getId();
        String destinoId = interacao.content().getId();
        if (!idParaIndice.contains(origemId) || !idParaIndice.contains(destinoId)) return;

        double peso = interacao.type() == InterationType.WATCH
                ? (1.0 - interacao.progress())
                : (5.0 - interacao.rating());

        grafo.addEdge(new DirectedEdge(idParaIndice.get(origemId), idParaIndice.get(destinoId), peso));
    }

    public void removeContentEdges(String contentId) {
        reconstruirGrafoExcluindo(null, contentId);
    }

    // -------------------------------------------------------------------------
    // R8a — Caminhos Mais Curtos (Refatorado para não duplicar código)
    // -------------------------------------------------------------------------

    /**
     * Auxiliar que cria um subgrafo apenas de utilizadores e mapeamentos, e corre o Dijkstra.
     * Isto resolve a duplicação de código apontada pelo IDE.
     */
    private DijkstraResult runDijkstraOnUsers(String idOrigem) {
        List<Integer> verticesUser = getVerticesUtilizadores();
        Set<Integer> userSet = new HashSet<>(verticesUser);

        Map<Integer, Integer> remap    = new HashMap<>();
        Map<Integer, Integer> remapInv = new HashMap<>();
        int k = 0;
        for (int v : verticesUser) { remap.put(v, k); remapInv.put(k, v); k++; }

        EdgeWeightedDigraph subUser = new EdgeWeightedDigraph(k);
        for (int v : verticesUser) {
            for (DirectedEdge e : grafo.adj(v)) {
                if (userSet.contains(e.to())) {
                    subUser.addEdge(new DirectedEdge(remap.get(e.from()), remap.get(e.to()), e.weight()));
                }
            }
        }

        int src = remap.get(idParaIndice.get(idOrigem));
        return new DijkstraResult(new DijkstraSP(subUser, src), remap, remapInv);
    }

    // Record local para devolver múltiplos valores de forma elegante
    private record DijkstraResult(DijkstraSP sp, Map<Integer, Integer> remap, Map<Integer, Integer> remapInv) {}

    public List<String> caminhoMaisCurtoBetweenUsers(String idOrigem, String idDestino) {
        List<String> caminho = new ArrayList<>();
        if (!idParaIndice.contains(idOrigem) || !idParaIndice.contains(idDestino)) return caminho;

        DijkstraResult res = runDijkstraOnUsers(idOrigem);
        int dest = res.remap().get(idParaIndice.get(idDestino));

        if (!res.sp().hasPathTo(dest)) return caminho;

        for (DirectedEdge e : res.sp().pathTo(dest)) {
            if (caminho.isEmpty()) caminho.add(indiceParaId.get(res.remapInv().get(e.from())));
            caminho.add(indiceParaId.get(res.remapInv().get(e.to())));
        }
        return caminho;
    }

    public double pesoCaminhoMaisCurto(String idOrigem, String idDestino) {
        if (!idParaIndice.contains(idOrigem) || !idParaIndice.contains(idDestino)) return Double.POSITIVE_INFINITY;

        DijkstraResult res = runDijkstraOnUsers(idOrigem);
        int dest = res.remap().get(idParaIndice.get(idDestino));

        return res.sp().hasPathTo(dest) ? res.sp().distTo(dest) : Double.POSITIVE_INFINITY;
    }

    // -------------------------------------------------------------------------
    // R8b — Extração de Subgrafos
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // R8c — Verificar Conectividade
    // -------------------------------------------------------------------------

    public boolean isGrafoUtilizadoresConexo() {
        List<Integer> verticesUtilizadores = getVerticesUtilizadores();
        if (verticesUtilizadores.size() <= 1) return true;

        Map<Integer, Integer> remap = new HashMap<>();
        int k = 0;
        for (int v : verticesUtilizadores) remap.put(v, k++);

        Set<Integer> userSet = new HashSet<>(verticesUtilizadores);
        Digraph digraphUtilizadores = new Digraph(k);

        for (int v : verticesUtilizadores) {
            for (DirectedEdge e : grafo.adj(v)) {
                if (userSet.contains(e.to())) {
                    digraphUtilizadores.addEdge(remap.get(e.from()), remap.get(e.to()));
                }
            }
        }

        KosarajuSharirSCC scc = new KosarajuSharirSCC(digraphUtilizadores);

        int componenteReferencia = scc.id(remap.get(verticesUtilizadores.get(0)));
        for (int v : verticesUtilizadores) {
            if (scc.id(remap.get(v)) != componenteReferencia) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // R8d — Recomendações
    // -------------------------------------------------------------------------

    public List<Content> recomendarConteudosPorProximidade(String userId, FollowManager followMgr, UserManager userMgr) {
        User utilizadorPrincipal = userMgr.get(userId);
        if (utilizadorPrincipal == null) return new ArrayList<>();

        Set<String> conteudosJaVistos = new HashSet<>();
        for (Interation i : utilizadorPrincipal.getInteractions()) {
            if (i.type() == InterationType.WATCH) conteudosJaVistos.add(i.content().getId());
        }

        LinkedHashMap<String, Content> recomendacoes = new LinkedHashMap<>();
        for (User utilizadorSeguido : followMgr.getFollowing(userId)) {
            User amigo = userMgr.get(utilizadorSeguido.getId());
            if (amigo == null) continue;

            for (Interation i : amigo.getInteractions()) {
                if (i.type() == InterationType.WATCH && !conteudosJaVistos.contains(i.content().getId())) {
                    recomendacoes.put(i.content().getId(), i.content());
                }
            }
        }
        return new ArrayList<>(recomendacoes.values());
    }

    // -------------------------------------------------------------------------
    // R8e, R8f, R8g — Estatísticas e Filtragem
    // -------------------------------------------------------------------------

    public Map<String, Double> estatisticasVisualizacao(String contentId, LocalDateTime de, LocalDateTime ate, UserManager userMgr) {
        Map<String, Double> stats = new HashMap<>();
        int totalVisualizacoes = 0;
        double somaProgresso = 0;
        double somaRating = 0;
        int totalRatings = 0;

        for (User u : userMgr.listAll()) {
            for (Interation i : u.getInteractions()) {
                if (i.content().getId().equals(contentId) && !i.watchDate().isBefore(de) && !i.watchDate().isAfter(ate)) {
                    if (i.type() == InterationType.WATCH) {
                        totalVisualizacoes++;
                        somaProgresso += i.progress();
                    } else if (i.type() == InterationType.RATE) {
                        somaRating += i.rating();
                        totalRatings++;
                    }
                }
            }
        }

        stats.put("visualizações", (double) totalVisualizacoes);
        stats.put("progressoMedio", totalVisualizacoes > 0 ? somaProgresso / totalVisualizacoes : 0.0);
        stats.put("ratingMedio", totalRatings > 0 ? somaRating / totalRatings : 0.0);
        return stats;
    }

    public List<User> utilizadoresQueViramSeriesDeGenero(String genreId, LocalDateTime de, LocalDateTime ate, UserManager userMgr, ContentManager contents) {
        List<User> resultado = new ArrayList<>();
        for (User u : userMgr.listAll()) {
            boolean viuSerie = u.getInteractions().stream().anyMatch(i ->
                    i.type() == InterationType.WATCH &&
                            i.content() instanceof Series &&
                            i.content().getGenre().getId().equals(genreId) &&
                            !i.watchDate().isBefore(de) && !i.watchDate().isAfter(ate)
            );
            if (viuSerie) resultado.add(u);
        }
        return resultado;
    }

    public List<User> seguidoresQueViramConteudo(String userId, String contentId, LocalDateTime de, LocalDateTime ate, FollowManager followMgr, UserManager userMgr) {
        List<User> resultado = new ArrayList<>();
        for (User seguidor : followMgr.getFollowers(userId)) {
            User dadosSeguidor = userMgr.get(seguidor.getId());
            if (dadosSeguidor == null) continue;

            boolean viuFilme = dadosSeguidor.getInteractions().stream().anyMatch(i ->
                    i.content().getId().equals(contentId) &&
                            i.type() == InterationType.WATCH &&
                            !i.watchDate().isBefore(de) && !i.watchDate().isAfter(ate)
            );
            if (viuFilme) resultado.add(seguidor);
        }
        return resultado;
    }

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    public int totalVertices() { return totalVertices; }
    public int totalArestas() { return grafo.E(); }

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

    // RESOLVIDO: O IDE avisou sobre Integer objects pesados. Trocado para int primitivo
    private void reconstruirGrafoExcluindo(String excludeUserId, String excludeContentId) {
        EdgeWeightedDigraph novoGrafo = new EdgeWeightedDigraph(capacidade);

        int idxUser    = (excludeUserId    != null && idParaIndice.contains(excludeUserId)) ? idParaIndice.get(excludeUserId)    : -1;
        int idxContent = (excludeContentId != null && idParaIndice.contains(excludeContentId)) ? idParaIndice.get(excludeContentId) : -1;

        if (idxUser != -1) {
            idParaIndice.delete(excludeUserId);
            indiceParaId.delete(idxUser);
            indiceParaTipo.delete(idxUser);
        }

        if (idxContent != -1) {
            idParaIndice.delete(excludeContentId);
            indiceParaId.delete(idxContent);
            indiceParaTipo.delete(idxContent);
        }

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