package edu.ufp.streaming.rec.managers;

import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.models.*;

import java.time.LocalDate;

/**
 * Central coordinator for the streaming platform's data layer (Phase 1).
 *
 * <p>Holds references to all managers and enforces R4 consistency:
 * removing an entity cascades automatically to all related structures.
 *
 * <ul>
 *   <li>Removing a {@link User} → cleans {@link FollowManager} (all follow edges)</li>
 *   <li>Removing an {@link Artist} → cleans {@link ArtistContentManager} (all participations)</li>
 *   <li>Removing a {@link Content} → cleans {@link ArtistContentManager} (all participations)</li>
 * </ul>
**/
public class StreamingDatabase {

    private final UserManager userManager;
    private final ArtistManager artistManager;
    private final ContentBST contentBST;
    private final ContentManager contentManager;
    private final GenreManager genreManager;
    private final ArtistContentManager artistContentManager;
    private final FollowManager followManager;

    /**
     * Constructs a new empty StreamingDatabase with all managers initialised.
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
    // Accessors
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
    // Consistent inserts
    // -------------------------------------------------------------------------

    /**
     * Inserts a {@link User} into the system.
     *
     * @param user the user to insert
     * @return {@code true} if inserted successfully
     */
    public boolean addUser(User user) {
        return userManager.insert(user);
    }

    /**
     * Inserts an {@link Artist} into the system.
     *
     * @param artist the artist to insert
     * @return {@code true} if inserted successfully
     */
    public boolean addArtist(Artist artist) {
        return artistManager.insert(artist);
    }

    /**
     * Inserts a {@link Content} item into the system.
     * Automatically added to both the ST and the ContentBST.
     *
     * @param content the content to insert
     * @return {@code true} if inserted successfully
     */
    public boolean addContent(Content content) {
        return contentManager.insert(content);
    }

    /**
     * Inserts a {@link Genre} into the system.
     *
     * @param genre the genre to insert
     * @return {@code true} if inserted successfully
     */
    public boolean addGenre(Genre genre) {
        return genreManager.insert(genre);
    }

    /**
     * Records an Artist↔Content participation.
     *
     * @param artistId  ID of the artist (must already exist)
     * @param contentId ID of the content (must already exist)
     * @param role      the artist's role in this content
     * @param date      the participation date
     * @return the created {@link ArtistContent}, or {@code null} on failure
     */
    public ArtistContent addParticipation(String artistId, String contentId,
                                          ArtistRole role, LocalDate date) {
        Artist artist   = artistManager.get(artistId);
        Content content = contentManager.get(contentId);
        if (artist == null || content == null) return null;
        return artistContentManager.addParticipation(artist, content, role, date);
    }

    /**
     * Records a follow relationship between two users.
     *
     * @param followerId ID of the follower (must already exist)
     * @param followedId ID of the user being followed (must already exist)
     * @return the created {@link UserFollow}, or {@code null} on failure
     */
    public UserFollow addFollow(String followerId, String followedId) {
        User follower = userManager.get(followerId);
        User followed = userManager.get(followedId);
        if (follower == null || followed == null) return null;
        return followManager.follow(follower, followed);
    }

    // -------------------------------------------------------------------------
    // R4 — Consistent removals (cascade)
    // -------------------------------------------------------------------------

    /**
     * Removes a {@link User} from the system and cascades to all related structures.
     * Cascade: removes all follow relationships (incoming and outgoing).
     *
     * @param userId the ID of the user to remove
     * @return the removed {@link User}, or {@code null} if not found
     */
    public User removeUser(String userId) {
        if (!userManager.contains(userId)) return null;
        followManager.removeAllRelationships(userId);
        return userManager.remove(userId);
    }

    /**
     * Removes an {@link Artist} from the system and cascades to all related structures.
     * Cascade: removes all Artist↔Content participation records.
     *
     * @param artistId the ID of the artist to remove
     * @return the removed {@link Artist}, or {@code null} if not found
     */
    public Artist removeArtist(String artistId) {
        if (!artistManager.contains(artistId)) return null;
        artistContentManager.removeAllByArtist(artistId);
        return artistManager.remove(artistId);
    }

    /**
     * Removes a {@link Content} item from the system and cascades to all related structures.
     * Cascade: removes all Artist↔Content participation records for this content.
     * Both the ST and the ContentBST are updated via ContentManager.remove().
     *
     * @param contentId the ID of the content to remove
     * @return the removed {@link Content}, or {@code null} if not found
     */
    public Content removeContent(String contentId) {
        if (contentManager.get(contentId) == null) return null;
        artistContentManager.removeAllByContent(contentId);
        return contentManager.remove(contentId);
    }

    /**
     * Removes a {@link Genre} from the system.
     *
     * @param genreId the ID of the genre to remove
     * @return the removed {@link Genre}, or {@code null} if not found
     */
    public Genre removeGenre(String genreId) {
        return genreManager.remove(genreId);
    }
}