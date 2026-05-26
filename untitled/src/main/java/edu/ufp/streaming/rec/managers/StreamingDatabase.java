package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.models.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Coordenador central da camada de dados da plataforma de streaming (Fase 1 + Fase 2).
 *
 * <p>Implementa o padrão de desenho "Facade" (Fachada), servindo de ponto de entrada único
 * para a interface gráfica interagir com todos os sub-gestores.
 * * <p>Garante a consistência R4: a remoção de uma entidade propaga-se automaticamente
 * para todas as estruturas relacionadas, incluindo o grafo.
 *
 * @author Diogo Vicente
 */
public class StreamingDatabase {

    private final UserManager userManager;
    private final ArtistManager artistManager;
    private final ContentManager contentManager;
    private final GenreManager genreManager;
    private final ArtistContentManager artistContentManager;
    private final FollowManager followManager;
    private final StreamingGraph graph;

    public StreamingDatabase() {
        this.userManager          = new UserManager();
        this.artistManager        = new ArtistManager();
        ContentBST contentBST = new ContentBST();
        this.contentManager       = new ContentManager(contentBST);
        this.genreManager         = new GenreManager();
        this.artistContentManager = new ArtistContentManager();
        this.followManager        = new FollowManager();
        this.graph                = new StreamingGraph(100);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    public UserManager users() { return userManager; }
    public ArtistManager artists() { return artistManager; }
    public ContentManager contents() { return contentManager; }
    public GenreManager genres() { return genreManager; }
    public ArtistContentManager participations() { return artistContentManager; }
    public FollowManager follows() { return followManager; }
    public StreamingGraph graph() { return graph; }

    // -------------------------------------------------------------------------
    // Autenticação
    // -------------------------------------------------------------------------

    public User authenticate(String id, String rawPassword) {
        return userManager.authenticate(id, rawPassword);
    }

    // -------------------------------------------------------------------------
    // Inserções Consistentes
    // -------------------------------------------------------------------------

    public boolean addUser(User user) {
        // Adiciona na BD e, se houver sucesso, insere logo como vértice no Grafo
        if (!userManager.insert(user)) return false;
        graph.addUser(user);
        return true;
    }

    public boolean addArtist(Artist artist) {
        return artistManager.insert(artist);
    }

    public boolean addContent(Content content) {
        if (!contentManager.insert(content)) return false;
        graph.addContent(content);
        return true;
    }

    public boolean addGenre(Genre genre) {
        return genreManager.insert(genre);
    }

    public ArtistContent addParticipation(String artistId, String contentId, ArtistRole role, LocalDate date) {
        Artist  artist  = artistManager.get(artistId);
        Content content = contentManager.get(contentId);
        if (artist == null || content == null) return null;
        return artistContentManager.addParticipation(artist, content, role, date);
    }

    public UserFollow addFollowWithDate(String followerId, String followedId, LocalDateTime followDate) {
        User follower = userManager.get(followerId);
        User followed = userManager.get(followedId);
        if (follower == null || followed == null) return null;
        UserFollow uf = followManager.followWithDate(follower, followed, followDate);
        if (uf != null) {
            graph.addFollowEdge(uf);
        }
        return uf;
    }

    public UserFollow addFollow(String followerId, String followedId) {
        //Princípio DRY (Don't Repeat Yourself). Aproveita o método acima enviando a data de agora.
        return addFollowWithDate(followerId, followedId, LocalDateTime.now());
    }

    public void addInteraction(Interation interaction) {
        if (interaction == null) return;
        User user = userManager.get(interaction.user().getId());
        if (user == null) return;
        user.addInteraction(interaction);
        graph.addInteractionEdge(interaction); // Reflete a interação como Aresta no Grafo
    }

    // -------------------------------------------------------------------------
    // R4 — Remoções Consistentes
    // -------------------------------------------------------------------------

    public User removeUser(String userId) {
        if (!userManager.contains(userId)) return null;
        //  Apaga o utilizador e limpa todos os rastos dele noutros gestores.
        followManager.removeAllRelationships(userId);
        graph.removeUserEdges(userId);
        return userManager.remove(userId);
    }

    public Artist removeArtist(String artistId) {
        if (!artistManager.contains(artistId)) return null;
        artistContentManager.removeAllByArtist(artistId);
        return artistManager.remove(artistId);
    }

    public Content removeContent(String contentId) {
        if (contentManager.get(contentId) == null) return null;
        artistContentManager.removeAllByContent(contentId);
        graph.removeContentEdges(contentId);
        return contentManager.remove(contentId);
    }

    public Genre removeGenre(String genreId) {
        //Stream API para verificar rapidamente se o género está preso a algum filme
        boolean isGeneroEmUso = contentManager.listAll().stream()
                .anyMatch(c -> c.getGenre() != null && c.getGenre().getId().equals(genreId));
        if (isGeneroEmUso) {
            throw new IllegalStateException("Não é possível remover o género '" + genreId + "': existem conteúdos que ainda o utilizam.");
        }
        return genreManager.remove(genreId);
    }
}