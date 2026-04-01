package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.models.*;

import java.time.LocalDate;

/**
 * Coordenador central da camada de dados da plataforma de streaming (Fase 1).
 *
 * <p>Mantém referências para todos os gestores e garante a consistência R4:
 * a remoção de uma entidade propaga-se automaticamente para todas as estruturas relacionadas.
 *
 * <ul>
 * <li>Remover um {@link User} → limpa o {@link FollowManager} (todas as arestas de seguimento)</li>
 * <li>Remover um {@link Artist} → limpa o {@link ArtistContentManager} (todas as participações)</li>
 * <li>Remover um {@link Content} → limpa o {@link ArtistContentManager} (todas as participações)</li>
 * </ul>
 * @author  Diogo Vicente
 * */

public class StreamingDatabase {

    private final UserManager userManager;
    private final ArtistManager artistManager;
    private final ContentBST contentBST;
    private final ContentManager contentManager;
    private final GenreManager genreManager;
    private final ArtistContentManager artistContentManager;
    private final FollowManager followManager;

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
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return the {@link UserManager} */
    public UserManager users() { return userManager; }

    /** @return the {@link ArtistManager} */
    public ArtistManager artists() { return artistManager; }

    /** @return the {@link ContentManager} */
    public ContentManager contents() { return contentManager; }

    /** @return the {@link ContentBST} */
    public ContentBST contentBST() { return contentBST; }

    /** @return the {@link GenreManager} */
    public GenreManager genres() { return genreManager; }

    /** @return the {@link ArtistContentManager} */
    public ArtistContentManager participations() { return artistContentManager; }

    /** @return the {@link FollowManager} */
    public FollowManager follows() { return followManager; }

    // -------------------------------------------------------------------------
    // Inserções Consistentes
    // -------------------------------------------------------------------------

    /**
     * Insere um {@link User} no sistema.
     *
     * @param user o utilizador a inserir
     * @return {@code true} se inserido com sucesso
     */
    public boolean addUser(User user) {
        return userManager.insert(user);
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
     * Insere um item de {@link Content} no sistema.
     * Adicionado automaticamente tanto à ST como à ContentBST.
     *
     * @param content o conteúdo a inserir
     * @return {@code true} se inserido com sucesso
     */
    public boolean addContent(Content content) {
        return contentManager.insert(content);
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
        Artist artist   = artistManager.get(artistId);
        Content content = contentManager.get(contentId);
        if (artist == null || content == null) return null;
        return artistContentManager.addParticipation(artist, content, role, date);
    }

    /**
     * Regista uma relação de seguimento entre dois utilizadores.
     *
     * @param followerId ID do seguidor (deve já existir)
     * @param followedId ID do utilizador seguido (deve já existir)
     * @return o {@link UserFollow} criado, ou {@code null} em caso de falha
     */
    public UserFollow addFollow(String followerId, String followedId) {
        User follower = userManager.get(followerId);
        User followed = userManager.get(followedId);
        if (follower == null || followed == null) return null;
        return followManager.follow(follower, followed);
    }

    // -------------------------------------------------------------------------
    // R4 — Remoções Consistentes (Cascata)
    // -------------------------------------------------------------------------

    /**
     * Remove um {@link User} do sistema e propaga para todas as estruturas relacionadas.
     * Cascata: remove todas as relações de seguimento (enviadas e recebidas).
     *
     * @param userId o ID do utilizador a remover
     * @return o {@link User} removido, ou {@code null} se não for encontrado
     */
    public User removeUser(String userId) {
        if (!userManager.contains(userId)) return null;
        followManager.removeAllRelationships(userId);
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
     * Remove um item de {@link Content} do sistema e propaga para todas as estruturas relacionadas.
     * Cascata: remove todos os registos de participação Artista↔Conteúdo para este conteúdo.
     * Tanto a ST como a ContentBST são atualizadas via ContentManager.remove().
     *
     * @param contentId o ID do conteúdo a remover
     * @return o {@link Content} removido, ou {@code null} se não for encontrado
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