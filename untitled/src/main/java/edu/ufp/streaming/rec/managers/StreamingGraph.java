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
 * <li>{@code User → User} (seguir) — peso = epoch seconds da data do follow</li>
 * <li>{@code User → Content} (WATCH) — peso = progresso de visualização (0.0 a 1.0)</li>
 * <li>{@code User → Content} (RATE) — peso = classificação (0.0 a 5.0)</li>
 * </ul>
 *
 * @author Diogo Vicente
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
    // Gestão de vértices e arestas
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

    public void removeFollowEdges(String userId) {
        if (!idParaIndice.contains(userId)) return;
        int idxUser = idParaIndice.get(userId);

        EdgeWeightedDigraph novoGrafo = new EdgeWeightedDigraph(capacidade);
        for (int v = 0; v < grafo.V(); v++) {
            for (DirectedEdge e : grafo.adj(v)) {
                String tipoDestino = indiceParaTipo.get(e.to());
                boolean isFollowEdge = "USER".equals(indiceParaTipo.get(e.from()))
                        && "USER".equals(tipoDestino);
                if (isFollowEdge && (e.from() == idxUser || e.to() == idxUser)) continue;
                novoGrafo.addEdge(e);
            }
        }
        grafo = novoGrafo;
    }

    public void removeUserEdges(String userId) {
        reconstruirGrafoExcluindo(userId, null);
    }

    public void addInteractionEdge(Interation interacao) {
        if (interacao == null) return;
        if (interacao.getType() != InterationType.WATCH
                && interacao.getType() != InterationType.RATE) return;

        String origemId  = interacao.getUser().getId();
        String destinoId = interacao.getContent().getId();
        if (!idParaIndice.contains(origemId) || !idParaIndice.contains(destinoId)) return;

        double peso = interacao.getType() == InterationType.WATCH
                ? (1.0 - interacao.getProgress())
                : (5.0 - interacao.getRating());

        grafo.addEdge(new DirectedEdge(idParaIndice.get(origemId), idParaIndice.get(destinoId), peso));
    }

    public void removeContentEdges(String contentId) {
        reconstruirGrafoExcluindo(null, contentId);
    }

    // -------------------------------------------------------------------------
    // R8a — Caminho mais curto entre utilizadores
    // -------------------------------------------------------------------------

    public List<String> caminhoMaisCurtoBetweenUsers(String idOrigem, String idDestino) {
        List<String> caminho = new ArrayList<>();
        if (!idParaIndice.contains(idOrigem) || !idParaIndice.contains(idDestino)) return caminho;

        List<Integer> verticesUser = getVerticesUtilizadores();
        java.util.Set<Integer> userSet = new java.util.HashSet<>(verticesUser);

        java.util.Map<Integer, Integer> remap    = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> remapInv = new java.util.HashMap<>();
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

        int src  = remap.get(idParaIndice.get(idOrigem));
        int dest = remap.get(idParaIndice.get(idDestino));

        DijkstraSP sp = new DijkstraSP(subUser, src);
        if (!sp.hasPathTo(dest)) return caminho;

        for (DirectedEdge e : sp.pathTo(dest)) {
            if (caminho.isEmpty()) caminho.add(indiceParaId.get(remapInv.get(e.from())));
            caminho.add(indiceParaId.get(remapInv.get(e.to())));
        }
        return caminho;
    }

    public double pesoCaminhoMaisCurto(String idOrigem, String idDestino) {
        if (!idParaIndice.contains(idOrigem) || !idParaIndice.contains(idDestino))
            return Double.POSITIVE_INFINITY;

        List<Integer> verticesUser = getVerticesUtilizadores();
        java.util.Set<Integer> userSet = new java.util.HashSet<>(verticesUser);
        java.util.Map<Integer, Integer> remap = new java.util.HashMap<>();
        int k = 0;
        for (int v : verticesUser) remap.put(v, k++);

        EdgeWeightedDigraph subUser = new EdgeWeightedDigraph(k);
        for (int v : verticesUser) {
            for (DirectedEdge e : grafo.adj(v)) {
                if (userSet.contains(e.to())) {
                    subUser.addEdge(new DirectedEdge(remap.get(e.from()), remap.get(e.to()), e.weight()));
                }
            }
        }

        int src  = remap.get(idParaIndice.get(idOrigem));
        int dest = remap.get(idParaIndice.get(idDestino));

        DijkstraSP sp = new DijkstraSP(subUser, src);
        return sp.hasPathTo(dest) ? sp.distTo(dest) : Double.POSITIVE_INFINITY;
    }

    // -------------------------------------------------------------------------
    // R8b — Extração de subgrafos
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

    public EdgeWeightedDigraph subgrafoByMinRating(double minRating, UserManager userMgr) {
        Set<Integer> idxUtilizadores = new HashSet<>();
        for (User u : userMgr.listAll()) {
            boolean qualifica = u.getInteractions().stream()
                    .anyMatch(i -> i.getType() == InterationType.RATE && i.getRating() >= minRating);

            if (qualifica && idParaIndice.contains(u.getId())) {
                idxUtilizadores.add(idParaIndice.get(u.getId()));
            }
        }

        EdgeWeightedDigraph sub = new EdgeWeightedDigraph(capacidade);
        for (int v : idxUtilizadores) {
            for (DirectedEdge e : grafo.adj(v)) {
                if (idxUtilizadores.contains(e.to())) sub.addEdge(e);
            }
        }
        return sub;
    }

    public List<String> caminhoMaisCurtoEntreArtistas(String artistIdOrigem, String artistIdDestino, ArtistContentManager acMgr) {
        List<ArtistContent> todasParticipacoes = acMgr.listAll();
        if (todasParticipacoes.isEmpty()) return new ArrayList<>();

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

        if (!artistIdx.contains(artistIdOrigem) || !artistIdx.contains(artistIdDestino)) return new ArrayList<>();

        ST<String, List<ArtistContent>> porConteudo = new ST<>();
        for (ArtistContent ac : todasParticipacoes) {
            String cid = ac.getContent().getId();
            List<ArtistContent> lista = porConteudo.contains(cid) ? porConteudo.get(cid) : new ArrayList<>();
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
                    double peso = Math.min(a.getDate().toEpochDay(), b.getDate().toEpochDay());
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

    public boolean isGrafoUtilizadoresConexo() {
        List<Integer> verticesUtilizadores = getVerticesUtilizadores();
        if (verticesUtilizadores.size() <= 1) return true;

        java.util.Map<Integer, Integer> remap = new java.util.HashMap<>();
        int k = 0;
        for (int v : verticesUtilizadores) remap.put(v, k++);

        java.util.Set<Integer> userSet = new java.util.HashSet<>(verticesUtilizadores);
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
    // R8d — Recomendações baseadas em proximidade
    // -------------------------------------------------------------------------

    public List<Content> recomendarConteudosPorProximidade(String userId, FollowManager followMgr, UserManager userMgr) {
        User utilizadorPrincipal = userMgr.get(userId);
        if (utilizadorPrincipal == null) return new ArrayList<>();

        // Extrair rapidamente para um Set os IDs de conteúdos já vistos pelo utilizador
        Set<String> conteudosJaVistos = new HashSet<>();
        for (Interation i : utilizadorPrincipal.getInteractions()) {
            if (i.getType() == InterationType.WATCH) {
                conteudosJaVistos.add(i.getContent().getId());
            }
        }

        // Usa um LinkedHashMap para manter a ordem de inserção e evitar recomendações duplicadas
        LinkedHashMap<String, Content> recomendacoes = new LinkedHashMap<>();
        for (User utilizadorSeguido : followMgr.getFollowing(userId)) {
            User amigo = userMgr.get(utilizadorSeguido.getId());
            if (amigo == null) continue;

            for (Interation i : amigo.getInteractions()) {
                boolean isVisualizacao = i.getType() == InterationType.WATCH;
                boolean euAindaNaoVi = !conteudosJaVistos.contains(i.getContent().getId());

                // Lógica Afirmativa
                if (isVisualizacao && euAindaNaoVi) {
                    recomendacoes.put(i.getContent().getId(), i.getContent());
                }
            }
        }
        return new ArrayList<>(recomendacoes.values());
    }

    // -------------------------------------------------------------------------
    // R8e — Estatísticas de visualização de um conteúdo entre duas datas
    // -------------------------------------------------------------------------

    public Map<String, Double> estatisticasVisualizacao(String contentId, LocalDateTime de, LocalDateTime ate, UserManager userMgr) {
        Map<String, Double> stats = new HashMap<>();
        int totalVisualizacoes = 0;
        double somaProgresso = 0;
        double somaRating = 0;
        int totalRatings = 0;

        for (User u : userMgr.listAll()) {
            for (Interation i : u.getInteractions()) {
                // Criar variáveis booleanas claras em vez de "ifs" encadeados que saltam a execução
                boolean isFilmeCerto = i.getContent().getId().equals(contentId);
                boolean isDataValida = !i.getWatchDate().isBefore(de) && !i.getWatchDate().isAfter(ate);

                if (isFilmeCerto && isDataValida) {
                    if (i.getType() == InterationType.WATCH) {
                        totalVisualizacoes++;
                        somaProgresso += i.getProgress();
                    } else if (i.getType() == InterationType.RATE) {
                        somaRating += i.getRating();
                        totalRatings++;
                    }
                }
            }
        }

        // Evita divisões por zero com o ternário (se não tem visualizações, dá 0.0)
        stats.put("visualizacoes", (double) totalVisualizacoes);
        stats.put("progressoMedio", totalVisualizacoes > 0 ? somaProgresso / totalVisualizacoes : 0.0);
        stats.put("ratingMedio", totalRatings > 0 ? somaRating / totalRatings : 0.0);
        return stats;
    }

    // -------------------------------------------------------------------------
    // R8f — Utilizadores que viram séries de um género num período
    // -------------------------------------------------------------------------

    public List<User> utilizadoresQueViramSeriesDeGenero(String genreId, LocalDateTime de, LocalDateTime ate, UserManager userMgr, ContentManager contentMgr) {
        List<User> resultado = new ArrayList<>();
        for (User u : userMgr.listAll()) {
            //  Uma única expressão diz-nos se a série é do tipo certo, do género certo e na data certa.
            boolean viuSérieValidada = u.getInteractions().stream().anyMatch(i ->
                    i.getType() == InterationType.WATCH &&
                            i.getContent() instanceof Series &&
                            i.getContent().getGenre().getId().equals(genreId) &&
                            !i.getWatchDate().isBefore(de) && !i.getWatchDate().isAfter(ate)
            );

            // Lógica Afirmativa
            if (viuSérieValidada) {
                resultado.add(u);
            }
        }
        return resultado;
    }

    // -------------------------------------------------------------------------
    // R8g — Seguidores que viram o mesmo conteúdo num intervalo
    // -------------------------------------------------------------------------

    public List<User> seguidoresQueViramConteudo(String userId, String contentId, LocalDateTime de, LocalDateTime ate, FollowManager followMgr, UserManager userMgr) {
        List<User> resultado = new ArrayList<>();
        List<User> seguidores = followMgr.getFollowers(userId);

        for (User seguidor : seguidores) {
            User dadosSeguidor = userMgr.get(seguidor.getId());
            if (dadosSeguidor == null) continue;

            // Verifica todo o histórico do seguidor sem ciclos manuais chatos.
            boolean seguidorViuFilme = dadosSeguidor.getInteractions().stream().anyMatch(i ->
                    i.getContent().getId().equals(contentId) &&
                            i.getType() == InterationType.WATCH &&
                            !i.getWatchDate().isBefore(de) && !i.getWatchDate().isAfter(ate)
            );

            if (seguidorViuFilme) {
                resultado.add(seguidor);
            }
        }
        return resultado;
    }

    // -------------------------------------------------------------------------
    // Utilitários e Auxiliares
    // -------------------------------------------------------------------------

    public int indiceDe(String id) {
        return idParaIndice.contains(id) ? idParaIndice.get(id) : -1;
    }

    public String idDe(int indice) {
        return indiceParaId.contains(indice) ? indiceParaId.get(indice) : null;
    }

    public String tipoDe(String id) {
        if (!idParaIndice.contains(id)) return null;
        return indiceParaTipo.get(idParaIndice.get(id));
    }

    public int totalVertices() { return totalVertices; }

    public int totalArestas() { return grafo.E(); }

    public EdgeWeightedDigraph getGrafo() { return grafo; }

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
        Integer idxUser    = (excludeUserId    != null && idParaIndice.contains(excludeUserId)) ? idParaIndice.get(excludeUserId)    : -1;
        Integer idxContent = (excludeContentId != null && idParaIndice.contains(excludeContentId)) ? idParaIndice.get(excludeContentId) : -1;

        if (excludeUserId != null && idParaIndice.contains(excludeUserId)) {
            idParaIndice.delete(excludeUserId);
            indiceParaId.delete(idxUser);
            indiceParaTipo.delete(idxUser);
        }

        if (excludeContentId != null && idParaIndice.contains(excludeContentId)) {
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