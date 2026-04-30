package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.models.*;

import java.time.LocalDate;

/**
 * Coordenador central da camada de dados da plataforma de streaming (Fase 1 + Fase 2).
 *
 * <p>Mantém referências para todos os gestores e garante a consistência R4:
 * a remoção de uma entidade propaga-se automaticamente para todas as estruturas
 * relacionadas, incluindo o grafo.
 *
 * <ul>
 *   <li>Remover um {@link User}    → limpa o {@link FollowManager} e {@link StreamingGraph}</li>
 *   <li>Remover um {@link Artist}  → limpa o {@link ArtistContentManager}</li>
 *   <li>Remover um {@link Content} → limpa o {@link ArtistContentManager} e {@link StreamingGraph}</li>
 * </ul>
 *
 * @author Diogo Vicente
 */
public class StreamingDatabase {

    /** Gere as entidades {@link User}. */
    private final UserManager userManager;

    /** Gere as entidades {@link Artist}. */
    private final ArtistManager artistManager;

    /** BST ordenada por data de lançamento para consultas de conteúdo. */
    private final ContentBST contentBST;

    /** Gere todos os conteúdos (Movie, Series, Documentary). */
    private final ContentManager contentManager;

    /** Gere o catálogo de géneros. */
    private final GenreManager genreManager;

    /** Gere as relações de participação Artista↔Conteúdo. */
    private final ArtistContentManager artistContentManager;

    /** Gere as relações de follow entre utilizadores. */
    private final FollowManager followManager;

    /** Grafo pesado direcionado que representa as relações User↔User e User↔Content. */
    private final StreamingGraph graph;

    /**
     * Constrói uma nova StreamingDatabase vazia com todos os gestores inicializados.
     */
    public StreamingDatabase() {
        this.userManager          = new UserManager();
        this.artistManager        = new ArtistManager();
        this.contentBST           = new ContentBST();
        this.contentManager       = new ContentManager(contentBST);
        this.genreManager         = new GenreManager();
        this.artistContentManager = new ArtistContentManager();
        this.followManager        = new FollowManager();
        this.graph                = new StreamingGraph(100);
    }

    // -------------------------------------------------------------------------
    // Getters dos gestores
    // -------------------------------------------------------------------------

    /** @return o {@link UserManager} */
    public UserManager users() { return userManager; }

    /** @return o {@link ArtistManager} */
    public ArtistManager artists() { return artistManager; }

    /** @return o {@link ContentManager} */
    public ContentManager contents() { return contentManager; }

    /** @return o {@link ContentBST} */
    public ContentBST contentBST() { return contentBST; }

    /** @return o {@link GenreManager} */
    public GenreManager genres() { return genreManager; }

    /** @return o {@link ArtistContentManager} */
    public ArtistContentManager participations() { return artistContentManager; }

    /** @return o {@link FollowManager} */
    public FollowManager follows() { return followManager; }

    /** @return o {@link StreamingGraph} */
    public StreamingGraph getGraph() { return graph; }

    // -------------------------------------------------------------------------
    // Autenticação
    // -------------------------------------------------------------------------

    /**
     * Autentica um utilizador com o ID e password fornecidos.
     *
     * @param id          ID do utilizador
     * @param rawPassword password em texto simples
     * @return o {@link User} autenticado, ou {@code null} se as credenciais forem inválidas
     *         ou se a password ainda não estiver definida
     */
    public User authenticate(String id, String rawPassword) {
        return userManager.authenticate(id, rawPassword);
    }

    /**
     * Altera a password de um utilizador.
     *
     * @param userId         ID do utilizador
     * @param newRawPassword nova password em texto simples
     * @return {@code true} se alterada com sucesso; {@code false} se o utilizador não existir
     */
    public boolean changePassword(String userId, String newRawPassword) {
        return userManager.changePassword(userId, newRawPassword);
    }

    /**
     * Define a password inicial de um utilizador que ainda não tem password.
     * Falha se o utilizador já tiver password definida.
     *
     * @param userId      ID do utilizador
     * @param rawPassword password em texto simples
     * @return {@code true} se definida com sucesso
     */
    public boolean setInitialPassword(String userId, String rawPassword) {
        return userManager.setInitialPassword(userId, rawPassword);
    }

    /**
     * Indica se um utilizador já tem password definida.
     *
     * @param userId ID do utilizador
     * @return {@code true} se a password estiver definida
     */
    public boolean hasPassword(String userId) {
        return userManager.hasPassword(userId);
    }

    // -------------------------------------------------------------------------
    // Inserções Consistentes
    // -------------------------------------------------------------------------

