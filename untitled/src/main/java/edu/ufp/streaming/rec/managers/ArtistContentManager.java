package edu.ufp.streaming.rec.managers;

import edu.princeton.cs.algs4.ST;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.ufp.streaming.rec.models.Artist;
import edu.ufp.streaming.rec.models.ArtistContent;
import edu.ufp.streaming.rec.enums.ArtistRole;
import edu.ufp.streaming.rec.models.Content;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ArtistContentManager {

    /** Primary ST: "artistId:contentId:role" → edu.pt.lp2.edu.ufp.streaming.rec.models.ArtistContent. */
    private final ST<String, ArtistContent> participationST;

    /** Index: artistId → list of edu.pt.lp2.edu.ufp.streaming.rec.models.ArtistContent (filmography). */
    private final ST<String, List<ArtistContent>> byArtistIndex;

    /** Index: contentId → list of edu.pt.lp2.edu.ufp.streaming.rec.models.ArtistContent (cast/crew of a content). */
    private final ST<String, List<ArtistContent>> byContentIndex;

    /** BST ordered by participation date for temporal range queries.
     * CORREÇÃO: Alterado de LocalDate para Long para funcionar com o RedBlackBST
     */
    private final RedBlackBST<Long, List<ArtistContent>> byDateBST;

    /**
     * Constructs an empty edu.pt.lp2.managers.edu.ufp.streaming.rec.managers.ArtistContentManager.
     */
    public ArtistContentManager() {
        this.participationST = new ST<>();
        this.byArtistIndex   = new ST<>();
        this.byContentIndex  = new ST<>();
        this.byDateBST       = new RedBlackBST<>();
    }

    // -------------------------------------------------------------------------
    // Insert / Remove
    // -------------------------------------------------------------------------

    public ArtistContent addParticipation(Artist artist, Content content,
                                          ArtistRole role, LocalDate date) {
        if (artist == null || content == null || role == null || date == null) return null;

        String key = compositeKey(artist.getId(), content.getId(), role);
        if (participationST.contains(key)) return null;

        ArtistContent ac = new ArtistContent(artist, content, role, date);
        participationST.put(key, ac);
        indexByArtist(ac);
        indexByContent(ac);
        indexByDate(ac);

        // Keep edu.pt.lp2.edu.ufp.streaming.rec.models.Artist's internal filmography in sync
        artist.addParticipation(ac);

        return ac;
    }

    public ArtistContent removeParticipation(String artistId, String contentId, ArtistRole role) {
        String key = compositeKey(artistId, contentId, role);
        if (!participationST.contains(key)) return null;

        ArtistContent ac = participationST.get(key);
        participationST.delete(key);
        removeFromArtistIndex(ac);
        removeFromContentIndex(ac);
        removeFromDateIndex(ac);
        return ac;
    }

    public void removeAllByArtist(String artistId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return;

        for (ArtistContent ac : new ArrayList<>(list)) {
            String key = compositeKey(ac.getArtist().getId(), ac.getContent().getId(), ac.getRole());
            participationST.delete(key);
            removeFromContentIndex(ac);
            removeFromDateIndex(ac);
        }
        byArtistIndex.delete(artistId);
    }

    public void removeAllByContent(String contentId) {
        List<ArtistContent> list = byContentIndex.get(contentId);
        if (list == null) return;

        for (ArtistContent ac : new ArrayList<>(list)) {
            String key = compositeKey(ac.getArtist().getId(), contentId, ac.getRole());
            participationST.delete(key);
            removeFromArtistIndex(ac);
            removeFromDateIndex(ac);
        }
        byContentIndex.delete(contentId);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public List<ArtistContent> getFilmography(String artistId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public List<ArtistContent> getCastAndCrew(String contentId) {
        List<ArtistContent> list = byContentIndex.get(contentId);
        return list != null ? new ArrayList<>(list) : new ArrayList<>();
    }

    public List<ArtistContent> getFilmographyByRole(String artistId, ArtistRole role) {
        List<ArtistContent> result = new ArrayList<>();
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return result;
        for (ArtistContent ac : list) {
            if (ac.getRole() == role) result.add(ac);
        }
        return result;
    }

    public List<ArtistContent> getFilmographyByDateRange(String artistId,
                                                         LocalDate from, LocalDate to) {
        List<ArtistContent> result = new ArrayList<>();
        // CORREÇÃO: from.toEpochDay() e to.toEpochDay()
        for (Long d : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<ArtistContent> bucket = byDateBST.get(d);
            if (bucket == null) continue;
            for (ArtistContent ac : bucket) {
                if (ac.getArtist().getId().equals(artistId)) result.add(ac);
            }
        }
        return result;
    }

    public List<ArtistContent> getAllByDateRange(LocalDate from, LocalDate to) {
        List<ArtistContent> result = new ArrayList<>();
        // CORREÇÃO: from.toEpochDay() e to.toEpochDay()
        for (Long d : byDateBST.keys(from.toEpochDay(), to.toEpochDay())) {
            List<ArtistContent> bucket = byDateBST.get(d);
            if (bucket != null) result.addAll(bucket);
        }
        return result;
    }

    public List<Artist> getDirectors(String contentId) {
        List<Artist> result = new ArrayList<>();
        List<ArtistContent> list = byContentIndex.get(contentId);
        if (list == null) return result;
        for (ArtistContent ac : list) {
            if (ac.getRole() == ArtistRole.DIRECTOR) result.add(ac.getArtist());
        }
        return result;
    }

    public List<Artist> getActors(String contentId) {
        List<Artist> result = new ArrayList<>();
        List<ArtistContent> list = byContentIndex.get(contentId);
        if (list == null) return result;
        for (ArtistContent ac : list) {
            if (ac.getRole() == ArtistRole.ACTOR) result.add(ac.getArtist());
        }
        return result;
    }

    public boolean hasParticipation(String artistId, String contentId) {
        List<ArtistContent> list = byArtistIndex.get(artistId);
        if (list == null) return false;
        for (ArtistContent ac : list) {
            if (ac.getContent().getId().equals(contentId)) return true;
        }
        return false;
    }


    public int size() {
        return participationST.size();
    }

    public List<ArtistContent> listAll() {
        List<ArtistContent> result = new ArrayList<>();
        for (String key : participationST.keys()) result.add(participationST.get(key));
        return result;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String compositeKey(String artistId, String contentId, ArtistRole role) {
        return artistId + ":" + contentId + ":" + role.name();
    }

    private void indexByArtist(ArtistContent ac) {
        String key = ac.getArtist().getId();
        List<ArtistContent> list = byArtistIndex.get(key);
        if (list == null) { list = new ArrayList<>(); byArtistIndex.put(key, list); }
        list.add(ac);
    }

    private void indexByContent(ArtistContent ac) {
        String key = ac.getContent().getId();
        List<ArtistContent> list = byContentIndex.get(key);
        if (list == null) { list = new ArrayList<>(); byContentIndex.put(key, list); }
        list.add(ac);
    }

    private void indexByDate(ArtistContent ac) {
        // CORREÇÃO: Transformar LocalDate num Long
        Long dateKey = ac.getDate().toEpochDay();
        List<ArtistContent> bucket = byDateBST.get(dateKey);
        if (bucket == null) { bucket = new ArrayList<>(); byDateBST.put(dateKey, bucket); }
        bucket.add(ac);
    }

    private void removeFromArtistIndex(ArtistContent ac) {
        String key = ac.getArtist().getId();
        List<ArtistContent> list = byArtistIndex.get(key);
        if (list != null) { list.remove(ac); if (list.isEmpty()) byArtistIndex.delete(key); }
    }

    private void removeFromContentIndex(ArtistContent ac) {
        String key = ac.getContent().getId();
        List<ArtistContent> list = byContentIndex.get(key);
        if (list != null) { list.remove(ac); if (list.isEmpty()) byContentIndex.delete(key); }
    }

    private void removeFromDateIndex(ArtistContent ac) {
        // CORREÇÃO: Transformar LocalDate num Long
        Long dateKey = ac.getDate().toEpochDay();
        List<ArtistContent> bucket = byDateBST.get(dateKey);
        if (bucket != null) { bucket.remove(ac); if (bucket.isEmpty()) byDateBST.delete(dateKey); }
    }
}