    /**
     * Insere um {@link User} no sistema e adiciona-o como vértice no grafo.
     *
     * @param user o utilizador a inserir
     * @return {@code true} se inserido com sucesso
     */
    public boolean addUser(User user) {
        if (!userManager.insert(user)) return false;
        graph.addUser(user);
        return true;
    }

    /**
     * Insere um {@link Artist} no sistema.
     *
     * @param artist o artista a inserir
     * @return {@code true} se inserido com sucesso
     */
    public boolean addArtist(Artist artist) {
        return artistManager.insert(artist);
    }

    /**
     * Insere um {@link Content} no sistema e adiciona-o como vértice no grafo.
     *
     * @param content o conteúdo a inserir
     * @return {@code true} se inserido com sucesso
     */
    public boolean addContent(Content content) {
        if (!contentManager.insert(content)) return false;
        graph.addContent(content);
        return true;
    }

    /**
     * Insere um {@link Genre} no sistema.
     *
     * @param genre o género a inserir
     * @return {@code true} se inserido com sucesso
     */
    public boolean addGenre(Genre genre) {
        return genreManager.insert(genre);
    }

    /**
     * Regista uma participação Artista↔Conteúdo.
     *
     * @param artistId  ID do artista (deve já existir)
     * @param contentId ID do conteúdo (deve já existir)
     * @param role      a função do artista neste conteúdo
     * @param date      a data da participação
     * @return o {@link ArtistContent} criado, ou {@code null} em caso de falha
     */
    public ArtistContent addParticipation(String artistId, String contentId,
                                          ArtistRole role, LocalDate date) {
        Artist  artist  = artistManager.get(artistId);
        Content content = contentManager.get(contentId);
        if (artist == null || content == null) return null;
        return artistContentManager.addParticipation(artist, content, role, date);
    }

    /**
     * Regista uma relação de follow entre dois utilizadores e adiciona a aresta ao grafo.
     *
     * @param followerId ID do seguidor (deve já existir)
     * @param followedId ID do utilizador a seguir (deve já existir)
     * @return o {@link UserFollow} criado, ou {@code null} em caso de falha
     */
    public UserFollow addFollow(String followerId, String followedId) {
        User follower = userManager.get(followerId);
        User followed = userManager.get(followedId);
        if (follower == null || followed == null) return null;
        UserFollow uf = followManager.follow(follower, followed);
        if (uf != null) graph.addFollowEdge(uf);
        return uf;
    }

    /**
     * Regista uma interação do utilizador com um conteúdo e adiciona a aresta ao grafo.
     * Apenas interações do tipo WATCH e RATE são adicionadas como arestas no grafo.
     *
     * @param interaction a {@link Interation} a registar
     */
    public void addInteraction(Interation interaction) {
        if (interaction == null) return;
        User user = userManager.get(interaction.getUser().getId());
        if (user == null) return;
        user.addInteraction(interaction);
        graph.addInteractionEdge(interaction);
    }

    // -------------------------------------------------------------------------
    // R4 — Remoções Consistentes (Cascata)
    // -------------------------------------------------------------------------

    /**
     * Remove um {@link User} do sistema e propaga a remoção a todas as estruturas relacionadas.
     * Cascata: remove todas as relações de follow e as arestas no grafo.
     *
     * @param userId o ID do utilizador a remover
     * @return o {@link User} removido, ou {@code null} se não encontrado
     */
    public User removeUser(String userId) {
        if (!userManager.contains(userId)) return null;
        followManager.removeAllRelationships(userId);
        graph.removeFollowEdges(userId);
        return userManager.remove(userId);
    }

    /**
     * Remove um {@link Artist} do sistema e propaga para todas as estruturas relacionadas.
     * Cascata: remove todos os registos de participação Artista↔Conteúdo.
     *
     * @param artistId o ID do artista a remover
     * @return o {@link Artist} removido, ou {@code null} se não for encontrado
     */
    public Artist removeArtist(String artistId) {
        if (!artistManager.contains(artistId)) return null;
        artistContentManager.removeAllByArtist(artistId);
        return artistManager.remove(artistId);
    }

    /**
     * Remove um {@link Content} do sistema e propaga a remoção a todas as estruturas relacionadas.
     * Cascata: remove todos os registos de participação e as arestas no grafo.
     *
     * @param contentId o ID do conteúdo a remover
     * @return o {@link Content} removido, ou {@code null} se não encontrado
     */
    public Content removeContent(String contentId) {
        if (contentManager.get(contentId) == null) return null;
        artistContentManager.removeAllByContent(contentId);
        return contentManager.remove(contentId);
    }

    /**
     * Remove um {@link Genre} do sistema.
     *
     * @param genreId o ID do género a remover
     * @return o {@link Genre} removido, ou {@code null} se não for encontrado
     */
    public Genre removeGenre(String genreId) {
        return genreManager.remove(genreId);
    }
}